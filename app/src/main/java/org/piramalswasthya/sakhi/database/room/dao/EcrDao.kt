package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache

@Dao
interface EcrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg ecrCache: EligibleCoupleRegCache)

    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE processed in ('N', 'U')")
    suspend fun getAllUnprocessedECR(): List<EligibleCoupleRegCache>

    @Query("SELECT * FROM ELIGIBLE_COUPLE_TRACKING WHERE processed in ('N','U')")
    suspend fun getAllUnprocessedECT(): List<EligibleCoupleTrackingCache>

    @Query("select count(*) from ELIGIBLE_COUPLE_REG")
    suspend fun ecrCount(): Int

    /**
     * Monthly Recap (READ-ONLY): counts DISTINCT couples the current ASHA either
     * registered OR tracked for family planning in the window [startInclusive,
     * endExclusive). Ownership is `createdBy = :userName` (local saves set the
     * logged-in userName; downloaded rows preserve the original owner's createdBy).
     * The UNION de-duplicates `benId` across the two tables and within each, and
     * `benId` is stable across sync (FK onUpdate=CASCADE), so a local row and its
     * re-downloaded copy — and a couple that is BOTH registered and tracked — each
     * count exactly once. Returns an aggregate only.
     */
    @Query(
        "SELECT COUNT(*) FROM (" +
                "SELECT benId FROM ELIGIBLE_COUPLE_REG " +
                "WHERE createdBy = :userName AND dateOfReg >= :startInclusive AND dateOfReg < :endExclusive " +
                "UNION " +
                "SELECT benId FROM ELIGIBLE_COUPLE_TRACKING " +
                "WHERE createdBy = :userName AND visitDate >= :startInclusive AND visitDate < :endExclusive" +
                ")"
    )
    suspend fun countCurrentAshaEligibleCouples(
        userName: String,
        startInclusive: Long,
        endExclusive: Long,
    ): Int

    @Query("SELECT * FROM ELIGIBLE_COUPLE_REG WHERE benId =:benId limit 1")
    suspend fun getSavedECR(benId: Long): EligibleCoupleRegCache?

//    @Query("SELECT noOfChildren FROM ELIGIBLE_COUPLE_REG WHERE benId = :benId LIMIT 1")
    @Query("SELECT noOfLiveChildren FROM ELIGIBLE_COUPLE_REG WHERE benId = :benId LIMIT 1")
    suspend fun getNoOfChildren(benId: Long): Int?

    @Update
    suspend fun update(it: EligibleCoupleRegCache)

    @Update
    suspend fun updateEligibleCoupleTracking(it: EligibleCoupleTrackingCache)

    @Query("select * from eligible_couple_tracking where benId = :benId and createdDate =:createdDate limit 1")
//    @Query("select * from eligible_couple_tracking where benId = :benId and CAST((strftime('%s','now') - visitDate/1000)/60/60/24 AS INTEGER) < 30 order by visitDate limit 1")
    fun getEct(benId: Long, createdDate: Long): EligibleCoupleTrackingCache?

    @Query("select * from eligible_couple_tracking where benId = :benId order by visitDate desc limit 1")
    suspend fun getLatestEct(benId: Long): EligibleCoupleTrackingCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg eligibleCoupleTrackingCache: EligibleCoupleTrackingCache)

    @Query("select count(*)>0 from eligible_couple_tracking where createdDate=:createdDate")
    suspend fun ectWithsameCreateDateExists(createdDate: Long): Boolean

    @Query("SELECT * FROM eligible_couple_tracking WHERE benId = :benId ORDER BY visitDate DESC")
    suspend fun getAllAntraDoses(benId: Long): List<EligibleCoupleTrackingCache>

    @Query("UPDATE ELIGIBLE_COUPLE_REG SET syncState = 0 WHERE syncState = 1")
    suspend fun resetRegSyncingToUnsynced()

    @Query("UPDATE ELIGIBLE_COUPLE_TRACKING SET syncState = 0 WHERE syncState = 1")
    suspend fun resetTrackingSyncingToUnsynced()
}