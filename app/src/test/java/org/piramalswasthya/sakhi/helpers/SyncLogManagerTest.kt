package org.piramalswasthya.sakhi.helpers

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.LogLevel

class SyncLogManagerTest {

    @MockK(relaxed = true)
    private lateinit var fileWriter: SyncLogFileWriter

    private lateinit var manager: SyncLogManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        manager = SyncLogManager(fileWriter)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // =====================================================
    // addLog() — File Writer Delegation Tests
    // =====================================================

    @Test
    fun `addLog writes to file writer with correct level`() {
        manager.addLog(LogLevel.INFO, "TestTag", "test message")

        verify { fileWriter.writeLog(LogLevel.INFO, "TestTag", "test message") }
    }

    @Test
    fun `addLog writes ERROR level to file writer`() {
        manager.addLog(LogLevel.ERROR, "Sync", "error occurred")

        verify { fileWriter.writeLog(LogLevel.ERROR, "Sync", "error occurred") }
    }

    @Test
    fun `addLog writes WARN level to file writer`() {
        manager.addLog(LogLevel.WARN, "Worker", "warning msg")

        verify { fileWriter.writeLog(LogLevel.WARN, "Worker", "warning msg") }
    }

    @Test
    fun `addLog writes DEBUG level to file writer`() {
        manager.addLog(LogLevel.DEBUG, "Pull", "debug info")

        verify { fileWriter.writeLog(LogLevel.DEBUG, "Pull", "debug info") }
    }

    @Test
    fun `multiple addLog calls write each to file writer`() {
        manager.addLog(LogLevel.INFO, "Tag1", "msg1")
        manager.addLog(LogLevel.WARN, "Tag2", "msg2")
        manager.addLog(LogLevel.ERROR, "Tag3", "msg3")

        verify(exactly = 3) { fileWriter.writeLog(any(), any(), any()) }
    }

    @Test
    fun `addLog preserves exact message`() {
        val longMessage = "a".repeat(1000)
        manager.addLog(LogLevel.INFO, "Tag", longMessage)

        verify { fileWriter.writeLog(LogLevel.INFO, "Tag", longMessage) }
    }

    // =====================================================
    // clearLogs() Tests
    // =====================================================

    @Test
    fun `clearLogs empties the logs flow`() {
        manager.addLog(LogLevel.INFO, "Tag", "msg1")
        manager.addLog(LogLevel.INFO, "Tag", "msg2")

        manager.clearLogs()

        assertTrue(manager.logs.value.isEmpty())
    }

    @Test
    fun `clearLogs on empty manager does not crash`() {
        manager.clearLogs()

        assertTrue(manager.logs.value.isEmpty())
    }

    @Test
    fun `addLog works after clearLogs`() {
        manager.addLog(LogLevel.INFO, "Tag", "before")
        manager.clearLogs()
        manager.addLog(LogLevel.INFO, "Tag", "after")

        verify { fileWriter.writeLog(LogLevel.INFO, "Tag", "after") }
    }

    // =====================================================
    // Initial State Tests
    // =====================================================

    @Test
    fun `logs flow is empty initially`() {
        assertTrue(manager.logs.value.isEmpty())
    }
}
