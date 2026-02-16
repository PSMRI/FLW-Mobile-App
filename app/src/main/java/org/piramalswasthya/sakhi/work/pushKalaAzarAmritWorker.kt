package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.KalaAzarRepo
import timber.log.Timber

@HiltWorker
class pushKalaAzarAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val kalaAzarRepo: KalaAzarRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushAesToAmritWorker"
    }

    override val workerName = "pushKalaAzarAmritWorker"

    override suspend fun doSyncWork(): Result {
        val workerResult = kalaAzarRepo.pushUnSyncedRecords()
        return if (workerResult) {
            Timber.d("Worker completed")
            Result.success()
        } else {
            Timber.d("Worker Failed as usual!")
            Result.failure()
        }
    }
}
