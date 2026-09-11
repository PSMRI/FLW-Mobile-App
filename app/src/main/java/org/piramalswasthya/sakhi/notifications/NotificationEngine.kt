package org.piramalswasthya.sakhi.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotifHistoryCache
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evening notification assembly (Notification LLD §5): counts today's forms,
 * classifies the day into a band, selects and fills a template, dispatches,
 * and records history. Zero network calls — reads today's data only.
 */
@Singleton
class NotificationEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: EveningNotifDao,
    private val interceptor: FormSaveInterceptor,
    private val selector: TemplateSelector,
    private val filler: TemplateFiller,
    private val pref: PreferenceDao
) {

    suspend fun deliverEveningNotification() = withContext(Dispatchers.IO) {
        try {
            val today = ActivityClassifier.dateKey(System.currentTimeMillis())
            // dedupe: notification already delivered today → skip (LLD §6)
            if (dao.shownCountForDay(today) > 0) return@withContext

            interceptor.captureToday()
            val count = dao.countForDay(today)
            val bucket = ActivityClassifier.bucketFor(count).name
            val language = try {
                pref.getCurrentLanguage().symbol
            } catch (e: Exception) {
                "en"
            }
            val template = selector.select(bucket, language)
            val name = try {
                pref.getLoggedInUser()?.name
            } catch (e: Exception) {
                null
            }
            val body = filler.fill(template.bodyTemplate, name)

            // Only record it if it actually went out. The history row is what makes delivery
            // once-a-day, so writing it after a post that was refused (no POST_NOTIFICATIONS,
            // or a throwing NotificationManager) burns the day: nothing appeared, and nothing
            // will try again until tomorrow.
            if (!notify(body)) {
                Timber.w("EveningNotif: not delivered, leaving the day open to retry")
                return@withContext
            }
            dao.logShown(NotifHistoryCache(templateId = template.templateId, shownDate = today, bucket = bucket))
            Timber.d("EveningNotif: delivered bucket=$bucket template=${template.templateId}")
        } catch (e: Exception) {
            Timber.e(e, "EveningNotif: delivery failed")
        }
    }

    /** @return true when the notification was handed to the system. */
    private fun notify(body: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("EveningNotif: POST_NOTIFICATIONS not granted — skipping")
            return false
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.evening_notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        // tap opens the Journey screen (LLD §1)
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_JOURNEY, true)
        }
        val pending = PendingIntent.getActivity(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        return try {
            manager.notify(NOTIFICATION_ID, notification)
            true
        } catch (e: Exception) {
            // A refused post (quota, a disabled channel, an OEM restriction) throws here
            // rather than returning, and swallowing it silently would mark the day delivered.
            Timber.w(e, "EveningNotif: notification manager refused the post")
            false
        }
    }

    companion object {
        const val CHANNEL_ID = "evening_notification"
        const val EXTRA_OPEN_JOURNEY = "open_journey"
        private const val NOTIFICATION_ID = 20001
        private const val REQUEST_CODE = 20001
    }
}
