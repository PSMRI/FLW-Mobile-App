package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.BenRepo
import timber.log.Timber


@HiltWorker
class GenerateBenIdsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val benRepo: BenRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "GenBenIDWorker"
        val constraint = Constraints.Builder()
            .build()

    }


    override suspend fun doWork(): Result {
        return try {
            benRepo.getBenIdsGeneratedFromServer()
            Result.success()
        } catch (e: Exception) {
            Timber.e("Caught Exception for Gen Ben iD worker $e")
            Result.failure()
        }
    }
}