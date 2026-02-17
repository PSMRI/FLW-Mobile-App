package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import timber.log.Timber

@HiltWorker
class PushMalariaAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val malariaRepo: MalariaRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushMalariaToAmritWorker"
    }

    override val workerName = name

    override suspend fun doSyncWork(): Result {
        val workerResult = malariaRepo.pushUnSyncedRecords()
        return if (workerResult) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.e("Worker Failed as usual!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
