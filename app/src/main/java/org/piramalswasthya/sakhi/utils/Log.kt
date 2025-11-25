package org.piramalswasthya.sakhi.utils

import org.piramalswasthya.sakhi.BuildConfig

object Log {
    var isLoggingEnabled: Boolean = true
//    var isLoggingEnabled: Boolean = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (isLoggingEnabled) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (isLoggingEnabled) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (isLoggingEnabled) Log.w(tag, message)
    }

    fun e(tag: String, message: String) {
        if (isLoggingEnabled) Log.e(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        if (isLoggingEnabled) Log.e(tag, message, throwable)
    }

    fun v(tag: String, message: String) {
        if (isLoggingEnabled) Log.v(tag, message)
    }
}