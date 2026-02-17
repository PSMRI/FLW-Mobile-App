package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import timber.log.Timber

@HiltWorker
class PushPmsmaToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val ecrRepo: EcrRepo,
    private val pmsmaRepo: PmsmaRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushEcToAmritWorker"
    }

    override val workerName = name

    override suspend fun doSyncWork(): Result {
        Timber.d("EC Worker started!")
        val workerResult = pmsmaRepo.processNewPmsma()
        return if (workerResult) {
            Timber.d("PMSMA Push Worker completed")
            Result.success()
        } else {
            Timber.d("PMSMA Worker Failed!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
