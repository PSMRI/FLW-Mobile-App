package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.BadgeConfigCache
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStateCache
import org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache
import org.piramalswasthya.sakhi.model.BadgeSyncLogCache

@Dao
interface BadgeDao {

    // ─── BADGE_STATE ───
    /**
     * Scoped to one ASHA: the drawer logout keeps the database, so another user's rows can
     * still be present and an unscoped read shows them her progress.
     */
    @Query("SELECT * FROM BADGE_STATE WHERE userId = :userId")
    fun getStatesFlow(userId: Int): Flow<List<BadgeStateCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStates(states: List<BadgeStateCache>)

    // ─── BADGE_EARNED (append-only; unique constraint gives idempotency) ───
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEarned(earned: List<BadgeEarnedCache>): List<Long>

    @Query("SELECT * FROM BADGE_EARNED WHERE userId = :userId")
    fun getEarnedFlow(userId: Int): Flow<List<BadgeEarnedCache>>

    @Query("SELECT * FROM BADGE_EARNED WHERE userId = :userId")
    suspend fun getEarned(userId: Int): List<BadgeEarnedCache>

    /**
     * Filtered by user as well as by sync flag. The push names the active ASHA in its body,
     * so an unscoped read would attribute a previous user's awards to whoever is logged in
     * now — and then mark them synced, making it permanent.
     */
    @Query("SELECT * FROM BADGE_EARNED WHERE synced = 0 AND userId = :userId")
    suspend fun getUnsyncedEarned(userId: Int): List<BadgeEarnedCache>

    @Query("UPDATE BADGE_EARNED SET synced = 1 WHERE id IN (:ids)")
    suspend fun markEarnedSynced(ids: List<Long>)

    // ─── BADGE_SYNC_LOG ───
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSyncLog(log: BadgeSyncLogCache)

    @Query("SELECT weekKey FROM BADGE_SYNC_LOG")
    suspend fun getAllSyncWeeks(): List<String>

    @Query("DELETE FROM BADGE_SYNC_LOG WHERE weekKey = :weekKey")
    suspend fun deleteSyncWeek(weekKey: String)

    // ─── BADGE_STREAK_FREEZE (replaced wholesale on every config pull) ───
    @Query("DELETE FROM BADGE_STREAK_FREEZE")
    suspend fun clearFreezes()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFreezes(freezes: List<BadgeStreakFreezeCache>)

    /**
     * Delete and re-insert in one transaction.
     *
     * Done separately the window between them is a state the evaluator can read: a nightly
     * evaluation landing there sees no freezes at all and breaks a streak the server had
     * explicitly protected, which is the one thing a freeze exists to prevent. A crash
     * between the two left the table empty permanently.
     */
    @Transaction
    suspend fun replaceFreezes(freezes: List<BadgeStreakFreezeCache>) {
        clearFreezes()
        insertFreezes(freezes)
    }

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
