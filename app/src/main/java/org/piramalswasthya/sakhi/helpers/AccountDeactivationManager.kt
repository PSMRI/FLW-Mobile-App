package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountDeactivationManager @Inject constructor() {

    companion object {
        private const val DIALOG_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val _deactivationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deactivationEvent: SharedFlow<String> = _deactivationEvent

    private val lastDialogTimestamp = AtomicLong(0L)

    fun emitIfCooldownPassed(errorMessage: String) {
        val now = System.currentTimeMillis()
        val last = lastDialogTimestamp.get()
        if (now - last >= DIALOG_COOLDOWN_MS && lastDialogTimestamp.compareAndSet(last, now)) {
            _deactivationEvent.tryEmit(errorMessage)
        }
    }
}
