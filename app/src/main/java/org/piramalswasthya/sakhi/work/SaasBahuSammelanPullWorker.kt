package org.piramalswasthya.sakhi.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo

@HiltWorker
class SaasBahuSammelanPullWorker  @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val saasBahuSammelanRepo: SaasBahuSammelanRepo,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "SaasBahuSammelanPullWorker"
        val constraint = Constraints.Builder()
            .build()
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                saasBahuSammelanRepo.SaasBahuSamelanGettDataFromServer()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("AHJAHA",e.toString())
            Result.retry()
        }
    }
}


