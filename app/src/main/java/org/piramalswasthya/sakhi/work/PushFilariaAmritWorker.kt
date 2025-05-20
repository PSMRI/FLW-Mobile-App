package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.FilariaRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class PushFilariaAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val filariaRepo: FilariaRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "PushFilariaToAmritWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            val workerResult = filariaRepo.pushUnSyncedRecords()
            if (workerResult) {
                Timber.d("Worker completed")
                Result.success()
            } else {
                Timber.d("Worker Failed as usual!")
                Result.failure()
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception for push amrit worker $e")
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