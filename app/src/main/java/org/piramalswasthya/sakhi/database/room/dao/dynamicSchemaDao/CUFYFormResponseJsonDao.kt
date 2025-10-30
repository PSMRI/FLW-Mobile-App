package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity


@Dao
interface CUFYFormResponseJsonDao {

    @Upsert
    suspend fun insertFormResponse(response: CUFYFormResponseJsonEntity)

    @Update
    suspend fun updateFormResponse(response: CUFYFormResponseJsonEntity): Int

    @Query("SELECT * FROM children_under_five_all_visit WHERE id = :id")
    suspend fun getFormResponseById(id: Int): CUFYFormResponseJsonEntity?

    @Query("SELECT * FROM children_under_five_all_visit WHERE benId = :benId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDate: String): CUFYFormResponseJsonEntity?

    @Query("DELETE FROM children_under_five_all_visit WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun deleteFormResponse(benId: Long, visitDate: String)

    @Query("SELECT * FROM children_under_five_all_visit WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<CUFYFormResponseJsonEntity>

    @Query("SELECT * FROM children_under_five_all_visit WHERE formId = :formId AND benId = :benId")
    suspend fun getFormsDataByFormID(formId: String, benId: Long): List<CUFYFormResponseJsonEntity>

    @Query("UPDATE children_under_five_all_visit SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM children_under_five_all_visit WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<CUFYFormResponseJsonEntity>

    @Query("UPDATE children_under_five_all_visit SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)

}
