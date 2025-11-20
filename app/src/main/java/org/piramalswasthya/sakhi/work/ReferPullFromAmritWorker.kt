package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.repositories.CbacRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import timber.log.Timber

@HiltWorker
class ReferPullFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val referalRepo: NcdReferalRepo
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "refer-Pull"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val getNumPages: Int = referalRepo.pullAndPersistReferRecord()
                if (getNumPages > 0) {
                    (1..getNumPages).forEach {
                        referalRepo.pullAndPersistReferRecord(it)
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Timber.d("refer pull failed : $e")
                Result.failure()
            }
        }
    }
}