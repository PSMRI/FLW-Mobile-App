package org.piramalswasthya.sakhi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the ASHA worker's overall gamification state.
 * One row per worker, keyed by userId (Int — matches User.userId).
 *
 * syncState matches the app-wide convention:
 *   0 = UNSYNCED, 1 = SYNCING, 2 = SYNCED
 */
@Entity(tableName = "GAMIFICATION_PROFILE")
data class GamificationProfile(

    @PrimaryKey
    @ColumnInfo(name = "userId")
    val userId: Int,

    @ColumnInfo(name = "totalPoints")
    val totalPoints: Int = 0,

    @ColumnInfo(name = "currentStreakDays")
    val currentStreakDays: Int = 0,

    @ColumnInfo(name = "longestStreakDays")
    val longestStreakDays: Int = 0,

    /**
     * ISO date string "yyyy-MM-dd" of the last day the worker earned points.
     * Null means the worker has never earned points yet.
     */
    @ColumnInfo(name = "lastActivityDate")
    val lastActivityDate: String? = null,

    @ColumnInfo(name = "level")
    val level: Int = 1,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "syncState")
    val syncState: Int = 0
)
