package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import timber.log.Timber

class TokenInsertTmcInterceptor(private val preferenceDao: PreferenceDao) : Interceptor {
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
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            val requestBuilder = request.newBuilder()
            preferenceDao.getLoggedInUser()?.userId?.let {
                requestBuilder.addHeader("userId", it.toString())
            }
            requestBuilder.addHeader("Jwttoken", JWT)
            request = requestBuilder.build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}