package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache

@Dao
interface TBDao {

    @Query("SELECT * FROM TB_SCREENING WHERE benId =:benId limit 1")
    suspend fun getTbScreening(benId: Long): TBScreeningCache?

    @Query("SELECT * FROM TB_SCREENING WHERE benId =:benId and (visitDate = :visitDate or visitDate = :visitDateGMT) limit 1")
    suspend fun getTbScreening(benId: Long, visitDate: Long, visitDateGMT: Long): TBScreeningCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTbScreening(tbScreeningCache: TBScreeningCache)

    @Query("SELECT * FROM TB_SUSPECTED WHERE benId =:benId limit 1")
    suspend fun getTbSuspected(benId: Long): TBSuspectedCache?

    @Query("""
    SELECT *
    FROM TB_CONFIRMED_TREATMENT
    WHERE benId = :benId
      AND followUpDate IS NOT NULL
    ORDER BY followUpDate DESC
    LIMIT 1
""")    suspend fun getTbConfirmed(benId: Long): TBConfirmedTreatmentCache?

    @Query("""
    SELECT *
    FROM TB_CONFIRMED_TREATMENT
    WHERE benId = :benId
      AND followUpDate IS NOT NULL
    ORDER BY followUpDate DESC
    limit 1
""")    suspend fun getALLTbConfirmed(benId: Long): TBConfirmedTreatmentCache?

    @Query("SELECT * FROM TB_SUSPECTED WHERE benId =:benId and (visitDate = :visitDate or visitDate = :visitDateGMT) limit 1")
    suspend fun getTbSuspected(benId: Long, visitDate: Long, visitDateGMT: Long): TBSuspectedCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTbSuspected(tbSuspectedCache: TBSuspectedCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTbConfirmed(tbConfirmedCache: TBConfirmedTreatmentCache)



    @Query("SELECT * FROM TB_SCREENING WHERE  syncState = :syncState")
    suspend fun getTBScreening(syncState: SyncState): List<TBScreeningCache>

    @Query("SELECT * FROM TB_SUSPECTED WHERE  syncState = :syncState")
    suspend fun getTbSuspected(syncState: SyncState): List<TBSuspectedCache>

    @Query("SELECT * FROM TB_CONFIRMED_TREATMENT WHERE  syncState = :syncState")
    suspend fun getTbConfirmed(syncState: SyncState): List<TBConfirmedTreatmentCache>

    @Query("SELECT * FROM TB_CONFIRMED_TREATMENT WHERE benId = :benId ORDER BY followUpDate DESC")
    suspend fun getAllFollowUpsForBeneficiary(benId: Long): List<TBConfirmedTreatmentCache>

}