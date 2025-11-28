package org.piramalswasthya.sakhi.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.TmcRefreshTokenRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class TokenAuthenticator @Inject constructor(
    private val pref: PreferenceDao,
    @Named("authApi") private val authApi: AmritApiService
) : Authenticator {

    private val refreshLock = java.lang.Object()
    @Volatile private var isRefreshing = false

    override fun authenticate(route: Route?, response: Response): Request? {
        Timber.d("TokenAuthenticator: Authenticate called for 401 response: ${response.request.url}")
        if (responseCount(response) >= 2) {
            Timber.w("TokenAuthenticator: Max retry attempts reached (2)")
            return null
        }

        val refreshToken = pref.getRefreshToken()
        Timber.d("TokenAuthenticator: Retrieved refreshToken: $refreshToken")
        if (refreshToken.isNullOrEmpty()) {
            Timber.e("TokenAuthenticator: Refresh token is null or empty, cannot refresh")
            return null
        }

        synchronized(refreshLock) {
            if (isRefreshing) {
                Timber.d("TokenAuthenticator: Another refresh is in progress, waiting")
                try {
                    var waited = 0
                    while (isRefreshing && waited < 10000) {
                        (refreshLock as java.lang.Object).wait(500)
                        waited += 500
                    }
                } catch (e: InterruptedException) {
                    Timber.e(e, "TokenAuthenticator: Interrupted while waiting for refresh")
                }

                val tokenAfter = pref.getAmritToken().orEmpty()
                val jwtAfter = pref.getJWTAmritToken().orEmpty()
                Timber.d("TokenAuthenticator: Post-wait tokens - Authorization: $tokenAfter, Jwttoken: $jwtAfter")
                return if (tokenAfter.isNotEmpty()) {
                    buildRequest(response.request, tokenAfter, jwtAfter)
                } else {
                    Timber.e("TokenAuthenticator: No valid token after waiting")
                    null
                }
            }

            isRefreshing = true
            Timber.d("TokenAuthenticator: Starting token refresh with refreshToken: $refreshToken")
            try {
                val refreshed = runBlocking {
                    performRefresh(refreshToken)
                }
                if (!refreshed) {
                    Timber.e("TokenAuthenticator: Token refresh failed")
                    return null
                }
            } finally {
                isRefreshing = false
                (refreshLock as java.lang.Object).notifyAll()
            }
        }

        val newKey = pref.getAmritToken().orEmpty()
        val newJwt = pref.getJWTAmritToken().orEmpty()
        Timber.d("TokenAuthenticator: New tokens after refresh - Authorization: $newKey, Jwttoken: $newJwt")
        return if (newKey.isNotEmpty()) buildRequest(response.request, newKey, newJwt) else {
            Timber.e("TokenAuthenticator: No valid token after refresh")
            null
        }
    }

    private suspend fun performRefresh(refreshToken: String): Boolean {
        return try {
            Timber.d("TokenAuthenticator: Calling getRefreshToken API with refreshToken: $refreshToken")
            val resp = authApi.getRefreshToken(json = TmcRefreshTokenRequest(refreshToken))
            if (!resp.isSuccessful) {
                Timber.e("TokenAuthenticator: Refresh API failed: ${resp.code()} ${resp.errorBody()?.string()}")
                return false
            }

            val body = resp.body()?.string().orEmpty()
            if (body.isEmpty()) {
                Timber.e("TokenAuthenticator: Refresh API response body is empty")
                return false
            }

            Timber.d("TokenAuthenticator: Refresh API raw response: $body")
            val json = JSONObject(body)
            // Directly access jwtToken and refreshToken from root JSON object
            val newJwt = json.optString("jwtToken", "")
            //     val newKey = json.optString("key", "")
            val newRefresh = json.optString("refreshToken", refreshToken)
            Timber.d("TokenAuthenticator: Refresh API success: jwtToken=$newJwt, refreshToken=$newRefresh")



            // Save in prefs
            //   pref.registerAmritToken(newKey)
            pref.registerJWTAmritToken(newJwt)
            pref.registerRefreshToken(newRefresh)
            Timber.d("TokenAuthenticator: Saved new tokens -  Jwttoken: $newJwt, refreshToken: $newRefresh")

            // Update static holders
            //  TokenInsertTmcInterceptor.setToken(newKey)
            TokenInsertTmcInterceptor.setJwt(newJwt)

            true
        } catch (e: JSONException) {
            Timber.e(e, "TokenAuthenticator: JSON parsing error in performRefresh: ")
            false
        } catch (e: Exception) {
            Timber.e(e, "TokenAuthenticator: Exception in performRefresh")
            false
        }
    }

    private fun buildRequest(original: Request, key: String, jwt: String): Request {
        val rb = original.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", key)
            .removeHeader("Jwttoken")
        if (jwt.isNotEmpty()) rb.addHeader("Jwttoken", jwt)
        Timber.d("TokenAuthenticator: Built new request with Authorization: $key, Jwttoken: $jwt")
        return rb.build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}