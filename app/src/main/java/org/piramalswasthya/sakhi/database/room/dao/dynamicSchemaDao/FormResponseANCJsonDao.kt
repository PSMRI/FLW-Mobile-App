package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity

@Dao
interface FormResponseANCJsonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormResponse(response: ANCFormResponseJsonEntity)

    @Query("SELECT * FROM ALL_VISIT_HISTORY_ANC WHERE benId = :benId AND visitDate = :visitDate LIMIT 1")
    suspend fun getFormResponse(benId: Long, visitDate: String): ANCFormResponseJsonEntity?

    @Query("DELETE FROM ALL_VISIT_HISTORY_ANC WHERE benId = :benId AND visitDay = :visitDay")
    suspend fun deleteFormResponse(benId: Long, visitDay: String)

    @Query("SELECT * FROM ALL_VISIT_HISTORY_ANC WHERE isSynced = 0")
    suspend fun getUnsyncedForms(): List<ANCFormResponseJsonEntity>

    @Query("UPDATE ALL_VISIT_HISTORY_ANC SET isSynced = 1, syncedAt = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: String)

    @Query("SELECT * FROM ALL_VISIT_HISTORY_ANC WHERE benId = :benId")
    suspend fun getSyncedVisitsByRchId(benId: Long): List<ANCFormResponseJsonEntity>

    @Query("UPDATE ALL_VISIT_HISTORY_ANC SET benId = :newBenId WHERE benId = :oldBenId")
    suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long)


    @Query("""
        SELECT * FROM ALL_VISIT_HISTORY_ANC
        WHERE benId = :benId
        ORDER BY visitDate DESC
    """)
    suspend fun getVisitsForBen(benId: Long): List<ANCFormResponseJsonEntity>

    @Query("""
        SELECT COUNT(*) FROM ALL_VISIT_HISTORY_ANC
        WHERE benId = :benId
    """)
    suspend fun getVisitCount(benId: Long): Int
}