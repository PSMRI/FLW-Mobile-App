package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenExpiryManager @Inject constructor() {

    companion object {
        private const val MAX_CONSECUTIVE_FAILURES = 5
        // Reset failure counter if no failure occurs within 10 minutes
        private const val FAILURE_WINDOW_MS = 10 * 60 * 1000L
    }

    private val consecutiveFailures = AtomicInteger(0)
    private val firstFailureTimestamp = AtomicLong(0)

    private val _forceLogoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogoutEvent: SharedFlow<Unit> = _forceLogoutEvent

    fun onRefreshFailed() {
        val now = System.currentTimeMillis()
        val firstFailure = firstFailureTimestamp.get()

        // Reset counter if the failure window has elapsed since the first failure
        if (firstFailure > 0 && (now - firstFailure) > FAILURE_WINDOW_MS) {
            consecutiveFailures.set(0)
            firstFailureTimestamp.set(0)
        }

        // Record timestamp of first failure in this window
        firstFailureTimestamp.compareAndSet(0, now)

        val count = consecutiveFailures.incrementAndGet()
        Timber.w("Auth refresh failed (401/403). Consecutive failures: $count")
        if (count >= MAX_CONSECUTIVE_FAILURES) {
            Timber.w("Max consecutive auth failures reached ($count). Forcing logout.")
            consecutiveFailures.set(0)
            firstFailureTimestamp.set(0)
            _forceLogoutEvent.tryEmit(Unit)
        }
    }

    fun onRefreshSuccess() {
        consecutiveFailures.set(0)
        firstFailureTimestamp.set(0)
    }
}
