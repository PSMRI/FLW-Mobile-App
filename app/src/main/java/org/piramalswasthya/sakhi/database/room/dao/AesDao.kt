package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache

@Dao
interface AesDao {
    @Query("SELECT * FROM AES_SCREENING WHERE benId =:benId limit 1")
    suspend fun getAESScreening(benId: Long): AESScreeningCache?

    @Query("SELECT * FROM AES_SCREENING WHERE benId =:benId and (visitDate = :visitDate or visitDate = :visitDateGMT) limit 1")
    suspend fun getAESScreening(benId: Long, visitDate: Long, visitDateGMT: Long): AESScreeningCache?

    @Query("SELECT * FROM AES_SCREENING WHERE  syncState = :syncState")
    suspend fun getAESScreening(syncState: SyncState): List<AESScreeningCache>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAESScreening(malariaScreeningCache: AESScreeningCache)


}