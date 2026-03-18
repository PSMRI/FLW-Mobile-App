package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.FilariaScreeningCache

@Dao
interface FilariaDao {
    @Query("SELECT * FROM FILARIA_SCREENING WHERE benId =:benId limit 1")
    suspend fun getFilariaScreening(benId: Long): FilariaScreeningCache?

    @Query("SELECT * FROM FILARIA_SCREENING WHERE benId =:benId and (mdaHomeVisitDate = :visitDate or mdaHomeVisitDate = :visitDateGMT) limit 1")
    suspend fun getFilariaScreening(benId: Long, visitDate: Long, visitDateGMT: Long): FilariaScreeningCache?

    @Query("SELECT * FROM FILARIA_SCREENING WHERE  syncState = :syncState")
    suspend fun getFilariaScreening(syncState: SyncState): List<FilariaScreeningCache>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFilariaScreening(malariaScreeningCache: FilariaScreeningCache)

    @Query("UPDATE FILARIA_SCREENING SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}