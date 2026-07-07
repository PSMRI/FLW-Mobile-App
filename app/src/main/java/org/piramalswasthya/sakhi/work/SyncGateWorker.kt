package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Debounce + serialization gate in front of the beneficiary push chain.
 *
 * WHY
 * ---
 * Every form-save calls [WorkerUtils.triggerAmritPushWorker]. Firing the full
 * 42-worker push chain directly on each save forces a lose-lose choice:
 *   • ExistingWorkPolicy.KEEP  → drops any save made while a cycle is running
 *                                (beneficiary never syncs until the next save).
 *   • ExistingWorkPolicy.APPEND_OR_REPLACE → never drops, but stacks a whole
 *                                new chain per save → duplicate/redundant
 *                                requests hammer the server.
 *
 * This gate resolves both. Callers enqueue only THIS worker, as unique work
 * (name [WorkerUtils.syncGateUniqueName]) with REPLACE + a short initial delay.
 * REPLACE is safe because this worker performs NO sync-state mutation — it only
 * inspects WorkManager state and enqueues. A burst of rapid saves therefore
 * collapses into a single gate run once the burst settles (debounce).
 *
 * WHAT IT DOES WHEN IT RUNS
 * -------------------------
 *   • If a push cycle ([WorkerUtils.pushWorkerUniqueName]) is currently active
 *     (ENQUEUED / RUNNING / BLOCKED) → it does NOT start another (which would
 *     be a redundant request). It RE-ARMS itself for a later re-check so the
 *     records saved during this cycle are picked up by the NEXT cycle.
 *   • Otherwise → it enqueues exactly one fresh push chain (with KEEP).
 *
 * GUARANTEES
 * ----------
 *   • Never drops: a save made mid-cycle re-arms the gate, which starts a
 *     follow-up cycle after the current one ends.
 *   • Never duplicates: at most one push chain exists at any time; bursts are
 *     coalesced; a re-check that finds a cycle already running does nothing but
 *     re-arm.
 *   • Never loops forever: re-arming is tied to "a cycle is currently RUNNING",
 *     which is transient (a chain always reaches SUCCEEDED/FAILED). A single
 *     gate run that finds the chain idle starts one cycle and does NOT re-arm,
 *     so a permanently-failing record cannot cause an infinite re-trigger loop.
 *     A [MAX_REARM] ceiling bounds polling of a pathologically long/stuck cycle.
 */
@HiltWorker
class SyncGateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: androidx.work.WorkerParameters,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "SyncGateWorker"

        /** Debounce window — rapid saves within this window coalesce into one cycle. */
        const val DEBOUNCE_SECONDS = 2L

        /** How often the gate re-checks while a push cycle is still running. */
        const val BUSY_RECHECK_SECONDS = 20L

        /** Ceiling on consecutive busy re-checks (~BUSY_RECHECK_SECONDS × this). */
        const val MAX_REARM = 60

        private const val KEY_REARM = "sync_gate_rearm_count"
    }

    override suspend fun doWork(): Result {
        return try {
            val workManager = WorkManager.getInstance(applicationContext)

            val chainInfos = withContext(Dispatchers.IO) {
                workManager.getWorkInfosForUniqueWork(WorkerUtils.pushWorkerUniqueName).get()
            }
            val chainActive = chainInfos.any { !it.state.isFinished }

            if (chainActive) {
                val reArm = inputData.getInt(KEY_REARM, 0)
                if (reArm >= MAX_REARM) {
                    // A cycle has stayed active for an unusually long time. Stop
                    // polling; the next save will re-arm the gate anyway.
                    Timber.w("SyncGate: push cycle still active after $reArm re-checks — stop polling")
                    return Result.success()
                }
                Timber.d("SyncGate: push cycle active — re-arming (attempt ${reArm + 1})")
                val next = OneTimeWorkRequestBuilder<SyncGateWorker>()
                    .setInitialDelay(BUSY_RECHECK_SECONDS, TimeUnit.SECONDS)
                    .setInputData(workDataOf(KEY_REARM to reArm + 1))
                    .build()
                workManager.enqueueUniqueWork(
                    WorkerUtils.syncGateUniqueName,
                    ExistingWorkPolicy.REPLACE,
                    next
                )
            } else {
                Timber.d("SyncGate: no active push cycle — starting one")
                WorkerUtils.enqueuePushChain(applicationContext, skipRegistration = false)
            }
            Result.success()
        } catch (e: Exception) {
            // Never leave the gate in retry back-off; a subsequent save will
            // re-arm it. Failing here must not block future syncs.
            Timber.e(e, "SyncGate: gate check failed")
            Result.success()
        }
    }
}