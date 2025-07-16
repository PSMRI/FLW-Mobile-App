package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity

@Dao
interface FormResponseJsonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: FormResponseJsonEntity)

    @Query("SELECT * FROM all_visit_history WHERE rchId = :rchId AND visitDay = :visitDay LIMIT 1")
    suspend fun getFormResponse(rchId: String, visitDay: String): FormResponseJsonEntity?

    @Query("DELETE FROM all_visit_history WHERE rchId = :rchId AND visitDay = :visitDay")
    suspend fun deleteFormResponse(rchId: String, visitDay: String)

    @Query("SELECT * FROM all_visit_history WHERE isSynced = 0")
    suspend fun getUnsyncedForms(): List<FormResponseJsonEntity>

    @Query("UPDATE all_visit_history SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM all_visit_history WHERE rchId = :rchId AND isSynced = 1")
    suspend fun getSyncedVisitsByRchId(rchId: String): List<FormResponseJsonEntity>


}
