package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.model.BenWithABHAGeneratedCache
import org.piramalswasthya.sakhi.model.TBScreeningCache

@Dao
interface ABHAGenratedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveABHA(abhaModel: ABHAModel)
    @Update
    suspend fun updateAbha(abhaModel: ABHAModel)

    @Query("DELETE FROM ABHA_GENERATED WHERE beneficiaryID = :benId")
    suspend fun deleteAbhaByBenId(benId: Long)

    @Query("SELECT * FROM ABHA_GENERATED")
    fun getAllAbha(): List<ABHAModel?>

    @Transaction
    @Query("SELECT * FROM BEN_BASIC_CACHE WHERE benId = :benId")
    suspend fun getBenWithAbha(benId: Long): BenWithABHAGeneratedCache?
}