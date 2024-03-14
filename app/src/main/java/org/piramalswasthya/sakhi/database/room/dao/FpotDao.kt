package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import org.piramalswasthya.sakhi.model.FPOTCache

@Dao
interface FpotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg fpotCache: FPOTCache)

    @Query("SELECT * FROM FPOT WHERE processed = 'N'")
    suspend fun getAllUnprocessedFPOT(): List<FPOTCache>

    @Query("select count(*) from FPOT")
    suspend fun fpotCount(): Int

    @Query("SELECT * FROM FPOT WHERE benId =:benId AND hhId = :hhId LIMIT 1")
    suspend fun getFpot(hhId: Long, benId: Long): FPOTCache?

    @Update
    suspend fun setSynced(it: FPOTCache)
}