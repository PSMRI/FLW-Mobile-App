package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.sakhi.model.LogLevel
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncLogFileWriter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val LOG_DIR = "sync-logs"
        private const val FILE_PREFIX = "sync-log-"
        private const val FILE_SUFFIX = ".log"
        private const val MAX_AGE_DAYS = 3
        private const val MAX_TOTAL_SIZE_BYTES = 100L * 1024 * 1024 // 100 MB
        private const val BUFFER_SIZE = 8 * 1024 // 8 KB
    }

    private val logDir = File(context.filesDir, LOG_DIR)

    private val dateFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
    private val timestampFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    }

    private val handlerThread = HandlerThread("SyncLogWriter").apply { start() }
    private val handler = Handler(handlerThread.looper)

    private var currentWriter: BufferedWriter? = null
    private var currentDate: String? = null

    fun getLogDirectory(): File = logDir

    fun writeLog(level: LogLevel, tag: String, message: String) {
        val now = System.currentTimeMillis()
        val levelStr = level.name.padEnd(5)
        val ts = timestampFormat.get()!!.format(Date(now))
        val line = "$ts [$levelStr] [$tag] $message\n"
        val today = dateFormat.get()!!.format(Date(now))

        handler.post {
            try {
                ensureWriter(today)
                currentWriter?.write(line)
                currentWriter?.flush()
            } catch (e: Exception) {
                Timber.e(e, "SyncLogFileWriter: failed to write log")
                closeWriter()
            }
        }
    }

    fun runRotation() {
        handler.post {
            try {
                rotate()
            } catch (e: Exception) {
                Timber.e(e, "SyncLogFileWriter: rotation failed")
            }
        }
    }

    fun shutdown() {
        handler.post { closeWriter() }
        handlerThread.quitSafely()
    }

    private fun ensureWriter(today: String) {
        if (currentDate == today && currentWriter != null) return

        closeWriter()
        logDir.mkdirs()
        val file = File(logDir, "$FILE_PREFIX$today$FILE_SUFFIX")
        currentWriter = BufferedWriter(
            OutputStreamWriter(FileOutputStream(file, true), Charsets.UTF_8),
            BUFFER_SIZE
        )
        currentDate = today
    }

    private fun closeWriter() {
        try {
            currentWriter?.close()
        } catch (_: Exception) {
        }
        currentWriter = null
        currentDate = null
    }

    private fun rotate() {
        closeWriter()

        if (!logDir.exists()) return
        val files = logDir.listFiles { f -> f.name.startsWith(FILE_PREFIX) && f.name.endsWith(FILE_SUFFIX) }
            ?: return

        // Pass 1: delete files older than MAX_AGE_DAYS
        val cutoff = System.currentTimeMillis() - MAX_AGE_DAYS * 24 * 60 * 60 * 1000L
        val remaining = files.filter { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
                false
            } else {
                true
            }
        }.sortedBy { it.lastModified() }

        // Pass 2: if total size exceeds limit, delete oldest until under
        var totalSize = remaining.sumOf { it.length() }
        for (file in remaining) {
            if (totalSize <= MAX_TOTAL_SIZE_BYTES) break
            totalSize -= file.length()
            file.delete()
        }
    }
}
