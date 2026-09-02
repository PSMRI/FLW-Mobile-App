package org.piramalswasthya.sakhi.debug

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.SakhiApplication
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotificationEntity
import org.piramalswasthya.sakhi.model.NotificationKeys
import org.piramalswasthya.sakhi.repositories.NotificationRepository
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.FcmTokenUploader
import timber.log.Timber

/**
 * DEBUG-ONLY helper to verify FCM token upload, local notification display, and the in-app
 * notification panel WITHOUT the Firebase console or a live backend. Lives in `src/debug`, so it
 * is never compiled into release builds.
 *
 * Trigger via adb (sakshamUat debug applicationId shown; adjust for your flavor):
 *
 *   // Upload the current FCM token for the logged-in user
 *   adb shell am broadcast -a org.piramalswasthya.sakhi.TEST_NOTIFICATION \
 *       -p org.piramalswasthya.sakhi.saksham.uat --es mode token
 *
 *   // Post two sample system-tray notifications through the shared channel
 *   adb shell am broadcast -a org.piramalswasthya.sakhi.TEST_NOTIFICATION \
 *       -p org.piramalswasthya.sakhi.saksham.uat --es mode notify
 *
 *   // Seed the in-app panel (Room) with sample rows for the logged-in user
 *   adb shell am broadcast -a org.piramalswasthya.sakhi.TEST_NOTIFICATION \
 *       -p org.piramalswasthya.sakhi.saksham.uat --es mode seed
 *
 *   // Omit "--es mode ..." to do token + notify
 *
 * Watch results:
 *   adb logcat | grep -iE "TEST|Firebase token"
 */
class NotificationTestReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TestEntryPoint {
        fun notificationRepository(): NotificationRepository
        fun preferenceDao(): PreferenceDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra("mode")) {
            "token" -> uploadToken(context)
            "notify" -> postNotifications(context)
            "seed" -> seedPanel(context)
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

    /**
     * Posts tray notifications carrying the same content intent the real [FBMessaging] builds, so
     * the tap → deeplink path (`HomeActivity.handleNotificationDeeplink`) can be verified without a
     * live push. The second sample deliberately carries an unknown `nav_id` to exercise the
     * event-type fallback.
     */
    private fun postNotifications(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val samples = listOf(
            Triple(
                "Your monthly claim was rejected",
                "October 2026 · Reason: Incomplete documentation. Please correct and resubmit.",
                "INCENTIVE_SCREEN" to "ASHA_CLAIM_REJECTED"
            ),
            Triple(
                "Pending verifications for October 2026",
                "3 ASHAs (12 activities) are pending your verification.",
                "SOME_UNKNOWN_TARGET" to "INCENTIVE_CLAIMED"
            )
        )
        samples.forEachIndexed { i, (title, body, routing) ->
            val (navId, eventType) = routing
            val id = (System.currentTimeMillis() + i).toInt()
            val tapIntent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(NotificationKeys.EXTRA_FROM_NOTIFICATION, true)
                putExtra(NotificationKeys.EXTRA_NAV_ID, navId)
                putExtra(NotificationKeys.EXTRA_EVENT_TYPE, eventType)
                putExtra(NotificationKeys.EXTRA_NOTIFICATION_ID, 1001L + i)
            }
            val notification =
                NotificationCompat.Builder(context, SakhiApplication.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setAutoCancel(true)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            id,
                            tapIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .build()
            nm.notify(id, notification)
        }
        Timber.d("[TEST] Posted ${samples.size} tappable test notifications on the shared channel")
    }

    /** Inserts sample rows into Room for the logged-in user so the in-app panel is demonstrable. */
    private fun seedPanel(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, TestEntryPoint::class.java
        )
        val userId = entryPoint.preferenceDao().getLoggedInUser()?.userId?.toLong()
        if (userId == null) {
            Timber.d("[TEST] Not logged in — cannot seed notification panel")
            return
        }
        val now = System.currentTimeMillis()
        val hour = 60 * 60 * 1000L
        val samples = listOf(
            NotificationEntity(
                notificationId = 1001, userId = userId, eventType = "INCENTIVE_CLAIMED",
                navId = "INCENTIVE_APPROVAL", title = "Incentive Claim Received",
                body = "ASHA Saurav Mishra has claimed for june month", priority = "HIGH",
                createdTs = now - hour, senderUserId = 4259, receiverUserId = userId,
                beneficiaryId = 98765, activityId = 140, referenceId = 78954
            ),
            NotificationEntity(
                notificationId = 1, userId = userId, eventType = "ASHA_CLAIM_REJECTED",
                title = "Your monthly claim was rejected",
                body = "October 2026 · Reason: Incomplete documentation. Please correct and resubmit.",
                createdTs = now - 2 * hour
            ),
            NotificationEntity(
                notificationId = 2, userId = userId, eventType = "SUPERVISOR_VERIFICATION_REMINDER",
                title = "Pending verifications for October 2026",
                body = "3 ASHAs (12 activities) are pending your verification.",
                createdTs = now - 26 * hour
            ),
            NotificationEntity(
                notificationId = 3, userId = userId, eventType = "ASHA_STAGE_CHANGE",
                title = "Your claim is now Verified by Supervisor",
                body = "Your October 2026 claim has moved to the CHO stage.",
                createdTs = now - 3 * 24 * hour, read = true
            )
        )
        /*CoroutineScope(Dispatchers.IO).launch {
            entryPoint.notificationRepository().upsert(samples)
            Timber.d("[TEST] Seeded ${samples.size} notification rows into Room for user $userId")
        }*/
    }
}