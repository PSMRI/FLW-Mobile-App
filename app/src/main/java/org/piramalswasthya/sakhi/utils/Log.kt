package org.piramalswasthya.sakhi.utils

object Log {
    var isLoggingEnabled: Boolean = true
//    var isLoggingEnabled: Boolean = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (isLoggingEnabled) android.util.Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (isLoggingEnabled) android.util.Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (isLoggingEnabled) android.util.Log.w(tag, message)
    }

    fun e(tag: String, message: String) {
        if (isLoggingEnabled) android.util.Log.e(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        if (isLoggingEnabled) android.util.Log.e(tag, message, throwable)
    }

    fun v(tag: String, message: String) {
        if (isLoggingEnabled) android.util.Log.v(tag, message)
    }
}