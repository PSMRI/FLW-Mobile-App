package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.dynamicRepo.GCReferralFollowUpRepository

@HiltWorker
class GCReferralFollowUpPullWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: GCReferralFollowUpRepository
) : BaseDynamicWorker(context, workerParams) {
    override val workerName = "GCReferralFollowUpPullWorker"

    override suspend fun doSyncWork(): Result =
        if (repository.pullFromServer()) Result.success() else Result.retry()
}
