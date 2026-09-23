package org.piramalswasthya.sakhi.network.interceptors

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber

/**
 * Unit tests for [LoggingInterceptor], the OkHttp logger that truncates
 * oversized log messages before handing them to Timber.
 */
class LoggingInterceptorTest {

    private val captured = mutableListOf<Pair<String?, String>>()
    private lateinit var logger: LoggingInterceptor

    @Before
    fun setUp() {
        captured.clear()
        Timber.uprootAll()
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                captured += tag to message
            }
        })
        logger = LoggingInterceptor()
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun `logs short message unchanged`() {
        logger.log("GET /api/ping")

        assertEquals(1, captured.size)
        assertEquals("GET /api/ping", captured[0].second)
    }

    @Test
    fun `logs under the okhttp tag`() {
        logger.log("hello")

        assertEquals("OkHttp", captured[0].first)
    }

    @Test
    fun `logs empty message without crashing`() {
        logger.log("")

        assertEquals(1, captured.size)
        assertEquals("", captured[0].second)
    }

    @Test
    fun `logs message exactly at the limit unchanged`() {
        val message = "a".repeat(4 * 1024)

        logger.log(message)

        assertEquals(message, captured[0].second)
    }

    @Test
    fun `truncates message over the limit`() {
        val message = "b".repeat(4 * 1024 + 100)

        logger.log(message)

        val logged = captured[0].second
        assertTrue(logged.endsWith("...[truncated]"))
        assertEquals("b".repeat(4 * 1024) + "...[truncated]", logged)
    }

    @Test
    fun `truncated message keeps the leading content`() {
        val message = "HEADER" + "c".repeat(4 * 1024)

        logger.log(message)

        assertTrue(captured[0].second.startsWith("HEADER"))
    }

    @Test
    fun `handles repeated calls independently`() {
        logger.log("one")
        logger.log("d".repeat(4 * 1024 + 1))
        logger.log("three")

        assertEquals(3, captured.size)
        assertEquals("one", captured[0].second)
        assertTrue(captured[1].second.endsWith("...[truncated]"))
        assertEquals("three", captured[2].second)
    }
}
