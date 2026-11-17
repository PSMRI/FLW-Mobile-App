package org.piramalswasthya.sakhi.badges.domain

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Newly-earned badge events for the in-app celebration overlay.
 * A Channel (not SharedFlow) so each award is shown exactly once, queues
 * while the app is backgrounded, and survives configuration changes.
 */
@Singleton
class BadgeCelebrations @Inject constructor() {

    /** (badgeId, level) */
    private val channel = Channel<Pair<String, Int>>(capacity = 16)

    val awards: Flow<Pair<String, Int>> = channel.receiveAsFlow()

    fun publish(badgeId: String, level: Int) {
        channel.trySend(badgeId to level)
    }
}
