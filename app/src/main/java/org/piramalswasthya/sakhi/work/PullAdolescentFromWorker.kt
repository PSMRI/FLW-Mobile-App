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
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.repositories.AdolescentHealthRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PullAdolescentFromWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val adolescentHealthRepo: AdolescentHealthRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullAdolescentFromAmritWorker"
        const val Progress = "Progress"

    }


    override suspend fun doWork(): Result {
        return try {
            try {
                // This ensures that you waiting for the Notification update to be done.
                setForeground(createForegroundInfo("Downloading Adolescent Data"))
            } catch (throwable: Throwable) {
                // Handle this exception gracefully
                Timber.d("FgLW", "Something bad happened", throwable)
            }
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                var numPages: Int
                val startPage =
                    if (preferenceDao.getLastSyncedTimeStamp() == Konstants.defaultTimeStamp)
                        preferenceDao.getFirstSyncLastSyncedPage()
                    else 0

                try {
                    val result1 =
                        awaitAll(
                            async { getAdolescentScreeningDetails() },
                        )

                    val endTime = System.currentTimeMillis()
                    val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                    Timber.d("Full Adolescent fetching took $timeTaken seconds $result1")

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

        return ForegroundInfo(0, notification)
    }


    private suspend fun getAdolescentScreeningDetails(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = adolescentHealthRepo.getadolescentHealthCacheFromServer()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }


}