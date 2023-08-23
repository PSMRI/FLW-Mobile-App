package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.*

@Dao
interface MaternalHealthDao {

    @Query("select * from pregnancy_register where benId = :benId limit 1")
    fun getSavedRecord(benId: Long): PregnantWomanRegistrationCache?

    @Query("select * from pregnancy_anc where benId = :benId and visitNumber = :visitNumber limit 1")
    fun getSavedRecord(benId: Long, visitNumber: Int): PregnantWomanAncCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(pregnancyRegistrationForm: PregnantWomanRegistrationCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(ancCache: PregnantWomanAncCache)

    @Query("select benId, visitNumber, :filled as formState, 0 as filledWeek from pregnancy_anc where benId = :benId order by visitNumber")
    suspend fun getAllAncRecordsFor(
        benId: Long,
        filled: AncFormState = AncFormState.ALREADY_FILLED
    ): List<AncStatus>

    @Query("select * from pregnancy_register reg left outer join pregnancy_anc anc on reg.benId=anc.benId ")
    fun getAllPregnancyRecords(): Flow<Map<PregnantWomanRegistrationCache, List<PregnantWomanAncCache>>>

    @Query("SELECT * FROM pregnancy_anc WHERE processed = 'N'")
    suspend fun getAllUnprocessedAncVisits(): List<PregnantWomanAncCache>

    @Query("SELECT * FROM pregnancy_register WHERE processed = 'N'")
    suspend fun getAllUnprocessedPWRs(): List<PregnantWomanRegistrationCache>
    @Update
    suspend fun updateANC(it: PregnantWomanAncCache)

    @Update
    suspend fun updatePwr(it: PregnantWomanRegistrationCache)
}