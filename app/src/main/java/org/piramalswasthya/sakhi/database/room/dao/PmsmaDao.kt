package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache

@Dao
interface PmsmaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg pmsmaCache: PMSMACache)

    @Query("SELECT * FROM PMSMA WHERE processed in ('N','U')")
    suspend fun getAllUnprocessedPmsma(): List<PMSMACache>

    @Query("select count(*) from PMSMA")
    suspend fun pmsmaCount(): Int

    @Query("SELECT * FROM PMSMA WHERE benId =:benId and isActive = 1 LIMIT 1")
    suspend fun getPmsma(benId: Long): PMSMACache?

    @Query("select * from PMSMA where benId = :benId and visitNumber = :visitNumber limit 1")
    fun getSavedRecord(benId: Long, visitNumber: Int): PMSMACache?

    @Query("SELECT COUNT(*) FROM pregnancy_anc WHERE benId IN (:benIds) AND isActive = 1")
    fun getActiveAncCountForBenIds(benIds: Long): Int

    @Query("SELECT * FROM PMSMA WHERE benId = :benId ORDER BY visitDate DESC LIMIT 1")
    fun getLastPmsmaVisit(benId: Long): PMSMACache?



    @Query("SELECT * FROM PMSMA WHERE benId IN(:benId) and isActive = 1")
    suspend fun getAllPmsma(benId: Set<Long>): List<PMSMACache>

    @Update
    fun updatePmsmaRecord(it: PMSMACache)

}