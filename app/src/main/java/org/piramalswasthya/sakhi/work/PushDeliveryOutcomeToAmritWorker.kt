package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import timber.log.Timber

@HiltWorker
class PushDeliveryOutcomeToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "PushDeliveryOutcomeToAmritWorker"
    }

    override val workerName = name

    override suspend fun doSyncWork(): Result {
        Timber.d("DeliveryOutcome Worker started!")

        val workerResult = deliveryOutcomeRepo.processNewDeliveryOutcome()
        return if (workerResult) {
            Timber.d("Delivery Outcome Push Worker completed")
            Result.success()
        } else {
            Timber.e("Delivery Outcome Worker Failed!")
            Result.failure(workDataOf(KEY_WORKER_NAME to workerName, KEY_ERROR to "Sync operation returned false"))
        }
    }
}
