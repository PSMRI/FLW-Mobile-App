package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo
import timber.log.Timber

@HiltWorker
class PushSaasBahuSamelanAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val saasBahuSammelanRepo: SaasBahuSammelanRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushPNCToAmritWorker"
    }

    override val workerName = "PushSaasBahuSamelanAmritWorker"

    override suspend fun doSyncWork(): Result {
        val workerResult = saasBahuSammelanRepo.pushUnSyncedRecordsSaasBahuSammelan()
        return if (workerResult) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.d("Worker Failed as usual!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
