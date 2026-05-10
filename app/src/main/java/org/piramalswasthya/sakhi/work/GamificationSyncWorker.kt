package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.repositories.GamificationRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Syncs unsynced gamification state (profiles, badges, transactions)
 * to the AMRIT backend when connectivity is restored.
 *
 * Runs every 6 hours, network-constrained, with exponential backoff.
 * Matches the app's existing offline-first WorkManager sync architecture.
 *
 * TODO: Replace stub sync calls with actual API calls once the
 *       gamification endpoints are available in FLW-API service.
 *       Endpoint contract is documented in docs/gamification/API_CONTRACT.md
 */
@HiltWorker
class GamificationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: GamificationRepo
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "GamificationSyncWorker"

        fun buildRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<GamificationSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
                .build()

        /**
         * Safe to call multiple times (e.g. on every login).
         * KEEP_EXISTING prevents duplicate workers.
         */
        fun schedule(workManager: WorkManager) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                buildRequest()
            )
        }
    }

    override suspend fun doWork(): Result {
        Timber.d("GamificationSyncWorker: starting sync")
        return try {
            syncProfiles()
            syncBadges()
            syncTransactions()
            Timber.d("GamificationSyncWorker: sync complete")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "GamificationSyncWorker: sync failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncProfiles() {
        val unsynced = repo.getUnsyncedProfiles()
        if (unsynced.isEmpty()) return
        Timber.d("GamificationSyncWorker: syncing ${unsynced.size} profiles")
        // TODO: apiService.syncGamificationProfiles(unsynced)
        unsynced.forEach { repo.markProfileSynced(it.userId) }
    }

    private suspend fun syncBadges() {
        val unsynced = repo.getUnsyncedBadges()
        if (unsynced.isEmpty()) return
        Timber.d("GamificationSyncWorker: syncing ${unsynced.size} badges")
        // TODO: apiService.syncGamificationBadges(unsynced)
        repo.markBadgesSynced(unsynced.map { it.id })
    }

    private suspend fun syncTransactions() {
        val unsynced = repo.getUnsyncedTransactions()
        if (unsynced.isEmpty()) return
        Timber.d("GamificationSyncWorker: syncing ${unsynced.size} transactions")
        // TODO: apiService.syncGamificationTransactions(unsynced)
        repo.markTransactionsSynced(unsynced.map { it.id })
    }
}
