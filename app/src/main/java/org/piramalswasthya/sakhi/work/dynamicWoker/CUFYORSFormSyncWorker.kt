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
class CUFYORSFormSyncWorker @AssistedInject constructor(
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
                ashaId = user.userId,
                userName = user.userName
            )



            val response = repository.getAllFormVisits(FormConstants.ORS_FORM_NAME, request)

            if (response.isSuccessful) {
                val visitList = response.body()?.data.orEmpty()

                if (visitList.isNotEmpty()) {
                    repository.saveDownloadedVisitList(visitList, FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID)
                } else {
                    Timber.tag("CUFYORSFormSyncWorker").d("doWork: No visits found to download from server")
                }
            } else {
                if (response.code() >= 500) {
                    throw IOException("Server error: ${response.code()}")
                }
            }

            val unsyncedForms = repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID)
            for (form in unsyncedForms) {
                if ((form.benId ?: -1) < 0) continue

                try{
                    val success = repository.syncFormToServer(user.userName,FormConstants.ORS_FORM_NAME,form)
                    if (success) {
                        repository.markFormAsSynced(form.id)
                    }
                }catch (e: Exception){
                    Timber.e(e, "Failed to sync form ${form.id}")
                }

            }

            Result.success()

        } catch (e: IllegalStateException) {
            Result.failure()
        } catch (e: java.net.UnknownHostException) {
            Result.retry()
        } catch (e: Exception) {
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

            val request = OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}