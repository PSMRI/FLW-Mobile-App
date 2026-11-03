package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import org.piramalswasthya.sakhi.helpers.AccountDeactivationManager
import timber.log.Timber
import javax.inject.Inject

class AccountDeactivationInterceptor @Inject constructor(
    private val deactivationManager: AccountDeactivationManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        try {
            // The deactivation reply is a small error JSON. Peeking only a few KB keeps large
            // sync pages from being copied and parsed a second time (OOM / GC ANRs in the field).
            val peekBody = response.peekBody(MAX_PEEK_BYTES)
            val truncated = peekBody.contentLength() >= MAX_PEEK_BYTES
            val bodyString = if (truncated) "" else peekBody.string()
            if (bodyString.contains("5002")) {
                val json = JSONObject(bodyString)
                val statusCode = json.optInt("statusCode", -1)
                if (statusCode == 5002) {
                    val errorMessage = json.optString("errorMessage", "")
                    if (errorMessage.contains("deactivat", ignoreCase = true) ||
                        errorMessage.contains("locked", ignoreCase = true)
                    ) {
                        Timber.w("Account deactivation detected: $errorMessage")
                        deactivationManager.emitIfCooldownPassed(errorMessage)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently ignore parse errors — don't break the normal flow
            Timber.d("AccountDeactivationInterceptor: skipping non-JSON response")
        }

        return response
    }

    private companion object {
        const val MAX_PEEK_BYTES = 16 * 1024L
    }
}
