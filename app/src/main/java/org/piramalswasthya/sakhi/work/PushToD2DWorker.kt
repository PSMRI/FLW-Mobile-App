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
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.CdrRepo
import org.piramalswasthya.sakhi.repositories.MdsrRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushToD2DWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val benRepo: BenRepo,
    private val mdsrRepo: MdsrRepo,
    private val cdrRepo: CdrRepo,
    private val pmsmaRepo: PmsmaRepo,
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

            return if (workerResult1 && workerResult2 && workerResult3 ) {
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
            TokenInsertTmcInterceptor.setToken(preferenceDao.getPrimaryApiToken()!!)
    }
}