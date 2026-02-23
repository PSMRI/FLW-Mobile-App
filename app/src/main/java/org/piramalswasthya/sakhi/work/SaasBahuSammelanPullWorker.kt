package org.piramalswasthya.sakhi.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo
import timber.log.Timber

@HiltWorker
class SaasBahuSammelanPullWorker  @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val saasBahuSammelanRepo: SaasBahuSammelanRepo,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "SaasBahuSammelanPullWorker"
        val constraint = Constraints.Builder()
            .build()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        try { setForeground(createForegroundInfo()) } catch (_: Throwable) {}
        return try {
            withContext(Dispatchers.IO) {
                saasBahuSammelanRepo.SaasBahuSamelanGettDataFromServer()
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SaasBahuSammelanPullWorker failed")
            Result.retry()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_sync_channel_id)
        ).setContentTitle("Data Sync").setContentText("Downloading Saas Bahu Sammelan Data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true).setOngoing(true).build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1003, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else ForegroundInfo(1003, notification)
    }
}
