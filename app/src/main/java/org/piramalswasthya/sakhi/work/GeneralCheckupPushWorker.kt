package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.GeneralCheckupRepo

/** Pushes General Check-up records (FLW-1229). The GC referral goes via NCDReferPushtoAmritWorker. */
@HiltWorker
class GeneralCheckupPushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val generalCheckupRepo: GeneralCheckupRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {

    override val workerName = "GeneralCheckupPushWorker"

    override suspend fun doSyncWork(): Result {
        return if (generalCheckupRepo.pushUnsynced()) Result.success()
        else Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "General Check-up push failed"))
    }
}
