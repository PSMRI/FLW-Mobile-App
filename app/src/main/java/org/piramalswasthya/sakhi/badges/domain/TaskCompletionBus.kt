package org.piramalswasthya.sakhi.badges.domain

import androidx.room.InvalidationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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
    private val events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Manual publish for callers outside Room (e.g. sync completion). */
    fun publish() {
        events.tryEmit(Unit)
    }

    /** Idempotent; call once from the home entry point. */
    @OptIn(FlowPreview::class)
    fun start() {
        if (!started.compareAndSet(false, true)) return
        try {
            db.invalidationTracker.addObserver(
                object : InvalidationTracker.Observer(BadgeFactsReader.MAPPED_TABLES) {
                    override fun onInvalidated(tables: Set<String>) = publish()
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Badges: could not observe health tables")
        }
        scope.launch {
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

    companion object {
        private const val DEBOUNCE_MS = 3_000L
    }
}
