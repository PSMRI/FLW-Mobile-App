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
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants

@HiltWorker
class CUFYORSPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: CUFYFormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "CUFYORSPushWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        val unsyncedForms = repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID)

        var successfulSyncs = 0
        var failedSyncs = 0

        for ((index, form) in unsyncedForms.withIndex()) {
            if ((form.benId ?: -1) < 0) {
                failedSyncs++
                continue
            }

            try {
                val success = repository.syncFormToServer(user.userName, FormConstants.ORS_FORM_NAME, form)

                if (success) {
                    repository.markFormAsSynced(form.id)
                    successfulSyncs++
                } else {
                    failedSyncs++
                }
            } catch (e: Exception) {
                failedSyncs++
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CUFYORSPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
