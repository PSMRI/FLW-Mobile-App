package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.CdrRepo
import org.piramalswasthya.sakhi.repositories.HbncRepo
import org.piramalswasthya.sakhi.repositories.HbycRepo
import org.piramalswasthya.sakhi.repositories.MdsrRepo
import org.piramalswasthya.sakhi.repositories.PmjayRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushToD2DWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val mdsrRepo: MdsrRepo,
    private val cdrRepo: CdrRepo,
    private val pmsmaRepo: PmsmaRepo,
    private val pmjayRepo: PmjayRepo,
    private val hbncRepo: HbncRepo,
    private val hbycRepo: HbycRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PushToD2DWorker"
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }


    override suspend fun doWork(): Result {
        init()
        try {
            val workerResult1 = cdrRepo.processNewCdr()
            val workerResult2 = mdsrRepo.processNewMdsr()
            val workerResult3 = pmsmaRepo.processNewPmsma()
            val workerResult4 = pmjayRepo.processNewPmjay()
            val workerResult5 = hbncRepo.processNewHbnc()
            val workerResult6 = hbycRepo.processNewHbyc()

            return if (workerResult1 && workerResult2 && workerResult3 && workerResult4 && workerResult5 && workerResult6) {
                Timber.d("Worker completed")
                Result.success()
            } else {
                Timber.d("Worker Failed as usual!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for Gen Ben iD worker $e")
            return Result.failure()
        }
    }

    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            TokenInsertTmcInterceptor.setToken(preferenceDao.getAmritToken()!!)
        if (TokenInsertTmcInterceptor.getJwt() == "")
            preferenceDao.getJwtToken()?.let {
                TokenInsertTmcInterceptor.setJwt(it)
            }
    }
}