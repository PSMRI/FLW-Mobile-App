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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import timber.log.Timber

@HiltWorker
class ReferPullFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val referalRepo: NcdReferalRepo
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "refer-Pull"
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        try { setForeground(createForegroundInfo()) } catch (_: Throwable) {}
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val getNumPages: Int = referalRepo.pullAndPersistReferRecord()
                if (getNumPages > 0) {
                    (1..getNumPages).forEach {
                        referalRepo.pullAndPersistReferRecord(it)
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Timber.d("refer pull failed : $e")
                Result.failure(workDataOf("worker_name" to "ReferPullFromAmritWorker", "error" to (e.message ?: "Unknown error")))
            }
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_sync_channel_id)
        ).setContentTitle("Data Sync").setContentText("Downloading Referral Data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true).setOngoing(true).build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1003, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else ForegroundInfo(1003, notification)
    }
}