package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
class MaaMeetingsPushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val maaMeetingRepo: MaaMeetingRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "MaaMeetingsPushWorker"
    }

    override suspend fun doWork(): Result {
        init()
        return try {
            maaMeetingRepo.tryUpsync()
            Timber.d("Worker completed")
            Result.success()
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught Exception $e")
            Result.retry()
        } catch (e: Exception) {
            Timber.e("Worker failed: $e")
            Result.failure()
        }
    }


    private fun init() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getAmritToken()?.let {
                TokenInsertTmcInterceptor.setToken(it)
            }
    }
}