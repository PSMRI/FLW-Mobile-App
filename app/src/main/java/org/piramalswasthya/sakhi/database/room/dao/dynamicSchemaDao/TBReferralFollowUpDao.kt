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
                "INNER JOIN tb_referral_follow_up AS t ON t.benId = b.benId " +
                "WHERE t.followUpStatus = :status " +
                "GROUP BY b.benId"
    )
    fun observeBeneficiariesByFollowUpStatus(status: String): Flow<List<BenWithTbReferralFollowUpCache>>

    @Query("SELECT * FROM tb_referral_follow_up WHERE benId = :benId AND followUpDate = :followUpDate LIMIT 1")
    suspend fun getByBeneficiaryAndDate(benId: Long, followUpDate: String): TBReferralFollowUpEntity?

    @Query("SELECT * FROM tb_referral_follow_up WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TBReferralFollowUpEntity>

    @Query("UPDATE tb_referral_follow_up SET isSynced = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markSynced(id: Long, updatedAt: Long)
}
