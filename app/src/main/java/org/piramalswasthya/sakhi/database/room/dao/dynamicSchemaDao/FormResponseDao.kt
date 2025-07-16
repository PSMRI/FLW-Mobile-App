package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity

@Dao
interface FormResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<FormResponseJsonEntity>)

    @Query("SELECT * FROM all_visit_history WHERE rchId = :rchId AND visitDay = :visitDay")
    suspend fun getResponsesForVisit(rchId: String, visitDay: String): List<FormResponseJsonEntity>

    @Query("DELETE FROM all_visit_history WHERE rchId = :rchId AND visitDay = :visitDay")
    suspend fun deleteResponsesForVisit(rchId: String, visitDay: String)
}
