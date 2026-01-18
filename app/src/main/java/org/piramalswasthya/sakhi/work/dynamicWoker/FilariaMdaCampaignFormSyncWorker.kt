package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber

@HiltWorker
class FilariaMdaCampaignFormSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: VLFRepo
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = repository.getFalariaMdaCampaignFromServer()
            when (result) {
                1 -> {
                    Timber.d("FilariaMdaCampaignFormSyncWorker: Successfully synced data from server")
                    Result.success()
                }
                0 -> {
                    Timber.d("FilariaMdaCampaignFormSyncWorker: No data to sync")
                    Result.success()
                }
                else -> {
                    Timber.e("FilariaMdaCampaignFormSyncWorker: Failed to sync data")
                    Result.retry()
                }
            }
        } catch (e: IllegalStateException) {
            Timber.e(e, "FilariaMdaCampaignFormSyncWorker failed: No user logged in")
            Result.failure()
        } catch (e: java.net.UnknownHostException) {
            Timber.w(e, "FilariaMdaCampaignFormSyncWorker: Network unavailable, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "FilariaMdaCampaignFormSyncWorker failed with unexpected error")
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

            val request = OneTimeWorkRequestBuilder<FilariaMdaCampaignFormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
