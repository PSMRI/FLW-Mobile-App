package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.utils.HelperUtil

@HiltWorker
class FormSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val repository: FormRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in")

            val request = HBNCVisitRequest(
                fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                toDate = HelperUtil.getCurrentDate(),
                pageNo = 0,
                ashaId = user.userId
            )

            val response = repository.getAllHbncVisits(request)
            if (response.isSuccessful) {
                val visitList = response.body()?.data.orEmpty()
                repository.saveDownloadedVisitList(visitList)
            }

            val unsyncedForms = repository.getUnsyncedForms()
            for (form in unsyncedForms) {
                if ((form.benId ?: -1) < 0) continue
                val success = repository.syncFormToServer(form)
                if (success) {
                    repository.markFormAsSynced(form.id)
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

            val request = OneTimeWorkRequestBuilder<FormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
