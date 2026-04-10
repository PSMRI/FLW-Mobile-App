package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

class LoggingInterceptor : HttpLoggingInterceptor.Logger {

    companion object {
        private const val MAX_LOG_LENGTH = 4 * 1024 // 4 KB
    }

    override fun log(message: String) {
        if (message.length > MAX_LOG_LENGTH) {
            Timber.tag("OkHttp").d("%s...[truncated]", message.substring(0, MAX_LOG_LENGTH))
        } else {
            Timber.tag("OkHttp").d("%s", message)
        }
    }
}