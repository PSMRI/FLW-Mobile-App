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
class PulsePolioCampaignPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: VLFRepo
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "PulsePolioCampaignPushWorker"

    override suspend fun doSyncWork(): Result {
        val unsyncedForms = repository.getUnsyncedPulsePolioCampaign()
        var successfulSyncs = 0
        var failedSyncs = 0

        for (form in unsyncedForms) {
            try {
                val success = repository.savePulsePolioCampaignToServer(form)
                if (success) {
                    successfulSyncs++
                } else {
                    failedSyncs++
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync Pulse Polio Campaign form ${form.id}")
                failedSyncs++
            }
        }

        if (unsyncedForms.isEmpty()) {
            Timber.d("PulsePolioCampaignPushWorker: No unsynced forms to push")
        } else {
            Timber.d("PulsePolioCampaignPushWorker: Synced $successfulSyncs forms, failed $failedSyncs forms")
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<PulsePolioCampaignPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
