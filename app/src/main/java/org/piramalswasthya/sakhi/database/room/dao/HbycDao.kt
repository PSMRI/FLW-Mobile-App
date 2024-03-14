package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.HBYCCache

@Dao
interface HbycDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg hbycCache: HBYCCache)

    @Query("SELECT * FROM HBYC WHERE processed = 'N'")
    suspend fun getAllUnprocessedHBYC(): List<HBYCCache>

    @Query("select count(*) from HBYC")
    suspend fun hbycCount(): Int

    @Query("SELECT * FROM HBYC WHERE benId =:benId AND hhId = :hhId AND month = :month LIMIT 1")
    suspend fun getHbyc(hhId: Long, benId: Long, month: String): HBYCCache?

    @Query("SELECT * FROM HBYC WHERE benId =:benId AND hhId = :hhId ORDER BY month")
    fun getAllHbycEntries(hhId: Long, benId: Long): Flow<List<HBYCCache>>

    @Update
    suspend fun setSynced(it: HBYCCache)

    @Query("SELECT * FROM HBYC WHERE processed = 'N' or processed = 'P'")
    fun getAllUnprocessedHbyc(): List<HBYCCache>
}