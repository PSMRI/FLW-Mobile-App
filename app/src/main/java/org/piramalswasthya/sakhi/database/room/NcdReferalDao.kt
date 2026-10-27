package org.piramalswasthya.sakhi.database.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.monthKey

@Dao
interface NcdReferalDao {

    // revisitMonth is part of the unique key, so every write goes through the wrappers below,
    // which derive it from revisitDate.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRows(vararg referalCache: ReferalCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(referalCache: List<ReferalCache>)

    @Update
    suspend fun updateRows(vararg referalCache: ReferalCache)

    suspend fun upsert(vararg referalCache: ReferalCache) =
        upsertRows(*referalCache.onEach { it.revisitMonth = monthKey(it.revisitDate) })

    suspend fun insertAll(referalCache: List<ReferalCache>) =
        insertRows(referalCache.onEach { it.revisitMonth = monthKey(it.revisitDate) })

    suspend fun update(vararg referalCache: ReferalCache) =
        updateRows(*referalCache.onEach { it.revisitMonth = monthKey(it.revisitDate) })

    @Query("SELECT * FROM NCD_REFER WHERE benId = :benId LIMIT 1")
    suspend fun getReferalFromBenId(benId: Long): ReferalCache?

    @Query("SELECT type FROM NCD_REFER WHERE benId = :benId AND referralReason = :referralReason LIMIT 1")
    suspend fun getReferralType(benId: Long, referralReason: String?): String?

    @Query("SELECT * FROM NCD_REFER WHERE syncState = 0")
    suspend fun getAllUnprocessedReferals(): List<ReferalCache>

}
