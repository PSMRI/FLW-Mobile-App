package org.piramalswasthya.sakhi.database.room.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
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

    @Query("UPDATE  BENEFICIARY SET syncState = :unsynced ,processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId")
    suspend fun updateBenToSync(
        householdId: Long,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

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

    @Query(
        "UPDATE BENEFICIARY " +
                "SET fatherName = :benName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND fatherName = :parentName AND (familyHeadRelationPosition = 9 OR familyHeadRelationPosition = 10)"
    )
    suspend fun updateFatherInChildren(
        benName: String,
        householdId: Long,
        parentName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET motherName = :benName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND motherName = :parentName AND (familyHeadRelationPosition = 9 OR familyHeadRelationPosition = 10)"
    )
    suspend fun updateMotherInChildren(
        benName: String,
        householdId: Long,
        parentName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET gen_spouseName = :benName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE gen_maritalStatusId = 2 AND householdId = :householdId AND gen_spouseName = :spouseName AND (familyHeadRelationPosition = 5 OR familyHeadRelationPosition = 6)"
    )
    suspend fun updateSpouseOfHoF(
        benName: String,
        householdId: Long,
        spouseName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET gen_marriageDate = :marriageDate , gen_ageAtMarriage = :ageAtMarriage, syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND gen_spouseName = :spouseName"
    )
    suspend fun updateMarriageAgeOfWife(
        marriageDate: Long,
        ageAtMarriage: Int,
        householdId: Long,
        spouseName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET gen_marriageDate = :marriageDate , gen_ageAtMarriage = :ageAtMarriage, syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND gen_spouseName = :spouseName"
    )
    suspend fun updateMarriageAgeOfHusband(
        marriageDate: Long,
        ageAtMarriage: Int,
        householdId: Long,
        spouseName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET gen_spouseName = :benName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE gen_maritalStatusId = 2 AND householdId = :householdId AND gen_spouseName = :spouseName"
    )
    suspend fun updateSpouse(
        benName: String,
        householdId: Long,
        spouseName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET fatherName = :benName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND fatherName = :parentName"
    )
    suspend fun updateFather(
        benName: String,
        householdId: Long,
        parentName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET motherName = :benName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND motherName = :parentName"
    )
    suspend fun updateMother(
        benName: String,
        householdId: Long,
        parentName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET firstName = :babyName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND motherName = :parentName AND age < 1"
    )
    suspend fun updateBabyName(
        babyName: String,
        householdId: Long,
        parentName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET lastName = :lastName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE gen_maritalStatusId = 2 AND householdId = :householdId AND gen_spouseName = :spouseName"
    )
    suspend fun updateSpouseLastName(
        lastName: String,
        householdId: Long,
        spouseName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query(
        "UPDATE BENEFICIARY " +
                "SET lastName = :lastName , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND fatherName = :parentName"
    )
    suspend fun updateChildrenLastName(
        lastName: String,
        householdId: Long,
        parentName: String,
        unsynced: SyncState,
        proccess: String,
        updateStatus: Int
    )

    @Query("UPDATE  BENEFICIARY SET isSpouseAdded = 1 , syncState = :unsynced , processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId AND beneficiaryId = :benId")
    suspend fun updateBeneficiarySpouseAdded(householdId: Long,benId: Long, unsynced: SyncState,  proccess: String,
                                             updateStatus: Int)

    @Query("SELECT * FROM BENEFICIARY WHERE isDraft = 1 and householdId =:hhId LIMIT 1")
    suspend fun getDraftBenKidForHousehold(hhId: Long): BenRegCache?

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND isDeactivate = 0")
    fun getAllBen(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("""
        SELECT * FROM BEN_BASIC_CACHE
        WHERE villageId = :selectedVillage
        AND isDeactivate = 0
        AND (:source = 0
            OR (:source = 1 AND abhaId IS NOT NULL)
            OR (:source = 2 AND rchId IS NOT NULL AND rchId != '')
            OR (:source = 3 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0)
            OR (:source = 4 AND gender = 'Female' AND isDeath = 0
                AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49
                AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2))
        )
        AND (:filterType = 0
            OR (:filterType = 1 AND abhaId IS NOT NULL)
            OR (:filterType = 2 AND abhaId IS NULL)
            OR (:filterType = 3 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0)
            OR (:filterType = 4 AND gender = 'Female' AND isDeath = 0
                AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49
                AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2))
        )
        AND (:query = '' OR
            benName LIKE '%' || :query || '%'
            OR benSurname LIKE '%' || :query || '%'
            OR CAST(mobileNo AS TEXT) LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR REPLACE(IFNULL(abhaId, ''), '-', '') LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR IFNULL(familyHeadName, '') LIKE '%' || :query || '%'
            OR IFNULL(spouseName, '') LIKE '%' || :query || '%'
            OR IFNULL(fatherName, '') LIKE '%' || :query || '%'
            OR CAST(benId AS TEXT) LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR CAST(hhId AS TEXT) LIKE '%' || :query || '%'
            OR IFNULL(rchId, '') LIKE '%' || REPLACE(:query, ' ', '') || '%'
        )
    """)
    fun searchBen(selectedVillage: Int, source: Int, filterType: Int, query: String): Flow<List<BenBasicCache>>

    @Query("""
        SELECT * FROM BEN_BASIC_CACHE
        WHERE villageId = :selectedVillage
        AND isDeactivate = 0
        AND (:source = 0
            OR (:source = 1 AND abhaId IS NOT NULL)
            OR (:source = 2 AND rchId IS NOT NULL AND rchId != '')
            OR (:source = 3 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0)
            OR (:source = 4 AND gender = 'Female' AND isDeath = 0
                AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49
                AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2))
        )
        AND (:filterType = 0
            OR (:filterType = 1 AND abhaId IS NOT NULL)
            OR (:filterType = 2 AND abhaId IS NULL)
            OR (:filterType = 3 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0)
            OR (:filterType = 4 AND gender = 'Female' AND isDeath = 0
                AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49
                AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2))
        )
        AND (:query = '' OR
            benName LIKE '%' || :query || '%'
            OR benSurname LIKE '%' || :query || '%'
            OR CAST(mobileNo AS TEXT) LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR REPLACE(IFNULL(abhaId, ''), '-', '') LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR IFNULL(familyHeadName, '') LIKE '%' || :query || '%'
            OR IFNULL(spouseName, '') LIKE '%' || :query || '%'
            OR IFNULL(fatherName, '') LIKE '%' || :query || '%'
            OR CAST(benId AS TEXT) LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR CAST(hhId AS TEXT) LIKE '%' || :query || '%'
            OR IFNULL(rchId, '') LIKE '%' || REPLACE(:query, ' ', '') || '%'
        )
        ORDER BY CASE
            WHEN isDeath = 0 THEN 0
            WHEN isDeath = 1 THEN 1
            ELSE 2
        END
    """)
    fun searchBenPaged(selectedVillage: Int, source: Int, filterType: Int, query: String): PagingSource<Int, BenBasicCache>

    @Query("""
        SELECT * FROM BEN_BASIC_CACHE
        WHERE villageId = :selectedVillage
        AND isDeactivate = 0
        AND (:source = 0
            OR (:source = 1 AND abhaId IS NOT NULL)
            OR (:source = 2 AND rchId IS NOT NULL AND rchId != '')
            OR (:source = 3 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0)
            OR (:source = 4 AND gender = 'Female' AND isDeath = 0
                AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49
                AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2))
        )
        AND (:filterType = 0
            OR (:filterType = 1 AND abhaId IS NOT NULL)
            OR (:filterType = 2 AND abhaId IS NULL)
            OR (:filterType = 3 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0)
            OR (:filterType = 4 AND gender = 'Female' AND isDeath = 0
                AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49
                AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2))
        )
        AND (:query = '' OR
            benName LIKE '%' || :query || '%'
            OR benSurname LIKE '%' || :query || '%'
            OR CAST(mobileNo AS TEXT) LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR REPLACE(IFNULL(abhaId, ''), '-', '') LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR IFNULL(familyHeadName, '') LIKE '%' || :query || '%'
            OR IFNULL(spouseName, '') LIKE '%' || :query || '%'
            OR IFNULL(fatherName, '') LIKE '%' || :query || '%'
            OR CAST(benId AS TEXT) LIKE '%' || REPLACE(:query, ' ', '') || '%'
            OR CAST(hhId AS TEXT) LIKE '%' || :query || '%'
            OR IFNULL(rchId, '') LIKE '%' || REPLACE(:query, ' ', '') || '%'
        )
       ORDER BY CASE
    WHEN isDeath = 0 AND isDeactivate = 0 THEN 0
    WHEN isDeath = 1 AND isDeactivate = 0 THEN 1
    WHEN isDeactivate = 1 THEN 2
    ELSE 4
    END ASC
    """)
    suspend fun searchBenOnce(selectedVillage: Int, source: Int, filterType: Int, query: String): List<BenBasicCache>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND abhaId IS NOT NULL AND isDeactivate = 0")
    fun getAllBenWithAbha(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND abhaId IS NULL and isDeactivate=0")
    fun getAllBenWithoutAbha(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND rchId IS NOT NULL AND rchId != '' AND isDeactivate = 0")
    fun getAllBenWithRch(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND  isDeactivate = 0 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 30 AND isDeath = 0")
    fun getAllBenAboveThirty(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE villageId = :selectedVillage AND isDeactivate=0 AND gender = 'Female' AND isDeath = 0 AND CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN 20 AND 49 AND (reproductiveStatusId = 1 OR reproductiveStatusId = 2)")
    fun getAllBenWARA(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and gender = :gender and isDeactivate=0")
    fun getAllBenGender(selectedVillage: Int, gender: String): Flow<List<BenBasicCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage and gender = :gender and isDeactivate=0")
    fun getAllBenGenderCount(selectedVillage: Int, gender: String): Flow<Int>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and isDeactivate=0")
    fun getAllTbScreeningBen(selectedVillage: Int): Flow<List<BenWithTbScreeningCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId  and isDeactivate=0")
    fun getAllMalariaScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithMalariaScreeningCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId and isDeactivate=0")
    fun getAllAESScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithAESScreeningCache>>

    @Transaction
    @Query("SELECT * FROM IRS_ROUND where householdId = :hhId")
    fun getAllIRSRoundBen(hhId: Long): Flow<List<IRSRoundScreening>>

    @Transaction
    @Query("SELECT * FROM IRS_ROUND WHERE householdId = :hhId ORDER BY rounds DESC LIMIT 1")
    fun getLastIRSRoundBen(hhId: Long): Flow<IRSRoundScreening?>
    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId and isDeactivate=0")
    fun getAllKALAZARScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithKALAZARScreeningCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId and isDeactivate=0")
    fun getAllLeprosyScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithLeprosyScreeningCache>>


    @Transaction
    @Query("""
    SELECT b.*, l.leprosySymptomsPosition 
    FROM BEN_BASIC_CACHE b 
    INNER JOIN LEPROSY_SCREENING l ON b.benId = l.benId 
    WHERE b.villageId = :selectedVillage
    AND b.isDeactivate=0
    AND l.leprosySymptomsPosition = :symptomsPosition
    AND l.isConfirmed = 0 
""")
    fun getLeprosyScreeningBenBySymptoms(selectedVillage: Int,  symptomsPosition: Int): Flow<List<BenWithLeprosyScreeningCache>>

    @Query("""
    SELECT COUNT(*) 
    FROM BEN_BASIC_CACHE b 
    INNER JOIN LEPROSY_SCREENING l ON b.benId = l.benId 
    WHERE b.villageId = :selectedVillage 
    AND b.isDeactivate=0
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
    AND b.isDeactivate=0
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
    AND  isDeactivate=0
    AND l.isConfirmed = 1
""")
    fun getConfirmedLeprosyCaseCount(
        selectedVillage: Int
    ): Flow<Int>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE benId = :benId and isDeactivate=0")
    suspend fun getBenWithLeprosyScreeningAndFollowUps(benId: Long): BenWithLeprosyScreeningCache?
    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE where villageId = :selectedVillage and hhId = :hhId and isDeactivate=0")
    fun getAllFilariaScreeningBen(selectedVillage: Int,hhId: Long): Flow<List<BenWithFilariaScreeningCache>>


    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage and isDeactivate=0")
    fun getAllBenCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND isDeactivate=0 AND abhaId IS NOT NULL")
    fun getAllBenWithAbhaCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND isDeactivate=0 AND abhaId IS NOT NULL AND isNewAbha = 0")
    fun getAllBenWithOldAbhaCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND isDeactivate=0 AND abhaId IS NOT NULL AND isNewAbha = 1")
    fun getAllBenWithNewAbhaCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE where villageId = :selectedVillage AND isDeactivate=0 AND rchId IS NOT NULL AND rchId != ''")
    fun getAllBenWithRchCount(selectedVillage: Int): Flow<Int>

    @Query("""
        SELECT parent.beneficiaryId as benId, COUNT(child.beneficiaryId) as childCount
        FROM BENEFICIARY parent
        LEFT JOIN BENEFICIARY child
            ON child.householdId = parent.householdId
            AND child.beneficiaryId != parent.beneficiaryId
            AND (parent.firstName IS NULL OR parent.firstName = '' OR child.motherName LIKE parent.firstName || '%')
        WHERE parent.isDraft = 0
            AND parent.loc_village_id = :selectedVillage
            AND parent.isDeactivate = 0
        GROUP BY parent.beneficiaryId
    """)
    fun getChildCountsForAllBen(selectedVillage: Int): Flow<List<BenChildCount>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE hhId = :hhId")
    fun getAllBasicBenForHousehold(hhId: Long): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BENEFICIARY WHERE householdId = :hhId")
    suspend fun getAllBenForHousehold(hhId: Long): List<BenRegCache>

    @Query("SELECT * FROM BENEFICIARY WHERE householdId = :hhId AND beneficiaryId != :selectedbenIdFromArgs AND (:firstName IS NULL OR :firstName = '' OR motherName LIKE :firstName || '%') order by age desc")
    suspend fun getChildBenForHousehold(hhId: Long, selectedbenIdFromArgs: Long, firstName: String?): List<BenRegCache>

    @Query("""
    SELECT COUNT(*) FROM BENEFICIARY
    WHERE householdId = :hhId
    AND beneficiaryId != :selectedbenIdFromArgs
    AND age < 15
    AND (:firstName IS NULL OR :firstName = '' OR motherName LIKE :firstName || '%')
    """)
    suspend fun getBelow15Count(
        hhId: Long,
        selectedbenIdFromArgs: Long,
        firstName: String?
    ): Int

    @Query("""
    SELECT COUNT(*) FROM BENEFICIARY
    WHERE householdId = :hhId
    AND beneficiaryId != :selectedbenIdFromArgs
    AND age >= 15
    AND (:firstName IS NULL OR :firstName = '' OR motherName LIKE :firstName || '%')
    """)
    suspend fun get15aboveCount(
        hhId: Long,
        selectedbenIdFromArgs: Long,
        firstName: String?
    ): Int

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
    @Query("SELECT b.* FROM ben_basic_cache b JOIN eligible_couple_reg r ON b.benId = r.benId LEFT JOIN pregnancy_anc a ON b.benId = a.benId WHERE CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min AND :max AND b.reproductiveStatusId = 1 AND  b.isDeactivate = 0 AND b.villageId = :selectedVillage AND (b.isDeath = 0 OR b.isDeath IS NULL OR b.isDeath = 'undefined') GROUP BY b.benId")
    fun getAllEligibleTrackingList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForEligibleCouple, max: Int = Konstants.maxAgeForEligibleCouple
    ): Flow<List<BenWithEcTrackingCache>>

    @Transaction
    @Query("SELECT * FROM ben_basic_cache WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and reproductiveStatusId = 1 and  villageId=:selectedVillage and isDeactivate = 0  and (isDeath = 0 or isDeath is NULL or isDeath = 'undefined') group by benId")
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
    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben left outer join pregnancy_register  pr on pr.benId = ben.benId  WHERE reproductiveStatusId = 2 and isDeactivate =0 and (pr.benId is null or pr.active = 1) and villageId=:selectedVillage")
    fun getAllPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithPwrCache>>

    @Transaction
    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben left outer join pregnancy_register  pr on pr.benId = ben.benId  WHERE reproductiveStatusId = 2 and isDeactivate =0  and (pr.benId is null or pr.active = 1) and villageId=:selectedVillage and ben.rchId is not null and ben.rchId != ''")
    fun getAllPregnancyWomenWithRchList(selectedVillage: Int): Flow<List<BenWithPwrCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 2 and isDeactivate=0 and villageId=:selectedVillage")
    fun getAllPregnancyWomenForHRList(selectedVillage: Int): Flow<List<BenWithHRPACache>>

    @Transaction
    @Query("SELECT count(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 2 and isDeactivate=0 and villageId=:selectedVillage")
    fun getAllPregnancyWomenForHRListCount(selectedVillage: Int): Flow<Int>


    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 2 and isDeactivate=0 and villageId=:selectedVillage")
    fun getAllPregnancyWomenListCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben  inner join pregnancy_register pwr on pwr.benId = ben.benId inner join pregnancy_anc anc on ben.benId = anc.benId WHERE ben.reproductiveStatusId =3 and ben.isDeactivate =0 and anc.pregnantWomanDelivered =1 and anc.isActive = 1 and pwr.active = 1 and villageId=:selectedVillage group by ben.benId order by anc.updatedDate desc ")
    fun getAllDeliveredWomenList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE ben  inner join pregnancy_register pwr on pwr.benId = ben.benId inner join pregnancy_anc anc on ben.benId = anc.benId WHERE ben.reproductiveStatusId =3 and ben.isDeactivate=0 and anc.pregnantWomanDelivered =1 and anc.isActive = 1 and pwr.active = 1 and villageId=:selectedVillage")
    fun getAllDeliveredWomenListCount(selectedVillage: Int): Flow<Int>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 1 and  isDeactivate=0  and gender = 'FEMALE' and villageId=:selectedVillage")
    fun getAllNonPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithHRNPACache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 1 and gender = 'FEMALE' and isDeactivate=0 and villageId=:selectedVillage")
    fun getAllNonPregnancyWomenListCount(selectedVillage: Int): Flow<Int>

    @Transaction
    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben inner join delivery_outcome do on do.benId = ben.benId where do.isActive = 1 and ben.isDeactivate=0 and villageId=:selectedVillage")
    fun getListForInfantRegister(selectedVillage: Int): Flow<List<BenWithDoAndIrCache>>

    @Query(""" SELECT SUM(do.liveBirth)
    FROM delivery_outcome do
    INNER JOIN BEN_BASIC_CACHE ben ON do.benId = ben.benId
    WHERE do.isActive = 1 AND do.liveBirth > 0 AND ben.isDeactivate=0 AND ben.villageId = :selectedVillage """)
    fun getInfantRegisterCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT * FROM BEN_BASIC_CACHE  WHERE pwHrp = 1 and villageId=:selectedVillage and  isDeactivate=0 ")
    fun getAllWomenListForPmsma(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE pwHrp = 1 and isDeactivate=0 and villageId=:selectedVillage")
    fun getAllWomenListForPmsmaCount(selectedVillage: Int): Flow<Int>

    @Transaction
    @Query("SELECT ben.*  from BEN_BASIC_CACHE  ben inner join pregnancy_register pwr on pwr.benId = ben.benId where pwr.active = 1 and ben.reproductiveStatusId=2 and ben.isDeactivate=0 and ben.villageId=:selectedVillage group by ben.benId")
    fun getAllRegisteredPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>

    @Transaction
    @Query("""
    SELECT ben.*  
    FROM BEN_BASIC_CACHE ben
    INNER JOIN pregnancy_register pwr ON pwr.benId = ben.benId
    WHERE pwr.active = 1 
      AND ben.reproductiveStatusId = 2  
      AND  ben.isDeactivate=0 
      AND ben.villageId = :selectedVillage
        AND (ben.isDeath = 0 OR ben.isDeath IS NULL OR ben.isDeath = 'undefined')
      AND (ben.benId IN (SELECT benId FROM PMSMA WHERE highriskSymbols = 1)
           OR ben.benId IN (SELECT benId FROM PREGNANCY_ANC WHERE anyHighRisk = 1))
    GROUP BY ben.benId
""")
    fun getAllHighRiskPregnancyWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>



    @Transaction
    @Query("""
    SELECT * 
    FROM BEN_BASIC_CACHE 
    WHERE reproductiveStatusId = 2 AND isDeactivate = 0
      AND villageId = :selectedVillage
""")
    fun getAllRegisteredPmsmaWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>


    @Transaction
    @Query("SELECT ben.*  from BEN_BASIC_CACHE  ben inner join pregnancy_anc pwr on pwr.benId = ben.benId where pwr.isAborted = 1 and ben.isDeactivate = 0 and ben.villageId=:selectedVillage group by ben.benId")
    fun getAllAbortionWomenList(selectedVillage: Int): Flow<List<BenWithAncVisitCache>>

    @Query("""
    SELECT * FROM BEN_BASIC_CACHE 
    WHERE 
        CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 15
        AND isDeath = 1
        and isDeactivate=0 
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
        AND  isDeactivate=0 
        AND (reasonOfDeath IS NULL OR reasonOfDeath != 'Maternal Death')
        AND villageId = :selectedVillage
     """)
    fun getAllNonMaternalDeathsList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("""
    SELECT COUNT(*) FROM BEN_BASIC_CACHE 
    WHERE 
        CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) >= 15
        AND isDeath = 1
        AND isDeactivate=0
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
        AND isDeactivate=0
        AND (reasonOfDeath IS NULL OR reasonOfDeath != 'Maternal Death')
        AND villageId = :selectedVillage
""")
    fun getAllNonMaternalDeathsCount(selectedVillage: Int): Flow<Int>
    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE  ben inner join pregnancy_register pwr on pwr.benId = ben.benId where pwr.active = 1 and ben.reproductiveStatusId=2 and isDeactivate=0 and ben.villageId=:selectedVillage")
    fun getAllRegisteredPregnancyWomenListCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT count(distinct(ben.benId)) FROM BEN_BASIC_CACHE  ben inner join pregnancy_anc pwr on pwr.benId = ben.benId where pwr.isAborted = 1 and ben.villageId=:selectedVillage and isDeactivate=0")
    fun getAllAbortionWomenListCount(selectedVillage: Int): Flow<Int>

    @Query("""
    SELECT COUNT(DISTINCT b.benId)
    FROM BEN_BASIC_CACHE b
    INNER JOIN pregnancy_register pwr ON pwr.benId = b.benId
    INNER JOIN PREGNANCY_ANC a ON b.benId = a.benId
    WHERE b.villageId = :selectedVillage
      AND pwr.active = 1
      AND b.reproductiveStatusId = 2
      AND isDeactivate=0
      AND (a.anyHighRisk = 1 OR a.placeOfAncId = 3)
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
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 5 and  isDeactivate=0  and villageId=:selectedVillage")
    fun getAllMenopauseStageList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE gender = :female and CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage and isDeactivate=0")
    fun getAllReproductiveAgeList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForReproductiveAge,
        max: Int = Konstants.maxAgeForReproductiveAge,
        female: Gender = Gender.FEMALE
    ): Flow<List<BenBasicCache>>

    @Transaction
    //@Query("SELECT ben.* FROM BEN_BASIC_CACHE ben left outer join delivery_outcome del on ben.benId = del.benId left outer join pnc_visit pnc on pnc.benId = ben.benId WHERE reproductiveStatusId = 3 and (pnc.isActive is null or pnc.isActive == 1) and CAST((strftime('%s','now') - del.dateOfDelivery/1000)/60/60/24 AS INTEGER) BETWEEN :minPncDate and :maxPncDate and  villageId=:selectedVillage group by ben.benId")
    //@Query("SELECT ben.* FROM BEN_BASIC_CACHE ben LEFT OUTER JOIN delivery_outcome del ON ben.benId = del.benId LEFT OUTER JOIN pnc_visit pnc ON pnc.benId = ben.benId WHERE reproductiveStatusId = 3 AND (pnc.isActive IS NULL OR pnc.isActive == 1) AND ( del.dateOfDelivery IS NULL OR CAST((strftime('%s','now') - COALESCE(del.dateOfDelivery, 0)/1000)/60/60/24 AS INTEGER) BETWEEN :minPncDate AND :maxPncDate) AND (:selectedVillage IS NULL OR villageId = :selectedVillage) GROUP BY ben.benId")

    @Query("SELECT ben.* FROM BEN_BASIC_CACHE ben LEFT OUTER JOIN delivery_outcome del ON ben.benId = del.benId LEFT OUTER JOIN pnc_visit pnc ON pnc.benId = ben.benId WHERE reproductiveStatusId = 3 AND (pnc.isActive IS NULL OR pnc.isActive = 1) AND (pnc.pncPeriod IS NULL OR pnc.pncPeriod != 42)  AND (ben.isDeath IS NULL OR ben.isDeath = 0  OR ben.isDeath = 'undefined' ) AND ben.isDeactivate = 0 AND (:selectedVillage IS NULL OR villageId = :selectedVillage) GROUP BY ben.benId")
    fun getAllPNCMotherList(
        selectedVillage: Int
//        minPncDate: Long = 0,
//        maxPncDate: Long = Konstants.pncEcGap
    ): Flow<List<BenWithDoAndPncCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) <= :max and villageId=:selectedVillage and isDeactivate=0")
    fun getAllInfantList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForInfant
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE benId = :benId LIMIT 1")
    suspend fun getBenById(benId: Long): BenBasicCache?

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) <= :max and villageId=:selectedVillage and rchId is not null and rchId != ''")
    fun getAllInfantWithRchList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForInfant
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST(((strftime('%s','now') - dob/1000)/60/60/24) AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage and isDeactivate =0")
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

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) BETWEEN :min and :max and villageId=:selectedVillage and isDeactivate = 0")
    fun getAllAdolescentList(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForAdolescent,
        max: Int = Konstants.maxAgeForAdolescentlist
    ): Flow<List<BenWithAdolescentCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE isKid = 1 or reproductiveStatusId in (2, 3) and villageId=:selectedVillage and  isDeactivate=0 ")
    fun getAllImmunizationDueList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE hrpStatus = 1 and villageId=:selectedVillage and  isDeactivate=0 ")
    fun getAllHrpCasesList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 4 and hhId = :hhId")
    suspend fun getAllPNCMotherListFromHousehold(hhId: Long): List<BenBasicCache>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) <= :max and villageId=:selectedVillage AND isDeath = 1 and  isDeactivate=0 " )
    fun getAllCDRList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForCdr
    ): Flow<List<BenBasicCache>>

    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER) <= :max and villageId=:selectedVillage AND isDeath = 1 and isDeactivate=0" )
    fun getAllCDRListCount(
        selectedVillage: Int, max: Int = Konstants.maxAgeForCdr
    ): Flow<Int>


    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId in (2, 3, 4) and isMdsr = 1 and villageId=:selectedVillage and isDeactivate=0 ")
    fun getAllMDSRList(selectedVillage: Int): Flow<List<BenWithAncDoPncCache>>
    @Transaction
    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE WHERE reproductiveStatusId in (2, 3, 4) and isMdsr = 1 and villageId=:selectedVillage and isDeactivate=0")
    fun getAllMDSRCount(selectedVillage: Int): Flow<Int>
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE  CAST((strftime('%s','now') - dob/1000)/60/60/24/365 AS INTEGER)<=:max and villageId=:selectedVillage")
    fun getAllChildrenImmunizationList(
        selectedVillage: Int, max: Int = Konstants.maxAgeForAdolescentlist
    ): Flow<List<BenBasicCache>>

    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE reproductiveStatusId = 4 and villageId=:selectedVillage and  isDeactivate=0 ")
    fun getAllMotherImmunizationList(selectedVillage: Int): Flow<List<BenBasicCache>>

    @Query("select ben.* from ben_basic_cache ben inner join  pregnancy_register pwr on ben.benId = pwr.benId left  outer join pregnancy_anc pwa on ben.benId = pwa.benId where ben.villageId = :villageId and ben.isDeactivate=0 and pwr.isHrp =1 or pwa.hrpConfirmed = 1 order by pwa.visitNumber desc ")
    fun getHrpCases(villageId: Int): Flow<List<BenBasicCache>>

    @Query("select * from BEN_BASIC_CACHE b inner join tb_screening t on  b.benId = t.benId where villageId = :villageId and tbsnFilled = 1 and (t.bloodInSputum =1 or t.coughMoreThan2Weeks = 1 or feverMoreThan2Weeks = 1 or nightSweats = 1 or lossOfWeight = 1 or historyOfTb = 1)")
    fun getScreeningList(villageId: Int): Flow<List<BenBasicCache>>

    @Transaction
    @Query("select b.* from BEN_BASIC_CACHE b inner join tb_screening t on  b.benId = t.benId LEFT JOIN TB_SUSPECTED ts ON b.benId = ts.benId  where villageId = :villageId and isDeactivate=0 and tbsnFilled = 1 and (t.bloodInSputum =1 or t.coughMoreThan2Weeks = 1 or feverMoreThan2Weeks = 1 or nightSweats = 1 or lossOfWeight = 1 or historyOfTb = 1)AND (\n" +
            "            ts.benId IS NULL\n" +
            "            OR ts.isConfirmed = 0\n" +
            "        )")
    fun getTbScreeningList(villageId: Int): Flow<List<BenWithTbSuspectedCache>>

    @Transaction
    @Query("select b.* from BEN_BASIC_CACHE b inner join TB_SUSPECTED t on b.benID = t.benId and t.isConfirmed =1 where villageId = :villageId and isDeactivate=0")
    fun getTbConfirmedList(villageId: Int): Flow<List<BenWithTbSuspectedCache>>


    @Transaction
    @Query("select b.*, t.slideTestName As slideTestName from BEN_BASIC_CACHE b inner join malaria_screening t on  b.benId = t.benId where villageId = :villageId and  isDeactivate=0 and t.caseStatus = 'Confirmed' ")
    fun getMalariaConfirmedCasesList(villageId: Int): Flow<List<BenWithMalariaConfirmedCache>>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE b where CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER)  >= :min and b.reproductiveStatusId!=2 and b.isDeactivate=0 and b.villageId=:selectedVillage group by b.benId order by b.regDate desc")
    fun getBenWithCbac(
        selectedVillage: Int, min: Int = Konstants.minAgeForNcd
    ): Flow<List<BenWithCbacCache>>



    @Transaction
    @Query("""
    SELECT  r.*, b.*
   FROM BEN_BASIC_CACHE b
   INNER JOIN NCD_REFER r ON b.benId = r.benId
   WHERE CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) >= :min
     AND b.reproductiveStatusId != 2
     AND b.villageId = :selectedVillage
     AND b.isDeactivate = 0
   ORDER BY b.regDate DESC
""")
    fun getBenWithReferredCbac(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForNcd
    ): Flow<List<BenWithCbacAndReferalCache>>


    @Query("""
  SELECT COUNT(b.benId)
    FROM BEN_BASIC_CACHE b
    INNER JOIN NCD_REFER r ON b.benId = r.benId
      AND CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER) >= :min
      AND b.reproductiveStatusId != 2
      AND b.villageId = :selectedVillage
      AND b.isDeactivate=0
""")
     fun getReferredBenCount(
        selectedVillage: Int,
        min: Int = Konstants.minAgeForNcd
    ): Flow<Int>

    @Query("""
  SELECT COUNT(b.benId)
    FROM BEN_BASIC_CACHE b
    INNER JOIN NCD_REFER r ON b.benId = r.benId
      AND b.villageId = :selectedVillage
      AND r.type = "MATERNAL"
       AND b.villageId = :selectedVillage
       AND isDeactivate=0
""")
    fun getReferredHWCBenCount(
        selectedVillage: Int,
    ): Flow<Int>

    @Query("""
    SELECT  r.*, b.*
    FROM BEN_BASIC_CACHE b
    INNER JOIN NCD_REFER r ON b.benId = r.benId
      AND b.villageId = :selectedVillage
      AND b.isDeactivate=0
      AND r.type = "MATERNAL"
""")
    fun getReferredHWCBenList(
        selectedVillage: Int,
    ):  Flow<List<BenWithCbacAndReferalCache>>


    @Query("SELECT COUNT(*) FROM BEN_BASIC_CACHE b where CAST((strftime('%s','now') - b.dob/1000)/60/60/24/365 AS INTEGER)  >= :min and b.reproductiveStatusId!=2 and isDeactivate=0 and b.villageId=:selectedVillage")
    fun getBenWithCbacCount(
        selectedVillage: Int, min: Int = Konstants.minAgeForNcd
    ): Flow<Int>

    @Query("select min(beneficiaryId) from beneficiary")
    suspend fun getMinBenId(): Long?


    @Transaction
    @Query("select * from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 2 and isDeactivate = 0 and gender = 'FEMALE' and benId in (select benId from HRP_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingPregList(villageId: Int): Flow<List<BenWithHRPTrackingCache>>

    @Transaction
    @Query("select * from BEN_BASIC_CACHE where benId = :benId;")
    fun getHRPTrackingPregForBen(benId: Long): BenWithHRPTrackingCache

    @Query("select count(*) from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 2 and isDeactivate=0 and gender = 'FEMALE' and benId in (select benId from HRP_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingPregListCount(villageId: Int): Flow<Int>

    @Transaction
    @Query("select * from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 1 and  isDeactivate=0 and gender = 'FEMALE' and benId in (select benId from HRP_NON_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingNonPregList(villageId: Int): Flow<List<BenWithHRNPTrackingCache>>

    @Query("select count(*) from BEN_BASIC_CACHE where villageId = :villageId and reproductiveStatusId = 1 and isDeactivate=0 and gender = 'FEMALE' and benId in (select benId from HRP_NON_PREGNANT_ASSESS where isHighRisk = 1);")
    fun getAllHRPTrackingNonPregListCount(villageId: Int): Flow<Int>

    @Query("select count(*) from INFANT_REG inf join ben_basic_cache ben on ben.benId = inf.motherBenId where isActive = 1 and ben.isDeactivate=0 and weight < :lowWeightLimit and  ben.villageId = :villageId")
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

    @Query("UPDATE BENEFICIARY SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}