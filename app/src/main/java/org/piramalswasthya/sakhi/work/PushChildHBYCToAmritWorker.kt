package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.HbycRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PushChildHBYCToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val hbycRepo: HbycRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {

    companion object {
        const val name = "PushChildHBYCFromAmritWorker"
        const val Progress = "Progress"

    }

    override val workerName = "PushChildHBYCToAmritWorker"

    override suspend fun doSyncWork(): Result {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()

            try {
                val result1 = pushChildHBYCDetails()

                val endTime = System.currentTimeMillis()
                val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                Timber.d("Full HByC fetching took $timeTaken seconds $result1")

                if (result1) {
                    preferenceDao.setLastSyncedTimeStamp(System.currentTimeMillis())
                    return@withContext Result.success()
                }
                return@withContext Result.failure()
            } catch (e: SQLiteConstraintException) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
                return@withContext Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "SQLite constraint: ${e.message}"))
            }

        }
    }


    private suspend fun pushChildHBYCDetails(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val res = hbycRepo.pushHBYCDetails()
                return@withContext res == 1
            } catch (e: Exception) {
                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
            }
            true
        }
    }
}
