package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache

@Dao
interface SaasBahuSammelanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSammelan(data: SaasBahuSammelanCache)

    @Update
    suspend fun updateSammelan(data: SaasBahuSammelanCache)

    @Delete
    suspend fun deleteSammelan(data: SaasBahuSammelanCache)

    @Query("select * from SAAS_BAHU_ACTIVITY where syncState = :state")
    fun getBySyncState(state: SyncState): List<SaasBahuSammelanCache>

    @Query("SELECT * FROM SAAS_BAHU_ACTIVITY ORDER BY date DESC")
    fun getAllSammelan(): Flow<List<SaasBahuSammelanCache>>

    @Query("SELECT * FROM SAAS_BAHU_ACTIVITY WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SaasBahuSammelanCache?

    @Query("""
    SELECT * FROM SAAS_BAHU_ACTIVITY
    WHERE ashaId = :ashaId
    ORDER BY date DESC
    LIMIT 1 """)
    suspend fun getLastUpdatedSammelan(ashaId: Int): SaasBahuSammelanCache?

    @Query("DELETE FROM SAAS_BAHU_ACTIVITY")
    suspend fun clearAll()

    @Query("UPDATE SAAS_BAHU_ACTIVITY SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}