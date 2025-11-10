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
        Timber.tag("CUFYORSFormSyncWorker").d("👷 doWork: START - PULL ONLY (Download from server)")
        return try {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in")

            Timber.tag("CUFYORSFormSyncWorker").d("👤 doWork: User found - userId=${user.userId}")


            val request = HBNCVisitRequest(
                fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                toDate = HelperUtil.getCurrentDate(),
                pageNo = 0,
                ashaId = user.userId
            )

            Timber.tag("CUFYORSFormSyncWorker").d("📥 doWork: DOWNLOADING data from server")


            val response = repository.getAllFormVisits(FormConstants.ORS_FORM_NAME, request)

            if (response.isSuccessful) {
                val visitList = response.body()?.data.orEmpty()
                Timber.tag("CUFYORSFormSyncWorker").d("📥 doWork: Successfully downloaded ${visitList.size} visits from server")

                if (visitList.isNotEmpty()) {
                    Timber.tag("CUFYORSFormSyncWorker").d("💾 doWork: Saving downloaded visits to local database")
                    repository.saveDownloadedVisitList(visitList, FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID)
                    Timber.tag("CUFYORSFormSyncWorker").d("✅ doWork: Successfully saved ${visitList.size} visits to local database")
                } else {
                    Timber.tag("CUFYORSFormSyncWorker").d("ℹ️ doWork: No visits found to download from server")
                }
            } else {
                Timber.tag("CUFYORSFormSyncWorker").w("⚠️ doWork: Server response not successful: ${response.code()}")
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

            Timber.tag("CUFYORSFormSyncWorker").d("✅ doWork: PULL OPERATION COMPLETED SUCCESSFULLY")
            Result.success()

        } catch (e: IllegalStateException) {
            Timber.tag("CUFYORSFormSyncWorker").e(e, "❌ doWork: Failed - No user logged in")
            Result.failure()
        } catch (e: java.net.UnknownHostException) {
            Timber.tag("CUFYORSFormSyncWorker").w(e, "🌐 doWork: Network unavailable, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.tag("CUFYORSFormSyncWorker").e(e, "❌ doWork: Failed with unexpected error, attempt ${runAttemptCount}")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        fun enqueue(context: Context) {
            Timber.tag("CUFYORSFormSyncWorker").d("🚀 enqueue: Enqueuing CUFYORSFormSyncWorker (PULL ONLY)")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
            Timber.tag("CUFYORSFormSyncWorker").d("✅ enqueue: CUFYORSFormSyncWorker enqueued successfully")
        }
    }
}