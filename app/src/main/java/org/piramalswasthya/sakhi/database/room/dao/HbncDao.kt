package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.HBNCCache


@Dao
interface HbncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg hbncCache: HBNCCache)

    @Query("SELECT * FROM HBNC WHERE processed = 'N' or processed = 'U'")
    suspend fun getAllUnprocessed(): List<HBNCCache>

    @Query("select count(*) from HBNC")
    suspend fun hbncCount(): Int

    @Query("SELECT * FROM HBNC WHERE benId =:benId AND hhId = :hhId and homeVisitDate = :nthDay LIMIT 1")
    suspend fun getHbnc(hhId: Long, benId: Long, nthDay: Int): HBNCCache?

    @Query("SELECT * FROM HBNC WHERE benId =:benId AND hhId = :hhId ORDER BY homeVisitDate")
    fun getAllHbncEntries(hhId: Long, benId: Long): Flow<List<HBNCCache>>

    @Update
    suspend fun setSynced(it: HBNCCache)

    /**
     * get all hbnc records with processed state N(new) or U(un processed)
     */
    @Query("SELECT * FROM HBNC WHERE processed = 'N' or processed = 'U'")
    suspend fun getAllUnprocessedHbnc(): List<HBNCCache>

    @Update
    suspend fun update(it: HBNCCache)

}