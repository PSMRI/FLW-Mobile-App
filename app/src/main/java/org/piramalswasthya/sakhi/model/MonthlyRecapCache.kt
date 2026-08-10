package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Language chosen for the Monthly Recap ONLY. This never changes the global
 * Sakhi application language.
 */
enum class MonthlyRecapLanguage(val token: String) {
    HINDI("HI"),
    ASSAMESE("AS");

    companion object {
        fun fromToken(token: String?): MonthlyRecapLanguage? =
            entries.firstOrNull { it.token == token }
    }
}

/** Local lifecycle of one monthly recap snapshot. Playback-only concern. */
enum class RecapStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    companion object {
        /** Corrupted/unknown persisted values fall back safely to NOT_STARTED. */
        fun fromToken(token: String?): RecapStatus =
            entries.firstOrNull { it.name == token } ?: NOT_STARTED
    }
}

/**
 * One Monthly Recap snapshot: exactly one row per (userId, recapYearMonth) —
 * enforced by a unique index so the database, not in-memory state, is the final
 * uniqueness boundary against rapid taps / rotation / process recreation.
 *
 * The snapshot must stay stable after first creation: the frozen activity window,
 * the variant seed and (later) the metric payload never change on reopen.
 * [metricsJson] stays NULL in Phase 3 — real on-device metric calculation is
 * Phase 4; nothing here fabricates production counts.
 *
 * Cleared automatically on logout via the existing clearAllTables() flow.
 */
@Entity(
    tableName = "MONTHLY_RECAP",
    indices = [Index(value = ["userId", "recapYearMonth"], unique = true)]
)
data class MonthlyRecapCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Logged-in ASHA (User.userId). Never hardcoded. */
    val userId: Int,

    /** Canonical year-month key of the recap month, e.g. 202606 for June 2026. */
    val recapYearMonth: Int,

    /** Frozen activity window: start-inclusive, device-local time at creation. */
    val windowStartMillis: Long,

    /** Frozen activity window: end-exclusive. */
    val windowEndMillis: Long,

    /** RecapStatus token; parse with [recapStatus]. */
    val status: String = RecapStatus.NOT_STARTED.name,

    /** MonthlyRecapLanguage token ("HI"/"AS"); null until explicitly selected. */
    val language: String? = null,

    /**
     * Stable identity for message-variant selection: generated once at snapshot
     * creation, never regenerated, independent of display language, so reopening
     * or switching Hindi/Assamese keeps the same semantic variant.
     */
    val variantSeed: Long,

    /** Schema/content versioning for forward compatibility. */
    val snapshotVersion: Int = 1,

    /** Phase 4 boundary: serialized metric payload; NULL until real calculation. */
    val metricsJson: String? = null,

    /** Last safely completed scene index (generic; no beneficiary/category data). */
    val progressScene: Int = 0,

    /** Total scene count; unknown until playback exists (Phase 6). */
    val totalScenes: Int? = null,

    val createdAt: Long,
    val updatedAt: Long,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
)

/** Safe typed accessors (unknown persisted values never crash). */
fun MonthlyRecapCache.recapStatus(): RecapStatus = RecapStatus.fromToken(status)

fun MonthlyRecapCache.recapLanguage(): MonthlyRecapLanguage? =
    MonthlyRecapLanguage.fromToken(language)

/**
 * Progress with corrupted values coerced into valid bounds.
 *
 * The upper bound is `totalScenes - 1`, not `totalScenes`: [progressScene] is an
 * INDEX into the scene list, so a story of 5 scenes has valid indices 0..4.
 * Clamping here (the resume path) as well as on write also repairs snapshots that
 * an earlier build already stored one past the end.
 */
fun MonthlyRecapCache.safeProgressScene(): Int {
    val lastIndex = totalScenes?.takeIf { it > 0 }?.let { it - 1 }
    val coerced = progressScene.coerceAtLeast(0)
    return if (lastIndex != null) coerced.coerceAtMost(lastIndex) else coerced
}
