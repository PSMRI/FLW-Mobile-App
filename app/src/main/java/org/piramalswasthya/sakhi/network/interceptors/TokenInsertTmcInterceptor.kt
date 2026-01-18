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
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("No-Auth") == null) {
            val userId = preferenceDao.getLoggedInUser()?.userId.toString()
            request = request
                .newBuilder()
                .addHeader("Authorization", TOKEN)
                .addHeader("userId", userId)
                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}