package org.piramalswasthya.sakhi.ui.home_activity


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.SakhiApplication
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotificationKeys
import org.piramalswasthya.sakhi.model.notificationEntityFromFcm
import org.piramalswasthya.sakhi.repositories.NotificationRepository
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.utils.FcmTokenUploader
import org.piramalswasthya.sakhi.utils.RoleConstants
import timber.log.Timber


class FBMessaging : FirebaseMessagingService() {

    /**
     * A [FirebaseMessagingService] is instantiated by the framework, so it can't use `@Inject`.
     * Dependencies are resolved through Hilt's [EntryPointAccessors] instead.
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FbmEntryPoint {
        fun notificationRepository(): NotificationRepository
        fun preferenceDao(): PreferenceDao
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Refreshed FCM token: $token")
        // Upload the refreshed token for the logged-in user (no-op if not logged in).
        FcmTokenUploader.uploadToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Timber.d("NAVTRACE 0/4 FCM message received: notification=${remoteMessage.notification}, data=$data")

        // Prefer the notification block; fall back to data payload keys.
        val title = remoteMessage.notification?.title ?: data["title"].orEmpty()
        val body = remoteMessage.notification?.body ?: data["body"].orEmpty()

        showNotification(title, body, data)
        EntryPointAccessors.fromApplication(applicationContext, FbmEntryPoint::class.java)
            .notificationRepository().onPushReceived()
    }

    /**
     * Upserts the incoming push into [NotificationRepository] (Room), scoped to the locally
     * logged-in user, so it surfaces on the toolbar bell badge and in the notification panel.
     *
     * No-op when no user is logged in, or when the payload lacks the required `notification_id`
     * (in which case it still shows in the system tray but can't be de-duplicated against the
     * poll/list sync, so it is intentionally not persisted). See [notificationEntityFromFcm].
     */
    private fun persistToInApp(data: Map<String, String>, title: String?, body: String?) {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext, FbmEntryPoint::class.java
        )
        val userId = entryPoint.preferenceDao().getLoggedInUser()?.userId?.toLong() ?: run {
            Timber.d("FCM message dropped from in-app store: no logged-in user")
            return
        }
        val entity = notificationEntityFromFcm(
            data = data,
            title = title,
            body = body,
            userId = userId,
            receivedTs = System.currentTimeMillis()
        ) ?: run {
            Timber.w("FCM message not persisted: missing/invalid ${NotificationKeys.NOTIFICATION_ID}")
            return
        }
        /*CoroutineScope(Dispatchers.IO).launch {
            entryPoint.notificationRepository().upsert(entity)
        }*/
    }

    companion object {
        var messageUpdate: MessageUpdate? = null
    }

    /**
     * Posts the system-tray notification. The Intent carries the routing context
     * ([NotificationKeys.EXTRA_NAV_ID] / [NotificationKeys.EXTRA_EVENT_TYPE] /
     * [NotificationKeys.EXTRA_NOTIFICATION_ID]) so the host activity can deeplink on tap —
     * see `HomeActivity.handleNotificationDeeplink`.
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val type = data[NotificationKeys.NOTIFICATION_TYPE].orEmpty()
        val role = EntryPointAccessors.fromApplication(
            applicationContext, FbmEntryPoint::class.java
        ).preferenceDao().getLoggedInUser()?.role
        val targetActivity = if (
            role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true) ||
            role.equals(RoleConstants.ROLE_ANM, true) ||
            role.equals(RoleConstants.ROLE_CHO, true)
        ) SupervisorActivity::class.java else HomeActivity::class.java

        // Unique id so notifications stack instead of overwriting each other. Also used as the
        // PendingIntent request code: with a shared code every notification would reuse the first
        // one's extras and every tap would route to whatever arrived first.
        val uniqueId = (System.currentTimeMillis() and 0xFFFFFFF).toInt()

        val intent = Intent(applicationContext, targetActivity).apply {
            // Reuse the task's existing activity instead of stacking a second copy on top of it.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationKeys.EXTRA_FROM_NOTIFICATION, true)
            putExtra("NotificationTypeId", type)
            putExtra("mTitle", title)
            putExtra(
                NotificationKeys.EXTRA_NAV_ID,
                data[NotificationKeys.NAV_ID] ?: data[NotificationKeys.REDIRECT]
            )
            putExtra(NotificationKeys.EXTRA_EVENT_TYPE, type)
            putExtra(
                NotificationKeys.EXTRA_NOTIFICATION_ID,
                data[NotificationKeys.NOTIFICATION_ID]?.toLongOrNull() ?: -1L
            )
        }

        Timber.d(
            "NAVTRACE 1/4 posting tray notification id=$uniqueId target=${targetActivity.simpleName} " +
                    "navId=${intent.getStringExtra(NotificationKeys.EXTRA_NAV_ID)} type=$type"
        )

        val pendingIntent = PendingIntent.getActivity(
            this,
            uniqueId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, SakhiApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_logo_icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(uniqueId, notification)
    }
}