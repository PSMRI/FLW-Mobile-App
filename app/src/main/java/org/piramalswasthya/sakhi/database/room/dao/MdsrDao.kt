package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import org.piramalswasthya.sakhi.model.MDSRCache

@Dao
interface MdsrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg mdsrCache: MDSRCache)

    @Query("SELECT * FROM MDSR WHERE processed in ('N','U')")
    suspend fun getAllUnprocessedMdsr(): List<MDSRCache>

    @Query("select count(*) from MDSR")
    suspend fun mdsrCount(): Int

    @Query("SELECT * FROM MDSR WHERE benId =:benId LIMIT 1")
    suspend fun getMDSR(benId: Long): MDSRCache?

    @Update
    suspend fun updateMdsrRecord(it: MDSRCache)
}