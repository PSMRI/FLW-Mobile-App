package org.piramalswasthya.sakhi.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class LogTest {

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0
        Log.isLoggingEnabled = true
    }

    @After
    fun tearDown() {
        Log.isLoggingEnabled = true
        unmockkAll()
    }

    @Test
    fun `d delegates to android Log when logging is enabled`() {
        Log.d("TAG", "message")

        verify { android.util.Log.d("TAG", "message") }
    }

    @Test
    fun `d does nothing when logging is disabled`() {
        Log.isLoggingEnabled = false

        Log.d("TAG", "message")

        verify(exactly = 0) { android.util.Log.d(any(), any()) }
    }

    @Test
    fun `i delegates to android Log when logging is enabled`() {
        Log.i("TAG", "info")

        verify { android.util.Log.i("TAG", "info") }
    }

    @Test
    fun `i does nothing when logging is disabled`() {
        Log.isLoggingEnabled = false

        Log.i("TAG", "info")

        verify(exactly = 0) { android.util.Log.i(any(), any()) }
    }

    @Test
    fun `w delegates to android Log when logging is enabled`() {
        Log.w("TAG", "warn")

        verify { android.util.Log.w("TAG", "warn") }
    }

    @Test
    fun `w does nothing when logging is disabled`() {
        Log.isLoggingEnabled = false

        Log.w("TAG", "warn")

        verify(exactly = 0) { android.util.Log.w(any(), any<String>()) }
    }

    @Test
    fun `e delegates to android Log when logging is enabled`() {
        Log.e("TAG", "error")

        verify { android.util.Log.e("TAG", "error") }
    }

    @Test
    fun `e does nothing when logging is disabled`() {
        Log.isLoggingEnabled = false

        Log.e("TAG", "error")

        verify(exactly = 0) { android.util.Log.e(any(), any()) }
    }

    @Test
    fun `e with throwable delegates to android Log when logging is enabled`() {
        val throwable = RuntimeException("boom")

        Log.e("TAG", "error", throwable)

        verify { android.util.Log.e("TAG", "error", throwable) }
    }

    @Test
    fun `e with throwable does nothing when logging is disabled`() {
        Log.isLoggingEnabled = false

        Log.e("TAG", "error", RuntimeException("boom"))

        verify(exactly = 0) { android.util.Log.e(any(), any(), any()) }
    }

    @Test
    fun `v delegates to android Log when logging is enabled`() {
        Log.v("TAG", "verbose")

        verify { android.util.Log.v("TAG", "verbose") }
    }

    @Test
    fun `v does nothing when logging is disabled`() {
        Log.isLoggingEnabled = false

        Log.v("TAG", "verbose")

        verify(exactly = 0) { android.util.Log.v(any(), any()) }
    }
}
