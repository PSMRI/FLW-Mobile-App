package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.BadgeConfigCache
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStateCache
import org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache
import org.piramalswasthya.sakhi.model.BadgeSyncLogCache

@Dao
interface BadgeDao {

    // ─── BADGE_STATE ───
    @Query("SELECT * FROM BADGE_STATE")
    fun getAllStatesFlow(): Flow<List<BadgeStateCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStates(states: List<BadgeStateCache>)

    // ─── BADGE_EARNED (append-only; unique constraint gives idempotency) ───
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEarned(earned: List<BadgeEarnedCache>): List<Long>

    @Query("SELECT * FROM BADGE_EARNED")
    fun getAllEarnedFlow(): Flow<List<BadgeEarnedCache>>

    @Query("SELECT * FROM BADGE_EARNED WHERE userId = :userId")
    suspend fun getEarned(userId: Int): List<BadgeEarnedCache>

    @Query("SELECT * FROM BADGE_EARNED WHERE synced = 0")
    suspend fun getUnsyncedEarned(): List<BadgeEarnedCache>

    @Query("UPDATE BADGE_EARNED SET synced = 1 WHERE id IN (:ids)")
    suspend fun markEarnedSynced(ids: List<Long>)

    // ─── BADGE_SYNC_LOG ───
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSyncLog(log: BadgeSyncLogCache)

    @Query("SELECT weekKey FROM BADGE_SYNC_LOG")
    suspend fun getAllSyncWeeks(): List<String>

    // ─── BADGE_STREAK_FREEZE (replaced wholesale on every config pull) ───
    @Query("DELETE FROM BADGE_STREAK_FREEZE")
    suspend fun clearFreezes()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFreezes(freezes: List<BadgeStreakFreezeCache>)

    @Query("SELECT * FROM BADGE_STREAK_FREEZE")
    suspend fun getFreezes(): List<BadgeStreakFreezeCache>

    // ─── BADGE_CONFIG ───
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(rows: List<BadgeConfigCache>)

    @Query("SELECT * FROM BADGE_CONFIG")
    suspend fun getConfig(): List<BadgeConfigCache>

    @Query("SELECT * FROM BADGE_CONFIG")
    fun getConfigFlow(): Flow<List<BadgeConfigCache>>
}
