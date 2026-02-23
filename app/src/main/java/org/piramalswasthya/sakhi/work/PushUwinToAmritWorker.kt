package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.UwinRepo
import timber.log.Timber

@HiltWorker
class PushUwinToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val uwinRepo: UwinRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, workerParams) {

    companion object {
        const val NAME = "PushUwinToAmritWorker"
    }

    override val workerName = "PushUwinToAmritWorker"

    override suspend fun doSyncWork(): Result {
        val workerResult = uwinRepo.tryUpsync()
        return if (workerResult) {
            Timber.d("U-win Worker completed")
            Result.success()
        } else {
            Timber.e("U-win Worker Failed as usual!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
