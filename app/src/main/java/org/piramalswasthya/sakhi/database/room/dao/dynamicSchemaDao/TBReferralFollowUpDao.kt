package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.dynamicEntity.BenWithTbReferralFollowUpCache
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpEntity

@Dao
interface TBReferralFollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: TBReferralFollowUpEntity): Long

    @Query("SELECT * FROM tb_referral_follow_up WHERE benId = :benId ORDER BY followUpDate DESC")
    suspend fun getByBeneficiary(benId: Long): List<TBReferralFollowUpEntity>

    @Query("SELECT * FROM tb_referral_follow_up WHERE followUpStatus = :status ORDER BY followUpDate DESC")
    fun observeByFollowUpStatus(status: String): Flow<List<TBReferralFollowUpEntity>>

    @Transaction
    @Query(
        "SELECT b.* FROM BEN_BASIC_CACHE AS b " +
                "WHERE EXISTS (" +
                "SELECT 1 FROM tb_referral_follow_up AS t " +
                "WHERE t.benId = b.benId AND t.followUpStatus = :status " +
                "AND t.followUpDate = (SELECT MAX(t2.followUpDate) FROM tb_referral_follow_up AS t2 WHERE t2.benId = b.benId)" +
                ") OR EXISTS (" +
                "SELECT 1 FROM TB_SUSPECTED AS ts WHERE ts.benId = b.benId " +
                "AND IFNULL(ts.isRecommendedForTpt, 0) = 1 " +
                "AND IFNULL(ts.isConfirmed, 0) = 0 " +
                "AND IFNULL(ts.isTBConfirmed, 0) = 0 " +
                "AND IFNULL(ts.isDRTBConfirmed, 0) = 0" +
                ")"
    )
    fun observeBeneficiariesByFollowUpStatus(status: String): Flow<List<BenWithTbReferralFollowUpCache>>

    @Query("SELECT * FROM tb_referral_follow_up WHERE benId = :benId AND followUpDate = :followUpDate LIMIT 1")
    suspend fun getByBeneficiaryAndDate(benId: Long, followUpDate: String): TBReferralFollowUpEntity?

    @Query("SELECT * FROM tb_referral_follow_up WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TBReferralFollowUpEntity>

    @Query("UPDATE tb_referral_follow_up SET isSynced = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markSynced(id: Long, updatedAt: Long)
}
