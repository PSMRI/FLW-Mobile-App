package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenExpiryManager @Inject constructor() {

    companion object {
        private const val MAX_CONSECUTIVE_FAILURES = 3
    }

    private val consecutiveFailures = AtomicInteger(0)

    private val _forceLogoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogoutEvent: SharedFlow<Unit> = _forceLogoutEvent

    fun onRefreshFailed() {
        val count = consecutiveFailures.incrementAndGet()
        Timber.w("Token refresh failed. Consecutive failures: $count")
        if (count >= MAX_CONSECUTIVE_FAILURES) {
            Timber.w("Max consecutive refresh failures reached ($count). Forcing logout.")
            consecutiveFailures.set(0)
            _forceLogoutEvent.tryEmit(Unit)
        }
    }

    fun onRefreshSuccess() {
        consecutiveFailures.set(0)
    }
}
