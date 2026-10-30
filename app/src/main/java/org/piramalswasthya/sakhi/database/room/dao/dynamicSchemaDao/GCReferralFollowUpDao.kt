package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.dynamicEntity.GCReferralFollowUpEntity

@Dao
interface GCReferralFollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: GCReferralFollowUpEntity): Long

    @Query(
        "SELECT * FROM gc_referral_follow_up WHERE benId = :benId AND referralReason = :referralReason " +
                "AND referralMonth = :referralMonth LIMIT 1"
    )
    suspend fun getForReferral(benId: Long, referralReason: String, referralMonth: Int): GCReferralFollowUpEntity?

    /** The beneficiary's General Check-up referrals (type, or the reason prefix when the type came back empty). */
    @Query(
        "SELECT * FROM NCD_REFER WHERE benId = :benId " +
                "AND (type = 'GENERAL_CHECKUP' OR referralReason LIKE 'General Check-up: %')"
    )
    suspend fun getGcReferrals(benId: Long): List<ReferalCache>

    @Query("SELECT * FROM gc_referral_follow_up WHERE isSynced = 0")
    suspend fun getUnsynced(): List<GCReferralFollowUpEntity>

    @Query("UPDATE gc_referral_follow_up SET isSynced = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markSynced(id: Long, updatedAt: Long)
}
