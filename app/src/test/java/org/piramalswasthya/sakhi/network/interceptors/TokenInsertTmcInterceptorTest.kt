package org.piramalswasthya.sakhi.network.interceptors

import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User

class TokenInsertTmcInterceptorTest {

    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var interceptor: TokenInsertTmcInterceptor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interceptor = TokenInsertTmcInterceptor(preferenceDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // =====================================================
    // Companion Object Tests
    // =====================================================

    @Test
    fun `setToken stores and getToken retrieves`() {
        TokenInsertTmcInterceptor.setToken("tmc_token")

        assertEquals("tmc_token", TokenInsertTmcInterceptor.getToken())
    }

    @Test
    fun `setJwt stores and getJwt retrieves`() {
        TokenInsertTmcInterceptor.setJwt("jwt_123")

        assertEquals("jwt_123", TokenInsertTmcInterceptor.getJwt())
    }

    // =====================================================
    // No-Auth Header Tests
    // =====================================================

    @Test
    fun `skips auth headers when No-Auth is true`() {
        val requestSlot = slot<Request>()
        val original = Request.Builder()
            .url("https://example.com/api/public")
            .addHeader("No-Auth", "true")
            .build()
        val chain = mockChain(original, requestSlot)

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertNull(captured.header("Jwttoken"))
        assertNull(captured.header("userId"))
    }

    @Test
    fun `proceeds with original request when No-Auth is true`() {
        val requestSlot = slot<Request>()
        val original = Request.Builder()
            .url("https://example.com/api/public")
            .addHeader("No-Auth", "true")
            .build()
        val chain = mockChain(original, requestSlot)

        interceptor.intercept(chain)

        assertEquals("https://example.com/api/public", requestSlot.captured.url.toString())
    }

    // =====================================================
    // JWT Header Tests
    // =====================================================

    @Test
    fun `adds Jwttoken header when jwt available`() {
        every { preferenceDao.getJWTAmritToken() } returns "jwt_abc"
        every { preferenceDao.getLoggedInUser() } returns null

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        assertEquals("jwt_abc", requestSlot.captured.header("Jwttoken"))
    }

    @Test
    fun `does not add Jwttoken when jwt is null`() {
        every { preferenceDao.getJWTAmritToken() } returns null
        every { preferenceDao.getLoggedInUser() } returns null

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Jwttoken"))
    }

    @Test
    fun `does not add Jwttoken when jwt is blank`() {
        every { preferenceDao.getJWTAmritToken() } returns "   "
        every { preferenceDao.getLoggedInUser() } returns null

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Jwttoken"))
    }

    @Test
    fun `does not add Jwttoken when jwt is empty`() {
        every { preferenceDao.getJWTAmritToken() } returns ""
        every { preferenceDao.getLoggedInUser() } returns null

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Jwttoken"))
    }

    // =====================================================
    // UserId Header Tests
    // =====================================================

    @Test
    fun `adds userId header when user available`() {
        every { preferenceDao.getJWTAmritToken() } returns null
        val user = mockk<User>()
        every { user.userId } returns 42
        every { preferenceDao.getLoggedInUser() } returns user

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        assertEquals("42", requestSlot.captured.header("userId"))
    }

    @Test
    fun `does not add userId when user is null`() {
        every { preferenceDao.getJWTAmritToken() } returns null
        every { preferenceDao.getLoggedInUser() } returns null

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("userId"))
    }

    // =====================================================
    // Combined Header Tests
    // =====================================================

    @Test
    fun `adds both jwt and userId when both available`() {
        every { preferenceDao.getJWTAmritToken() } returns "jwt_xyz"
        val user = mockk<User>()
        every { user.userId } returns 99
        every { preferenceDao.getLoggedInUser() } returns user

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/data", requestSlot)

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals("jwt_xyz", captured.header("Jwttoken"))
        assertEquals("99", captured.header("userId"))
    }

    @Test
    fun `preserves original url`() {
        every { preferenceDao.getJWTAmritToken() } returns "jwt"
        every { preferenceDao.getLoggedInUser() } returns null

        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/specific/path", requestSlot)

        interceptor.intercept(chain)

        assertEquals("https://example.com/api/specific/path", requestSlot.captured.url.toString())
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
