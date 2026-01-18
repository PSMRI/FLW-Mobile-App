package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity

@Dao
interface FilariaMdaCampaignJsonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaignFormResponse(response: FilariaMDACampaignFormResponseJsonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaignAll(responses: List<FilariaMDACampaignFormResponseJsonEntity>)

    @Query("SELECT * FROM FILARIA_MDA_CAMPAIGN_HISTORY WHERE visitDate = :visitDate LIMIT 1")
    suspend fun getCampaignFormResponse(visitDate: String): FilariaMDACampaignFormResponseJsonEntity?

    @Query("DELETE FROM FILARIA_MDA_CAMPAIGN_HISTORY WHERE  visitDate = :visitDate")
    suspend fun deleteCampaignFormResponse( visitDate: String)

    @Query("SELECT * FROM FILARIA_MDA_CAMPAIGN_HISTORY WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedCampaignForms(formId: String): List<FilariaMDACampaignFormResponseJsonEntity>

    @Query("UPDATE FILARIA_MDA_CAMPAIGN_HISTORY SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markCampaignAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM FILARIA_MDA_CAMPAIGN_HISTORY")
    suspend fun getCampaignSyncedVisitsByRchId(): List<FilariaMDACampaignFormResponseJsonEntity>


    @Query("SELECT formDataJson FROM FILARIA_MDA_CAMPAIGN_HISTORY  ORDER BY substr(visitDate, 7, 4) || '-' || substr(visitDate, 4, 2) || '-' || substr(visitDate, 1, 2) DESC LIMIT 3")
    suspend fun getCampaignFormJsonList(): List<String>
    @Query("SELECT formDataJson FROM FILARIA_MDA_CAMPAIGN_HISTORY  ORDER BY createdAt DESC LIMIT 3")
    suspend fun getCampaignLatest3Json(): List<String>

    @Query("SELECT * FROM FILARIA_MDA_CAMPAIGN_HISTORY WHERE formId = :formId AND visitYear = :visitYear LIMIT 1")
    suspend fun getCampaignByBenFormYear(formId: String, visitYear: String): FilariaMDACampaignFormResponseJsonEntity?

    @Query("SELECT * FROM FILARIA_MDA_CAMPAIGN_HISTORY WHERE  formId = :formId ORDER BY visitYear DESC LIMIT 1")
    suspend fun getCampaignLatestForBenForm( formId: String): FilariaMDACampaignFormResponseJsonEntity?

    @Transaction
    suspend fun upsertByYear(entity: FilariaMDACampaignFormResponseJsonEntity) {
        val existing = getCampaignByBenFormYear(entity.formId, entity.visitYear)
        val toSave = existing?.let { entity.copy(id = it.id) } ?: entity
        insertCampaignFormResponse(toSave)
    }

    @Transaction
    suspend fun insertOncePerYear(entity: FilariaMDACampaignFormResponseJsonEntity): Boolean {
        val existing = getCampaignByBenFormYear(entity.formId, entity.visitYear)

        return if (existing != null) {
            false
        } else {
            insertCampaignFormResponse(entity)
            true
        }
    }

}