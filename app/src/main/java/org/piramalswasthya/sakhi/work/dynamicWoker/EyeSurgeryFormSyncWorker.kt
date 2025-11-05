package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.EyeSurgeryFormRepository
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber
import java.io.IOException

@HiltWorker
class EyeSurgeryFormSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val repository: EyeSurgeryFormRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val formName = inputData.getString("formName")

            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in")

            val formIdsToSync = if (formName.isNullOrBlank()) {
                listOf(
                    FormConstants.EYE_SURGERY_FORM_NAME,
//                    FormConstants.IFA_DISTRIBUTION_FORM_ID,
                )
            } else {
                listOf(formName)
            }

            for (formName in formIdsToSync) {
                val unsyncedForms = repository.getUnsyncedForms(formName)

                for (form in unsyncedForms) {
                    if ((form.benId ?: -1) < 0) continue
                    try {
                        val success = repository.syncFormToServer(user.userName, formName, form)
                        if (success) repository.markFormAsSynced(form.id)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync form ${form.id}")
                    }
                }

                val request = HBNCVisitRequest(
                    fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                    toDate = HelperUtil.getCurrentDate(),
                    pageNo = 0,
                    ashaId = user.userId
                )

                val response = repository.getAllFormVisits(formName, request)
                if (response.isSuccessful) {
                    val visitList = response.body()?.data.orEmpty()
                    repository.saveDownloadedVisitList(visitList, formName)
                } else if (response.code() >= 500) {
                    throw IOException("Server error: ${response.code()}")
                }
            }

            Result.success()

        } catch (e: IllegalStateException) {
            Timber.e(e, "FormSyncWorker failed: No user logged in")
            Result.failure()
        } catch (e: java.net.UnknownHostException) {
            Timber.w(e, "FormSyncWorker: Network unavailable, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "FormSyncWorker failed with unexpected error")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun enqueue(context: Context, formName: String? = null) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = if (!formName.isNullOrBlank()) {
                workDataOf("formName" to formName)
            } else {
                Data.EMPTY
            }

            val request = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
