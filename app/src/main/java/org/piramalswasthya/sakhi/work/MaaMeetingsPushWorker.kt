package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo
import timber.log.Timber

@HiltWorker
class MaaMeetingsPushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val maaMeetingRepo: MaaMeetingRepo,
    override val preferenceDao: PreferenceDao,
) : BasePushWorker(appContext, params) {
    companion object {
        const val name = "MaaMeetingsPushWorker"
    }

    override val workerName = "MaaMeetingsPushWorker"

    override suspend fun doSyncWork(): Result {
        maaMeetingRepo.tryUpsync()
        Timber.d("Worker completed")
        return Result.success()
    }
}
