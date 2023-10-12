package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.*

@Dao
interface MaternalHealthDao {

    @Query("select * from pregnancy_register where benId = :benId and active = 1 limit 1")
    fun getSavedRecord(benId: Long): PregnantWomanRegistrationCache?
    @Query("select * from pregnancy_register where benId = :benId and active = 1 order by createdDate limit 1")
    fun getSavedActiveRecord(benId: Long): PregnantWomanRegistrationCache?

    @Query("select * from pregnancy_anc where benId = :benId and visitNumber = :visitNumber limit 1")
    fun getSavedRecord(benId: Long, visitNumber: Int): PregnantWomanAncCache?
    @Query("select * from pregnancy_anc where isActive== 1 and benId = :benId")
    fun getAllActiveAncRecords(benId: Long): List<PregnantWomanAncCache>
    @Query("select * from pregnancy_anc where benId in (:benId) and isActive = 1")
    fun getAllActiveAncRecords(benId: Set<Long>): List<PregnantWomanAncCache>

    @Query("select * from pregnancy_register where benId in (:benId)")
    fun getAllActivePwrRecords(benId: Set<Long>): List<PregnantWomanRegistrationCache>

    @Query("select * from pregnancy_anc where benId = :benId order by ancDate desc limit 1")
    fun getLatestAnc(benId: Long): PregnantWomanAncCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(ancCache: PregnantWomanAncCache)

    @Query("select benId, visitNumber, 0 as filledWeek from pregnancy_anc where benId = :benId order by visitNumber")
    suspend fun getAllAncRecordsFor(
        benId: Long,
    ): List<AncStatus>

    @Query("select * from pregnancy_register reg left outer join pregnancy_anc anc on reg.benId=anc.benId where reg.active = 1 and (anc.benId is null or anc.isActive = 1)")
    fun getAllPregnancyRecords(): Flow<Map<PregnantWomanRegistrationCache, List<PregnantWomanAncCache>>>

    @Query("select count(*) from HRP_NON_PREGNANT_ASSESS assess where isHighRisk = 1")
    fun getAllECRecords(): Flow<Int>

    @Query("SELECT * FROM pregnancy_anc WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedAncVisits(): List<PregnantWomanAncCache>

    @Query("SELECT * FROM pregnancy_register WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedPWRs(): List<PregnantWomanRegistrationCache>
    @Update
    suspend fun updateANC(vararg it: PregnantWomanAncCache)

    @Update
    suspend fun updatePwr(it: PregnantWomanRegistrationCache)
    @Query("select * from HRP_NON_PREGNANT_ASSESS assess where ((select count(*) from BEN_BASIC_CACHE b where benId = assess.benId and b.reproductiveStatusId = 1) = 1)")
    fun getAllNonPregnancyAssessRecords(): Flow<List<HRPNonPregnantAssessCache>>

    @Query("select * from HRP_PREGNANT_ASSESS assess where ((select count(*) from BEN_BASIC_CACHE b where benId = assess.benId and b.reproductiveStatusId = 2) = 1)")
    fun getAllPregnancyAssessRecords(): Flow<List<HRPPregnantAssessCache>>
}