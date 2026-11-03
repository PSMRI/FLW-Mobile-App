package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.IOException

/**
 * [HttpLoggingInterceptor] at [HttpLoggingInterceptor.Level.BODY] copies the whole request body into
 * a String before [LoggingInterceptor] ever gets to truncate it. A bulk upload (e.g. couple/register
 * saveAll carrying ~180 MB) then dies with OutOfMemoryError on the OkHttp dispatcher thread.
 *
 * Requests whose body is larger than [maxLoggedBodyBytes] (or of unknown length) are therefore
 * logged at HEADERS level only; everything else keeps full BODY logging for the sync log.
 */
class BodyCappedLoggingInterceptor(
    logger: HttpLoggingInterceptor.Logger,
    private val maxLoggedBodyBytes: Long = MAX_LOGGED_BODY_BYTES,
) : Interceptor {

    private val bodyLogger = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val headersLogger = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val bodyLength = request.body?.contentLength() ?: 0L
        val delegate =
            if (bodyLength in 0..maxLoggedBodyBytes) bodyLogger else headersLogger

        val response = try {
            delegate.intercept(chain)
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
        }
        return response
    }

    companion object {
        const val MAX_LOGGED_BODY_BYTES = 1024L * 1024L // 1 MB
        private const val MAX_ERROR_BODY_BYTES = 4L * 1024L // same cap as LoggingInterceptor
        private const val TAG = "OkHttp"
    }
}
