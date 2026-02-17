package org.piramalswasthya.sakhi.work

import android.content.pm.ServiceInfo
import android.os.Build
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PullPmsmaFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val pmsmaRepo: PmsmaRepo,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullPmsmaFromAmritWorker"
        const val Progress = "Progress"

    }


    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo("Syncing data...")

    override suspend fun doWork(): Result {
        return try {
            try {
                // This ensures that you waiting for the Notification update to be done.
                setForeground(createForegroundInfo("Downloading PMSMA Data"))
            } catch (throwable: Throwable) {
                // Handle this exception gracefully
                Timber.d("FgLW", "Something bad happened", throwable)
            }
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()

                try {
                    val result1 =
                        awaitAll(
                            async { getPmsmaDetails() }
                        )

                    val endTime = System.currentTimeMillis()
                    val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                    Timber.d("Full PMSMA details fetching took $timeTaken seconds $result1")

                    if (result1.all { it }) {
//                        preferenceDao.setLastSyncedTimeStamp(System.currentTimeMillis())
                        return@withContext Result.success()
                    }
                    return@withContext Result.failure(workDataOf("worker_name" to "PullPmsmaFromAmritWorker", "error" to "Pull operation returned incomplete results"))
                } catch (e: SQLiteConstraintException) {
                    Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
                    return@withContext Result.failure(workDataOf("worker_name" to "PullPmsmaFromAmritWorker", "error" to "SQLite constraint: ${e.message}"))
                }

            }

        } catch (e: java.lang.Exception) {
            Timber.d("Error occurred in PullPMSMAFromAmritWorker $e ${e.stackTrace}")

            Result.failure(workDataOf("worker_name" to "PullPmsmaFromAmritWorker", "error" to (e.message ?: "Unknown error")))
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {

        val notification = NotificationCompat.Builder(
            appContext,
            appContext.getString(org.piramalswasthya.sakhi.R.string.notification_sync_channel_id)
        )
            .setContentTitle("Syncing Data")
            .setContentText(progress)
            .setSmallIcon(org.piramalswasthya.sakhi.R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1003, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1003, notification)
        }
    }


    private suspend fun getPmsmaDetails(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = pmsmaRepo.getPmsmaDetailsFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }
}