package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ChildImmunizationDetailsCache
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationCategory
import org.piramalswasthya.sakhi.model.MotherImmunizationDetailsCache
import org.piramalswasthya.sakhi.model.Vaccine

@Dao
interface ImmunizationDao {

    @Query("SELECT COUNT(*)>0 FROM VACCINE")
    suspend fun vaccinesLoaded(): Boolean

    @Insert
    suspend fun addVaccine(vararg vaccine: Vaccine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImmunizationRecord(imm: ImmunizationCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImmunizationRecord( imm:  List<ImmunizationCache>):LongArray

    @Query("SELECT * FROM IMMUNIZATION WHERE beneficiaryId=:benId AND vaccineId =:vaccineId limit 1")
    suspend fun getImmunizationRecord(benId: Long, vaccineId: Int): ImmunizationCache?


    @Query("SELECT * FROM IMMUNIZATION WHERE  syncState = :syncState")
    suspend fun getUnsyncedImmunization(syncState: SyncState): List<ImmunizationCache>

    @Query("select count(*) as count from (select * from beneficiary b inner join vaccine v on  (strftime('%s', 'now') * 1000)  - b.dob between v.minAllowedAgeInMillis and v.overdueDurationSinceMinInMillis and v.vaccineId not in (select vaccineId from immunization i where i.beneficiaryId = b.beneficiaryId) group by b.beneficiaryId)")
    fun getChildrenImmunizationDueListCount(): Flow<Int>


    @Transaction
    @Query(
        "SELECT ben.* FROM BEN_BASIC_CACHE ben LEFT OUTER JOIN IMMUNIZATION imm WHERE ben.dob BETWEEN :minDob AND :maxDob group by ben.benId"
    )
    fun getBenWithImmunizationRecords(
        minDob: Long,
        maxDob: Long,
//        vaccineIdList: List<Int>
    ): Flow<List<ChildImmunizationDetailsCache>>

    @Transaction
    @Query(
        "SELECT ben.*, reg.lmpDate as lmp, imm.* FROM BEN_BASIC_CACHE ben inner join pregnancy_register reg on ben.benId = reg.benId LEFT OUTER JOIN IMMUNIZATION imm WHERE ben.reproductiveStatusId = :reproductiveStatusId "
    )
    fun getBenWithImmunizationRecords(
        reproductiveStatusId: Int = 2
//        vaccineIdList: List<Int>
    ): Flow<List<MotherImmunizationDetailsCache>>

    @Query("SELECT * FROM VACCINE where category = :immCat order by vaccineId")
    suspend fun getVaccinesForCategory(immCat: ImmunizationCategory): List<Vaccine>

    @Query("SELECT * FROM VACCINE where category = :immCat order by vaccineId")
    suspend fun getVaccinesForCategory(immCat: ChildImmunizationCategory): List<Vaccine>

    @Query("SELECT * FROM VACCINE WHERE vaccineId = :vaccineId limit 1")
    suspend fun getVaccineById(vaccineId: Int): Vaccine?

    @Query("SELECT * FROM VACCINE WHERE vaccineName = :name limit 1")
    suspend fun getVaccineByName(name: String): Vaccine?

    @Query("UPDATE IMMUNIZATION SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}