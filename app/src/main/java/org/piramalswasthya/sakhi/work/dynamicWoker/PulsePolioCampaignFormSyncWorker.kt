package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber

@HiltWorker
class PulsePolioCampaignFormSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: VLFRepo
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "PulsePolioCampaignFormSyncWorker"

    override suspend fun doSyncWork(): Result {
        val result = repository.getPulsePolioCampaignFromServer()
        return when (result) {
            1 -> {
                Timber.d("PulsePolioCampaignFormSyncWorker: Successfully synced data from server")
                Result.success()
            }
            0 -> {
                Timber.d("PulsePolioCampaignFormSyncWorker: No data to sync")
                Result.success()
            }
            else -> {
                Timber.e("PulsePolioCampaignFormSyncWorker: Failed to sync data")
                Result.retry()
            }
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<PulsePolioCampaignFormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
