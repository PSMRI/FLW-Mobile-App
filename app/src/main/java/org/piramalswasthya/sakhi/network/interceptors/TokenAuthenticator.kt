package org.piramalswasthya.sakhi.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
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

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) >= 2) return null

        if (response.request.header("No-Auth") == "true") return null

        val refreshToken = pref.getRefreshToken() ?: return null

        val newJwt = runBlocking {
            try {
                val resp = authApi.getRefreshToken(
                    TmcRefreshTokenRequest(refreshToken)
                )
                if (!resp.isSuccessful) return@runBlocking null

                val body = resp.body()?.string().orEmpty()
                if (body.isEmpty()) return@runBlocking null

                val json = JSONObject(body)
                val jwt = json.optString("jwtToken", "")
                val newRefresh = json.optString("refreshToken", refreshToken)

                if (jwt.isEmpty()) null else {
                    pref.registerJWTAmritToken(jwt)
                    pref.registerRefreshToken(newRefresh)
                    jwt
                }
            } catch (e: Exception) {
                Timber.e(e, "Token refresh failed")
                null
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