package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao

/**
 * Interceptor for the chatbot API.
 * Auth mechanism is TBD — currently sends a placeholder API key header.
 * Update this once the backend team confirms the auth scheme.
 */
class ChatAuthInterceptor(
    private val preferenceDao: PreferenceDao
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // TODO: Replace with actual auth once backend confirms.
        // Options: API key, Bearer JWT, or backend-to-backend relay.
        // For now, pass the AMRIT JWT as a fallback identifier.
        val jwt = preferenceDao.getJWTAmritToken()
        if (!jwt.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $jwt")
        }

        // Pass user language preference for localized responses
        val lang = preferenceDao.getCurrentLanguage().symbol
        requestBuilder.header("Accept-Language", lang)

        return chain.proceed(requestBuilder.build())
    }
}
