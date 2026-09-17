package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.MonthlyRecapCache

/**
 * Minimal DAO for Monthly Recap snapshots. All operations target one
 * (userId, recapYearMonth) identity; nothing loads all recaps.
 * Suspend/Flow only — never called on the main thread.
 */
@Dao
interface MonthlyRecapDao {

    /**
     * IGNORE on conflict: the unique (userId, recapYearMonth) index makes the
     * database the final uniqueness boundary. Returns -1 when a row already won.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recap: MonthlyRecapCache): Long

    @Query("SELECT * FROM MONTHLY_RECAP WHERE userId = :userId AND recapYearMonth = :yearMonth LIMIT 1")
    suspend fun get(userId: Int, yearMonth: Int): MonthlyRecapCache?

    @Query("SELECT * FROM MONTHLY_RECAP WHERE userId = :userId AND recapYearMonth = :yearMonth LIMIT 1")
    fun observe(userId: Int, yearMonth: Int): Flow<MonthlyRecapCache?>

    @Query(
        "UPDATE MONTHLY_RECAP SET language = :languageToken, updatedAt = :now " +
                "WHERE userId = :userId AND recapYearMonth = :yearMonth"
    )
    suspend fun setLanguage(userId: Int, yearMonth: Int, languageToken: String, now: Long)

    @Query(
        "UPDATE MONTHLY_RECAP SET status = 'IN_PROGRESS', startedAt = COALESCE(startedAt, :now), " +
                "updatedAt = :now WHERE userId = :userId AND recapYearMonth = :yearMonth " +
                "AND status != 'COMPLETED'"
    )
    suspend fun markStarted(userId: Int, yearMonth: Int, now: Long)

    /**
     * Persists the CURRENT scene index (the scene being shown — the resume
     * target), not a "completed" marker: reopening starts AT this scene.
     */
    @Query(
        "UPDATE MONTHLY_RECAP SET progressScene = :scene, updatedAt = :now " +
                "WHERE userId = :userId AND recapYearMonth = :yearMonth AND status = 'IN_PROGRESS'"
    )
    suspend fun updateProgress(userId: Int, yearMonth: Int, scene: Int, now: Long)

    /** Records how many scenes the frozen story has (bounds progress clamping/resume). */
    @Query(
        "UPDATE MONTHLY_RECAP SET totalScenes = :totalScenes, updatedAt = :now " +
                "WHERE userId = :userId AND recapYearMonth = :yearMonth"
    )
    suspend fun setTotalScenes(userId: Int, yearMonth: Int, totalScenes: Int, now: Long)

    @Query(
        "UPDATE MONTHLY_RECAP SET status = 'COMPLETED', completedAt = COALESCE(completedAt, :now), " +
                "updatedAt = :now WHERE userId = :userId AND recapYearMonth = :yearMonth"
    )
    suspend fun markCompleted(userId: Int, yearMonth: Int, now: Long)

    /**
     * Freezes the aggregate metrics payload — but ONLY when none is stored yet
     * (metricsJson IS NULL), making the database the final freeze boundary: the
     * first writer wins and later writers cannot clobber it. Returns the number of
     * rows updated (1 = this caller froze it, 0 = someone already did).
     * Touches only metricsJson + updatedAt, so language, status, progress,
     * variantSeed, createdAt and snapshot identity are all preserved.
     */
    @Query(
        "UPDATE MONTHLY_RECAP SET metricsJson = :metricsJson, updatedAt = :now " +
                "WHERE userId = :userId AND recapYearMonth = :yearMonth AND metricsJson IS NULL"
    )
    suspend fun setMetricsIfAbsent(userId: Int, yearMonth: Int, metricsJson: String, now: Long): Int
}
