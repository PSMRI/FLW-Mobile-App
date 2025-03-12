package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class TokenInsertTmcInterceptor : Interceptor {
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
            request = request
                .newBuilder()
                .addHeader("Authorization", TOKEN)
                .addHeader("Cookie" , "Jwttoken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5OTAwMDAwMDAxIiwidXNlcklkIjoiMTIwNSIsImlhdCI6MTc0MDY2MDc0MywiZXhwIjoxNzQwNzQ3MTQzfQ.a0lgGk5BO5vBlVGRL1F-0jN_-NpzTvc7xd05EXcW_gs")

                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}