package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormNCDFollowUpSubmitRequest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.NCDFollowUpFormRepository
import org.piramalswasthya.sakhi.utils.Log
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber
import java.net.UnknownHostException

@HiltWorker
class NDCFollowUpPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val repository: NCDFollowUpFormRepository
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {

        return try {
            val user = preferenceDao.getLoggedInUser()
                ?: return Result.failure()

            val unsyncedForms = repository.getUnsyncedForms(FormConstants.CDTF_001)


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

                    if (success) {
                        repository.markFormAsSynced(form.id)
                    } else {
                    }

                } catch (e: Exception) {
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
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
