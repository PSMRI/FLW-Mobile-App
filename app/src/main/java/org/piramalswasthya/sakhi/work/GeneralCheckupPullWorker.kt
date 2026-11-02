package org.piramalswasthya.sakhi.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.repositories.GeneralCheckupRepo
import timber.log.Timber

/** Pulls the ASHA's General Check-ups (FLW-1229). */
@HiltWorker
class GeneralCheckupPullWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val generalCheckupRepo: GeneralCheckupRepo,
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            appContext,
            appContext.getString(R.string.notification_sync_channel_id)
        ).setContentTitle("Data Sync").setContentText("Downloading General Check-up data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true).setOngoing(true).build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1003, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else ForegroundInfo(1003, notification)
    }

    override suspend fun doWork(): Result {
        return try {
            if (generalCheckupRepo.pullFromServer()) Result.success()
            else Result.failure(
                workDataOf("worker_name" to "GeneralCheckupPullWorker", "error" to "Check-up download failed")
            )
        } catch (e: Exception) {
            Timber.e(e, "General Check-up pull failed")
            Result.failure(workDataOf("worker_name" to "GeneralCheckupPullWorker", "error" to (e.message ?: "Unknown error")))
        }
    }
}
