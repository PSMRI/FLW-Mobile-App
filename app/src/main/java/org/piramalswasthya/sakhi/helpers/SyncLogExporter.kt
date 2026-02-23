package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncLogExporter @Inject constructor(
    private val fileWriter: SyncLogFileWriter
) {

    /**
     * Creates an [Intent.ACTION_SEND] for sharing sync log files as a zip.
     * Must be called from a background thread (performs file I/O).
     * Returns `null` if no log files exist.
     */
    fun createShareIntent(context: Context): Intent? {
        val logDir = fileWriter.getLogDirectory()
        val logFiles = logDir.listFiles { f ->
            f.name.startsWith("sync-log-") && f.name.endsWith(".log")
        }

        if (logFiles.isNullOrEmpty()) return null

        val zipFile = File(context.cacheDir, "sync-logs-${System.currentTimeMillis()}.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            for (file in logFiles.sortedBy { it.name }) {
                zos.putNextEntry(ZipEntry(file.name))
                BufferedInputStream(FileInputStream(file)).use { bis ->
                    bis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            zipFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sync Logs")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
