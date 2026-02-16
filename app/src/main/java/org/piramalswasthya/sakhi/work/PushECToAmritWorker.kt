package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.EcrRepo
import timber.log.Timber

@HiltWorker
class PushECToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val ecrRepo: EcrRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushEcToAmritWorker"
    }

    override val workerName = name

    override suspend fun doSyncWork(): Result {
        Timber.d("EC Worker started!")
        val workerResult = ecrRepo.pushAndUpdateEcrRecord()

        val workerResult1 = if (workerResult) ecrRepo.pushAndUpdateEctRecord() else false
        return if (workerResult1) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.d("Worker Failed as usual!")
            Result.failure()
        }
    }
}
