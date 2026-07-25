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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.SakhiApplication
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotificationKeys
import org.piramalswasthya.sakhi.model.notificationEntityFromFcm
import org.piramalswasthya.sakhi.repositories.NotificationRepository
import org.piramalswasthya.sakhi.utils.FcmTokenUploader
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
        Timber.d("FCM message received: notification=${remoteMessage.notification}, data=$data")

        // Prefer the notification block; fall back to data payload keys.
        val title = remoteMessage.notification?.title ?: data["title"].orEmpty()
        val body = remoteMessage.notification?.body ?: data["body"].orEmpty()

        // Persist to the in-app store first so the drawer bell badge and notification list
        // (both observe Room) update in real time; then raise the system-tray notification.
        persistToInApp(data, title, body)

        val type = data[NotificationKeys.NOTIFICATION_TYPE].orEmpty()
        showNotification(title, body, type)
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

    private fun showNotification(title: String, body: String, type: String) {
        val intent = Intent(applicationContext, HomeActivity::class.java).apply {
            putExtra("FBM", true)
            putExtra("NotificationTypeId", type)
            putExtra("mTitle", title)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
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
        // Unique id so notifications stack instead of overwriting each other.
        val uniqueId = (System.currentTimeMillis() and 0xFFFFFFF).toInt()
        notificationManager.notify(uniqueId, notification)
    }
}