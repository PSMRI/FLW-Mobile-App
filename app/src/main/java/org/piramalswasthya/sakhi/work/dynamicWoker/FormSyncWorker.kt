package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import java.io.IOException

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

            val hbncRequest = HBNCVisitRequest(
                fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                toDate = HelperUtil.getCurrentDate(),
                pageNo = 0,
                ashaId = user.userId
            )

            val hbncResponse = repository.getAllHbncVisits(hbncRequest)
            if (hbncResponse.isSuccessful) {
                val visitList = hbncResponse.body()?.data.orEmpty()
                repository.saveDownloadedVisitList(visitList)
            } else {
                Timber.e("Failed to fetch HBNC visits: ${hbncResponse.code()} - ${hbncResponse.message()}")
                if (hbncResponse.code() >= 500) throw IOException("Server error: ${hbncResponse.code()}")
            }

            val hbycRequest = HBNCVisitRequest(
                fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                toDate = HelperUtil.getCurrentDate(),
                pageNo = 0,
                ashaId = user.userId
            )

            val hbycResponse = repository.getAllHbycVisits(hbycRequest)
            if (hbycResponse.isSuccessful) {
                val visitList = hbycResponse.body()?.data.orEmpty()
                repository.saveDownloadedVisitListHBYC(visitList)
            } else {
                Timber.e("Failed to fetch HBYC visits: ${hbycResponse.code()} - ${hbycResponse.message()}")
                if (hbycResponse.code() >= 500) throw IOException("Server error: ${hbycResponse.code()}")
            }

            val unsyncedForms = repository.getUnsyncedForms()
            for (form in unsyncedForms) {
                if ((form.benId ?: -1) < 0) continue
                try {
                    val success = repository.syncFormToServer(form)
                    if (success) repository.markFormAsSynced(form.id)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync HBNC form ${form.id}")
                }
            }

            val unsyncedFormsHBYC = repository.getUnsyncedFormsHBYC()
            for (form in unsyncedFormsHBYC) {
                if ((form.benId ?: -1) < 0) continue
                try {
                    val success = repository.syncFormToServerHBYC(form)
                    if (success) repository.markFormAsSyncedHBYC(form.id)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync HBYC form ${form.id}")
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
