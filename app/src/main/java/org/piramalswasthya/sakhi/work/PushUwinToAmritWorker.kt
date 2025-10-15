package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.UwinRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushUwinToAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val uwinRepo: UwinRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val NAME = "PushUwinToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        try {
            val workerResult = uwinRepo.tryUpsync()
            return if (workerResult ) {
                Timber.d("U-win Worker completed")
                Result.success()
            } else {
                Timber.d("U-win Worker Failed as usual!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for U-win push amrit worker $e")
            return Result.retry()
        }
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getAmritToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
    }
}
