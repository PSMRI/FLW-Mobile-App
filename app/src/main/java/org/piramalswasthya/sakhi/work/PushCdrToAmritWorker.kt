package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.CdrRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushCdrToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val cdrRepo: CdrRepo,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushCdrToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        try {
            val workerResult = cdrRepo.processNewCdr()
            return if (workerResult /*&& workerResult2*/) {
                Timber.d("Worker completed")
                Result.success()
            } else {
                Timber.d("Worker Failed as usual!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for push amrit worker $e")
            return Result.retry()
        }
    }


    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getAmritToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
        if (TokenInsertTmcInterceptor.getJwt() == "")
            preferenceDao.getJwtToken()?.let {
                TokenInsertTmcInterceptor.setJwt(it)
            }
    }
}