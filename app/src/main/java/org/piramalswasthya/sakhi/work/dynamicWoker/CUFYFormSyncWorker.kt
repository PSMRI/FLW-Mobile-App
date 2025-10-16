package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber
import java.io.IOException

@HiltWorker
class CUFYFormSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferenceDao: PreferenceDao,
    private val repository: CUFYFormRepository
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

            val response = repository.getAllOrsVisits(request)
            if (response.isSuccessful) {
                val visitList = response.body()?.data.orEmpty()
                repository.saveDownloadedVisitList(visitList, FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID)
//                repository.saveDownloadedVisitList(visitList, FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID)
//                repository.saveDownloadedVisitList(visitList, FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID)
            } else {
                timber.log.Timber.e("Failed to fetch HBNC visits: ${response.code()} - ${response.message()}")
                // Decide whether to continue with sync or fail based on the error
                if (response.code() >= 500) {
                    // Server error - retry later
                    throw IOException("Server error: ${response.code()}")
                }
            }

            val unsyncedForms = repository.getUnsyncedForms()
            for (form in unsyncedForms) {
                if ((form.benId ?: -1) < 0) continue

                try{
                    val success = repository.syncFormToServer(form)
                    if (success) {
                        repository.markFormAsSynced(form.id)
                    }
                }catch (e: Exception){
                    Timber.e(e, "Failed to sync form ${form.id}")
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
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CUFYFormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
