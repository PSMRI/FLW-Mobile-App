package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.Query
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity

@Dao
interface NCDReferalFormResponseJsonDao {

    /* ---------------- INSERT / UPDATE ---------------- */
    @Upsert
    suspend fun insertFormResponse(
        response: NCDReferalFormResponseJsonEntity
    )

    @Update
    suspend fun updateFormResponse(
        response: NCDReferalFormResponseJsonEntity
    ): Int

    /* ---------------- GET SINGLE ---------------- */
    @Query("SELECT * FROM ncd_referal_all_visit WHERE id = :id")
    suspend fun getFormResponseById(id: Int): NCDReferalFormResponseJsonEntity?

    // ✅ Updated to fetch by visitNo + followUpNo
    @Query("""
        SELECT * FROM ncd_referal_all_visit 
        WHERE benId = :benId AND visitNo = :visitNo AND followUpNo = :followUpNo
        LIMIT 1
    """)
    suspend fun getFormResponse(
        benId: Long,
        visitNo: Int,
        followUpNo: Int
    ): NCDReferalFormResponseJsonEntity?

    /* ---------------- DELETE ---------------- */
    @Query("""
        DELETE FROM ncd_referal_all_visit 
        WHERE benId = :benId AND visitNo = :visitNo AND followUpNo = :followUpNo
    """)
    suspend fun deleteFormResponse(
        benId: Long,
        visitNo: Int,
        followUpNo: Int
    )

    /* ---------------- SYNC ---------------- */
    @Query("""
        SELECT * FROM ncd_referal_all_visit 
        WHERE isSynced = 0 AND formId = :formId
    """)
    suspend fun getUnsyncedForms(
        formId: String
    ): List<NCDReferalFormResponseJsonEntity>

    @Query("""
        UPDATE ncd_referal_all_visit 
        SET isSynced = 1, syncedAt = :syncedAt 
        WHERE id = :id
    """)
    suspend fun markAsSynced(
        id: Int,
        syncedAt: Long
    )

    /* ---------------- LIST / HISTORY ---------------- */
    @Query("""
        SELECT * FROM ncd_referal_all_visit 
        WHERE formId = :formId AND benId = :benId
        ORDER BY visitNo ASC, followUpNo ASC
    """)
    suspend fun getAllVisitsByBeneficiary(
        benId: Long,
        formId: String
    ): List<NCDReferalFormResponseJsonEntity>

    @Query("""
        SELECT * FROM ncd_referal_all_visit 
        WHERE benId = :benId
        ORDER BY visitNo ASC, followUpNo ASC
    """)
    suspend fun getSyncedVisitsByRchId(
        benId: Long
    ): List<NCDReferalFormResponseJsonEntity>

    /* ---------------- UTIL ---------------- */
    @Query("""
        UPDATE ncd_referal_all_visit 
        SET benId = :newBenId 
        WHERE benId = :oldBenId
    """)
    suspend fun updateVisitBenId(
        oldBenId: Long,
        newBenId: Long
    )

    @Query("""
        SELECT formDataJson 
        FROM ncd_referal_all_visit 
        WHERE benId = :benId AND formId = :formId
    """)
    suspend fun getFormJsonList(
        benId: Long,
        formId: String
    ): List<String>
}
