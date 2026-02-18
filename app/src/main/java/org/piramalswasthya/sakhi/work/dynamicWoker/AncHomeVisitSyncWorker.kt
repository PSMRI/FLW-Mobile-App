package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import java.io.IOException

@HiltWorker
class AncHomeVisitSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    override val preferenceDao: PreferenceDao,
    private val repository: FormRepository
) : BaseDynamicWorker(context, workerParams) {

    override val workerName = "AncHomeVisitSyncWorker"

    override suspend fun doSyncWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        val ancRequest = HBNCVisitRequest(
            fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
            toDate = HelperUtil.getCurrentDate(),
            pageNo = 0,
            ashaId = user.userId,
            userName = user.userName
        )

        val ancResponse = repository.getAllAncVisits(ancRequest)
        if (ancResponse.isSuccessful) {
            val visitList = ancResponse.body()?.data.orEmpty()
            Log.d("anc_visit", "getAllAncVisits: called api")
            repository.saveDownloadedVisitListANC(visitList)
        } else {
            Timber.e("Failed to fetch ANC visits: ${ancResponse.code()} - ${ancResponse.message()}")
            if (ancResponse.code() >= 500) throw IOException("Server error: ${ancResponse.code()}")
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<AncHomeVisitSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
