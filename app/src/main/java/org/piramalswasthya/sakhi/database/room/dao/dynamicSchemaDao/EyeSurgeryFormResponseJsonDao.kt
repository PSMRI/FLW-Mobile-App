package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity

@Dao
interface EyeSurgeryFormResponseJsonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: EyeSurgeryFormResponseJsonEntity)

    @Query("SELECT * FROM all_eye_surgery_visit_history WHERE benId = :benId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDate: String): EyeSurgeryFormResponseJsonEntity?

    @Query("DELETE FROM all_eye_surgery_visit_history WHERE benId = :benId AND visitDate = :visitDate")
    suspend fun deleteFormResponse(benId: Long, visitDate: String)


    @Query("SELECT * FROM all_eye_surgery_visit_history WHERE isSynced = 0 AND formId = :formId")
    suspend fun getUnsyncedForms(formId: String): List<EyeSurgeryFormResponseJsonEntity>


    @Query("UPDATE all_eye_surgery_visit_history SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM all_eye_surgery_visit_history WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<EyeSurgeryFormResponseJsonEntity>

    @Query("UPDATE all_eye_surgery_visit_history SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)

}
