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
                .addHeader("Cookie" , "Jwttoken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5OTAwMDAwMDAxIiwidXNlcklkIjoiMTIwNSIsInRva2VuX3R5cGUiOiJhY2Nlc3MiLCJqdGkiOiIwMWJlNWNkZC0zYmQ3LTQ3MGItOWZlOC1iMTYzOTZmNDc5MWUiLCJpYXQiOjE3NDYwMDYwNzMsImV4cCI6MTc0NjAwNjk3M30.420jSLBaHSmQLFlfSdytiUSQKgyQ9hgwkgCydzGGjJQ")
                .build()
        }
        Timber.d("Request : $request")
        return chain.proceed(request)
    }
}