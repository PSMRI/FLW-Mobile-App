package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import java.time.LocalDate

@Dao
interface VLFDao {

    @Query("select * from VHND where id = :id limit 1")
    fun getVHND(id: Int): VHNDCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(vhndCache: VHNDCache)

    @Query("SELECT * FROM VHND")
    fun getAllVHND(): Flow<List<VHNDCache>>

    @Query("select * from VHNC where id = :id limit 1")
    fun getVHNC(id: Int): VHNCCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(vhndCache: VHNCCache)

    @Query("SELECT * FROM VHNC")
    fun getAllVHNC(): Flow<List<VHNCCache>>

    @Query("select * from PHCReviewMeeting where id = :id limit 1")
    fun getPHC(id: Int): PHCReviewMeetingCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(phcCache: PHCReviewMeetingCache)

    @Query("SELECT * FROM PHCReviewMeeting")
    fun getAllPHC(): Flow<List<PHCReviewMeetingCache>>

    @Query("SELECT * FROM AHDMeeting WHERE id = :id")
    suspend fun getAHD(id: Int): AHDCache?

    @Query("SELECT * FROM AHDMeeting")
    fun getAllAHD(): Flow<List<AHDCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(ahdCache: AHDCache)


    @Query("SELECT * FROM DewormingMeeting WHERE id = :id")
    suspend fun getDeworming(id: Int): DewormingCache?

    @Query("SELECT * FROM DewormingMeeting ")
    fun getAllDeworming(): Flow<List<DewormingCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(dewormingCache: DewormingCache)


    @Query("select * from VHND where syncState = :syncState")
    fun getVHND(syncState: SyncState): List<VHNDCache>?


    @Query("select * from VHNC where syncState = :syncState")
    fun getVHNC(syncState: SyncState): List<VHNCCache>?

    @Query("select * from PHCReviewMeeting where syncState = :syncState")
    fun getPHC(syncState: SyncState): List<PHCReviewMeetingCache>?


    @Query("select * from AHDMeeting where syncState = :syncState")
    fun getAHD(syncState: SyncState): List<AHDCache>?

    @Query("select * from DewormingMeeting where syncState = :syncState")
    fun getDeworming(syncState: SyncState): List<DewormingCache>?


    @Query("SELECT COUNT(*) FROM VHND WHERE vhndDate BETWEEN :startDate AND :endDate")
    fun countVHNDFormsInDateRange(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM VHNC WHERE vhncDate BETWEEN :startDate AND :endDate")
    fun countVHNCFormsInDateRange(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM PHCReviewMeeting WHERE phcReviewDate BETWEEN :startDate AND :endDate")
    fun countPHCFormsInDateRange(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM AHDMeeting WHERE ahdDate BETWEEN :startDate AND :endDate")
    fun countAHDFormsInDateRange(startDate: String, endDate: String): Flow<Int>
    @Query("""
    SELECT COUNT(*) 
    FROM DewormingMeeting
    WHERE date(
        substr(dewormingDate, 7, 4) || '-' ||
        substr(dewormingDate, 4, 2) || '-' ||
        substr(dewormingDate, 1, 2)
    ) >= date('now', '-6 months')
""")
    fun countDewormingInLastSixMonths(): Flow<Int>

    // For VHND form
    @Query("SELECT MAX(vhndDate) FROM VHND")
    fun getLastVHNDSubmissionDate(): Flow<String?>

    // For VHNC form
    @Query("SELECT MAX(vhncDate) FROM VHNC")
    fun getLastVHNCSubmissionDate(): Flow<String?>

    // For PHC form
    @Query("SELECT MAX(phcReviewDate) FROM PHCReviewMeeting")
    fun getLastPHCSubmissionDate(): Flow<String?>

    // For AHD form
    @Query("SELECT MAX(ahdDate) FROM AHDMeeting")
    fun getLastAHDSubmissionDate(): Flow<String?>

    // For Deworming form
    @Query("SELECT MAX(regDate) FROM DewormingMeeting")
    fun getLastDewormingSubmissionDate(): Flow<String?>

    // For Pulse Polio Campaign form
    @Query("SELECT * FROM PulsePolioCampaign WHERE id = :id")
    suspend fun getPulsePolioCampaign(id: Int): PulsePolioCampaignCache?

    @Query("SELECT * FROM PulsePolioCampaign")
    fun getAllPulsePolioCampaign(): Flow<List<PulsePolioCampaignCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(pulsePolioCampaignCache: PulsePolioCampaignCache)

    @Query("SELECT * FROM PulsePolioCampaign WHERE syncState = :syncState")
    fun getPulsePolioCampaign(syncState: SyncState): List<PulsePolioCampaignCache>?

    @Query("SELECT * FROM PulsePolioCampaign")
    fun getAllPulsePolioCampaignForDate(): Flow<List<PulsePolioCampaignCache>>

    @Query("SELECT * FROM PulsePolioCampaign")
    suspend fun getAllPulsePolioCampaigns(): List<PulsePolioCampaignCache>


    // For ORS Campaign form
    @Query("SELECT * FROM ORSCampaign WHERE id = :id")
    suspend fun getORSCampaign(id: Int): ORSCampaignCache?

    @Query("SELECT * FROM ORSCampaign")
    fun getAllORSCampaign(): Flow<List<ORSCampaignCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(orsCampaignCache: ORSCampaignCache)

    @Query("SELECT * FROM ORSCampaign WHERE syncState = :syncState")
    fun getORSCampaign(syncState: SyncState): List<ORSCampaignCache>?

    @Query("SELECT * FROM ORSCampaign")
    fun getAllORSCampaignForDate(): Flow<List<ORSCampaignCache>>

    @Query("SELECT * FROM ORSCampaign")
    suspend fun getAllORSCampaigns(): List<ORSCampaignCache>
    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE id = :id")
    suspend fun getFilariaMdaCampaign(id: Int): FilariaMDAFormResponseJsonEntity?

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY")
    fun getAllFilariaMdaCampaign(): Flow<List<FilariaMDAFormResponseJsonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(pulsePolioCampaignCache: FilariaMDAFormResponseJsonEntity)

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY WHERE syncState = :syncState")
    fun getFilariaMdaCampaign(syncState: SyncState): List<FilariaMDAFormResponseJsonEntity>?

    @Query("SELECT * FROM FILARIA_MDA_VISIT_HISTORY")
    fun getAllFilariaMdaCampaignForDate(): Flow<List<FilariaMDAFormResponseJsonEntity>>

}