package org.piramalswasthya.sakhi.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.repositories.AshaRepo
import java.util.Calendar

@HiltWorker
class PullAshaWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val ashaRepo: AshaRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        try { setForeground(createForegroundInfo()) } catch (_: Throwable) {}
        val user = preferenceDao.getLoggedInUser()
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "User not found")).build()
            )
        ashaRepo.pullAndSaveAllAshaActivities(user).takeIf { it }
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "Network Call failed act. Check in logcat"))
                    .build()
            )
        preferenceDao.lastAshaPullTimestamp =
            Calendar.getInstance().setToStartOfTheDay().timeInMillis
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_sync_channel_id)
        ).setContentTitle("Data Sync").setContentText("Downloading ASHA Data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true).setOngoing(true).build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1003, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else ForegroundInfo(1003, notification)
    }
}