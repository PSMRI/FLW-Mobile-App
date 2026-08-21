package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Badges module tables (LLD §4.1). The module is strictly read-only over
 * health tables; these are the only tables it writes.
 */

/** Permanent, append-only log of earned levels. "Once earned, never revoked". */
@Entity(
    tableName = "BADGE_EARNED",
    indices = [Index(value = ["userId", "badgeId", "level", "caseRef"], unique = true)]
)
data class BadgeEarnedCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Int,
    val badgeId: String,
    val level: Int,
    // "" for cumulative/streak levels, quarterKey for re-earnable badges,
    // benId for per-case recognitions. Never transmitted to the server.
    val caseRef: String = "",
    val earnedAt: Long,
    val synced: Boolean = false
)

/** Latest progress and streak snapshot, overwritten on every evaluation. */
@Entity(tableName = "BADGE_STATE")
data class BadgeStateCache(
    @PrimaryKey
    val badgeId: String,
    val currentLevel: Int,
    val progress: Long,
    val nextTarget: Long,
    val streakCount: Long = 0,
    val graceRemaining: Int = 0,
    val lastEvaluatedAt: Long
)

/** One row per ISO week with at least one successful sync. */
@Entity(tableName = "BADGE_SYNC_LOG")
data class BadgeSyncLogCache(
    @PrimaryKey
    val weekKey: String,
    val syncCompletedAt: Long
)

/** Server-pushed streak-freeze windows (illness / alternate duty). */
@Entity(
    tableName = "BADGE_STREAK_FREEZE",
    indices = [Index(value = ["badgeId", "startDate", "endDate"], unique = true)]
)
data class BadgeStreakFreezeCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // "" applies to all badges
    val badgeId: String = "",
    val startDate: Long,
    val endDate: Long
)

/** Server-pushed thresholds, copy version and feature flags, as key/value rows. */
@Entity(tableName = "BADGE_CONFIG")
data class BadgeConfigCache(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long
)
