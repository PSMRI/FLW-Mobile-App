package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao

class TokenInsertTmcInterceptor(
    private val preferenceDao: PreferenceDao
) : Interceptor {
        companion object {
        private var TOKEN: String = ""
        fun setToken(iToken: String) {
            TOKEN = iToken
        }

        fun getToken(): String {
            return TOKEN
        }

        private var JWT: String = ""
        fun setJwt(iJWT: String) {
            JWT = iJWT
        }

        fun getJwt(): String {
            return JWT
        }
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header("No-Auth") == "true") {
            return chain.proceed(originalRequest)
        }

        val jwt = preferenceDao.getJWTAmritToken()
        val user = preferenceDao.getLoggedInUser()

        val requestBuilder = originalRequest.newBuilder()

        if (!jwt.isNullOrBlank()) {
            requestBuilder.header("Jwttoken", jwt)
        }

        user?.userId?.let {
            requestBuilder.header("userId", it.toString())
        }

        val finalRequest = requestBuilder.build()

       // Timber.d("Request URL=${finalRequest.url}, headers=${finalRequest.headers}")

        return chain.proceed(finalRequest)
    }
}