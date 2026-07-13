package org.piramalswasthya.sakhi.ui.home_activity


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.SakhiApplication
import org.piramalswasthya.sakhi.utils.FcmTokenUploader
import timber.log.Timber


class FBMessaging : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Refreshed FCM token: $token")
        // Upload the refreshed token for the logged-in user (no-op if not logged in).
        FcmTokenUploader.uploadToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Timber.d("FCM message received: notification=${remoteMessage.notification}, data=$data")

        val type = data["NotificationTypeId"].orEmpty()
        // Prefer the notification block; fall back to data payload keys.
        val title = remoteMessage.notification?.title ?: data["title"].orEmpty()
        val body = remoteMessage.notification?.body ?: data["body"].orEmpty()

        showNotification(title, body, type)
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