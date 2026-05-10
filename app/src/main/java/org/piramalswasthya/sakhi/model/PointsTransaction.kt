package org.piramalswasthya.sakhi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Immutable audit log — one row per points award event.
 * Never updated after insert.
 *
 * Allows the backend to reconstruct gamification state
 * from raw events if needed (event-sourcing friendly).
 */
@Entity(
    tableName = "GAMIFICATION_POINTS_TX",
    foreignKeys = [
        ForeignKey(
            entity = GamificationProfile::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class PointsTransaction(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Int,

    @ColumnInfo(name = "points")
    val points: Int,

    /**
     * Mirrors GamificationEvent.eventType string.
     * e.g. "ANC_VISIT_COMPLETED", "STREAK_BONUS"
     */
    @ColumnInfo(name = "eventType")
    val eventType: String,

    /**
     * Optional reference ID — beneficiary ID, visit ID, household ID etc.
     * Used for deduplication and audit on backend.
     */
    @ColumnInfo(name = "eventRefId")
    val eventRefId: String? = null,

    /** Human-readable English reason shown in the activity feed. */
    @ColumnInfo(name = "reason")
    val reason: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "syncState")
    val syncState: Int = 0
)
