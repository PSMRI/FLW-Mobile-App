package org.piramal.gamification.core

/**
 * A gamification event triggered by a health-worker action.
 *
 * Each event corresponds to a concrete workflow step (e.g., household
 * registration, ANC visit, immunization) and carries the points earned
 * as well as sync metadata for offline-first operation.
 *
 * @property eventId Unique event identifier (used for idempotent dedup).
 * @property workerId The health worker who performed the action.
 * @property actionType The type of health action (e.g., "registration",
 *   "anc_visit", "immunization").
 * @property points Points earned for this event.
 * @property localDate ISO date string for daily-cap calculation.
 * @property syncStatus Tracks offline-sync lifecycle: "pending" | "synced".
 * @property rewardId Stable reward identifier across sync retries.
 */
data class GamificationEvent(
    val eventId: String,
    val workerId: String,
    val actionType: String,
    val points: Int,
    val localDate: String,
    val syncStatus: String = "pending",
    val rewardId: String? = null,
)
