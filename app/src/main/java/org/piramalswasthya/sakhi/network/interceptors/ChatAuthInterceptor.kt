package org.piramalswasthya.sakhi.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.piramalswasthya.sakhi.utils.KeyUtils

/**
 * Interceptor for the chatbot API.
 * Sends the chat API key as a Bearer token.
 */
class ChatAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        requestBuilder.header("Authorization", "Bearer ${KeyUtils.chatApiKey()}")

        return chain.proceed(requestBuilder.build())
    }
}
