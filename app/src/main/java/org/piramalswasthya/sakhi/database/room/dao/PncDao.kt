package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.sakhi.model.PNCVisitCache

@Dao
interface PncDao {

    @Query("select * from pnc_visit where benId = :benId and pncPeriod = :visitNumber and isActive = 1 limit 1")
    suspend fun getSavedRecord(benId: Long, visitNumber: Int): PNCVisitCache?

    @Query("select * from pnc_visit where benId = :benId and isActive = 1 order by pncPeriod desc limit 1")
    suspend fun getLastSavedRecord(benId: Long): PNCVisitCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pncCache: PNCVisitCache)

    @Query("SELECT * FROM pnc_visit WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedPncVisits(): List<PNCVisitCache>

    @Update
    suspend fun update(vararg pnc: PNCVisitCache)

    @Query("select * from pnc_visit where benId in (:eligBenIds) and isActive = 1")
    suspend fun getAllPNCs(eligBenIds: Set<Long>): List<PNCVisitCache>


    @Query("SELECT * FROM PNC_VISIT WHERE benId = :benId")
    suspend fun getPncVisitsByBenId(benId: Long): List<PNCVisitCache>

    @Query("UPDATE PNC_VISIT SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}