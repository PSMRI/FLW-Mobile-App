package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.repositories.AshaRepo
import org.piramalswasthya.sakhi.repositories.IncentiveRepo
import java.util.Calendar

@HiltWorker
class PullAshaWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val ashaRepo: AshaRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val user = preferenceDao.getLoggedInUser()
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "User not found")).build()
            )
        ashaRepo.pullAndSaveAllAshaActivities(user).takeIf { it }
            ?: return Result.failure(
                Data.Builder().putAll(mapOf("result" to "Network Call failed act. Check in logcat"))
                    .build()
            )
        preferenceDao.lastAshaPullTimestamp =
            Calendar.getInstance().setToStartOfTheDay().timeInMillis
        return Result.success()
    }


}