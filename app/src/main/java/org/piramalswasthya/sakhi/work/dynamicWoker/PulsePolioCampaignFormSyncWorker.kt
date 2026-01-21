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
    private val preferenceDao: PreferenceDao,
    private val repository: VLFRepo
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = repository.getPulsePolioCampaignFromServer()
            when (result) {
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
        } catch (e: IllegalStateException) {
            Timber.e(e, "PulsePolioCampaignFormSyncWorker failed: No user logged in")
            Result.failure()
        } catch (e: java.net.UnknownHostException) {
            Timber.w(e, "PulsePolioCampaignFormSyncWorker: Network unavailable, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "PulsePolioCampaignFormSyncWorker failed with unexpected error")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
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
