package org.piramalswasthya.sakhi.debug

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.SakhiApplication
import org.piramalswasthya.sakhi.utils.FcmTokenUploader
import timber.log.Timber

/**
 * DEBUG-ONLY helper to verify FCM token upload and local notification display
 * WITHOUT the Firebase console. Lives in `src/debug`, so it is never compiled into
 * release builds.
 *
 * Trigger via adb (sakshamUat debug applicationId shown; adjust for your flavor):
 *
 *   // Upload the current FCM token for the logged-in user
 *   adb shell am broadcast -a org.piramalswasthya.sakhi.TEST_NOTIFICATION \
 *       -p org.piramalswasthya.sakhi.saksham.uat --es mode token
 *
 *   // Post two sample notifications through the shared channel
 *   adb shell am broadcast -a org.piramalswasthya.sakhi.TEST_NOTIFICATION \
 *       -p org.piramalswasthya.sakhi.saksham.uat --es mode notify
 *
 *   // Omit "--es mode ..." to do both
 *
 * Watch results:
 *   adb logcat | grep -iE "TEST|Firebase token"
 */
class NotificationTestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra("mode")) {
            "token" -> uploadToken(context)
            "notify" -> postNotifications(context)
            else -> {
                uploadToken(context)
                postNotifications(context)
            }
        }
    }

    private fun uploadToken(context: Context) {
        Timber.d("[TEST] Triggering FCM token upload (no-op if not logged in)")
        FcmTokenUploader.uploadToken(context)
    }

    private fun postNotifications(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val samples = listOf(
            "Your monthly claim was rejected" to
                "October 2026 · Reason: Incomplete documentation. Please correct and resubmit.",
            "Pending verifications for October 2026" to
                "3 ASHAs (12 activities) are pending your verification."
        )
        samples.forEachIndexed { i, (title, body) ->
            val notification =
                NotificationCompat.Builder(context, SakhiApplication.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setAutoCancel(true)
                    .build()
            nm.notify((System.currentTimeMillis() + i).toInt(), notification)
        }
        Timber.d("[TEST] Posted ${samples.size} test notifications on the shared channel")
    }
}