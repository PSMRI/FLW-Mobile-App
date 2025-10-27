package org.piramalswasthya.sakhi.database.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.sakhi.model.ReferalCache
@Dao
interface NcdReferalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg referalCache: ReferalCache)

    @Update
    suspend fun update(vararg referalCache: ReferalCache)

    @Query("SELECT * FROM NCD_REFER WHERE benId = :benId LIMIT 1")
    suspend fun getReferalFromBenId(benId: Long): ReferalCache?

    @Query("SELECT * FROM NCD_REFER WHERE syncState = 0")
    suspend fun getAllUnprocessedReferals(): List<ReferalCache>

}