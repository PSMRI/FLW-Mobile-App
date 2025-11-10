package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity


@Dao
interface MosquitoNetFormResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: MosquitoNetFormResponseJsonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<MosquitoNetFormResponseJsonEntity>)

    @Query("SELECT * FROM mosquito_net_visit WHERE benId = :benId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDate: String): MosquitoNetFormResponseJsonEntity?

    @Query("DELETE FROM mosquito_net_visit WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun deleteFormResponse(benId: Long, visitDate: String)

    @Query("SELECT * FROM mosquito_net_visit WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<MosquitoNetFormResponseJsonEntity>

    @Query("UPDATE mosquito_net_visit SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM mosquito_net_visit WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<MosquitoNetFormResponseJsonEntity>

    @Query("UPDATE mosquito_net_visit SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)

    @Query("SELECT DISTINCT benId FROM mosquito_net_visit")
    suspend fun getAllUniqueBenIds(): List<Long>

    @Query("SELECT formDataJson FROM mosquito_net_visit WHERE benId = :benId AND formId = :formId")
    suspend fun getFormJsonList(benId: Long, formId: String): List<String>

    // NEW: month-keyed helpers for uniqueness/upsert
    @Query("SELECT * FROM mosquito_net_visit WHERE benId = :benId AND formId = :formId AND visitDay = :visitMonth LIMIT 1")
    suspend fun getByBenFormMonth(benId: Long, formId: String, visitMonth: String): MosquitoNetFormResponseJsonEntity?

    @Query("SELECT * FROM mosquito_net_visit WHERE benId = :benId AND formId = :formId ORDER BY visitDay DESC LIMIT 1")
    suspend fun getLatestForBenForm(benId: Long, formId: String): MosquitoNetFormResponseJsonEntity?

    @Transaction
    suspend fun upsertByMonth(entity: MosquitoNetFormResponseJsonEntity) {
        val existing = getByBenFormMonth(entity.benId, entity.formId, entity.visitDay)
        val toSave = existing?.let { entity.copy(id = it.id) } ?: entity
        insertFormResponse(toSave)
    }
}
