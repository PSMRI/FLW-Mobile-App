package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.*

@Dao
interface HrpDao {

    @Query("select * from HRP_PREGNANT_ASSESS where benId = :benId")
    fun getPregnantAssess(benId: Long): HRPPregnantAssessCache?

    @Query("select * from HRP_PREGNANT_ASSESS where benId = :benId and visitDate = :visitDate")
    fun getPregnantAssess(benId: Long, visitDate: Long): HRPPregnantAssessCache?

    @Query("select * from HRP_PREGNANT_ASSESS where syncState = :syncState")
    fun getHRPAssess(syncState: SyncState): List<HRPPregnantAssessCache>?

    @Query("select * from HRP_MICRO_BIRTH_PLAN where syncState = :syncState")
    fun getMicroBirthPlan(syncState: SyncState): List<HRPMicroBirthPlanCache>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpPregnantAssessCache: HRPPregnantAssessCache)

    @Query("select * from HRP_MICRO_BIRTH_PLAN where benId = :benId limit 1")
    fun getSavedRecord(benId: Long): HRPMicroBirthPlanCache?


    @Query("select * from HRP_NON_PREGNANT_ASSESS where benId = :benId")
    fun getNonPregnantAssess(benId: Long): HRPNonPregnantAssessCache?

    @Query("select * from HRP_NON_PREGNANT_ASSESS where benId = :benId and visitDate = :visitDate")
    fun getNonPregnantAssess(benId: Long, visitDate: Long): HRPNonPregnantAssessCache?

    @Query("select * from HRP_NON_PREGNANT_ASSESS where syncState = :syncState")
    fun getNonPregnantAssess(syncState: SyncState): List<HRPNonPregnantAssessCache>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpNonPregnantAssessCache: HRPNonPregnantAssessCache)

    @Query("select * from HRP_NON_PREGNANT_TRACK where benId = :benId")
    fun getNonPregnantTrackList(benId: Long): List<HRPNonPregnantTrackCache>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpNonPregnantTrackCache: HRPNonPregnantTrackCache)

    @Query("select * from HRP_PREGNANT_TRACK where benId = :benId")
    fun getPregnantTrackList(benId: Long): List<HRPPregnantTrackCache>?

    @Query("select * from HRP_PREGNANT_TRACK where id = :trackId")
    fun getHRPTrack(trackId: Long): HRPPregnantTrackCache?

    @Query("select * from HRP_PREGNANT_TRACK where benId = :benId and visit = :visit and visitDate = :visitDate")
    fun getHRPTrack(benId: Long, visit: String, visitDate: Long): HRPPregnantTrackCache?

    @Query("select * from HRP_PREGNANT_TRACK where syncState = :syncState")
    fun getHRPTrack(syncState: SyncState): List<HRPPregnantTrackCache>?

    @Query("select * from HRP_NON_PREGNANT_TRACK where id = :trackId")
    fun getHRPNonTrack(trackId: Long): HRPNonPregnantTrackCache?

    @Query("select * from HRP_NON_PREGNANT_TRACK where benId = :benId and visitDate = :visitDate")
    fun getHRPNonTrack(benId: Long, visitDate: Long): HRPNonPregnantTrackCache?

    @Query("select * from HRP_NON_PREGNANT_TRACK where syncState = :syncState")
    fun getHRNonPTrack(syncState: SyncState): List<HRPNonPregnantTrackCache>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpPregnantTrackCache: HRPPregnantTrackCache)

    @Query("select * from HRP_MICRO_BIRTH_PLAN where benId = :benId limit 1")
    fun getMicroBirthPlan(benId: Long): HRPMicroBirthPlanCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpMicroBirthPlanCache: HRPMicroBirthPlanCache)

    @Query("select * from HRP_PREGNANT_TRACK order by visitDate desc")
    fun getAllPregTrack(): List<HRPPregnantTrackCache>?

    @Query("select * from HRP_PREGNANT_TRACK where benId = :benId order by visit desc")
    fun getAllPregTrackforBen(benId: Long): List<HRPPregnantTrackCache>?

    @Query("select * from HRP_NON_PREGNANT_TRACK order by visitDate desc")
    fun getAllNonPregTrack(): List<HRPNonPregnantTrackCache>?

    @Query("select * from HRP_NON_PREGNANT_TRACK where benId = :benId order by visitDate desc")
    fun getAllNonPregTrackforBen(benId: Long): List<HRPNonPregnantTrackCache>?

    @Query("select max(lmp) from HRP_NON_PREGNANT_TRACK where benId = :benId")
    fun getMaxLmp(benId: Long): Long?

    @Query("select max(visitDate) from HRP_NON_PREGNANT_TRACK where benId = :benId")
    fun getMaxDoV(benId: Long): Long?

    @Query("select max(visitDate) from HRP_PREGNANT_TRACK where benId = :benId")
    fun getMaxDoVhrp(benId: Long): Long?

    @Query("select * from HRP_PREGNANT_TRACK where benId = :benId and visit = :visit and (visitDate = :visitDate or visitDate = :visitDateGmt)")
    fun getHRPTrack(
        benId: Long,
        visit: String,
        visitDate: Long,
        visitDateGmt: Long
    ): HRPPregnantTrackCache?

    @Query("select * from HRP_NON_PREGNANT_TRACK where benId = :benId and (visitDate = :visitDate or visitDate = :visitDateGMT)")
    fun getHRPNonTrack(benId: Long, visitDate: Long, visitDateGMT: Long): HRPNonPregnantTrackCache?
}