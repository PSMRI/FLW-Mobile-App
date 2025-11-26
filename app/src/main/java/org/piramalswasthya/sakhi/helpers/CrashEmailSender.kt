package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import java.io.File

object CrashEmailSender {
    private const val TEST_EMAIL = "android.developer@piramalswasthya.org"
    lateinit var pref: PreferenceDao

    fun sendCrashReport(context: Context, crashFile: File) {
        if (!crashFile.exists()) {
            return
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            crashFile
        )

       var currentLocation = pref.getLocationRecord()?.village

       var message= "Hi Team, \n Please check crash file for below user\nUsername:${pref.getRememberedUserName()}\nVillage:$currentLocation"


        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(TEST_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "Crash Report: ${crashFile.name}")
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(emailIntent, "Send crash via email..."))
    }
}