package org.piramal.gamification.core

/**
 * Configuration rule mapping a health-action type to its gamification parameters.
 *
 * @property actionType The health action this rule applies to.
 * @property pointsAwarded Points granted when this action is recorded.
 * @property dailyCap Maximum points per day from this action type.
 *   Applies globally across all action types unless overridden.
 * @property badgeId Optional badge awarded on first completion.
 */
data class RewardRule(
    val actionType: String,
    val pointsAwarded: Int,
    val dailyCap: Int = DAILY_CAP_DEFAULT,
    val badgeId: String? = null,
) {
    companion object {
        const val DAILY_CAP_DEFAULT = 100
    }
}

/**
 * Aggregated progress snapshot for a single health worker.
 *
 * @property acceptedEventIds Set of event IDs already processed (dedup).
 * @property dailyPoints Accumulated points per ISO date string.
 * @property totalPoints Lifetime points across all days.
 */
data class WorkerProgress(
    val acceptedEventIds: Set<String> = emptySet(),
    val dailyPoints: Map<String, Int> = emptyMap(),
    val totalPoints: Int = 0,
)
