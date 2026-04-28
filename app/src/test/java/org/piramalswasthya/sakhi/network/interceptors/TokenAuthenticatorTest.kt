package org.piramalswasthya.sakhi.network.interceptors

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.TokenExpiryManager
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.TmcRefreshTokenRequest
import retrofit2.Response as RetrofitResponse

class TokenAuthenticatorTest {

    @MockK
    private lateinit var pref: PreferenceDao

    @MockK
    private lateinit var authApi: AmritApiService

    @MockK(relaxed = true)
    private lateinit var tokenExpiryManager: TokenExpiryManager

    private lateinit var tokenAuthenticator: TokenAuthenticator

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        tokenAuthenticator = TokenAuthenticator(pref, authApi, tokenExpiryManager)
    }

    @Test
    fun `authenticate refreshes jwt when response has nested data object`() {
        every { pref.getRefreshToken() } returns "old-refresh"
        every { pref.getJWTAmritToken() } returns "old-jwt"
        coEvery { authApi.getRefreshToken(TmcRefreshTokenRequest("old-refresh")) } returns
            RetrofitResponse.success(
                """
                    {"statusCode":200,"data":{"jwtToken":"new-jwt","refreshToken":"new-refresh"}}
                """.trimIndent().toResponseBody("application/json".toMediaType())
            )

        val request = tokenAuthenticator.authenticate(null, buildUnauthorizedResponse("old-jwt"))

        assertNotNull(request)
        assertEquals("new-jwt", request!!.header("Jwttoken"))
        verify { pref.registerJWTAmritToken("new-jwt") }
        verify { pref.registerRefreshToken("new-refresh") }
        verify { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `authenticate supports flat jwt response shape`() {
        every { pref.getRefreshToken() } returns "old-refresh"
        every { pref.getJWTAmritToken() } returns "old-jwt"
        coEvery { authApi.getRefreshToken(TmcRefreshTokenRequest("old-refresh")) } returns
            RetrofitResponse.success(
                """
                    {"statusCode":200,"jwtToken":"flat-jwt","refreshToken":"flat-refresh"}
                """.trimIndent().toResponseBody("application/json".toMediaType())
            )

        val request = tokenAuthenticator.authenticate(null, buildUnauthorizedResponse("old-jwt"))

        assertNotNull(request)
        assertEquals("flat-jwt", request!!.header("Jwttoken"))
        verify { pref.registerJWTAmritToken("flat-jwt") }
        verify { pref.registerRefreshToken("flat-refresh") }
        verify { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `authenticate treats missing statusCode as success for backward compatibility`() {
        every { pref.getRefreshToken() } returns "old-refresh"
        every { pref.getJWTAmritToken() } returns "old-jwt"
        coEvery { authApi.getRefreshToken(TmcRefreshTokenRequest("old-refresh")) } returns
            RetrofitResponse.success(
                """
                    {"data":{"jwtToken":"new-jwt","refreshToken":"new-refresh"}}
                """.trimIndent().toResponseBody("application/json".toMediaType())
            )

        val request = tokenAuthenticator.authenticate(null, buildUnauthorizedResponse("old-jwt"))

        assertNotNull(request)
        assertEquals("new-jwt", request!!.header("Jwttoken"))
        verify { pref.registerJWTAmritToken("new-jwt") }
        verify { pref.registerRefreshToken("new-refresh") }
        verify { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `authenticate returns null and marks failure when body statusCode is not 200`() {
        every { pref.getRefreshToken() } returns "old-refresh"
        every { pref.getJWTAmritToken() } returns "old-jwt"
        coEvery { authApi.getRefreshToken(TmcRefreshTokenRequest("old-refresh")) } returns
            RetrofitResponse.success(
                """
                    {"statusCode":5002,"errorMessage":"invalid refresh token"}
                """.trimIndent().toResponseBody("application/json".toMediaType())
            )

        val request = tokenAuthenticator.authenticate(null, buildUnauthorizedResponse("old-jwt"))

        assertNull(request)
        verify { tokenExpiryManager.onRefreshFailed() }
    }

    @Test
    fun `authenticate does not overwrite refresh token when response refresh token is blank`() {
        every { pref.getRefreshToken() } returns "old-refresh"
        every { pref.getJWTAmritToken() } returns "old-jwt"
        coEvery { authApi.getRefreshToken(TmcRefreshTokenRequest("old-refresh")) } returns
            RetrofitResponse.success(
                """
                    {"statusCode":200,"data":{"jwtToken":"new-jwt","refreshToken":""}}
                """.trimIndent().toResponseBody("application/json".toMediaType())
            )

        val request = tokenAuthenticator.authenticate(null, buildUnauthorizedResponse("old-jwt"))

        assertNotNull(request)
        assertEquals("new-jwt", request!!.header("Jwttoken"))
        verify(exactly = 0) { pref.registerRefreshToken("") }
        verify { pref.registerRefreshToken("old-refresh") }
        verify { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `authenticate skips refresh when request has No-Auth header`() {
        val response = buildUnauthorizedResponse(
            oldJwt = "old-jwt",
            noAuth = true
        )

        val request = tokenAuthenticator.authenticate(null, response)

        assertNull(request)
        verify(exactly = 0) { pref.getRefreshToken() }
    }

    private fun buildUnauthorizedResponse(
        oldJwt: String,
        noAuth: Boolean = false
    ): Response {
        val requestBuilder = Request.Builder()
            .url("https://example.com/secure")
            .header("Jwttoken", oldJwt)

        if (noAuth) {
            requestBuilder.header("No-Auth", "true")
        }

        return Response.Builder()
            .request(requestBuilder.build())
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
    }
}
