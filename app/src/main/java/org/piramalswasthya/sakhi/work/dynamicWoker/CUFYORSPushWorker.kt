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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber


@HiltWorker
class CUFYORSPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val repository: CUFYFormRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag("CUFYORSPushWorker").d("👷 doWork: START - PUSH ONLY (Upload to server)")
        return try {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in")

            Timber.tag("CUFYORSPushWorker").d("📋 doWork: Getting unsynced forms")
            val unsyncedForms = repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID)
            Timber.tag("CUFYORSPushWorker").d("📋 doWork: Found ${unsyncedForms.size} unsynced forms")

            var successfulSyncs = 0
            var failedSyncs = 0

            for ((index, form) in unsyncedForms.withIndex()) {
                Timber.tag("CUFYORSPushWorker").d("🔄 doWork: Processing form $index/${unsyncedForms.size} - id=${form.id}, benId=${form.benId}")

                if ((form.benId ?: -1) < 0) {
                    Timber.tag("CUFYORSPushWorker").w("⚠️ doWork: Skipping form with invalid benId")
                    failedSyncs++
                    continue
                }

                try {
                    Timber.tag("CUFYORSPushWorker").d("🌐 doWork: Syncing form ${form.id} to server")
                    val success = repository.syncFormToServer(FormConstants.ORS_FORM_NAME, form)
                    Timber.tag("CUFYORSPushWorker").d("🌐 doWork: Sync result for form ${form.id} - success=$success")

                    if (success) {
                        Timber.tag("CUFYORSPushWorker").d("✅ doWork: Marking form ${form.id} as synced")
                        repository.markFormAsSynced(form.id)
                        successfulSyncs++
                    } else {
                        Timber.tag("CUFYORSPushWorker").w("⚠️ doWork: Sync failed for form ${form.id}")
                        failedSyncs++
                    }
                } catch (e: Exception) {
                    Timber.tag("CUFYORSPushWorker").e(e, "❌ doWork: Failed to sync form ${form.id}")
                    failedSyncs++
                }
            }

            Timber.tag("CUFYORSPushWorker").d("✅ doWork: PUSH OPERATION COMPLETED - Successful: $successfulSyncs, Failed: $failedSyncs")
            Result.success()

        } catch (e: IllegalStateException) {
            Timber.tag("CUFYORSPushWorker").e(e, "❌ doWork: Failed - No user logged in")
            Result.failure()
        } catch (e: java.net.UnknownHostException) {
            Timber.tag("CUFYORSPushWorker").w(e, "🌐 doWork: Network unavailable, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.tag("CUFYORSPushWorker").e(e, "❌ doWork: Failed with unexpected error, attempt ${runAttemptCount}")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        fun enqueue(context: Context) {
            Timber.tag("CUFYORSPushWorker").d("🚀 enqueue: Enqueuing CUFYORSPushWorker (PUSH ONLY)")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CUFYORSPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
            Timber.tag("CUFYORSPushWorker").d("✅ enqueue: CUFYORSPushWorker enqueued successfully")
        }
    }
}