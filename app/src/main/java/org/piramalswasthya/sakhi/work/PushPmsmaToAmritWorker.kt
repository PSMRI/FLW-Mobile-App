package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushPmsmaToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val ecrRepo: EcrRepo,
    private val pmsmaRepo: PmsmaRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushEcToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            Timber.d("EC Worker started!")
            val workerResult = pmsmaRepo.processNewPmsma()
            if (workerResult) {
                Timber.d("PMSMA Push Worker completed")
                Result.success()
            } else {
                Timber.d("PMSMA Worker Failed!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for pmsma push amrit worker $e")
            Result.retry()
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