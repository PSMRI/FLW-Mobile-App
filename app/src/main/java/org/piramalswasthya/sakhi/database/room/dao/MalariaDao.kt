package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache

@Dao
interface MalariaDao {
    @Query("SELECT * FROM MALARIA_SCREENING WHERE benId =:benId limit 1")
    suspend fun getMalariaScreening(benId: Long): MalariaScreeningCache?

    @Query("SELECT * FROM MALARIA_SCREENING WHERE benId =:benId and (caseDate = :visitDate or caseDate = :visitDateGMT) limit 1")
    suspend fun getMalariaScreening(benId: Long, visitDate: Long, visitDateGMT: Long): MalariaScreeningCache?

    @Query("SELECT * FROM MALARIA_SCREENING WHERE  syncState = :syncState")
    suspend fun getMalariaScreening(syncState: SyncState): List<MalariaScreeningCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMalariaScreening(malariaScreeningCache: MalariaScreeningCache)


    @Query("SELECT * FROM MALARIA_CONFIRMED WHERE benId =:benId limit 1")
    suspend fun getMalariaConfirmed(benId: Long): MalariaConfirmedCasesCache?

    @Query("SELECT * FROM MALARIA_CONFIRMED WHERE benId =:benId and (dateOfDiagnosis = :visitDate or dateOfDiagnosis = :visitDateGMT) limit 1")
    suspend fun getMalariaConfirmed(benId: Long, visitDate: Long, visitDateGMT: Long): MalariaConfirmedCasesCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMalariaConfirmed(tbSuspectedCache: MalariaConfirmedCasesCache)

    @Query("SELECT * FROM MALARIA_CONFIRMED WHERE  syncState = :syncState")
    suspend fun getMalariaConfirmed(syncState: SyncState): List<MalariaConfirmedCasesCache>




    @Query("SELECT * FROM IRS_ROUND WHERE householdId =:hhId limit 1")
    suspend fun getIRSScreening(hhId: Long): IRSRoundScreening?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveIRSScreening(vararg ireScreeningCache: IRSRoundScreening)

    @Update
    suspend fun update(ireScreeningCache: IRSRoundScreening)

    @Query("select * from IRS_ROUND where householdId = :hhId")
    fun getAllActiveIRSRecords(hhId: Long): List<IRSRoundScreening>

    @Update
    suspend fun updateIRS(vararg it: IRSRoundScreening)
}