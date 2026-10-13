package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.GeneralOPEDBeneficiary

@Dao
interface GeneralOpdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beneficiaries: List<GeneralOPEDBeneficiary>)

    @Query("SELECT * FROM GENERAL_OPD_ACTIVITY ORDER BY COALESCE(modified_date, visitDate, registrationDate, benVisitDate) DESC, beneficiaryId DESC")
    fun getAll(): Flow<List<GeneralOPEDBeneficiary>>
}