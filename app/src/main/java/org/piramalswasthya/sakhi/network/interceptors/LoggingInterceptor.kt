package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

class LoggingInterceptor : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Timber.tag("OkHttp").d(message)
    }
}