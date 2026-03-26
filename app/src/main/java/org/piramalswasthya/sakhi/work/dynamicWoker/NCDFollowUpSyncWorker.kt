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
import java.io.IOException

@HiltWorker
class NCDFollowUpSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: NCDFollowUpFormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "NCDFollowUpSyncWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        try {
            val serverForms = repository.fetchFormsFromServer(
                formId = FormConstants.CDTF_001,
                userName = user.userName
            )
            repository.saveDownloadedForms(serverForms)
        } catch (e: IOException) {
            return Result.retry()
        } catch (e: Exception) {
            // Swallow non-IO exceptions from fetch — continue to push
        }

        val unsyncedForms = repository.getUnsyncedForms(FormConstants.CDTF_001)

        var successCount = 0
        var failCount = 0

        unsyncedForms.forEachIndexed { index, form ->
            if (form.benId < 0) {
                failCount++
                return@forEachIndexed
            }

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

                if (success) {
                    repository.markFormAsSynced(form.id)
                    successCount++
                } else {
                    failCount++
                }
            } catch (e: Exception) {
                failCount++
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<NCDFollowUpSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
