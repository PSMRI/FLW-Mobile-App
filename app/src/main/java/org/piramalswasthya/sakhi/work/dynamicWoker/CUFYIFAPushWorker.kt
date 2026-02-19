package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber

@HiltWorker
class CUFYIFAPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: CUFYFormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "CUFYIFAPushWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        val unsyncedForms = repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID)
        for (form in unsyncedForms) {
            if ((form.benId ?: -1) < 0) continue

            try {
                val success = repository.syncFormToServer(user.userName, FormConstants.IFA_FORM_NAME, form)
                if (success) {
                    repository.markFormAsSynced(form.id)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync form ${form.id}")
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CUFYIFAPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
