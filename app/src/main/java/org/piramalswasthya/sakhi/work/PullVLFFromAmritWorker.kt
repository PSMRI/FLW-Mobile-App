package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PullVLFFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val vlfRepo: VLFRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "Pull VLF From Amrit"
        const val Progress = "Progress"

    }


    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo("Syncing data...")

    override suspend fun doWork(): Result {
        return try {
            try {
                // This ensures that you waiting for the Notification update to be done.
                setForeground(createForegroundInfo("Downloading VLF Data"))
            } catch (throwable: Throwable) {
                // Handle this exception gracefully
                Timber.d("error", "Something bad happened", throwable)
            }
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()

                try {
                    val result1 =
                        awaitAll(
                            async { getVHND() },
                            async { getVHNC() },
                            async { getPHC() },
                            async { getAHD() },
                            async { getDeworming() },
                        )

                    val endTime = System.currentTimeMillis()
                    val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                    Timber.d("Full tb fetching took $timeTaken seconds $result1")

                    if (result1.all { it }) {
                        return@withContext Result.success()
                    }
                    return@withContext Result.failure()
                } catch (e: SQLiteConstraintException) {
                    Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
                    return@withContext Result.failure()
                }

            }

        } catch (e: java.lang.Exception) {
            Timber.d("Error occurred in PullTBFromAmritWorker $e ${e.stackTrace}")

            Result.failure()
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

        return ForegroundInfo(1003, notification)
    }

    private suspend fun getVHND(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = vlfRepo.getVHNDFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }
    private suspend fun getPHC(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = vlfRepo.getPHCFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }
  private suspend fun getVHNC(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = vlfRepo.getVHNCFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }

private suspend fun getAHD(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = vlfRepo.getAHDFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }



private suspend fun getDeworming(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = vlfRepo.getDewormingFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }



}