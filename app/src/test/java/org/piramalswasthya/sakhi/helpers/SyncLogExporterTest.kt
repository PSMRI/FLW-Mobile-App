package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.core.content.FileProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

class SyncLogExporterTest {

    private val context: Context = mockk(relaxed = true)

    @After
    fun tearDown() {
        unmockkAll()
    }

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

    @Test
    fun `createShareIntent zips matching log files and returns a share intent`() {
        val logDir = Files.createTempDirectory("synclog-source").toFile()
        logDir.deleteOnExit()
        File(logDir, "sync-log-2026-08-20.log").writeText("first log contents")
        File(logDir, "sync-log-2026-08-19.log").writeText("second log contents")
        File(logDir, "readme.txt").writeText("ignored")
        val fileWriter: SyncLogFileWriter = mockk()
        every { fileWriter.getLogDirectory() } returns logDir

        val cacheDir = Files.createTempDirectory("synclog-cache").toFile()
        cacheDir.deleteOnExit()
        val realContext: Context = mockk(relaxed = true)
        every { realContext.cacheDir } returns cacheDir
        every { realContext.packageName } returns "org.piramalswasthya.sakhi"

        mockkStatic(FileProvider::class)
        val expectedUri = mockk<Uri>()
        every { FileProvider.getUriForFile(realContext, "org.piramalswasthya.sakhi.provider", any()) } returns expectedUri

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setType(any()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Parcelable>()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<String>()) } answers { self as Intent }
        every { anyConstructed<Intent>().addFlags(any()) } answers { self as Intent }

        val exporter = SyncLogExporter(fileWriter)
        val intent = exporter.createShareIntent(realContext)

        assertTrue(intent != null)
        verify { anyConstructed<Intent>().setType("application/zip") }
        verify { anyConstructed<Intent>().putExtra(Intent.EXTRA_STREAM, expectedUri) }
        verify { anyConstructed<Intent>().putExtra(Intent.EXTRA_SUBJECT, "Sync Logs") }
        verify { anyConstructed<Intent>().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }

        val zipFile = cacheDir.listFiles { f -> f.name.startsWith("sync-logs-") && f.name.endsWith(".zip") }?.firstOrNull()
        assertTrue(zipFile != null && zipFile.exists())
        ZipFile(zipFile).use { zip ->
            val names = mutableListOf<String>()
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                names.add(entries.nextElement().name)
            }
            assertEquals(listOf("sync-log-2026-08-19.log", "sync-log-2026-08-20.log"), names.sorted())
        }
    }
}
