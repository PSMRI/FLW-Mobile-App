package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.InfantEntity

@Dao
interface InfantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfant(infant: InfantEntity)

    @Query("SELECT * FROM infant WHERE rchId = :rchId LIMIT 1")
    suspend fun getInfantByRchId(rchId: String): InfantEntity?

    @Query("SELECT * FROM infant")
    fun getAllInfants(): LiveData<List<InfantEntity>>
}
