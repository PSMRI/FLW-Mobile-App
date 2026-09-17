package org.piramalswasthya.sakhi.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.helpers.RecapNotificationState
import org.piramalswasthya.sakhi.helpers.isMonthlyRecapWindowOpen
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * The trigger the audit found missing: nothing previously scheduled the recap
 * snapshot or told her it was ready. This worker is that trigger.
 *
 * Runs once a day (cheap: at most one small DB read/write when nothing is due).
 * On each run, only inside the active window (days 1..ACTIVE_WINDOW_DAYS of the
 * month — the same rule the dashboard strip itself uses):
 *   1. Freeze/fetch this month's snapshot via [MonthlyRecapRepo.getOrCreateCurrentRecap]
 *      (idempotent — repeated calls never re-freeze an existing snapshot).
 *   2. If she has not already been notified for THIS recap month, post one local
 *      notification inviting her to watch it, then remember that she was told.
 *
 * Entirely on-device, no server call, no clinical writes. Silently no-ops when
 * the mechanic is gated off, nobody is logged in, or outside the window —
 * failures here must never surface to her as an error.
 */
@HiltWorker
class MonthlyRecapReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val recapRepo: MonthlyRecapRepo,
    private val configProvider: GamificationConfigProvider,
    private val pref: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "MONTHLY_RECAP_REMINDER"
        private const val NOTIFICATION_ID = 9001

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MonthlyRecapReminderWorker>(
                1, TimeUnit.DAYS
            ).build()
            // KEEP: an already-scheduled periodic worker survives app restarts and
            // device reboots on its own; re-enqueuing with KEEP is a harmless no-op
            // rather than resetting its 24h cycle every time the app launches.
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        createForegroundInfo("Checking your monthly recap...")

    override suspend fun doWork(): Result = try {
        val user = pref.getLoggedInUser()
        if (user == null) {
            Result.success()
        } else if (!configProvider.isMechanicEnabled(
                GamificationConfigProvider.Mechanic.MONTHLY_RECAP, user.userId
            )
        ) {
            Result.success() // gated off: not an error, just nothing to do yet
        } else if (!isMonthlyRecapWindowOpen(Calendar.getInstance())) {
            Result.success() // outside days 1..7: nothing to freeze or announce
        } else {
            val recap = recapRepo.getOrCreateCurrentRecap()
            if (recap != null &&
                !RecapNotificationState.hasNotified(appContext, user.userId, recap.recapYearMonth)
            ) {
                postReminder()
                RecapNotificationState.markNotified(appContext, user.userId, recap.recapYearMonth)
            }
            Result.success()
        }
    } catch (e: Exception) {
        Timber.e(e, "MonthlyRecapReminderWorker failed")
        Result.success() // gamification must never retry-storm or alarm the user
    }

    private fun postReminder() {
        val channelId = appContext.getString(R.string.notification_recap_channel_id)
        val openHome = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            Intent(appContext, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(appContext.getString(R.string.notification_recap_title))
            .setContentText(appContext.getString(R.string.notification_recap_body))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(appContext.getString(R.string.notification_recap_body))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openHome)
            .build()
        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            appContext, appContext.getString(R.string.notification_sync_channel_id)
        )
            .setContentTitle(progress)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1004, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1004, notification)
        }
    }
}
