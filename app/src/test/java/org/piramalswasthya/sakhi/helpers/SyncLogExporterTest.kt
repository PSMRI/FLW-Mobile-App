package org.piramalswasthya.sakhi.helpers

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SyncLogExporterTest {

    private val context: Context = mockk(relaxed = true)

    @Test
    fun `createShareIntent returns null when log directory is empty`() {
        val emptyDir = Files.createTempDirectory("synclog-empty").toFile()
        emptyDir.deleteOnExit()
        val fileWriter: SyncLogFileWriter = mockk()
        every { fileWriter.getLogDirectory() } returns emptyDir

        val exporter = SyncLogExporter(fileWriter)
        assertNull(exporter.createShareIntent(context))
    }

    @Test
    fun `createShareIntent returns null when log directory does not exist`() {
        val missing = File(
            Files.createTempDirectory("synclog-missing").toFile(),
            "does-not-exist"
        )
        val fileWriter: SyncLogFileWriter = mockk()
        every { fileWriter.getLogDirectory() } returns missing

        val exporter = SyncLogExporter(fileWriter)
        assertNull(exporter.createShareIntent(context))
    }

    @Test
    fun `createShareIntent returns null when directory has only unrelated files`() {
        val dir = Files.createTempDirectory("synclog-unrelated").toFile()
        dir.deleteOnExit()
        File(dir, "readme.txt").writeText("hello")
        File(dir, "other.log").writeText("data") // wrong prefix
        val fileWriter: SyncLogFileWriter = mockk()
        every { fileWriter.getLogDirectory() } returns dir

        val exporter = SyncLogExporter(fileWriter)
        assertNull(exporter.createShareIntent(context))
    }
}
