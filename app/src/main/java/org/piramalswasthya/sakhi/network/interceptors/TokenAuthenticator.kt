package org.piramalswasthya.sakhi.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.TokenExpiryManager
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.TmcRefreshTokenRequest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class TokenAuthenticator @Inject constructor(
    private val pref: PreferenceDao,
    @Named("authApi") private val authApi: AmritApiService,
    private val tokenExpiryManager: TokenExpiryManager
) : Authenticator {

    private val refreshLock = Any()

    private var lastFailedRefreshToken: String? = null
    private var lastFailedAt: Long = 0L
    private val DEDUPE_WINDOW_MS = 3000L

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) >= 3) return null
        if (response.request.header("No-Auth") == "true") return null

        val oldJwt = response.request.header("Jwttoken")
        val refreshToken = pref.getRefreshToken() ?: return null

        val newJwt = synchronized(refreshLock) {
            val currentJwt = pref.getJWTAmritToken()
            if (!oldJwt.isNullOrBlank() && !currentJwt.isNullOrBlank() && currentJwt != oldJwt) {
                return@synchronized currentJwt
            }

            val now = System.currentTimeMillis()
            if (refreshToken == lastFailedRefreshToken && (now - lastFailedAt) < DEDUPE_WINDOW_MS) {
                return@synchronized null
            }

            runBlocking {
                try {
                    val resp = authApi.getRefreshToken(
                        TmcRefreshTokenRequest(refreshToken)
                    )

                    if (!resp.isSuccessful) {
                        val code = resp.code()
                        resp.errorBody()?.close()
                        Timber.w(
                            "Token refresh failed: HTTP $code"
                        )
                        // Only count genuine auth failures, not transient server errors
                        if (code == 401 || code == 403) {
                            tokenExpiryManager.onRefreshFailed()
                            lastFailedRefreshToken = refreshToken
                            lastFailedAt = System.currentTimeMillis()
                        }
                        return@runBlocking null
                    }

                    val body = resp.body()?.string().orEmpty()
                    if (body.isEmpty()) {
                        Timber.w("Token refresh returned empty body")
                        return@runBlocking null
                    }

                    val json = JSONObject(body)
                    val statusCode = json.optInt("statusCode", 200)
                    if (statusCode != 200) {
                        tokenExpiryManager.onRefreshFailed()
                        return@runBlocking null
                    }

                    val dataObject = json.optJSONObject("data")
                    val jwt = when {
                        !dataObject?.optString("jwtToken").isNullOrBlank() ->
                            dataObject!!.optString("jwtToken")
                        else -> json.optString("jwtToken", "")
                    }
                    val newRefreshCandidate = when {
                        !dataObject?.optString("refreshToken").isNullOrBlank() ->
                            dataObject.optString("refreshToken")
                        else -> json.optString("refreshToken", "")
                    }
                    val newRefresh = newRefreshCandidate.ifBlank { refreshToken }

                    if (jwt.isBlank()) {
                        Timber.w("Token refresh returned blank JWT")
                        null
                    } else {
                        pref.registerJWTAmritToken(jwt)
                        if (newRefresh.isNotBlank()) {
                            pref.registerRefreshToken(newRefresh)
                        }
                        tokenExpiryManager.onRefreshSuccess()
                        jwt
                    }

                } catch (e: Exception) {
                    // Network exceptions (timeout, connection lost, etc.)
                    // are transient — do NOT count as auth failures
                    Timber.e(e, "Token refresh failed due to network error")
                    null
                }
            }
        } ?: return null

        return response.request.newBuilder()
            .removeHeader("Jwttoken")
            .header("Jwttoken", newJwt)
            .build()
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
