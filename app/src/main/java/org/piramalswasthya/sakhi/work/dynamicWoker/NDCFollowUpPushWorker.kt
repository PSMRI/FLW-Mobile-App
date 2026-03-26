package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormNCDFollowUpSubmitRequest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.NCDFollowUpFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber

@HiltWorker
class NDCFollowUpPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: NCDFollowUpFormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "NDCFollowUpPushWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        val unsyncedForms = repository.getUnsyncedForms(FormConstants.CDTF_001)

        var anyFailure = false

        unsyncedForms.forEach { form ->
            try {
                val request = FormNCDFollowUpSubmitRequest(
                    id = form.id,
                    benId = form.benId,
                    hhId = form.hhId,
                    visitNo = form.visitNo,
                    followUpNo = form.followUpNo,
                    treatmentStartDate = form.treatmentStartDate,
                    followUpDate = form.followUpDate,
                    diagnosisCodes = form.diagnosisCodes,
                    formId = form.formId,
                    version = form.version,
                    formDataJson = form.formDataJson
                )

                val success = repository.syncFormToServer(
                    userName = user.userName,
                    formName = form.formId,
                    request = request
                )

                try {
                    if (success) {
                        repository.markFormAsSynced(form.id)
                    } else {
                        anyFailure = true
                        Timber.w("Form sync failed for id=${form.id}")
                    }
                } catch (e: Exception) {
                    anyFailure = true
                    Timber.e(e, "Failed to mark form as synced: id=${form.id}")
                }

            } catch (e: Exception) {
                anyFailure = true
                Timber.e(e, "Failed to sync form to server: id=${form.id}")
            }
        }

        return if (anyFailure) Result.retry() else Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<NDCFollowUpPushWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
