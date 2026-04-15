package org.piramalswasthya.sakhi.network.interceptors

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenInsertAbhaInterceptorTest {

    private lateinit var interceptor: TokenInsertAbhaInterceptor

    @Before
    fun setUp() {
        interceptor = TokenInsertAbhaInterceptor()
        TokenInsertAbhaInterceptor.setToken("")
        TokenInsertAbhaInterceptor.setXToken("")
    }

    @After
    fun tearDown() {
        TokenInsertAbhaInterceptor.setToken("")
        TokenInsertAbhaInterceptor.setXToken("")
    }

    // =====================================================
    // setToken / getToken Tests
    // =====================================================

    @Test
    fun `setToken stores token`() {
        TokenInsertAbhaInterceptor.setToken("my_token")

        assertEquals("my_token", TokenInsertAbhaInterceptor.getToken())
    }

    @Test
    fun `setToken with null stores empty string`() {
        TokenInsertAbhaInterceptor.setToken("something")
        TokenInsertAbhaInterceptor.setToken(null)

        assertEquals("", TokenInsertAbhaInterceptor.getToken())
    }

    @Test
    fun `setXToken stores x-token`() {
        TokenInsertAbhaInterceptor.setXToken("x_token_123")

        assertEquals("x_token_123", TokenInsertAbhaInterceptor.getXToken())
    }

    @Test
    fun `setXToken with null stores empty string`() {
        TokenInsertAbhaInterceptor.setXToken(null)

        assertEquals("", TokenInsertAbhaInterceptor.getXToken())
    }

    // =====================================================
    // Authorization Header Tests
    // =====================================================

    @Test
    fun `adds bearer token when no No-Auth header`() {
        TokenInsertAbhaInterceptor.setToken("abha_token")
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/create", requestSlot)

        interceptor.intercept(chain)

        assertEquals("Bearer abha_token", requestSlot.captured.header("Authorization"))
    }

    @Test
    fun `skips authorization when No-Auth header present`() {
        TokenInsertAbhaInterceptor.setToken("abha_token")
        val requestSlot = slot<Request>()
        val original = Request.Builder()
            .url("https://example.com/api/public")
            .addHeader("No-Auth", "true")
            .build()
        val chain = mockChain(original, requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Authorization"))
    }

    // =====================================================
    // X-Token Header Tests
    // =====================================================

    @Test
    fun `adds x-token for getCard url`() {
        TokenInsertAbhaInterceptor.setToken("token")
        TokenInsertAbhaInterceptor.setXToken("x_token_value")
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/getCard", requestSlot)

        interceptor.intercept(chain)

        assertEquals("Bearer x_token_value", requestSlot.captured.header("x-token"))
    }

    @Test
    fun `adds x-token for getPngCard url`() {
        TokenInsertAbhaInterceptor.setToken("token")
        TokenInsertAbhaInterceptor.setXToken("x_token_value")
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/getPngCard", requestSlot)

        interceptor.intercept(chain)

        assertEquals("Bearer x_token_value", requestSlot.captured.header("x-token"))
    }

    @Test
    fun `adds x-token for abha-card url`() {
        TokenInsertAbhaInterceptor.setToken("token")
        TokenInsertAbhaInterceptor.setXToken("x_token_value")
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/abha-card", requestSlot)

        interceptor.intercept(chain)

        assertEquals("Bearer x_token_value", requestSlot.captured.header("x-token"))
    }

    @Test
    fun `does not add x-token for non card url`() {
        TokenInsertAbhaInterceptor.setToken("token")
        TokenInsertAbhaInterceptor.setXToken("x_token_value")
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/profile", requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("x-token"))
    }

    @Test
    fun `does not add x-token when xtoken is empty`() {
        TokenInsertAbhaInterceptor.setToken("token")
        TokenInsertAbhaInterceptor.setXToken("")
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/getCard", requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("x-token"))
    }

    // --- Helper ---

    private fun mockChain(url: String, requestSlot: CapturingSlot<Request>): Interceptor.Chain {
        return mockChain(Request.Builder().url(url).build(), requestSlot)
    }

    private fun mockChain(request: Request, requestSlot: CapturingSlot<Request>): Interceptor.Chain {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()
        return chain
    }
}
