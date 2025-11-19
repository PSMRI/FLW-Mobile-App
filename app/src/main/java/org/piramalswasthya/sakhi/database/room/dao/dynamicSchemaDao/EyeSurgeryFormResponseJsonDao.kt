package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity

@Dao
interface EyeSurgeryFormResponseJsonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: EyeSurgeryFormResponseJsonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<EyeSurgeryFormResponseJsonEntity>)

    @Query("SELECT * FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE benId = :benId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDate: String): EyeSurgeryFormResponseJsonEntity?

    @Query("DELETE FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun deleteFormResponse(benId: Long, visitDate: String)

    @Query("SELECT * FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<EyeSurgeryFormResponseJsonEntity>

    @Query("UPDATE ALL_EYE_SURGERY_VISIT_HISTORY SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<EyeSurgeryFormResponseJsonEntity>

    @Query("UPDATE ALL_EYE_SURGERY_VISIT_HISTORY SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)

    @Query("SELECT DISTINCT benId FROM ALL_EYE_SURGERY_VISIT_HISTORY")
    suspend fun getAllUniqueBenIds(): List<Long>

    @Query("SELECT formDataJson FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE benId = :benId AND formId = :formId")
    suspend fun getFormJsonList(benId: Long, formId: String): List<String>

    // NEW: month-keyed helpers for uniqueness/upsert
    @Query("SELECT * FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE benId = :benId AND formId = :formId AND visitMonth = :visitMonth LIMIT 1")
    suspend fun getByBenFormMonth(benId: Long, formId: String, visitMonth: String): EyeSurgeryFormResponseJsonEntity?

    @Query("SELECT * FROM ALL_EYE_SURGERY_VISIT_HISTORY WHERE benId = :benId AND formId = :formId ORDER BY visitMonth DESC LIMIT 1")
    suspend fun getLatestForBenForm(benId: Long, formId: String): EyeSurgeryFormResponseJsonEntity?

    @Transaction
    suspend fun upsertByMonth(entity: EyeSurgeryFormResponseJsonEntity) {
        val existing = getByBenFormMonth(entity.benId, entity.formId, entity.visitMonth)
        val toSave = existing?.let { entity.copy(id = it.id) } ?: entity
        insertFormResponse(toSave)
    }
}
