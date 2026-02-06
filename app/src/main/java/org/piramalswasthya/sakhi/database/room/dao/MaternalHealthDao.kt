package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.*

@Dao
interface MaternalHealthDao {

    @Query("select * from pregnancy_register where benId = :benId and active = 1 AND isDraft = 0 limit 1")
    fun getSavedRecord(benId: Long): PregnantWomanRegistrationCache?

    @Query("select * from pregnancy_register where benId = :benId and active = 1 AND isDraft = 1 limit 1")
    suspend fun getDraftPWR(benId: Long): PregnantWomanRegistrationCache?

    @Query("select * from pregnancy_register where benId = :benId and active = 1 order by createdDate limit 1")
    fun getSavedActiveRecord(benId: Long): PregnantWomanRegistrationCache?

    @Query("select * from pregnancy_anc where benId = :benId and visitNumber = :visitNumber AND isDraft = 0 limit 1")
    fun getSavedRecord(benId: Long, visitNumber: Int): PregnantWomanAncCache?

    @Query("select * from pregnancy_anc where benId = :benId AND visitNumber = :visitNumber AND isDraft = 1 limit 1")
    suspend fun getDraftANC(benId: Long, visitNumber: Int): PregnantWomanAncCache?

    @Query("select * from pregnancy_anc where benId = :benId AND isDraft = 0 limit 1")
    fun getSavedRecordANC(benId: Long): PregnantWomanAncCache?

    @Query("select * from pregnancy_anc where isActive== 1 and benId = :benId AND isDraft = 0")
    fun getAllActiveAncRecords(benId: Long): List<PregnantWomanAncCache>

    @Query("SELECT * FROM pregnancy_anc WHERE isActive== 0 and benId = :benId AND isDraft = 0")
    fun getAllInActiveAncRecords(benId: Long): List<PregnantWomanAncCache>

    @Query("select * from pregnancy_anc where benId in (:benId) and isActive = 1 AND isDraft = 0")
    fun getAllActiveAncRecords(benId: Set<Long>): List<PregnantWomanAncCache>

    @Query("select * from pregnancy_register where benId in (:benId) AND isDraft = 0")
    fun getAllActivePwrRecords(benId: Set<Long>): List<PregnantWomanRegistrationCache>

    @Query("select * from pregnancy_anc where benId = :benId AND isDraft = 0 order by ancDate desc limit 1")
    fun getLatestAnc(benId: Long): PregnantWomanAncCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(ancCache: PregnantWomanAncCache)

    @Query("select benId, visitNumber, 0 as filledWeek from pregnancy_anc where benId = :benId AND isDraft = 0 order by visitNumber")
    suspend fun getAllAncRecordsFor(
        benId: Long,
    ): List<AncStatus>

    @Query("select * from pregnancy_register reg left outer join pregnancy_anc anc on reg.benId\u003danc.benId where reg.active \u003d 1 AND reg.isDraft \u003d 0 and (anc.benId is null or (anc.isActive \u003d 1 AND anc.isDraft \u003d 0))")
    fun getAllPregnancyRecords(): Flow<Map<PregnantWomanRegistrationCache, List<PregnantWomanAncCache>>>

    @Query("select count(*) from HRP_NON_PREGNANT_ASSESS assess where isHighRisk \u003d 1")
    fun getAllECRecords(): Flow<Int>

    @Query("SELECT * FROM pregnancy_anc WHERE processed in ('N', 'U') AND isDraft \u003d 0")
    suspend fun getAllUnprocessedAncVisits(): List<PregnantWomanAncCache>

    @Query("SELECT * FROM pregnancy_register WHERE processed in ('N', 'U') AND isDraft \u003d 0")
    suspend fun getAllUnprocessedPWRs(): List<PregnantWomanRegistrationCache>

    @Update
    suspend fun updateANC(vararg it: PregnantWomanAncCache)

    @Update
    suspend fun updatePwr(it: PregnantWomanRegistrationCache)

    @Query("DELETE FROM pregnancy_register WHERE id \u003d :id")
    suspend fun deletePwrById(id: Long)

    @Query("DELETE FROM pregnancy_anc WHERE id \u003d :id")
    suspend fun deleteAncById(id: Long)

    @Query("select * from HRP_NON_PREGNANT_ASSESS assess where ((select count(*) from BEN_BASIC_CACHE b where benId \u003d assess.benId and b.reproductiveStatusId \u003d 1) \u003d 1)")
    fun getAllNonPregnancyAssessRecords(): Flow<List<HRPNonPregnantAssessCache>>

    @Query("select * from HRP_PREGNANT_ASSESS assess where ((select count(*) from BEN_BASIC_CACHE b where benId \u003d assess.benId and b.reproductiveStatusId \u003d 2) \u003d 1)")
    fun getAllPregnancyAssessRecords(): Flow<List<HRPPregnantAssessCache>>
}
