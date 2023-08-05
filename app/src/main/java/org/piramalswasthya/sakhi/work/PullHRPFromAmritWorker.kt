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
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PullHRPFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val hrpRepo: HRPRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "Pull HRP From Amrit"
        const val Progress = "Progress"

    }


    override suspend fun doWork(): Result {
        return try {
            try {
                // This ensures that you waiting for the Notification update to be done.
                setForeground(createForegroundInfo("Downloading HRP Data"))
            } catch (throwable: Throwable) {
                // Handle this exception gracefully
                Timber.d("error", "Something bad happened", throwable)
            }
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()

                try {
                    val result1 =
                        awaitAll(
                            async { getHRPAssess() },
                            async { getHRPTrack() },
                            async { getHRNonPAssess() },
                            async { getHRNonPTrack() }
                        )

                    val endTime = System.currentTimeMillis()
                    val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                    Timber.d("Full tb fetching took $timeTaken seconds $result1")

                    if (result1.all { it }) {
                        preferenceDao.setLastSyncedTimeStamp(System.currentTimeMillis())
                        return@withContext Result.success()
                    }
                    return@withContext Result.failure()
                }catch (e : SQLiteConstraintException){
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

        return ForegroundInfo(0, notification)
    }

    private suspend fun getHRPAssess() : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = hrpRepo.getHRPAssessDetailsFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }

    private suspend fun getHRPTrack() : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = hrpRepo.getHRPTrackDetailsFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }

    private suspend fun getHRNonPAssess() : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = hrpRepo.getHRNonPAssessDetailsFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }

    private suspend fun getHRNonPTrack() : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = hrpRepo.getHRNonPTrackDetailsFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }

}