package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import timber.log.Timber
import java.io.EOFException
import java.io.IOException

/**
 * [HttpLoggingInterceptor] at [HttpLoggingInterceptor.Level.BODY] copies the whole request body into
 * a String before [LoggingInterceptor] ever gets to truncate it. A bulk upload (e.g. couple/register
 * saveAll carrying ~180 MB) then dies with OutOfMemoryError on the OkHttp dispatcher thread.
 *
 * FLW-1256: the RESPONSE side had the same problem. BODY level buffers every response in full and
 * turns it into a String; multi-MB incentive downloads running in parallel crashed in
 * HttpLoggingInterceptor (Buffer.readString / Buffer.clone) on 2.13.0.
 *
 * So the network call itself is logged at HEADERS level only, and bodies are logged here with a
 * bounded preview:
 *  - request body: only when its length is known and at most [maxLoggedBodyBytes] (as before),
 *    previewing the first [MAX_PREVIEW_BYTES];
 *  - response body: [Response.peekBody] of [MAX_PREVIEW_BYTES], which reads at most that many
 *    bytes and leaves the body intact for the caller, whatever the response size.
 * [LoggingInterceptor] already cut every line to 4 KB, so the sync log shows the same text as before.
 */
class BodyCappedLoggingInterceptor(
    private val logger: HttpLoggingInterceptor.Logger,
    private val maxLoggedBodyBytes: Long = MAX_LOGGED_BODY_BYTES,
) : Interceptor {

    /*
    private val bodyLogger = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    */
    private val headersLogger = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        /*
        val bodyLength = request.body?.contentLength() ?: 0L
        val delegate =
            if (bodyLength in 0..maxLoggedBodyBytes) bodyLogger else headersLogger
        */
        logRequestBody(request)

        val response = try {
            headersLogger.intercept(chain)
        } catch (e: IOException) {
            // Timeouts / no network: the delegate logs "HTTP FAILED" at debug; repeat it at
            // error level so every failed call stands out in logcat and the sync log.
            Timber.tag(TAG).e("API FAILED %s %s: %s", request.method, request.url.encodedPath, e.toString())
            throw e
        }

        // Every non-2xx reply is logged with its error body at error level. This also covers
        // oversized uploads, where the delegate logs headers only and the error text was lost.
        if (!response.isSuccessful) {
            val errorBody = try {
                response.peekBody(MAX_ERROR_BODY_BYTES).string()
            } catch (_: Exception) {
                ""
            }
            Timber.tag(TAG).e(
                "API FAILED %s %s -> HTTP %d: %s",
                request.method, request.url.encodedPath, response.code, errorBody
            )
        } else {
            logResponseBody(response)
        }
        return response
    }

    /** Logs a preview of the request body. Never throws: logging must not fail the call. */
    private fun logRequestBody(request: Request) {
        try {
            val body = request.body ?: return
            val length = body.contentLength()
            // Unknown (-1) or oversized bodies stay headers-only, exactly as before.
            if (length !in 1..maxLoggedBodyBytes) return
            if (body.isDuplex() || body.isOneShot() || hasEncodedBody(request.headers)) return

            val buffer = Buffer()
            body.writeTo(buffer)
            logger.log("--> BODY ${request.method} ${request.url.encodedPath} (${length}-byte body)")
            logger.log(preview(buffer, body.contentType()))
        } catch (e: Exception) {
            Timber.tag(TAG).w("Could not log request body: %s", e.toString())
        }
    }

    /** Logs a bounded preview of a successful response body. Never throws. */
    private fun logResponseBody(response: Response) {
        try {
            if (hasEncodedBody(response.headers)) return
            // One byte more than the preview, only to know whether the body was cut.
            val peeked = response.peekBody(MAX_PREVIEW_BYTES + 1)
            val buffer = Buffer().apply { write(peeked.bytes()) }
            if (buffer.size == 0L) return
            logger.log(
                "<-- BODY ${response.request.method} ${response.request.url.encodedPath} " +
                    "(${response.body?.contentLength() ?: -1L}-byte body)"
            )
            logger.log(preview(buffer, response.body?.contentType()))
        } catch (e: Exception) {
            Timber.tag(TAG).w("Could not log response body: %s", e.toString())
        }
    }

    /** First [MAX_PREVIEW_BYTES] of [buffer] as text, or a placeholder for binary content. */
    private fun preview(buffer: Buffer, contentType: MediaType?): String {
        if (!buffer.isProbablyUtf8()) return "(binary body omitted)"
        val charset = contentType?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        val truncated = buffer.size > MAX_PREVIEW_BYTES
        val text = buffer.readString(minOf(buffer.size, MAX_PREVIEW_BYTES), charset)
        return if (truncated) "$text...[truncated]" else text
    }

    // Compressed bodies are skipped (OkHttp already removes transparent gzip before this runs).
    private fun hasEncodedBody(headers: Headers): Boolean {
        val encoding = headers["Content-Encoding"] ?: return false
        return !encoding.equals("identity", ignoreCase = true)
    }

    // Same check HttpLoggingInterceptor uses: the first 16 code points contain no control
    // characters other than whitespace.
    private fun Buffer.isProbablyUtf8(): Boolean = try {
        val prefix = Buffer()
        copyTo(prefix, 0, minOf(size, 64))
        var plain = true
        for (i in 0 until 16) {
            if (prefix.exhausted()) break
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                plain = false
                break
            }
        }
        plain
    } catch (_: EOFException) {
        false
    }

    companion object {
        const val MAX_LOGGED_BODY_BYTES = 1024L * 1024L // 1 MB
        private const val MAX_ERROR_BODY_BYTES = 4L * 1024L // same cap as LoggingInterceptor
        private const val MAX_PREVIEW_BYTES = 4L * 1024L // same cap as LoggingInterceptor
        private const val TAG = "OkHttp"
    }
}
