package org.piramalswasthya.sakhi.network.interceptors

import android.util.Log
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.helpers.AccountDeactivationManager
import timber.log.Timber

/**
 * Unit tests for AccountDeactivationInterceptor.
 *
 * NOTE: Tests that verify deactivation detection (statusCode 5002 + keyword match)
 * cannot run in JVM unit tests because the interceptor uses org.json.JSONObject
 * which is an Android SDK class and is not available in local JVM tests.
 * Those tests require Android instrumented tests.
 *
 * The tests below cover response passthrough, edge cases, and error resilience.
 */
class AccountDeactivationInterceptorTest {

    @MockK(relaxed = true)
    private lateinit var deactivationManager: AccountDeactivationManager

    private lateinit var interceptor: AccountDeactivationInterceptor
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(Log::class)
        every { Log.println(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        Timber.uprootAll()
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
        })
        interceptor = AccountDeactivationInterceptor(deactivationManager)
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
        Timber.uprootAll()
        unmockkAll()
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    // =====================================================
    // Response Passthrough Tests
    // =====================================================

    @Test
    fun `returns response with correct status code`() {
        server.enqueue(MockResponse().setBody("ok"))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(200, response.code)
    }

    @Test
    fun `returns 500 response unchanged`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("error"))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(500, response.code)
    }

    @Test
    fun `response body is still readable after interception`() {
        val body = """{"statusCode":200,"data":"hello"}"""
        server.enqueue(MockResponse().setBody(body))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()
        val responseBody = response.body?.string()

        assertEquals(body, responseBody)
    }

    // =====================================================
    // Error Resilience Tests
    // =====================================================

    @Test
    fun `handles empty response body without crash`() {
        server.enqueue(MockResponse().setBody(""))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(200, response.code)
        verify(exactly = 0) { deactivationManager.emitIfCooldownPassed(any()) }
    }

    @Test
    fun `handles non json response without crash`() {
        server.enqueue(MockResponse().setBody("not json at all"))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(200, response.code)
        verify(exactly = 0) { deactivationManager.emitIfCooldownPassed(any()) }
    }

    @Test
    fun `handles html response without crash`() {
        server.enqueue(MockResponse().setBody("<html><body>Error</body></html>"))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(200, response.code)
        verify(exactly = 0) { deactivationManager.emitIfCooldownPassed(any()) }
    }

    @Test
    fun `does not trigger deactivation for normal json`() {
        // JSONObject is android stub in JVM tests, so the catch block handles it
        server.enqueue(MockResponse().setBody("""{"statusCode":200}"""))

        val response = buildClient().newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(200, response.code)
        verify(exactly = 0) { deactivationManager.emitIfCooldownPassed(any()) }
    }

    @Test
    fun `preserves request url through interceptor`() {
        server.enqueue(MockResponse().setBody("ok"))

        buildClient().newCall(Request.Builder().url(server.url("/api/test")).build()).execute()

        val recorded = server.takeRequest()
        assertEquals("/api/test", recorded.path)
    }

    @Test
    fun `preserves request headers through interceptor`() {
        server.enqueue(MockResponse().setBody("ok"))

        buildClient().newCall(
            Request.Builder()
                .url(server.url("/"))
                .addHeader("Authorization", "Bearer token")
                .build()
        ).execute()

        val recorded = server.takeRequest()
        assertEquals("Bearer token", recorded.getHeader("Authorization"))
    }
}
