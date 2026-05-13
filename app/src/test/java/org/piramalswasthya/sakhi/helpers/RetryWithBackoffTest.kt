package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class RetryWithBackoffTest {

    @Test
    fun `returns the block result on first success without delaying`() = runTest {
        var invocations = 0
        val result = retryWithBackoff {
            invocations++
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(1, invocations)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `retries on SocketTimeoutException and succeeds on the second attempt`() = runTest {
        var invocations = 0
        val result = retryWithBackoff(initialDelayMs = 100L) {
            invocations++
            if (invocations < 2) throw SocketTimeoutException("first attempt timed out")
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(2, invocations)
        assertEquals(100L, currentTime)
    }

    @Test
    fun `stops after maxAttempts and rethrows the last retryable exception`() = runTest {
        var invocations = 0
        var caught: SocketTimeoutException? = null
        try {
            retryWithBackoff(maxAttempts = 3, initialDelayMs = 100L) {
                invocations++
                throw SocketTimeoutException("attempt $invocations timed out")
            }
            fail("Expected SocketTimeoutException after attempts exhausted")
        } catch (e: SocketTimeoutException) {
            caught = e
        }
        assertNotNull(caught)
        assertEquals(3, invocations)
        assertTrue(
            "Last exception message should surface to the caller",
            caught!!.message!!.contains("attempt 3")
        )
    }

    @Test
    fun `non-retryable exceptions propagate immediately without sleeping`() = runTest {
        var invocations = 0
        var caught: IllegalStateException? = null
        try {
            retryWithBackoff(initialDelayMs = 1_000L) {
                invocations++
                throw IllegalStateException("auth failure should not retry")
            }
            fail("Expected IllegalStateException to propagate without retry")
        } catch (e: IllegalStateException) {
            caught = e
        }
        assertNotNull(caught)
        assertEquals(1, invocations)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `delay grows by backoffFactor between attempts`() = runTest {
        var invocations = 0
        try {
            retryWithBackoff(
                maxAttempts = 4,
                initialDelayMs = 100L,
                maxDelayMs = 10_000L,
                backoffFactor = 2.0
            ) {
                invocations++
                throw SocketTimeoutException("still down")
            }
            fail("Expected SocketTimeoutException after attempts exhausted")
        } catch (_: SocketTimeoutException) {
            // expected
        }
        assertEquals(4, invocations)
        assertEquals(
            "Cumulative virtual time should reflect 100ms + 200ms + 400ms backoff",
            700L, currentTime
        )
    }

    @Test
    fun `delay is capped at maxDelayMs once the cap is exceeded`() = runTest {
        var invocations = 0
        try {
            retryWithBackoff(
                maxAttempts = 5,
                initialDelayMs = 1_000L,
                maxDelayMs = 2_500L,
                backoffFactor = 2.0
            ) {
                invocations++
                throw SocketTimeoutException("still down")
            }
            fail("Expected SocketTimeoutException after attempts exhausted")
        } catch (_: SocketTimeoutException) {
            // expected
        }
        assertEquals(5, invocations)
        // 1000 + 2000 + capped 2500 + capped 2500 = 8000
        assertEquals(8_000L, currentTime)
    }

    @Test
    fun `custom retryOn predicate lets callers expand the retryable set`() = runTest {
        var invocations = 0
        val result = retryWithBackoff(
            initialDelayMs = 50L,
            retryOn = { it is IOException }
        ) {
            invocations++
            if (invocations < 2) throw IOException("connection reset")
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(2, invocations)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires at least one attempt`() = runTest {
        retryWithBackoff(maxAttempts = 0) { "never called" }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires non-negative initial delay`() = runTest {
        retryWithBackoff(initialDelayMs = -1L) { "never called" }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires maxDelay to be at least initialDelay`() = runTest {
        retryWithBackoff(initialDelayMs = 1_000L, maxDelayMs = 500L) { "never called" }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires positive backoff factor`() = runTest {
        retryWithBackoff(backoffFactor = 0.0) { "never called" }
    }

    @Test
    fun `default predicate isTransientNetworkFailure matches only socket timeouts`() {
        assertTrue(isTransientNetworkFailure(SocketTimeoutException()))
        assertFalse(isTransientNetworkFailure(IOException()))
        assertFalse(isTransientNetworkFailure(IllegalStateException()))
    }
}
