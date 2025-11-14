package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity

@Dao
interface FilariaMDAFormResponseJsonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: FilariaMDAFormResponseJsonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<FilariaMDAFormResponseJsonEntity>)

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(hhId: Long, visitDate: String): FilariaMDAFormResponseJsonEntity?

    @Query("DELETE FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId AND visitDate = :visitDate")
    suspend fun deleteFormResponse(hhId: Long, visitDate: String)

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<FilariaMDAFormResponseJsonEntity>

    @Query("UPDATE FILARIA_MDA_VISIT_HISTORY SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId")
    suspend fun getSyncedVisitsByRchId(hhId: Long): List<FilariaMDAFormResponseJsonEntity>

    @Query("UPDATE FILARIA_MDA_VISIT_HISTORY SET hhId = :newHhId WHERE hhId = :oldHhId")
    suspend fun updateVisitBenId(newHhId: Long, oldHhId: Long)

    @Query("SELECT formDataJson FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId  ORDER BY substr(visitDate, 7, 4) || '-' || substr(visitDate, 4, 2) || '-' || substr(visitDate, 1, 2) DESC LIMIT 3")
    suspend fun getFormJsonList(hhId: Long): List<String>
    @Query("SELECT formDataJson FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId ORDER BY createdAt DESC LIMIT 3")
    suspend fun getLatest3Json(hhId: Long): List<String>

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId AND formId = :formId AND visitMonth = :visitMonth LIMIT 1")
    suspend fun getByBenFormMonth(hhId: Long, formId: String, visitMonth: String): FilariaMDAFormResponseJsonEntity?

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE hhId = :hhId AND formId = :formId ORDER BY visitMonth DESC LIMIT 1")
    suspend fun getLatestForBenForm(hhId: Long, formId: String): FilariaMDAFormResponseJsonEntity?

    @Transaction
    suspend fun upsertByMonth(entity: FilariaMDAFormResponseJsonEntity) {
        val existing = getByBenFormMonth(entity.hhId, entity.formId, entity.visitMonth)
        val toSave = existing?.let { entity.copy(id = it.id) } ?: entity
        insertFormResponse(toSave)
    }

    @Transaction
    suspend fun insertOncePerMonth(entity: FilariaMDAFormResponseJsonEntity): Boolean {
        val existing = getByBenFormMonth(entity.hhId, entity.formId, entity.visitMonth)

        return if (existing != null) {
            false
        } else {
            insertFormResponse(entity)
            true
        }
    }

}
