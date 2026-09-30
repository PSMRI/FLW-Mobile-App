package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository

@HiltWorker
class TPTFollowUpPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: TPTStartRepository
) : BaseDynamicWorker(context, workerParams) {
    override val workerName = "TPTFollowUpPushWorker"

    override suspend fun doSyncWork(): Result =
        if (repository.pushPending()) Result.success() else Result.retry()
}
