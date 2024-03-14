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
import timber.log.Timber

@HiltWorker
class CbacPullFromAmritWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val cbacRepo: CbacRepo
) : CoroutineWorker(appContext, params) {
    companion object {
        const val name = "Cbac-Pull"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val getNumPages: Int = cbacRepo.pullAndPersistCbacRecord()
                if (getNumPages > 0) {
                    (1..getNumPages).forEach {
                        cbacRepo.pullAndPersistCbacRecord(it)
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Timber.d("cbac pull failed : $e")
                Result.failure()
            }
        }
    }
}