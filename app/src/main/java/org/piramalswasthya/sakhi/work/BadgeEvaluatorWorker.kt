package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.badges.domain.BadgeEvaluator
import timber.log.Timber

/**
 * Nightly time-based re-evaluation (LLD §2.3): streak and quarter boundaries
 * move with the calendar even when no new records are saved.
 */
@HiltWorker
class BadgeEvaluatorWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val evaluator: BadgeEvaluator
) : CoroutineWorker(appContext, params) {

    companion object {
        const val periodicName = "BadgeEvaluatorWorker-periodic"
        const val oneShotName = "BadgeEvaluatorWorker-oneShot"
    }

    override suspend fun doWork(): Result {
        return try {
            evaluator.evaluateAll()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Badges: nightly evaluation failed")
            Result.success() // next trigger recomputes from scratch anyway
        }
    }
}
