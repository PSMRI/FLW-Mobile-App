package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.ImmunizationRepo
import timber.log.Timber

@HiltWorker
class PushChildImmunizationToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val immunizationRepo: ImmunizationRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushChildImmunizationToAmritWorker"
    }

    override val workerName = "PushChildImmunizationToAmritWorker"

    override suspend fun doSyncWork(): Result {
        val workerResult = immunizationRepo.pushUnSyncedChildImmunizationRecords()
        return if (workerResult) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.d("Worker Failed for push Child Immunization!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
