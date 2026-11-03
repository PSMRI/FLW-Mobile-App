package org.piramalswasthya.sakhi.network.interceptors

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [BodyCappedLoggingInterceptor]: every failed API call must reach the log at
 * error level, including oversized uploads that are otherwise logged at HEADERS level only.
 */
class BodyCappedLoggingInterceptorTest {

    private val errors = mutableListOf<String>()
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        errors.clear()
        Timber.uprootAll()
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (priority == Log.ERROR) errors += message
            }
        })
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
        Timber.uprootAll()
    }

    // Small cap so the "oversized upload" path is easy to hit.
    private fun client() = OkHttpClient.Builder()
        .addInterceptor(BodyCappedLoggingInterceptor(LoggingInterceptor(), maxLoggedBodyBytes = 10))
        .readTimeout(1, TimeUnit.SECONDS)
        .build()

    private fun post(body: String) = Request.Builder()
        .url(server.url("/api/save"))
        .post(body.toRequestBody("application/json".toMediaType()))
        .build()

    @Test
    fun `successful call logs no error`() {
        server.enqueue(MockResponse().setBody("""{"statusCode":200}"""))

        client().newCall(post("{}")).execute().close()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `http error is logged with code and error body`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"db down"}"""))

        client().newCall(post("{}")).execute().close()

        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("HTTP 500"))
        assertTrue(errors[0].contains("db down"))
        assertTrue(errors[0].contains("/api/save"))
    }

    @Test
    fun `http error on oversized upload still logs the error body`() {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"invalid benId"}"""))

        client().newCall(post("x".repeat(100))).execute().close()

        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("invalid benId"))
    }

    @Test
    fun `error body is still readable by the caller`() {
        val body = """{"error":"db down"}"""
        server.enqueue(MockResponse().setResponseCode(500).setBody(body))

        val response = client().newCall(post("{}")).execute()

        assertEquals(body, response.body?.string())
    }

    @Test
    fun `network failure is logged at error level and rethrown`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val thrown = try {
            client().newCall(post("{}")).execute().close()
            null
        } catch (e: IOException) {
            e
        }

        assertTrue(thrown != null)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("API FAILED POST /api/save"))
    }
}
