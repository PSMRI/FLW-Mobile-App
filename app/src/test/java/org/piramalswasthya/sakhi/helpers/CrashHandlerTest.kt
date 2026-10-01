package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceManager
import java.io.File
import java.io.IOException
import java.nio.file.Files

class CrashHandlerTest {

    private lateinit var tempDir: File
    private lateinit var context: Context
    private var originalDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var mockDefaultHandler: Thread.UncaughtExceptionHandler

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("crash_handler_test").toFile()
        tempDir.deleteOnExit()
        context = mockk(relaxed = true)
        every { context.filesDir } returns tempDir
        every { context.packageName } returns "org.piramalswasthya.sakhi"

        originalDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        mockDefaultHandler = mockk(relaxed = true)
        Thread.setDefaultUncaughtExceptionHandler(mockDefaultHandler)
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalDefaultHandler)
        unmockkAll()
    }

    private fun crashesDir() = File(tempDir, "crashes")

    @Test
    fun `uncaughtException swallows the failure and delegates to the default handler when pref is unset`() {
        val handler = CrashHandler(context)
        val thread = Thread.currentThread()
        val error = RuntimeException("boom")

        handler.uncaughtException(thread, error)

        verify { mockDefaultHandler.uncaughtException(thread, error) }
        assertTrue(crashesDir().exists())
        assertEquals(0, crashesDir().listFiles()?.size ?: 0)
    }

    @Test
    fun `uncaughtException does not crash when the default handler is null`() {
        Thread.setDefaultUncaughtExceptionHandler(null)
        val handler = CrashHandler(context)
        val thread = Thread.currentThread()

        handler.uncaughtException(thread, RuntimeException("boom"))
    }

    @Test
    fun `uncaughtException writes a crash report when the preference lookup succeeds`() {
        mockkObject(PreferenceManager.Companion)
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString(any(), any()) } returns "asha1"
        every { PreferenceManager.getInstance(any()) } returns prefs

        val handler = CrashHandler(context)
        handler.pref = PreferenceDao(context)
        val thread = Thread.currentThread()
        val error = RuntimeException("boom")

        handler.uncaughtException(thread, error)

        verify { mockDefaultHandler.uncaughtException(thread, error) }
        val crashFiles = crashesDir().listFiles()
        assertEquals(1, crashFiles?.size ?: 0)
        val content = crashFiles!![0].readText()
        assertTrue(content.contains("Username: asha1"))
        assertTrue(content.contains("Package: org.piramalswasthya.sakhi"))
        assertTrue(content.contains("boom"))
    }

    @Test
    fun `uncaughtException does not write a crash report when PreferenceDao construction fails`() {
        mockkObject(PreferenceManager.Companion)
        every { PreferenceManager.getInstance(any()) } throws IOException("keystore unavailable")

        val handler = CrashHandler(context)
        handler.pref = mockk(relaxed = true) {
            every { getRememberedUserName() } returns "asha1"
        }
        val thread = Thread.currentThread()

        handler.uncaughtException(thread, RuntimeException("boom"))

        assertEquals(0, crashesDir().listFiles()?.size ?: 0)
    }
}
