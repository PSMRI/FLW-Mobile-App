package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ApiAnalyticsInterceptorTest {

    private lateinit var context: Context
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var interceptor: ApiAnalyticsInterceptor

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        firebaseAnalytics = mockk(relaxed = true)

        mockkStatic("com.google.firebase.analytics.ktx.AnalyticsKt")
        every { Firebase.analytics } returns firebaseAnalytics

        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putLong(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putInt(any(), any()) } just Runs

        interceptor = ApiAnalyticsInterceptor(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun mockChain(request: Request, response: Response? = null, error: Exception? = null): Interceptor.Chain {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        if (error != null) {
            every { chain.proceed(request) } throws error
        } else {
            every { chain.proceed(request) } returns response!!
        }
        return chain
    }

    private fun successResponse(request: Request, code: Int): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .build()
    }

    @Test
    fun `intercept returns the response from the chain on success`() {
        val request = Request.Builder().url("https://example.com/api/sync").build()
        val response = successResponse(request, 200)
        val chain = mockChain(request, response = response)

        val result = interceptor.intercept(chain)

        assertEquals(response, result)
    }

    @Test
    fun `intercept logs success event with endpoint and status`() {
        val request = Request.Builder().url("https://example.com/api/sync").build()
        val response = successResponse(request, 200)
        val chain = mockChain(request, response = response)

        interceptor.intercept(chain)

        verify { anyConstructed<Bundle>().putString("api_name", "/api/sync") }
        verify { anyConstructed<Bundle>().putString("status", "success") }
        verify { anyConstructed<Bundle>().putInt("response_code", 200) }
        verify { firebaseAnalytics.logEvent("api_call_event", any()) }
    }

    @Test
    fun `intercept logs failure status for non successful response`() {
        val request = Request.Builder().url("https://example.com/api/push").build()
        val response = successResponse(request, 500)
        val chain = mockChain(request, response = response)

        interceptor.intercept(chain)

        verify { anyConstructed<Bundle>().putString("status", "failure") }
        verify { anyConstructed<Bundle>().putInt("response_code", 500) }
    }

    @Test
    fun `intercept records response time`() {
        val request = Request.Builder().url("https://example.com/api/sync").build()
        val response = successResponse(request, 200)
        val chain = mockChain(request, response = response)
        val captured = slot<Long>()
        every { anyConstructed<Bundle>().putLong("response_time_ms", capture(captured)) } just Runs

        interceptor.intercept(chain)

        assertTrue(captured.captured >= 0L)
    }

    @Test
    fun `intercept logs failure and rethrows when chain proceed throws`() {
        val request = Request.Builder().url("https://example.com/api/sync").build()
        val error = IOException("network unreachable")
        val chain = mockChain(request, error = error)

        try {
            interceptor.intercept(chain)
            org.junit.Assert.fail("expected IOException to propagate")
        } catch (e: IOException) {
            assertEquals("network unreachable", e.message)
        }

        verify { anyConstructed<Bundle>().putString("status", "failure") }
        verify { anyConstructed<Bundle>().putString("error_message", "network unreachable") }
        verify(exactly = 0) { anyConstructed<Bundle>().putInt("response_code", any()) }
    }

    @Test
    fun `intercept truncates long error message to 100 characters`() {
        val request = Request.Builder().url("https://example.com/api/sync").build()
        val error = IOException("e".repeat(150))
        val chain = mockChain(request, error = error)
        val captured = slot<String>()
        every { anyConstructed<Bundle>().putString("error_message", capture(captured)) } just Runs

        try {
            interceptor.intercept(chain)
        } catch (_: IOException) {
        }

        assertEquals(100, captured.captured.length)
    }

    @Test
    fun `intercept includes request body in the logged bundle`() {
        val body = RequestBody.create(null, "{\"key\":\"value\"}")
        val request = Request.Builder().url("https://example.com/api/submit").post(body).build()
        val response = successResponse(request, 201)
        val chain = mockChain(request, response = response)
        val captured = slot<String>()
        every { anyConstructed<Bundle>().putString("api_request_body", capture(captured)) } just Runs

        interceptor.intercept(chain)

        assertTrue(captured.captured.isNotEmpty())
    }

    @Test
    fun `intercept handles null request body`() {
        val request = Request.Builder().url("https://example.com/api/fetch").build()
        val response = successResponse(request, 200)
        val chain = mockChain(request, response = response)
        val captured = slot<String>()
        every { anyConstructed<Bundle>().putString("api_request_body", capture(captured)) } just Runs

        interceptor.intercept(chain)

        assertEquals("null", captured.captured)
    }
}
