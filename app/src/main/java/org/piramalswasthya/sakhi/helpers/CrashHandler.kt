package org.piramalswasthya.sakhi.helpers

import android.content.Context
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val crashDirName = "crashes"
    lateinit var pref: PreferenceDao


    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            val crashDir = File(context.filesDir, crashDirName)
            if (!crashDir.exists()) crashDir.mkdirs()

            val time = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "crash_${pref.getRememberedUserName()}$time.txt"
            val crashFile = File(crashDir, fileName)


            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            pw.flush()

            pref =PreferenceDao(context)

// You can add more context info here: device info, app version, user id, etc.
            val header = StringBuilder()
            header.append("Crash time: ").append(time).append('\n')
            header.append("Thread: ").append(t.name).append('\n')
            header.append("Package: ").append(context.packageName).append('\n')
            header.appendLine("Username: ${pref.getRememberedUserName()}")
            header.append("\n\n")


            crashFile.writeText(header.toString() + sw.toString())
        } catch (ex: Exception) {
// If writing the crash file also fails, just swallow it and continue to default handler
        } finally {
// Let default handler (system) handle the rest (shows crash dialog / kills process)
            defaultHandler?.uncaughtException(t, e)
        }
    }
}