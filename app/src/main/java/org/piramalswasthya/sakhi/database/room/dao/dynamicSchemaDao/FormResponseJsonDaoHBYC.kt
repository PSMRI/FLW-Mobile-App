package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.*
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC

@Dao
interface FormResponseJsonDaoHBYC {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: FormResponseJsonEntityHBYC)

    @Query("SELECT * FROM ALL_VISIT_HISTORY_HBYC WHERE benId = :benId AND visitDay = :visitDay LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDay: String): FormResponseJsonEntityHBYC?

    @Query("DELETE FROM ALL_VISIT_HISTORY_HBYC WHERE benId = :benId AND visitDay = :visitDay")
    suspend fun deleteFormResponse(benId: Long, visitDay: String)

    @Query("SELECT * FROM ALL_VISIT_HISTORY_HBYC WHERE isSynced = 0")
    suspend fun getUnsyncedForms(): List<FormResponseJsonEntityHBYC>

    @Query("UPDATE ALL_VISIT_HISTORY_HBYC SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM ALL_VISIT_HISTORY_HBYC WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<FormResponseJsonEntityHBYC>

    @Query("UPDATE ALL_VISIT_HISTORY_HBYC SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)
}
