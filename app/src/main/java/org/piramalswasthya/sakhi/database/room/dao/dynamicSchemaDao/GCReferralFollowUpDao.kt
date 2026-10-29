package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.dynamicEntity.GCReferralFollowUpEntity

@Dao
interface GCReferralFollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: GCReferralFollowUpEntity): Long

    @Query("SELECT * FROM gc_referral_follow_up WHERE benId = :benId ORDER BY followUpDate DESC")
    suspend fun getByBeneficiary(benId: Long): List<GCReferralFollowUpEntity>

    @Query("SELECT * FROM gc_referral_follow_up WHERE benId = :benId AND followUpDate = :followUpDate LIMIT 1")
    suspend fun getByBeneficiaryAndDate(benId: Long, followUpDate: String): GCReferralFollowUpEntity?

    @Query("SELECT * FROM gc_referral_follow_up WHERE isSynced = 0")
    suspend fun getUnsynced(): List<GCReferralFollowUpEntity>

    @Query("UPDATE gc_referral_follow_up SET isSynced = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markSynced(id: Long, updatedAt: Long)
}
