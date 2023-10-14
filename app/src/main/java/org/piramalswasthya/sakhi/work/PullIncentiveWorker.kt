package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.IncentiveRepo

@HiltWorker
class PullIncentiveWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val incentiveRepo: IncentiveRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "User not found")).build()
            )
        incentiveRepo.pullAndSaveAllIncentiveActivities(user).takeIf { it }
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "Network Call failed act. Check in logcat"))
                    .build()
            )
        incentiveRepo.pullAndSaveAllIncentiveRecords(user).takeIf { it }
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "Network Call failed rec. Check in logcat"))
                    .build()
            )
        return Result.success()
    }


}