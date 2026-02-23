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
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import timber.log.Timber

@HiltWorker
class AncHomeVisitPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: FormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "AncHomeVisitPushWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        val unsyncedFormsANC = repository.getUnsyncedFormsANC()
        for (form in unsyncedFormsANC) {
            if ((form.benId ?: -1) < 0) continue
            try {
                val success = repository.syncFormToServerANC(form)
                if (success) repository.markFormAsSyncedANC(form.id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync ANC form ${form.id}")
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<AncHomeVisitPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
