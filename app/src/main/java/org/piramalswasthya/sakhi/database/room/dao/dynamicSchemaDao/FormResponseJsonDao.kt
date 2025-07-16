package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import android.util.Log
import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity

@Dao
interface FormResponseJsonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: FormResponseJsonEntity)

    @Query("SELECT * FROM all_visit_history WHERE benId = :benId AND visitDay = :visitDay LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDay: String): FormResponseJsonEntity?

    @Query("DELETE FROM all_visit_history WHERE benId = :benId AND visitDay = :visitDay")
    suspend fun deleteFormResponse(benId: Long, visitDay: String)

    @Query("SELECT * FROM all_visit_history WHERE isSynced = 0")
    suspend fun getUnsyncedForms(): List<FormResponseJsonEntity>

    @Query("UPDATE all_visit_history SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM all_visit_history WHERE benId = :benId AND isSynced = 1")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<FormResponseJsonEntity>


}
