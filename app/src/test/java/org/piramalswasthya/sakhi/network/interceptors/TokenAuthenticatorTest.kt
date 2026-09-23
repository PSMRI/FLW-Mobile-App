package org.piramalswasthya.sakhi.network.interceptors

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.TokenExpiryManager
import org.piramalswasthya.sakhi.network.AmritApiService
import java.io.IOException
import retrofit2.Response as RetrofitResponse

/**
 * Unit tests for [TokenAuthenticator] covering the early-exit guards,
 * the concurrent-refresh short circuit, and the refresh success / failure arms.
 */
class TokenAuthenticatorTest {

    private lateinit var pref: PreferenceDao
    private lateinit var authApi: AmritApiService
    private lateinit var tokenExpiryManager: TokenExpiryManager
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setUp() {
        pref = mockk(relaxed = true)
        authApi = mockk(relaxed = true)
        tokenExpiryManager = mockk(relaxed = true)
        authenticator = TokenAuthenticator(pref, authApi, tokenExpiryManager)
    }

    // ---------------- helpers ----------------

    private fun request(
        jwt: String? = "old-jwt",
        noAuth: String? = null
    ): Request {
        val builder = Request.Builder().url("https://example.org/api/resource")
        jwt?.let { builder.header("Jwttoken", it) }
        noAuth?.let { builder.header("No-Auth", it) }
        return builder.build()
    }

    private fun response(request: Request, prior: Response? = null): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .apply { prior?.let { priorResponse(it) } }
            .build()

    private fun jsonBody(json: String): ResponseBody =
        json.toResponseBody("application/json".toMediaTypeOrNull())

    private fun successRefresh(json: String) {
        coEvery { authApi.getRefreshToken(any()) } returns
                RetrofitResponse.success(jsonBody(json))
    }

    private fun failedRefresh(code: Int) {
        coEvery { authApi.getRefreshToken(any()) } returns
                RetrofitResponse.error(code, jsonBody("""{"error":"nope"}"""))
    }

    // ---------------- early-exit guards ----------------

    @Test
    fun `returns null when response count reaches three`() {
        val req = request()
        val first = response(req)
        val second = response(req, prior = first)
        val third = response(req, prior = second)

        assertNull(authenticator.authenticate(null, third))
        coVerify(exactly = 0) { authApi.getRefreshToken(any()) }
    }

    @Test
    fun `returns null when request is marked No-Auth`() {
        val res = response(request(noAuth = "true"))

        assertNull(authenticator.authenticate(null, res))
        verify(exactly = 0) { pref.getRefreshToken() }
    }

    @Test
    fun `returns null when refresh token is missing`() {
        every_refresh(null)
        val res = response(request())

        assertNull(authenticator.authenticate(null, res))
        coVerify(exactly = 0) { authApi.getRefreshToken(any()) }
    }

    private fun every_refresh(token: String?) {
        io.mockk.every { pref.getRefreshToken() } returns token
    }

    // ---------------- concurrent refresh short circuit ----------------

    @Test
    fun `uses already refreshed jwt without calling the api`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns "brand-new-jwt"

        val result = authenticator.authenticate(null, response(request(jwt = "old-jwt")))

        assertNotNull(result)
        assertEquals("brand-new-jwt", result!!.header("Jwttoken"))
        coVerify(exactly = 0) { authApi.getRefreshToken(any()) }
    }

    @Test
    fun `refreshes when stored jwt equals the failed jwt`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns "old-jwt"
        successRefresh("""{"jwtToken":"fresh-jwt","refreshToken":"fresh-refresh"}""")

        val result = authenticator.authenticate(null, response(request(jwt = "old-jwt")))

        assertNotNull(result)
        assertEquals("fresh-jwt", result!!.header("Jwttoken"))
        coVerify(exactly = 1) { authApi.getRefreshToken(any()) }
    }

    // ---------------- refresh success ----------------

    @Test
    fun `stores new tokens and reports success on refresh`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("""{"jwtToken":"fresh-jwt","refreshToken":"fresh-refresh"}""")

        val result = authenticator.authenticate(null, response(request(jwt = null)))

        assertNotNull(result)
        assertEquals("fresh-jwt", result!!.header("Jwttoken"))
        verify { pref.registerJWTAmritToken("fresh-jwt") }
        verify { pref.registerRefreshToken("fresh-refresh") }
        verify { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `keeps existing refresh token when response omits one`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("""{"jwtToken":"fresh-jwt"}""")

        val result = authenticator.authenticate(null, response(request(jwt = null)))

        assertEquals("fresh-jwt", result!!.header("Jwttoken"))
        verify { pref.registerRefreshToken("refresh-token") }
    }

    @Test
    fun `returns null when refresh response has blank jwt`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("""{"jwtToken":"","refreshToken":"x"}""")

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 0) { pref.registerJWTAmritToken(any()) }
        verify(exactly = 0) { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `returns null when refresh response body is empty`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("")

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 0) { pref.registerJWTAmritToken(any()) }
    }

    @Test
    fun `returns null when refresh response body is not json`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("plain text, definitely not json")

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 0) { tokenExpiryManager.onRefreshSuccess() }
    }

    // ---------------- refresh failure ----------------

    @Test
    fun `counts 401 refresh failure as auth failure`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        failedRefresh(401)

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 1) { tokenExpiryManager.onRefreshFailed() }
    }

    @Test
    fun `counts 403 refresh failure as auth failure`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        failedRefresh(403)

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 1) { tokenExpiryManager.onRefreshFailed() }
    }

    @Test
    fun `does not count 500 refresh failure as auth failure`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        failedRefresh(500)

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 0) { tokenExpiryManager.onRefreshFailed() }
    }

    @Test
    fun `treats network exception as transient and returns null`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        coEvery { authApi.getRefreshToken(any()) } throws IOException("boom")

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        verify(exactly = 0) { tokenExpiryManager.onRefreshFailed() }
        verify(exactly = 0) { tokenExpiryManager.onRefreshSuccess() }
    }

    @Test
    fun `dedupes repeated refresh attempts for the same failed token`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        failedRefresh(401)

        assertNull(authenticator.authenticate(null, response(request(jwt = null))))
        assertNull(authenticator.authenticate(null, response(request(jwt = null))))

        // second attempt is short-circuited by the dedupe window
        coVerify(exactly = 1) { authApi.getRefreshToken(any()) }
    }

    @Test
    fun `sends the stored refresh token to the api`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("""{"jwtToken":"fresh-jwt"}""")

        authenticator.authenticate(null, response(request(jwt = null)))

        coVerify {
            authApi.getRefreshToken(match { it.refreshToken == "refresh-token" })
        }
    }

    @Test
    fun `retried request keeps original url and single jwt header`() {
        every_refresh("refresh-token")
        io.mockk.every { pref.getJWTAmritToken() } returns null
        successRefresh("""{"jwtToken":"fresh-jwt"}""")

        val result = authenticator.authenticate(null, response(request(jwt = "old-jwt")))!!

        assertEquals("https://example.org/api/resource", result.url.toString())
        assertEquals(listOf("fresh-jwt"), result.headers.values("Jwttoken"))
    }
}
