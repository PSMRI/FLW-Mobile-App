package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber

@HiltWorker
class FilariaMdaCampaignPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: VLFRepo
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "FilariaMdaCampaignPushWorker"

    override suspend fun doSyncWork(): Result {
        val unsyncedForms = repository.getUnsyncedFilariaMdaCampaign()
        var successfulSyncs = 0
        var failedSyncs = 0

        for (form in unsyncedForms) {
            try {
                val success = repository.saveMdaFilariaCampaignToServer(form)
                if (success) {
                    successfulSyncs++
                } else {
                    failedSyncs++
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync Filaria MDA Campaign form ${form.id}")
                failedSyncs++
            }
        }

        if (unsyncedForms.isEmpty()) {
            Timber.d("FilariaMdaCampaignPushWorker: No unsynced forms to push")
        } else {
            Timber.d("FilariaMdaCampaignPushWorker: Synced $successfulSyncs forms, failed $failedSyncs forms")
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<FilariaMdaCampaignPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
