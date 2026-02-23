package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.ChildRegCache
import org.piramalswasthya.sakhi.model.InfantRegWithBen

@Dao
interface ChildRegistrationDao {

    //    @Query("SELECT b.*, c.* FROM BEN_BASIC_CACHE b join infant_reg i on b.benId = i.motherBenId left outer join CHILD_REG c on i.motherBenId= c.benId WHERE villageId=:selectedVillage")
    @Transaction
    @Query("SELECT i.* FROM infant_reg i  join beneficiary b on b.beneficiaryId = i.motherBenId where i.isActive = 1 and b.loc_village_id = :selectedVillage and b.isDeactivate=0")
    fun getAllRegisteredInfants(selectedVillage: Int): Flow<List<InfantRegWithBen>>

    @Query("SELECT count(*) FROM infant_reg i  join beneficiary b on b.beneficiaryId = i.motherBenId where i.isActive = 1 and b.loc_village_id = :selectedVillage")
    fun getAllRegisteredInfantsCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT * FROM CHILD_REG WHERE motherBenId =:benId limit 1")
    suspend fun getInfantReg(benId: Long): ChildRegCache?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveInfantReg(childReg: ChildRegCache)

}