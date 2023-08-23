package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache

@Dao
interface EcrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg ecrCache: EligibleCoupleRegCache)

    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE processed = 'N'")
    suspend fun getAllUnprocessedECR(): List<EligibleCoupleRegCache>

    @Query("SELECT * FROM ELIGIBLE_COUPLE_TRACKING WHERE processed = 'N'")
    suspend fun getAllUnprocessedECT(): List<EligibleCoupleTrackingCache>

    @Query("select count(*) from ELIGIBLE_COUPLE_REG")
    suspend fun ecrCount(): Int

    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE benId =:benId limit 1")
    suspend fun getSavedECR(benId: Long): EligibleCoupleRegCache?

    @Update
    suspend fun update(it: EligibleCoupleRegCache)

    @Update
    suspend fun updateEligibleCoupleTracking(it: EligibleCoupleTrackingCache)

    @Query("select * from eligible_couple_tracking where benId = :benId and createdDate =:createdDate limit 1")
//    @Query("select * from eligible_couple_tracking where benId = :benId and CAST((strftime('%s','now') - visitDate/1000)/60/60/24 AS INTEGER) < 30 order by visitDate limit 1")
    fun getEct(benId: Long, createdDate : Long): EligibleCoupleTrackingCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg eligibleCoupleTrackingCache: EligibleCoupleTrackingCache)

    @Query("select count(*)>0 from eligible_couple_tracking where createdDate=:createdDate")
    suspend fun ectWithsameCreateDateExists(createdDate: Long): Boolean

}