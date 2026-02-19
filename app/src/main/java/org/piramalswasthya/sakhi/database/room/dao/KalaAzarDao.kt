package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
@Dao
interface KalaAzarDao {
    @Query("SELECT * FROM KALAZAR_SCREENING WHERE benId =:benId limit 1")
    suspend fun getKalaAzarScreening(benId: Long): KalaAzarScreeningCache?

    @Query("SELECT * FROM KALAZAR_SCREENING WHERE benId =:benId and (visitDate = :visitDate or visitDate = :visitDateGMT) limit 1")
    suspend fun getKalaAzarScreening(benId: Long, visitDate: Long, visitDateGMT: Long): KalaAzarScreeningCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveKalaAzarScreening(tbScreeningCache: KalaAzarScreeningCache)

    @Query("SELECT * FROM KALAZAR_SCREENING WHERE benId =:benId limit 1")
    suspend fun getKalaAzarSuspected(benId: Long): KalaAzarScreeningCache?

    @Query("SELECT * FROM KALAZAR_SCREENING WHERE benId =:benId and (visitDate = :visitDate or visitDate = :visitDateGMT) limit 1")
    suspend fun getKalaAzarSuspected(benId: Long, visitDate: Long, visitDateGMT: Long): KalaAzarScreeningCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTbSuspected(tbSuspectedCache: KalaAzarScreeningCache)

    @Query("SELECT * FROM KALAZAR_SCREENING WHERE  syncState = :syncState")
    suspend fun getKalaAzarScreening(syncState: SyncState): List<KalaAzarScreeningCache>

    @Query("SELECT * FROM KALAZAR_SCREENING WHERE  syncState = :syncState")
    suspend fun getKalaAzarSuspected(syncState: SyncState): List<KalaAzarScreeningCache>

    @Query("UPDATE KALAZAR_SCREENING SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}