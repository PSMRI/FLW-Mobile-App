package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushDeliveryOutcomeToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushDeliveryOutcomeToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            Timber.d("DeliveryOutcome Worker started!")

            val workerResult = deliveryOutcomeRepo.processNewDeliveryOutcome()
            if (workerResult) {
                Timber.d("Delivery Outcome Push Worker completed")
                Result.success()
            } else {
                Timber.d("Delivery Outcome Worker Failed!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for Delivery Outcome push amrit worker $e")
            Result.retry()
        }
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getAmritToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
    }
}