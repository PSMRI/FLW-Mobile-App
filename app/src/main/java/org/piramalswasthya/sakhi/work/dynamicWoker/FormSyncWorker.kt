package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.hbncschemademo.ui.repo.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import java.util.concurrent.TimeUnit
@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted  workerParams: WorkerParameters,
    private val repository: FormRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("FormSyncWorker", "🚀 Starting sync (DownSync + UpSync)")

            // ✅ 1. DownSync: Get visits from API and save
            val request = HBNCVisitRequest(
                villageID = 0,
                fromDate = "2025-07-01T00:00:00.000Z",
                toDate = "2025-07-31T23:59:59.999Z",
                pageNo = 0,
                userId = 0,
                userName = "asha1",
                ashaId = 123
            )

            val response = repository.getAllHbncVisits(request)

            if (response.isSuccessful) {
                val visitList = response.body()?.data.orEmpty()
                Log.d("DownSync", "✅ Downloaded ${visitList.size} visits")
                repository.saveDownloadedVisitList(visitList)
            } else {
                Log.e("DownSync", "API error: ${response.code()} ${response.message()}")
            }

            // ✅ 2. UpSync: Push local unsynced forms
            val unsyncedForms = repository.getUnsyncedForms()
            Log.d("FormSyncWorker", "🔼 Unsynced forms: ${unsyncedForms.size}")
            unsyncedForms.forEach {
                Log.d("FormSyncWorkerss", "🧾 Form: ${it.id} | Processed: ${it.isSynced}")
            }

            for (form in unsyncedForms) {
                val success = repository.syncFormToServer(form)
                if (success) {
                    repository.markFormAsSynced(form.id)
                    Log.d("FormSyncWorker", "✅ Synced form ID=${form.id}")
                } else {
                    Log.e("FormSyncWorker", "❌ Failed to sync form ID=${form.id}")
                }
            }

            Log.d("FormSyncWorker", "✅ Sync finished.")
            Result.success()

        } catch (e: Exception) {
            Log.e("FormSyncWorker", "❌ Exception during sync: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<FormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
            Log.d("FormSyncWorker", "📬 Manual sync worker enqueued")
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<FormSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FormPeriodicSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )

            Log.d("FormSyncWorker", "⏰ Periodic sync scheduled every 6 hours")
        }
    }
}
