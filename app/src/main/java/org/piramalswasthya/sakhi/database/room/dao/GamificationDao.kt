package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.GamificationBadge
import org.piramalswasthya.sakhi.model.GamificationProfile
import org.piramalswasthya.sakhi.model.PointsTransaction

@Dao
interface GamificationDao {

    // ── Profile ───────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: GamificationProfile)

    @Query("SELECT * FROM GAMIFICATION_PROFILE WHERE userId = :userId")
    suspend fun getProfile(userId: Int): GamificationProfile?

    @Query("SELECT * FROM GAMIFICATION_PROFILE WHERE userId = :userId")
    fun observeProfile(userId: Int): Flow<GamificationProfile?>

    @Query("SELECT * FROM GAMIFICATION_PROFILE WHERE syncState = 0")
    suspend fun getUnsyncedProfiles(): List<GamificationProfile>

    @Query("UPDATE GAMIFICATION_PROFILE SET syncState = 2 WHERE userId = :userId")
    suspend fun markProfileSynced(userId: Int)

    // ── Badges ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: GamificationBadge): Long

    @Query("SELECT COUNT(*) FROM GAMIFICATION_BADGE WHERE userId = :userId AND badgeType = :badgeType")
    suspend fun hasBadge(userId: Int, badgeType: String): Int

    @Query("SELECT * FROM GAMIFICATION_BADGE WHERE userId = :userId ORDER BY earnedAt DESC")
    fun observeBadges(userId: Int): Flow<List<GamificationBadge>>

    @Query("SELECT * FROM GAMIFICATION_BADGE WHERE syncState = 0")
    suspend fun getUnsyncedBadges(): List<GamificationBadge>

    @Query("UPDATE GAMIFICATION_BADGE SET syncState = 2 WHERE id IN (:ids)")
    suspend fun markBadgesSynced(ids: List<Long>)

    // ── Points Transactions ───────────────────────────────────────────────────

    @Insert
    suspend fun insertTransaction(tx: PointsTransaction): Long

    @Query("SELECT * FROM GAMIFICATION_POINTS_TX WHERE userId = :userId ORDER BY createdAt DESC LIMIT 20")
    fun observeRecentTransactions(userId: Int): Flow<List<PointsTransaction>>

    @Query("SELECT * FROM GAMIFICATION_POINTS_TX WHERE syncState = 0")
    suspend fun getUnsyncedTransactions(): List<PointsTransaction>

    @Query("UPDATE GAMIFICATION_POINTS_TX SET syncState = 2 WHERE id IN (:ids)")
    suspend fun markTransactionsSynced(ids: List<Long>)
}
