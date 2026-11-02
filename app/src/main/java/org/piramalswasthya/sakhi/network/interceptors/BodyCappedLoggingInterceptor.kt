package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

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
        val bodyLength = chain.request().body?.contentLength() ?: 0L
        val delegate =
            if (bodyLength in 0..maxLoggedBodyBytes) bodyLogger else headersLogger
        return delegate.intercept(chain)
    }

    companion object {
        const val MAX_LOGGED_BODY_BYTES = 1024L * 1024L // 1 MB
    }
}
