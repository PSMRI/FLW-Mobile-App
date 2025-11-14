package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity

@Dao
interface CUFYFormResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<CUFYFormResponseJsonEntity>)

    @Query("SELECT * FROM children_under_five_all_visit WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun getResponsesForVisit(benId: Long, visitDate: String): List<CUFYFormResponseJsonEntity>

    @Query("DELETE FROM children_under_five_all_visit WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun deleteResponsesForVisit(benId: Long, visitDate: String)
}