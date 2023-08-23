package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushToAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val benRepo: BenRepo,
    private val ecrRepo: EcrRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushToAmritWorker"
    }
    override suspend fun doWork(): Result {
        init()
        try {
//            val workerResult = benRepo.syncUnprocessedRecords()
            val workerResult = benRepo.processNewBen()
//            val workerResult1 = ecrRepo.pushAndUpdateEcrRecord()
//            val workerResult2 = maternalHealthRepo.processNewAncVisit()
            return if (workerResult /*&& workerResult1 && workerResult2*/) {
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
            preferenceDao.getAmritToken()?.let{
                TokenInsertTmcInterceptor.setToken(it)
            }
    }
}