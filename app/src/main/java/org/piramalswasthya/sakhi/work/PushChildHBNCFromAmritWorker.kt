package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.HbncRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PushChildHBNCFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val hbncRepo: HbncRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {

    companion object {
        const val name = "PushChildHBNCFromAmritWorker"
        const val Progress = "Progress"

    }

    override val workerName = "PushChildHBNCFromAmritWorker"

    override suspend fun doSyncWork(): Result {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()

            try {
                val result1 = pushChildHBNCDetails()

                val endTime = System.currentTimeMillis()
                val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                Timber.d("Full HBNC fetching took $timeTaken seconds $result1")

                if (result1) {
                    preferenceDao.setLastSyncedTimeStamp(System.currentTimeMillis())
                    return@withContext Result.success()
                }
                return@withContext Result.failure()
            } catch (e: SQLiteConstraintException) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
                return@withContext Result.failure()
            }

        }
    }


    private suspend fun pushChildHBNCDetails(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = hbncRepo.pushHBNCDetails()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }
}
