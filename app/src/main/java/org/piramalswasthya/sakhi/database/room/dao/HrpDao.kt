package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.*

@Dao
interface HrpDao {

    @Query("select * from HRP_PREGNANT_ASSESS where benId = :benId")
    fun getPregnantAssess(benId: Long) : HRPPregnantAssessCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpPregnantAssessCache: HRPPregnantAssessCache)

    @Query("select * from HRP_NON_PREGNANT_ASSESS where benId = :benId")
    fun getNonPregnantAssess(benId: Long) : HRPNonPregnantAssessCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(hrpNonPregnantAssessCache: HRPNonPregnantAssessCache)

    @Query("select * from HRP_NON_PREGNANT_TRACK where benId = :benId")
    fun getNonPregnantTrackList(benId: Long) : List<HRPNonPregnantTrackCache>?

    @Insert
    fun saveRecord(hrpNonPregnantTrackCache: HRPNonPregnantTrackCache)

    @Query("select * from HRP_PREGNANT_TRACK where benId = :benId")
    fun getPregnantTrackList(benId: Long) : List<HRPPregnantTrackCache>?

    @Query("select * from HRP_PREGNANT_TRACK where id = :trackId")
    fun getHRPTrack(trackId: Long) : HRPPregnantTrackCache?

    @Query("select * from HRP_NON_PREGNANT_TRACK where id = :trackId")
    fun getHRPNonTrack(trackId: Long) : HRPNonPregnantTrackCache?

    @Insert
    fun saveRecord(hrpPregnantTrackCache: HRPPregnantTrackCache)

    @Query("select * from HRP_MICRO_BIRTH_PLAN where benId = :benId limit 1")
    fun getMicroBirthPlan(benId: Long) : HRPMicroBirthPlanCache?

    @Insert
    fun saveRecord(hrpMicroBirthPlanCache: HRPMicroBirthPlanCache)

    @Query("select * from HRP_PREGNANT_TRACK order by dateOfVisit desc")
    fun getAllPregTrack() : List<HRPPregnantTrackCache>?

    @Query("select * from HRP_PREGNANT_TRACK where benId = :benId order by dateOfVisit desc")
    fun getAllPregTrackforBen(benId: Long): List<HRPPregnantTrackCache>?

    @Query("select * from HRP_NON_PREGNANT_TRACK order by dateOfVisit desc")
    fun getAllNonPregTrack() : List<HRPNonPregnantTrackCache>?

    @Query("select * from HRP_NON_PREGNANT_TRACK where benId = :benId order by dateOfVisit desc")
    fun getAllNonPregTrackforBen(benId: Long) :List<HRPNonPregnantTrackCache>?

    @Query("select max(lmp) from HRP_NON_PREGNANT_TRACK where benId = :benId")
    fun getMaxLmp(benId: Long): Long?
    @Query("select max(dateOfVisit) from HRP_NON_PREGNANT_TRACK where benId = :benId")
    fun getMaxDoV(benId: Long) : Long?

    @Query("select max(dateOfVisit) from HRP_PREGNANT_TRACK where benId = :benId")
    fun getMaxDoVhrp(benId: Long) : Long?
}