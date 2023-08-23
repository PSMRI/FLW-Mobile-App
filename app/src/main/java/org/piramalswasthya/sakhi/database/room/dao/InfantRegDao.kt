package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.InfantRegCache

@Dao
interface InfantRegDao {

    @Query("SELECT * FROM INFANT_REG WHERE motherBenId =:benId limit 1")
    fun getInfantReg(benId: Long): InfantRegCache?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveInfantReg(infantRegCache: InfantRegCache)
}