package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo

@HiltWorker
class MaaMeetingDownsyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val maaMeetingRepo: MaaMeetingRepo,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "MaaMeetingDownsyncWorker"
        val constraint = Constraints.Builder()
            .build()
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                maaMeetingRepo.downSyncAndPersist()
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}


