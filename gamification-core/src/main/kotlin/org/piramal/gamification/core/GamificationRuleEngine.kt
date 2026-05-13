package org.piramal.gamification.core

/**
 * Core rule engine for the FLW gamification module.
 *
 * Processes [GamificationEvent] instances against a worker's current
 * [WorkerProgress] and returns the updated progress. All state is
 * immutable — the engine returns a new copy on each invocation.
 *
 * Key behaviours:
 * - **Idempotency**: duplicate event IDs are silently accepted without
 *   double-counting points.
 * - **Daily cap**: points exceeding [RewardRule.DAILY_CAP_DEFAULT] for
 *   a single day are discarded.
 * - **Offline safety**: events with any [syncStatus] are tracked in
 *   [WorkerProgress.acceptedEventIds] so retries after connectivity
 *   restoration do not double-count.
 */
class GamificationRuleEngine(
    private val rules: Map<String, RewardRule> = emptyMap(),
) {
    /**
     * Apply an event and return the resulting progress.
     *
     * @param event The incoming gamification event.
     * @param progress The worker's current progress snapshot.
     * @return Updated progress with points applied (subject to cap and dedup).
     */
    fun apply(event: GamificationEvent, progress: WorkerProgress): WorkerProgress {
        // Idempotency: skip point award for already-processed events.
        if (event.eventId in progress.acceptedEventIds) {
            return progress.copy(
                acceptedEventIds = progress.acceptedEventIds + event.eventId,
            )
        }

        val currentDayPoints = progress.dailyPoints[event.localDate] ?: 0
        val cap = rules[event.actionType]?.dailyCap ?: RewardRule.DAILY_CAP_DEFAULT

        // If the daily cap is already reached, record the event but award no points.
        if (currentDayPoints >= cap) {
            return progress.copy(
                acceptedEventIds = progress.acceptedEventIds + event.eventId,
                dailyPoints = progress.dailyPoints,
            )
        }

        // Award points up to the remaining daily capacity.
        val available = cap - currentDayPoints
        val pointsToAdd = minOf(event.points, available)

        return progress.copy(
            acceptedEventIds = progress.acceptedEventIds + event.eventId,
            dailyPoints = progress.dailyPoints + (event.localDate to currentDayPoints + pointsToAdd),
            totalPoints = progress.totalPoints + pointsToAdd,
        )
    }
}
