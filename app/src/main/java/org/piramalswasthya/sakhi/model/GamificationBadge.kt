package org.piramalswasthya.sakhi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per badge earned. New rows inserted on unlock — never updated.
 * Trilingual badge names stored directly so UI renders offline
 * without any resource lookup.
 *
 * Foreign key to GAMIFICATION_PROFILE ensures badges are always
 * tied to a valid worker profile.
 */
@Entity(
    tableName = "GAMIFICATION_BADGE",
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
data class GamificationBadge(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Int,

    /**
     * Stable identifier for the badge type.
     * e.g. "FIRST_REGISTRATION", "STREAK_7", "ANC_HERO"
     * Used to prevent duplicate awards.
     */
    @ColumnInfo(name = "badgeType")
    val badgeType: String,

    @ColumnInfo(name = "badgeNameEn")
    val badgeNameEn: String,

    @ColumnInfo(name = "badgeNameHi")
    val badgeNameHi: String,

    @ColumnInfo(name = "badgeNameAs")
    val badgeNameAs: String,

    @ColumnInfo(name = "earnedAt")
    val earnedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "syncState")
    val syncState: Int = 0
)
