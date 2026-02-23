package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.sakhi.model.InfantRegCache

@Dao
interface InfantRegDao {

    @Query("SELECT * FROM INFANT_REG WHERE motherBenId =:benId and babyIndex = :babyIndex and isActive = 1 limit 1")
    suspend fun getInfantReg(benId: Long, babyIndex: Int): InfantRegCache?

    @Query("SELECT * FROM INFANT_REG WHERE childBenId =:benId limit 1")
    suspend fun getInfantRegFromChildBenId(benId: Long): InfantRegCache?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveInfantReg(infantRegCache: InfantRegCache)

    @Query("SELECT * FROM INFANT_REG WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedInfantReg(): List<InfantRegCache>

    @Update
    suspend fun updateInfantReg(it: InfantRegCache)

    @Query("select count(*) from infant_reg where isActive =1 and motherBenId = :benId ")
    suspend fun getNumBabiesRegistered(benId: Long): Int

    @Query("SELECT * FROM INFANT_REG WHERE motherBenId in (:eligBenIds) and isActive = 1")
    suspend fun getAllInfantRegs(eligBenIds: Set<Long>): List<InfantRegCache>

    @Query("UPDATE INFANT_REG SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}