package org.piramalswasthya.sakhi.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.*

@Dao
interface BenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg ben: BenRegCache)

    @Update
    suspend fun updateBen(ben: BenRegCache)

    @Query("UPDATE  BENEFICIARY SET isSpouseAdded = 1 , syncState = :unsynced ,processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND familyHeadRelationPosition = 19")
    suspend fun updateHofSpouseAdded(
        householdId: Long,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET isChildrenAdded = 1 , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND beneficiaryId = :benId"
    )
    suspend fun updateBeneficiaryChildrenAdded(
        householdId: Long,
        benId: Long,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )
    @Query("UPDATE  BENEFICIARY SET isSpouseAdded = 1 , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND beneficiaryId = :benId")
    suspend fun updateBeneficiarySpouseAdded(householdId: Long,benId: Long, unsynced: SyncState,  proccess: String,
                                             updateStatus: Int)

    @Query("SELECT * FROM BENEFICIARY WHERE isDraft = 1 and householdId =:hhId LIMIT 1")
    suspend fun getDraftBenKidForHousehold(hhId: Long): BenRegCache?

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage")
    fun getAllBen(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND abhaId IS NOT NULL")
    fun getAllBenWithAbha(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND abhaId IS NULL")
    fun getAllBenWithoutAbha(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND rchId IS NOT NULL AND rchId != ''")
    fun getAllBenWithRch(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0")
    fun getAllBenAboveThirty(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE villageId = :selectedVillage AND gender = 'Female' AND isDeath = 0 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49 AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2)")
    fun getAllBenWARA(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and gender = :gender")
    fun getAllBenGender(selectedVillage: Int, gender: String): Flow<List<BenBasicCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage and gender = :gender")
    fun getAllBenGenderCount(selectedVillage: Int, gender: String): Flow<Int>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage")
    fun getAllTbScreeningBen(selectedVillage: Int): Flow<List<BenWithTbScreeningCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId")
    fun getAllMalariaScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithMalariaScreeningCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId")
    fun getAllAESScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithAESScreeningCache>>

    @Transaction
    @Query("SELECT * FROM IRS_ROUND where householdId = :hhId")
    fun getAllIRSRoundBen(hhId: Long): Flow<List<IRSRoundScreening>>

    @Transaction
    @Query("SELECT * FROM IRS_ROUND WHERE householdId = :hhId ORDER BY rounds DESC LIMIT 1")
    fun getLastIRSRoundBen(hhId: Long): Flow<IRSRoundScreening?>
    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId")
    fun getAllKALAZARScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithKALAZARScreeningCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId")
    fun getAllLeprosyScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithLeprosyScreeningCache>>


    @Transaction
    @Query("""
    SELECT b.*, l.leprosySymptomsPosition 
    FROM BEN_BASIC_CACHE b 
    INNER JOIN LEPROSY_SCREENING l ON b.benId = l.benId 
    WHERE b.villageId = :selectedVillage
    AND l.leprosySymptomsPosition = :symptomsPosition
    AND l.isConfirmed = 0
""")
    fun getLeprosyScreeningBenBySymptoms(selectedVillage: Int,  symptomsPosition: Int): Flow<List<BenWithLeprosyScreeningCache>>

    @Query("""
    SELECT COUNT(*) 
    FROM BEN_BASIC_CACHE b 
    INNER JOIN LEPROSY_SCREENING l ON b.benId = l.benId 
    WHERE b.villageId = :selectedVillage 
    AND l.leprosySymptomsPosition = :symptomsPosition
    AND l.isConfirmed = 0
""")
    fun getLeprosyScreeningBenCountBySymptoms(selectedVillage: Int, symptomsPosition: Int): Flow<Int>

    @Transaction
    @Query("""
    SELECT b.*, l.isConfirmed
    FROM BEN_BASIC_CACHE b
    INNER JOIN LEPROSY_SCREENING l ON b.benId = l.benId
    WHERE b.villageId = :selectedVillage
      AND l.isConfirmed = 1
""")
    fun getConfirmedLeprosyCases(
        selectedVillage: Int
    ): Flow<List<BenWithLeprosyScreeningCache>>

    @Query("""
    SELECT COUNT(*)
    FROM BEN_BASIC_CACHE b
    INNER JOIN LEPROSY_SCREENING l ON b.benId = l.benId
    WHERE b.villageId = :selectedVillage
      AND l.isConfirmed = 1
""")
    fun getConfirmedLeprosyCaseCount(
        selectedVillage: Int
    ): Flow<Int>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE benId = :benId")
    suspend fun getBenWithLeprosyScreeningAndFollowUps(benId: Long): BenWithLeprosyScreeningCache?
    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId")
    fun getAllFilariaScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithFilariaScreeningCache>>


    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage")
    fun getAllBenCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND abhaId IS NOT NULL")
    fun getAllBenWithAbhaCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND rchId IS NOT NULL AND rchId != ''")
    fun getAllBenWithRchCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE hhId = :hhId")
    fun getAllBasicBenForHousehold(hhId: Long): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BENEFICIARY WHERE householdId = :hhId")
    suspend fun getAllBenForHousehold(hhId: Long): List<BenRegCache>

    @Query("SELECT * FROM BENEFICIARY WHERE beneficiaryId =:benId AND householdId = :hhId LIMIT 1")
    suspend fun getBen(hhId: Long, benId: Long): BenRegCache?

    @Query("SELECT * FROM BENEFICIARY WHERE beneficiaryId =:benId LIMIT 1")
    suspend fun getBen(benId: Long): BenRegCache?

    @Query("SELECT EXISTS(SELECT 1 FROM BENEFICIARY WHERE beneficiaryId = :benId AND isDeath = 1)")
    suspend fun isBenDead(benId: Long): Boolean


    @Query("UPDATE BENEFICIARY SET syncState = :syncState WHERE beneficiaryId =:benId AND householdId = :hhId")
    suspend fun setSyncState(hhId: Long, benId: Long, syncState: SyncState)

    @Query("DELETE FROM BENEFICIARY WHERE householdId = :hhId and isKid = :kid")
    suspend fun deleteBen(hhId: Long, kid: Boolean)

    @Query("UPDATE BENEFICIARY SET beneficiaryId = :newId, benRegId = :benRegId WHERE householdId = :hhId AND beneficiaryId =:oldId")
    suspend fun substituteBenId(hhId: Long, oldId: Long, newId: Long, benRegId: Long)

    @Query("UPDATE BENEFICIARY SET serverUpdatedStatus = 1 , beneficiaryId = :newId , benRegId =:newBenRegId ,processed = 'U', userImage = :imageUri  WHERE householdId = :hhId AND beneficiaryId =:oldId")
    suspend fun updateToFinalBenId(hhId: Long, oldId: Long, newId: Long, imageUri: String? = null,newBenRegId:Long)

    @Query("SELECT * FROM BENEFICIARY WHERE isDraft = 0 AND processed = 'N' AND syncState =:unsynced ")
    suspend fun getAllUnprocessedBen(unsynced: SyncState = SyncState.UNSYNCED): List<BenRegCache>

    @Query("SELECT * FROM BENEFICIARY WHERE isDraft = 0 AND (processed = 'N' OR processed = 'U') AND syncState =:unsynced ")
    suspend fun getAllUnsyncedBen(unsynced: SyncState = SyncState.UNSYNCED): List<BenRegCache>

    @Query("SELECT COUNT(*) FROM BENEFICIARY WHERE isDraft = 0 AND (processed = 'N' OR processed = 'U') AND syncState =0")
    fun getUnProcessedRecordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM BENEFICIARY WHERE isDraft = 0 AND (processed = 'N' OR processed = 'U') AND syncState =0")
    fun getAllUnProcessedRecordCount(): Flow<Int>

//    @Query("SELECT * FROM BENEFICIARY WHERE isDraft = 0 AND processed = 'U' AND syncState =:unsynced AND isConsent = 1")
    @Query("SELECT * FROM BENEFICIARY WHERE isDraft = 0 AND processed = 'U' AND syncState =:unsynced")
    suspend fun getAllBenForSyncWithServer(unsynced: SyncState = SyncState.UNSYNCED): List<BenRegCache>

    @Query("UPDATE BENEFICIARY SET processed = 'P' , syncState = 2 WHERE beneficiaryId in (:benId)")
    suspend fun benSyncedWithServer(vararg benId: Long)

    @Query("UPDATE BENEFICIARY SET processed = 'U' , syncState = 0 WHERE beneficiaryId in (:benId)")
    suspend fun benSyncWithServerFailed(vararg benId: Long)

    @Query("SELECT beneficiaryId FROM BENEFICIARY WHERE beneficiaryId IN (:list)")
    fun getAllBeneficiaryFromList(list: List<Long>): LiveData<List<Long>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and reproductiveStatusId = 1  and villageId=:selectedVillage")
    fun getAllEligibleCoupleList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForEligibleCouple, max: Int = Konstants.maxAgeForEligibleCouple
    ): Flow<List<BenBasicCache>>


//    @Query("SELECT b.benId as ecBenId,b.*, r.noOfLiveChildren as numChildren , t.* FROM ben_basic_cache b join eligible_couple_reg r on b.benId=r.benId left outer join eligible_couple_tracking t on t.benId=b.benId WHERE CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and b.reproductiveStatusId = 1 and  b.villageId=:selectedVillage group by b.benId")
    @Transaction
//    @Query("SELECT b.* FROM ben_basic_cache b join eligible_couple_reg r on b.benId=r.benId  LEFT JOIN pregnancy_anc a ON b.benId = a.benId WHERE CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and b.reproductiveStatusId = 1 and  b.villageId=:selectedVillage and isDeath = 0 or isDeath is NULL group by b.benId")
    @Query("SELECT b.* FROM ben_basic_cache b JOIN eligible_couple_reg r ON b.benId = r.benId LEFT JOIN pregnancy_anc a ON b.benId = a.benId WHERE CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min AND :max AND b.reproductiveStatusId = 1 AND b.villageId = :selectedVillage AND (b.isDeath = 0 OR b.isDeath IS NULL) GROUP BY b.benId")
    fun getAllEligibleTrackingList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForEligibleCouple, max: Int = Konstants.maxAgeForEligibleCouple
    ): Flow<List<BenWithEcTrackingCache>>

//    @Transaction
//    @Query("""
//    SELECT b.*
//    FROM ben_basic_cache b
//    JOIN eligible_couple_reg r ON b.benId = r.benId
//    LEFT JOIN pregnancy_anc a ON b.benId = a.benId
//    WHERE CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min AND :max
//      AND b.reproductiveStatusId = 1
//      AND b.villageId = :selectedVillage
//      AND (
//            (b.isDeath = 0 OR b.isDeath IS NULL)
//            OR (a.isAborted = 1)
//          )
//    GROUP BY b.benId
//""")
//    fun getAllEligibleTrackingList(
//        selectedVillage: Int,
//        min: Int = Konstants.minAgeForEligibleCouple,
//        max: Int = Konstants.maxAgeForEligibleCouple
//    ): Flow<List<BenWithEcTrackingCache>>


    @Transaction
    @Query("SELECT * FROM ben_basic_cache WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and reproductiveStatusId = 1 and  villageId=:selectedVillage and isDeath = 0 or isDeath is NULL group by benId")
    fun getAllEligibleRegistrationList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForEligibleCouple, max: Int = Konstants.maxAgeForEligibleCouple
    ): Flow<List<BenWithECRCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and reproductiveStatusId = 1 and villageId=:selectedVillage")
    fun getAllEligibleCoupleListCount(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForEligibleCouple, max: Int = Konstants.maxAgeForEligibleCouple
    ): Flow<Int>

    @Transaction
    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben left outer join pregnancy_register  pr on pr.benId = ben.benId  WHERE reproductiveStatusId = 2 and (pr.benId is null or pr.active = 1) and villageId=:selectedVillage")
    fun getAllPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithPwrCache>>

    @Transaction
    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben left outer join pregnancy_register  pr on pr.benId = ben.benId  WHERE reproductiveStatusId = 2 and (pr.benId is null or pr.active = 1) and villageId=:selectedVillage and ben.rchId is not null and ben.rchId != ''")
    fun getAllPregnancyWomenWithRchList(selectedVillage: Int): Flow<List<BenWithPwrCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 2 and villageId=:selectedVillage")
    fun getAllPregnancyWomenForHRList(selectedVillage: Int): Flow<List<BenWithHRPACache>>

    @Transaction
    @Query("SELECT count(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 2 and villageId=:selectedVillage")
    fun getAllPregnancyWomenForHRListCount(selectedVillage: Int): Flow<Int>


    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 2 and villageId=:selectedVillage")
    fun getAllPregnancyWomenListCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben  inner join pregnancy_register pwr on pwr.benId = ben.benId inner join pregnancy_anc anc on ben.benId = anc.benId WHERE ben.reproductiveStatusId =3 and anc.pregnantWomanDelivered =1 and anc.isActive = 1 and pwr.active = 1 and villageId=:selectedVillage group by ben.benId order by anc.updatedDate desc ")
    fun getAllDeliveredWomenList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE ben  inner join pregnancy_register pwr on pwr.benId = ben.benId inner join pregnancy_anc anc on ben.benId = anc.benId WHERE ben.reproductiveStatusId =3 and anc.pregnantWomanDelivered =1 and anc.isActive = 1 and pwr.active = 1 and villageId=:selectedVillage")
    fun getAllDeliveredWomenListCount(selectedVillage: Int): Flow<Int>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 1 and gender = 'FEMALE' and villageId=:selectedVillage")
    fun getAllNonPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithHRNPACache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 1 and gender = 'FEMALE' and villageId=:selectedVillage")
    fun getAllNonPregnancyWomenListCount(selectedVillage: Int): Flow<Int>

    @Transaction
    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben inner join delivery_outcome do on do.benId = ben.benId where do.isActive = 1 and villageId=:selectedVillage")
    fun getListForInfantRegister(selectedVillage: Int): Flow<List<BenWithDoAndIrCache>>

    @Query(""" SELECT SUM(do.liveBirth)
    FROM delivery_outcome do
    INNER JOIN BEN_BASIC_CACHE ben ON do.benId = ben.benId
    WHERE do.isActive = 1 AND do.liveBirth > 0 AND ben.villageId = :selectedVillage """)
    fun getInfantRegisterCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT * FROM BEN_BASIC_CACHE  WHERE pwHrp = 1 and villageId=:selectedVillage")
    fun getAllWomenListForPmsma(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE pwHrp = 1 and villageId=:selectedVillage")
    fun getAllWomenListForPmsmaCount(selectedVillage: Int): Flow<Int>

    @Transaction
    @Query("SELECT ben.*  from BEN_BASIC_CACHE  ben inner join pregnancy_register pwr on pwr.benId = ben.benId where pwr.active = 1 and ben.reproductiveStatusId=2 and ben.villageId=:selectedVillage group by ben.benId")
    fun getAllRegisteredPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>

    @Transaction
    @Query("""
    SELECT ben.*  
    FROM BEN_BASIC_CACHE ben
    INNER JOIN pregnancy_register pwr ON pwr.benId = ben.benId
    WHERE pwr.active = 1 
      AND ben.reproductiveStatusId = 2  
      AND ben.villageId = :selectedVillage
        AND (ben.isDeath = 0 OR ben.isDeath IS NULL)
      AND (ben.benId IN (SELECT benId FROM PMSMA WHERE highriskSymbols = 1)
           OR ben.benId IN (SELECT benId FROM PREGNANCY_ANC WHERE anyHighRisk = 1))
    GROUP BY ben.benId
""")
    fun getAllHighRiskPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>



    @Transaction
    @Query("""
    SELECT * 
    FROM BEN_BASIC_CACHE 
    WHERE reproductiveStatusId = 2 
      AND villageId = :selectedVillage
""")
    fun getAllRegisteredPmsmaWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>


    @Transaction
    @Query("SELECT ben.*  from BEN_BASIC_CACHE  ben inner join pregnancy_anc pwr on pwr.benId = ben.benId where pwr.isAborted = 1  and ben.villageId=:selectedVillage group by ben.benId")
    fun getAllAbortionWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>

    @Query("""
    SELECT * FROM BEN_BASIC_CACHE 
    WHERE 
        CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 15
        AND isDeath = 1
        AND (reasonOfDeath IS NULL OR reasonOfDeath != 'Maternal Death')
        AND villageId = :selectedVillage
""")
    fun getAllGeneralDeathsList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("""
    SELECT * FROM BEN_BASIC_CACHE 
    WHERE 
        gender = 'FEMALE'
        AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 15 AND 49
        AND isDeath = 1
        AND (reasonOfDeath IS NULL OR reasonOfDeath != 'Maternal Death')
        AND villageId = :selectedVillage
     """)
    fun getAllNonMaternalDeathsList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("""
    SELECT COUNT(*) FROM BEN_BASIC_CACHE 
    WHERE 
        CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 15
        AND isDeath = 1
        AND (reasonOfDeath IS NULL OR reasonOfDeath != 'Maternal Death')
        AND villageId = :selectedVillage
""")
    fun getAllGeneralDeathsCount(selectedVillage: Int): Flow<Int>

    @Query("""
    SELECT COUNT(*) FROM BEN_BASIC_CACHE 
    WHERE 
        gender = 'FEMALE'
        AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 15 AND 49
        AND isDeath = 1
        AND (reasonOfDeath IS NULL OR reasonOfDeath != 'Maternal Death')
        AND villageId = :selectedVillage
""")
    fun getAllNonMaternalDeathsCount(selectedVillage: Int): Flow<Int>
    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE  ben inner join pregnancy_register pwr on pwr.benId = ben.benId where pwr.active = 1 and ben.reproductiveStatusId=2 and ben.villageId=:selectedVillage")
    fun getAllRegisteredPregnancyWomenListCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE  ben inner join pregnancy_anc pwr on pwr.benId = ben.benId where pwr.isAborted = 1 and ben.villageId=:selectedVillage")
    fun getAllAbortionWomenListCount(selectedVillage: Int): Flow<Int>

    @Query("""
    SELECT COUNT(DISTINCT b.benId)
    FROM BEN_BASIC_CACHE b
    INNER JOIN pregnancy_register pwr ON pwr.benId = b.benId
    LEFT JOIN PMSMA p ON b.benId = p.benId AND p.highriskSymbols = 1
    LEFT JOIN PREGNANCY_ANC a ON b.benId = a.benId AND a.anyHighRisk = 1
    WHERE b.villageId = :selectedVillage
      AND pwr.active = 1
      AND b.reproductiveStatusId = 2
      AND (p.benId IS NOT NULL OR a.benId IS NOT NULL)
""")
    fun getHighRiskWomenCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE  ben inner join pregnancy_anc pwr on pwr.benId = ben.benId where pwr.isActive = 1 and (pwr.ancDate + :nonFollowUpDuration) <= :currentTime and pwr.ancDate > (:currentTime - :year) and ben.reproductiveStatusId = 2 and ben.villageId = :selectedVillage")
    fun getAllRegisteredPregnancyWomenNonFollowUpListCount(
        selectedVillage: Int,
        nonFollowUpDuration: Long = Konstants.nonFollowUpDuration,
        year: Long = Konstants.minMillisBwtweenCbacFiling,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<Int>

    @Query("SELECT * FROM BEN_BASIC_CACHE where  CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER)  >= :min and villageId=:selectedVillage")
    fun getAllNCDList(
        selectedVillage: Int, min: Int = Konstants.minAgeForNcd
    ): Flow<List<BenBasicCache>>


    @Query("SELECT b.* FROM BEN_BASIC_CACHE b LEFT OUTER JOIN CBAC c ON b.benId=c.benId where CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER)  >= :min and c.benId IS NULL and b.villageId=:selectedVillage")
    fun getAllNCDEligibleList(
        selectedVillage: Int, min: Int = Konstants.minAgeForNcd
    ): Flow<List<BenBasicCache>>

    @Query("SELECT b.* FROM BEN_BASIC_CACHE b INNER JOIN CBAC c on b.benId==c.benId WHERE c.total_score >= 4 and b.villageId=:selectedVillage")
    fun getAllNCDPriorityList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT b.* FROM BEN_BASIC_CACHE b INNER JOIN CBAC c on b.benId==c.benId WHERE c.total_score < 4 and b.villageId=:selectedVillage")
    fun getAllNCDNonEligibleList(selectedVillage: Int): Flow<List<BenBasicCache>>

    // have to add those as well who we are adding to menopause entries manually from app
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 5 and villageId=:selectedVillage")
    fun getAllMenopauseStageList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE gender = :female and CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage")
    fun getAllReproductiveAgeList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForReproductiveAge,
        max: Int = Konstants.maxAgeForReproductiveAge,
        female: Gender = Gender.FEMALE
    ): Flow<List<BenBasicCache>>

    @Transaction
    //@Query("SELECT ben.* FROM BEN_BASIC_CACHE ben left outer join delivery_outcome del on ben.benId = del.benId left outer join pnc_visit pnc on pnc.benId = ben.benId WHERE reproductiveStatusId = 3 and (pnc.isActive is null or pnc.isActive == 1) and CAST((strftime('%s','now') - del.dateOfDelivery/1000)/60/60/24 AS INTEGER) BETWEEN :minPncDate and :maxPncDate and  villageId=:selectedVillage group by ben.benId")
    //@Query("SELECT ben.* FROM BEN_BASIC_CACHE ben LEFT OUTER JOIN delivery_outcome del ON ben.benId = del.benId LEFT OUTER JOIN pnc_visit pnc ON pnc.benId = ben.benId WHERE reproductiveStatusId = 3 AND (pnc.isActive IS NULL OR pnc.isActive == 1) AND ( del.dateOfDelivery IS NULL OR CAST((strftime('%s','now') - COALESCE(del.dateOfDelivery, 0)/1000)/60/60/24 AS INTEGER) BETWEEN :minPncDate AND :maxPncDate) AND (:selectedVillage IS NULL OR villageId = :selectedVillage) GROUP BY ben.benId")

    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben LEFT OUTER JOIN delivery_outcome del ON ben.benId = del.benId LEFT OUTER JOIN pnc_visit pnc ON pnc.benId = ben.benId WHERE reproductiveStatusId = 3 AND (pnc.isActive IS NULL OR pnc.isActive = 1) AND (pnc.pncPeriod IS NULL OR pnc.pncPeriod != 42)  AND (ben.isDeath IS NULL OR ben.isDeath = 0) AND (:selectedVillage IS NULL OR villageId = :selectedVillage) GROUP BY ben.benId")
    fun getAllPNCMotherList(
        selectedVillage: Int
//        minPncDate: Long = 0,
//        maxPncDate: Long = Konstants.pncEcGap
    ): Flow<List<BenWithDoAndPncCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) <= :max and villageId=:selectedVillage")
    fun getAllInfantList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForInfant
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE benId = :benId LIMIT 1")
    suspend fun getBenById(benId: Long): BenBasicCache?

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) <= :max and villageId=:selectedVillage and rchId is not null and rchId != ''")
    fun getAllInfantWithRchList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForInfant
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage")
    fun getAllChildList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForChild,
        max: Int = Konstants.maxAgeForChild
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage and rchId is not null and rchId != ''")
    fun getAllChildWithRchList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForChild,
        max: Int = Konstants.maxAgeForChild
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage")
    fun getAllAdolescentList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForAdolescent,
        max: Int = Konstants.maxAgeForAdolescentlist
    ): Flow<List<BenWithAdolescentCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE isKid = 1 or reproductiveStatusId in (2, 3) and villageId=:selectedVillage")
    fun getAllImmunizationDueList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE hrpStatus = 1 and villageId=:selectedVillage")
    fun getAllHrpCasesList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 4 and hhId = :hhId")
    suspend fun getAllPNCMotherListFromHousehold(hhId: Long): List<BenBasicCache>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) <= :max and villageId=:selectedVillage AND isDeath = 1" )
    fun getAllCDRList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForCdr
    ): Flow<List<BenBasicCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) <= :max and villageId=:selectedVillage AND isDeath = 1" )
    fun getAllCDRListCount(
        selectedVillage: Int, max: Int = Konstants.maxAgeForCdr
    ): Flow<Int>


    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId in (2, 3, 4) and isMdsr = 1 and villageId=:selectedVillage")
    fun getAllMDSRList(selectedVillage: Int): Flow<List<BenWithAncDoPncCache>>
    @Transaction
    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId in (2, 3, 4) and isMdsr = 1 and villageId=:selectedVillage")
    fun getAllMDSRCount(selectedVillage: Int): Flow<Int>
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER)<=:max and villageId=:selectedVillage")
    fun getAllChildrenImmunizationList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForAdolescentlist
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 4 and villageId=:selectedVillage")
    fun getAllMotherImmunizationList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("select ben.* from ben_basic_cache ben inner join  pregnancy_register pwr on ben.benId = pwr.benId left  outer join pregnancy_anc pwa on ben.benId = pwa.benId where ben.villageId = :villageId and pwr.isHrp =1 or pwa.hrpConfirmed = 1 order by pwa.visitNumber desc ")
    fun getHrpCases(villageId: Int): Flow<List<BenBasicCache>>

    @Query("select * from BEN_BASIC_CACHE b inner join tb_screening t on  b.benId = t.benId where villageId = :villageId and tbsnFilled = 1 and (t.bloodInSputum =1 or t.coughMoreThan2Weeks = 1 or feverMoreThan2Weeks = 1 or nightSweats = 1 or lossOfWeight = 1 or historyOfTb = 1)")
    fun getScreeningList(villageId: Int): Flow<List<BenBasicCache>>

    @Transaction
    @Query("select b.* from BEN_BASIC_CACHE b inner join tb_screening t on  b.benId = t.benId where villageId = :villageId and tbsnFilled = 1 and (t.bloodInSputum =1 or t.coughMoreThan2Weeks = 1 or feverMoreThan2Weeks = 1 or nightSweats = 1 or lossOfWeight = 1 or historyOfTb = 1)")
    fun getTbScreeningList(villageId: Int): Flow<List<BenWithTbSuspectedCache>>

    @Transaction
    @Query("select b.*, t.slideTestName As slideTestName from BEN_BASIC_CACHE b inner join malaria_screening t on  b.benId = t.benId where villageId = :villageId  and t.caseStatus = 'Confirmed' ")
    fun getMalariaConfirmedCasesList(villageId: Int): Flow<List<BenWithMalariaConfirmedCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE b where CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER)  >= :min and b.reproductiveStatusId!=2 and b.villageId=:selectedVillage group by b.benId order by b.regDate desc")
    fun getBenWithCbac(
        selectedVillage: Int, min: Int = Konstants.minAgeForNcd
    ): Flow<List<BenWithCbacCache>>



    @Transaction
    @Query("""
   SELECT DISTINCT b.*
    FROM BEN_BASIC_CACHE b
    INNER JOIN CBAC c ON b.benId = c.benId
    INNER JOIN NCD_REFER r ON b.benId = r.benId
    WHERE c.isReffered = 1
      AND CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) >= :min
      AND b.reproductiveStatusId != 2
      AND b.villageId = :selectedVillage
    ORDER BY b.regDate DESC
""")
    fun getBenWithReferredCbac(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForNcd
    ): Flow<List<BenWithCbacAndReferalCache>>


    @Query("""
  SELECT COUNT(DISTINCT b.benId)
    FROM BEN_BASIC_CACHE b
    INNER JOIN CBAC c ON b.benId = c.benId
    WHERE c.isReffered = 1
      AND CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) >= :min
      AND b.reproductiveStatusId != 2
      AND b.villageId = :selectedVillage
""")
     fun getReferredBenCount(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForNcd
    ): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE b where CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER)  >= :min and b.reproductiveStatusId!=2 and b.villageId=:selectedVillage")
    fun getBenWithCbacCount(
        selectedVillage: Int, min: Int = Konstants.minAgeForNcd
    ): Flow<Int>

    @Query("select min(beneficiaryId) from beneficiary")
    suspend fun getMinBenId(): Long?


    @Transaction
    @Query("select * from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 2 and gender = 'FEMALE' and benId in (select benId from HRP_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingPregList(villageId: Int): Flow<List<BenWithHRPTrackingCache>>

    @Transaction
    @Query("select * from BEN_BASIC_CACHE where benId = :benId;")
    fun getHRPTrackingPregForBen(benId: Long): BenWithHRPTrackingCache

    @Query("select count(*) from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 2 and gender = 'FEMALE' and benId in (select benId from HRP_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingPregListCount(villageId: Int): Flow<Int>

    @Transaction
    @Query("select * from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 1 and gender = 'FEMALE' and benId in (select benId from HRP_NON_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingNonPregList(villageId: Int): Flow<List<BenWithHRNPTrackingCache>>

    @Query("select count(*) from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 1 and gender = 'FEMALE' and benId in (select benId from HRP_NON_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingNonPregListCount(villageId: Int): Flow<Int>

    @Query("select count(*) from INFANT_REG inf join ben_basic_cache ben on ben.benId = inf.motherBenId where isActive = 1 and weight < :lowWeightLimit and  ben.villageId = :villageId")
    fun getLowWeightBabiesCount(
        villageId: Int,
        lowWeightLimit: Double = Konstants.babyLowWeight
    ): Flow<Int>

    // Pregnancy Death
    @Query("SELECT COUNT(*) FROM PREGNANCY_ANC WHERE benId = :benId AND deathDate IS NOT NULL AND isAborted = 0")
    suspend fun checkPregnancyDeath(benId: Long): Boolean


    // Abortion Death
    @Query("SELECT COUNT(*) FROM PREGNANCY_ANC WHERE benId = :benId AND deathDate IS NOT NULL AND isAborted = 1")
    suspend fun checkAbortionDeath(benId: Long): Boolean

    // Delivery Outcome
    @Query("SELECT COUNT(*) FROM DELIVERY_OUTCOME WHERE benId = :benId AND dateOfDeath IS NOT NULL")
    suspend fun checkDeliveryDeath(benId: Long): Boolean

    // PNC
    @Query("SELECT COUNT(*) FROM PNC_VISIT WHERE benId = :benId AND deathDate IS NOT NULL")
    suspend fun checkPncDeath(benId: Long): Boolean

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM PNC_VISIT
        WHERE benId = :benId
        AND deathDate IS NOT NULL
        AND causeOfDeath = :cause
    )
""")
    suspend fun isDeathByCause(benId: Long, cause: String): Boolean

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM PREGNANCY_ANC
        WHERE benId = :benId
        AND deathDate IS NOT NULL
        AND maternalDeathProbableCause = :cause
    )
""")
    suspend fun isDeathByCauseAnc(benId: Long,cause: String): Boolean


}