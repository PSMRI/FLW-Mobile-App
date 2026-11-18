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

    // ── FLW-1256: response bodies are previewed, never buffered whole ──────────

    private val logged = mutableListOf<String>()
    private val recordingLogger = okhttp3.logging.HttpLoggingInterceptor.Logger { logged += it }

    private fun recordingClient(cap: Long = BodyCappedLoggingInterceptor.MAX_LOGGED_BODY_BYTES) =
        OkHttpClient.Builder()
            .addInterceptor(BodyCappedLoggingInterceptor(recordingLogger, maxLoggedBodyBytes = cap))
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

    @Test
    fun `large response reaches the caller intact while only a bounded preview is logged`() {
        val big = "{\"data\":\"" + "a".repeat(3 * 1024 * 1024) + "\"}"
        server.enqueue(MockResponse().setBody(big))

        val response = recordingClient().newCall(post("{}")).execute()

        assertEquals(big, response.body?.string())
        assertTrue(logged.isNotEmpty())
        assertTrue("every log line is bounded", logged.all { it.length <= 4 * 1024 + 64 })
        assertTrue(logged.any { it.startsWith("<-- BODY POST /api/save") })
        assertTrue(logged.any { it.endsWith("...[truncated]") })
    }

    @Test
    fun `small request and response bodies are logged in full`() {
        server.enqueue(MockResponse().setBody("""{"statusCode":200,"data":"ok"}"""))

        recordingClient().newCall(post("""{"benId":1}""")).execute().close()

        assertTrue(logged.contains("""{"benId":1}"""))
        assertTrue(logged.contains("""{"statusCode":200,"data":"ok"}"""))
        assertTrue(logged.any { it.startsWith("--> POST") })
        assertTrue(logged.any { it.startsWith("<-- 200") })
    }

    @Test
    fun `oversized request body is not logged but the call still succeeds`() {
        server.enqueue(MockResponse().setBody("ok"))

        val response = recordingClient(cap = 10).newCall(post("x".repeat(100))).execute()

        assertEquals("ok", response.body?.string())
        assertTrue(logged.none { it.contains("x".repeat(100)) })
        assertTrue(logged.none { it.startsWith("--> BODY") })
        assertEquals(100, server.takeRequest().bodySize)
    }

    @Test
    fun `binary response is not logged as text`() {
        val bytes = ByteArray(64) { it.toByte() }
        server.enqueue(MockResponse().setBody(okio.Buffer().write(bytes)))

        val response = recordingClient().newCall(post("{}")).execute()

        assertEquals(64, response.body?.bytes()?.size)
        assertTrue(logged.contains("(binary body omitted)"))
    }

    @Test
    fun `empty response body logs no body preview`() {
        server.enqueue(MockResponse().setResponseCode(204))

        recordingClient().newCall(post("{}")).execute().close()

        assertTrue(logged.none { it.startsWith("<-- BODY") })
    }
}
