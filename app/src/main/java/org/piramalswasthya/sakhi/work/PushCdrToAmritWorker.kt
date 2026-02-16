package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.CdrRepo
import timber.log.Timber

@HiltWorker
class PushCdrToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val cdrRepo: CdrRepo,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushCdrToAmritWorker"
    }

    override val workerName = name

    override suspend fun doSyncWork(): Result {
        val workerResult = cdrRepo.processNewCdr()
        return if (workerResult /*&& workerResult2*/) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.d("Worker Failed as usual!")
            Result.failure()
        }
    }
}
