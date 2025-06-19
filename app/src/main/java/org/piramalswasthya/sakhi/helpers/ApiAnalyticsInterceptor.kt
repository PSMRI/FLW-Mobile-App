package org.piramalswasthya.sakhi.helpers


import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class ApiAnalyticsInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    private val analytics = Firebase.analytics

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()
        val response: Response

        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startTime) / 1_000_000
            logToFirebase(request, durationMs, false, null, e.message)
            throw e
        }

        val durationMs = (System.nanoTime() - startTime) / 1_000_000
        logToFirebase(request, durationMs, response.isSuccessful, response.code, null)

        return response
    }

    private fun logToFirebase(
        request: Request,
        durationMs: Long,
        isSuccess: Boolean,
        responseCode: Int?,
        errorMessage: String?
    ) {
        val endpoint = request.url.encodedPath // e.g., "/user/profile"

        val bundle = Bundle().apply {
            putString("api_name", endpoint)
            putLong("response_time_ms", durationMs)
            putString("status", if (isSuccess) "success" else "failure")
            responseCode?.let { putInt("response_code", it) }
            errorMessage?.let { putString("error_message", it.take(100)) }
        }

        analytics.logEvent("api_call_event", bundle)
    }
}
