package org.piramalswasthya.sakhi.helpers

import androidx.work.ListenableWorker
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import timber.log.Timber

/**
 * Thin wrapper around Firebase Performance Monitoring custom traces.
 *
 * Never throws: if Firebase is missing or not initialised (e.g. the incomplete-install
 * path in SakhiApplication), tracing is skipped and the wrapped work still runs.
 *
 * Trace names, attribute values and metric names must never carry beneficiary data
 * (names, IDs, phone numbers): Performance Monitoring forbids PII and the data leaves
 * the device. Attribute values are limited to 100 characters, 5 attributes per trace.
 */
object PerfTracer {

    /** Whole pull chain, from the beneficiary download to the last module. */
    const val FULL_PULL = "full_pull"

    /** Room build to first open, including any migrations after an app update. */
    const val DB_OPEN = "db_open"

    fun start(name: String): Trace? = try {
        FirebasePerformance.getInstance().newTrace(name).also { it.start() }
    } catch (e: Exception) {
        Timber.w(e, "PerfTracer: could not start trace $name")
        null
    }

    fun stop(trace: Trace?) {
        try {
            trace?.stop()
        } catch (e: Exception) {
            Timber.w(e, "PerfTracer: could not stop trace")
        }
    }

    /** Runs [block] inside a custom trace named [name]; the trace stops even if [block] throws. */
    inline fun <T> trace(name: String, block: (Trace?) -> T): T {
        val trace = start(name)
        try {
            return block(trace)
        } finally {
            stop(trace)
        }
    }

    // Traces that start in one component and stop in another (e.g. across a WorkManager chain).
    // In-memory only: if the process dies in between, the trace is never stopped and never sent.
    private val openTraces = java.util.concurrent.ConcurrentHashMap<String, Trace>()

    /**
     * Starts a trace that [stopOpen] finishes elsewhere. [restart] = false keeps an already-open
     * trace running (a retried worker stays part of the same measurement); true drops any
     * stale one left by a run that never finished, so it cannot inflate the next measurement.
     */
    fun startOpen(name: String, restart: Boolean) {
        if (!restart && openTraces.containsKey(name)) return
        start(name)?.let { openTraces[name] = it }
    }

    fun stopOpen(name: String, attributes: Map<String, String> = emptyMap()) {
        val trace = openTraces.remove(name) ?: return
        try {
            attributes.forEach { (key, value) -> trace.putAttribute(key, value) }
        } catch (e: Exception) {
            Timber.w(e, "PerfTracer: could not set attributes on $name")
        }
        stop(trace)
    }

    /** Low-cardinality label for a worker outcome, for use as a trace attribute. */
    fun label(result: ListenableWorker.Result): String = when (result) {
        is ListenableWorker.Result.Success -> "success"
        is ListenableWorker.Result.Retry -> "retry"
        else -> "failure"
    }
}
