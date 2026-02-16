package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenWithLeprosyScreeningCache
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache

@Dao
interface LeprosyDao {
    @Query("SELECT * FROM LEPROSY_SCREENING WHERE benId =:benId limit 1")
    suspend fun getLeprosyScreening(benId: Long): LeprosyScreeningCache?

    @Query("SELECT * FROM LEPROSY_SCREENING WHERE benId =:benId and (homeVisitDate = :visitDate or homeVisitDate = :visitDateGMT) limit 1")
    suspend fun getLeprosyScreening(benId: Long, visitDate: Long, visitDateGMT: Long): LeprosyScreeningCache?

    @Query("SELECT * FROM LEPROSY_SCREENING WHERE  syncState = :syncState")
    suspend fun getLeprosyScreening(syncState: SyncState): List<LeprosyScreeningCache>

    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE syncState = :syncState")
    suspend fun getLeprosyFollowUP(syncState: SyncState): List<LeprosyFollowUpCache>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLeprosyScreening(malariaScreeningCache: LeprosyScreeningCache)

    @Update
    suspend fun updateLeprosyScreening(malariaScreeningCache: LeprosyScreeningCache)



    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE benId = :benId AND visitNumber = :visitNumber")
    suspend fun getFollowUpByVisit(benId: Long, visitNumber: Int): LeprosyFollowUpCache?

    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE benId = :benId ORDER BY visitNumber")
    suspend fun getAllFollowUpsByBenId(benId: Long): List<LeprosyFollowUpCache>

    @Query("DELETE FROM LEPROSY_FOLLOW_UP WHERE benId = :benId AND visitNumber = :visitNumber")
    suspend fun deleteFollowUp(benId: Long, visitNumber: Int)

    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE benId = :benId ORDER BY visitNumber DESC, followUpDate DESC")
    suspend fun getAllFollowUpsForBeneficiary(benId: Long): List<LeprosyFollowUpCache>

    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE benId = :benId AND visitNumber = :visitNumber ORDER BY followUpDate DESC")
    suspend fun getFollowUpsByVisit(benId: Long, visitNumber: Int): List<LeprosyFollowUpCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: LeprosyFollowUpCache)

    @Update
    suspend fun updateFollowUp(followUp: LeprosyFollowUpCache)

    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE benId = :benId AND visitNumber = :visitNumber AND followUpDate = :followUpDate")
    suspend fun getFollowUp(benId: Long, visitNumber: Int, followUpDate: Long): LeprosyFollowUpCache?


    @Query("SELECT * FROM LEPROSY_FOLLOW_UP WHERE benId = :benId AND visitNumber = :visitNumber ORDER BY followUpDate DESC")
    suspend fun getFollowUpsForVisit(benId: Long, visitNumber: Int): List<LeprosyFollowUpCache>


    @Query("SELECT * FROM LEPROSY_FOLLOW_UP")
    suspend fun getAllFollowUpsByBenId(): List<LeprosyFollowUpCache>

    @Query("UPDATE LEPROSY_SCREENING SET syncState = 0 WHERE syncState = 1")
    suspend fun resetScreeningSyncingToUnsynced()

    @Query("UPDATE LEPROSY_FOLLOW_UP SET syncState = 0 WHERE syncState = 1")
    suspend fun resetFollowUpSyncingToUnsynced()
}
