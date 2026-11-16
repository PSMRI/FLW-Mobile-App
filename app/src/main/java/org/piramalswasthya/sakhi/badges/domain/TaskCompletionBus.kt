package org.piramalswasthya.sakhi.badges.domain

import androidx.room.InvalidationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.room.InAppDb
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signals new records in badge-mapped health tables to wake the evaluator
 * (LLD §2.1). Fed by Room's InvalidationTracker so every repository save path
 * publishes automatically — no per-repository wiring. Bursts of saves are
 * debounced into one recompute; recompute semantics (LLD §5.1) make the
 * new-vs-edit distinction irrelevant for correctness.
 */
@Singleton
class TaskCompletionBus @Inject constructor(
    private val db: InAppDb,
    private val evaluator: BadgeEvaluator
) {

    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // replay = 1: the startup publish() must survive until the collector
    // subscribes — without it, launch-time evaluation silently never runs
    // unless a sync happens to invalidate a table afterwards
    private val events = MutableSharedFlow<Unit>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Manual publish for callers outside Room (e.g. sync completion). */
    fun publish() {
        events.tryEmit(Unit)
    }

    /** Room observer while started; kept so [stop] can remove it (FLW-1251). */
    private var observer: InvalidationTracker.Observer? = null

    /** Evaluation collector while started; kept so [stop] can cancel it (FLW-1251). */
    private var collectJob: Job? = null

    /** Idempotent; call once from the home entry point. */
    @OptIn(FlowPreview::class)
    @Synchronized
    fun start() {
        if (!started.compareAndSet(false, true)) return
        try {
            val newObserver =
                object : InvalidationTracker.Observer(BadgeFactsReader.MAPPED_TABLES) {
                    override fun onInvalidated(tables: Set<String>) = publish()
                }
            db.invalidationTracker.addObserver(newObserver)
            observer = newObserver
        } catch (e: Exception) {
            Timber.e(e, "Badges: could not observe health tables")
        }
        collectJob = scope.launch {
            events.debounce(DEBOUNCE_MS).collect {
                try {
                    evaluator.evaluateAll()
                } catch (e: Exception) {
                    Timber.e(e, "Badges: evaluation from bus failed")
                }
            }
        }
        publish() // evaluate once on startup so UI is fresh
    }

    /**
     * FLW-1251: badges switched off by the server — stop watching the health tables.
     * Idempotent; [start] can be called again when badges are switched back on.
     */
    @Synchronized
    fun stop() {
        if (!started.compareAndSet(true, false)) return
        observer?.let {
            try {
                db.invalidationTracker.removeObserver(it)
            } catch (e: Exception) {
                Timber.w(e, "Badges: could not stop observing health tables")
            }
        }
        observer = null
        collectJob?.cancel()
        collectJob = null
    }

    companion object {
        // Keep short so awards show soon after a form save; Room bursts still coalesce.
        private const val DEBOUNCE_MS = 400L
    }
}
