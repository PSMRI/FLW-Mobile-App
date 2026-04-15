package org.piramalswasthya.sakhi.network.interceptors

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContentTypeInterceptorTest {

    private lateinit var interceptor: ContentTypeInterceptor

    @Before
    fun setUp() {
        interceptor = ContentTypeInterceptor()
    }

    // =====================================================
    // Content-Type Header Tests
    // =====================================================

    @Test
    fun `adds content type json header`() {
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api", requestSlot)

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals("application/json", captured.header("Content-Type"))
    }

    @Test
    fun `preserves original url`() {
        val requestSlot = slot<Request>()
        val chain = mockChain("https://example.com/api/test", requestSlot)

        interceptor.intercept(chain)

        assertEquals("https://example.com/api/test", requestSlot.captured.url.toString())
    }

    @Test
    fun `preserves existing headers`() {
        val requestSlot = slot<Request>()
        val original = Request.Builder()
            .url("https://example.com/api")
            .addHeader("Authorization", "Bearer token123")
            .build()
        val chain = mockChain(original, requestSlot)

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals("Bearer token123", captured.header("Authorization"))
        assertEquals("application/json", captured.header("Content-Type"))
    }

    @Test
    fun `works with different http methods`() {
        val requestSlot = slot<Request>()
        val original = Request.Builder()
            .url("https://example.com/api")
            .post(okhttp3.RequestBody.create(null, "{}"))
            .build()
        val chain = mockChain(original, requestSlot)

        interceptor.intercept(chain)

        assertEquals("application/json", requestSlot.captured.header("Content-Type"))
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
