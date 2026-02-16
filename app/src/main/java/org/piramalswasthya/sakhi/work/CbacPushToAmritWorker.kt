package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.CbacRepo

@HiltWorker
class CbacPushToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val cbacRepo: CbacRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushToAmritWorker"
    }

    override val workerName = "CbacPushToAmritWorker"

    override suspend fun doSyncWork(): Result {
        cbacRepo.pushAndUpdateCbacRecord()

        return Result.success()
    }
}
