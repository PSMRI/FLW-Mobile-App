package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity

@Dao
interface FormResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<FormResponseJsonEntity>)

    @Query("SELECT * FROM all_visit_history WHERE benId = :benId AND visitDay = :visitDay")
    suspend fun getResponsesForVisit(benId: Long, visitDay: String): List<FormResponseJsonEntity>

    @Query("DELETE FROM all_visit_history WHERE benId = :benId AND visitDay = :visitDay")
    suspend fun deleteResponsesForVisit(benId: Long, visitDay: String)
}
