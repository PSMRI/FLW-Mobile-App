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
    override val preferenceDao: PreferenceDao,
    private val repository: EyeSurgeryFormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "EyeSurgeryFormSyncWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        val request = HBNCVisitRequest(
            fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
            toDate = HelperUtil.getCurrentDate(),
            pageNo = 0,
            ashaId = user.userId,
            userName = user.userName
        )

        val unsyncedForms = repository.getUnsyncedForms(FormConstants.EYE_SURGERY_FORM_ID)
        for (form in unsyncedForms) {
            if ((form.benId ?: -1) < 0) continue

            try {
                val success = repository.syncFormToServer(user.userName, FormConstants.EYE_SURGERY_FORM_NAME, form)
                if (success) {
                    repository.markFormAsSynced(form.id)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync form ${form.id}")
            }
        }

        val response = repository.getAllFormVisits(FormConstants.EYE_SURGERY_FORM_NAME, request)
        if (response.isSuccessful) {
            val visitList = response.body()?.data.orEmpty()
            repository.saveDownloadedVisitList(visitList, FormConstants.EYE_SURGERY_FORM_ID)
        } else {
            if (response.code() >= 500) {
                throw IOException("Server error: ${response.code()}")
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
