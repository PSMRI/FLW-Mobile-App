package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber

@HiltWorker
class PushPWRToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val maternalHealthRepo: MaternalHealthRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushPWRToAmritWorker"
    }

    override val workerName = name

    override suspend fun doSyncWork(): Result {
        val workerResult = maternalHealthRepo.processNewPwr()
        val workerResult1 = if (workerResult) maternalHealthRepo.processNewAncVisit() else false
        return if (workerResult1) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.e("Worker Failed as usual!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
