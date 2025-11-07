package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity

@Dao
interface BenIfaFormResponseJsonDao {

    @Upsert
    suspend fun insertFormResponse(response: BenIfaFormResponseJsonEntity)

    @Query("SELECT * FROM ALL_BEN_IFA_VISIT_HISTORY WHERE benId = :benId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDate: String): BenIfaFormResponseJsonEntity?

    @Query("DELETE FROM ALL_BEN_IFA_VISIT_HISTORY WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun deleteFormResponse(benId: Long, visitDate: String)

    @Query("SELECT * FROM ALL_BEN_IFA_VISIT_HISTORY WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<BenIfaFormResponseJsonEntity>

    @Query("UPDATE ALL_BEN_IFA_VISIT_HISTORY SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM ALL_BEN_IFA_VISIT_HISTORY WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<BenIfaFormResponseJsonEntity>

    @Query("UPDATE ALL_BEN_IFA_VISIT_HISTORY SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)

    @Query("SELECT DISTINCT benId FROM ALL_BEN_IFA_VISIT_HISTORY")
    suspend fun getAllUniqueBenIds(): List<Long>

    @Query("SELECT formDataJson FROM ALL_BEN_IFA_VISIT_HISTORY WHERE benId = :benId AND formId = :formId")
    suspend fun getFormJsonList(benId: Long, formId: String): List<String>

    @Query("SELECT COUNT(*) FROM ALL_BEN_IFA_VISIT_HISTORY WHERE benId = :benId AND strftime('%m-%Y', substr(visitDate, 4, 2) || '-' || substr(visitDate, 7, 4)) = strftime('%m-%Y', 'now')")
    suspend fun hasVisitThisMonth(benId: Long): Int
}
