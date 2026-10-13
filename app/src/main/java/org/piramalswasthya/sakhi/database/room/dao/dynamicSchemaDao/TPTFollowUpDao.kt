package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity

@Dao
interface TPTFollowUpDao {
    @Upsert
    suspend fun upsert(response: TPTFollowUpEntity)

    @Query("SELECT * FROM tb_tpt_follow_up WHERE benId = :benId ORDER BY visitNo, followUpNo")
    fun observeByBeneficiary(benId: Long): Flow<List<TPTFollowUpEntity>>

    @Query("SELECT * FROM tb_tpt_follow_up ORDER BY visitNo, followUpNo")
    fun observeAll(): Flow<List<TPTFollowUpEntity>>

    @Query("SELECT * FROM tb_tpt_follow_up WHERE benId = :benId ORDER BY visitNo, followUpNo")
    suspend fun getByBeneficiary(benId: Long): List<TPTFollowUpEntity>

    @Query("SELECT * FROM tb_tpt_follow_up WHERE benId = :benId ORDER BY visitNo DESC, followUpNo DESC LIMIT 1")
    suspend fun getLatest(benId: Long): TPTFollowUpEntity?

    @Query("SELECT * FROM tb_tpt_follow_up WHERE benId = :benId AND followUpNo = :followUpNo ORDER BY visitNo DESC LIMIT 1")
    suspend fun getByFollowUpNo(benId: Long, followUpNo: Int): TPTFollowUpEntity?

    @Query("SELECT * FROM tb_tpt_follow_up WHERE isSynced = 0 ORDER BY updatedAt")
    suspend fun getUnsynced(): List<TPTFollowUpEntity>

    @Query("UPDATE tb_tpt_follow_up SET isSynced = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markSynced(id: Long, updatedAt: Long)
}
