package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenCheckupCount
import org.piramalswasthya.sakhi.model.GeneralCheckupCache

/** General Check-up records (FLW-1229). Check-ups are never edited after save. */
@Dao
interface GeneralCheckupDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCheckup(checkup: GeneralCheckupCache): Long

    /** Pulled rows; a checkupId already on the device is kept as is (check-ups are never edited). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckupsIfAbsent(checkups: List<GeneralCheckupCache>)

    /** Pulled rows are kept only for beneficiaries on the device (GENERAL_CHECKUP.benId is a foreign key). */
    @Query("SELECT beneficiaryId FROM BENEFICIARY WHERE beneficiaryId IN (:benIds)")
    suspend fun getExistingBenIds(benIds: List<Long>): List<Long>

    @Query("SELECT * FROM GENERAL_CHECKUP WHERE benId = :benId ORDER BY checkupDate DESC, createdDate DESC")
    fun getHistory(benId: Long): Flow<List<GeneralCheckupCache>>

    @Query("SELECT * FROM GENERAL_CHECKUP WHERE benId = :benId AND referralMonth = :referralMonth")
    suspend fun getCheckupsInMonth(benId: Long, referralMonth: Int): List<GeneralCheckupCache>

    @Query("SELECT benId, COUNT(*) AS count FROM GENERAL_CHECKUP GROUP BY benId")
    fun getCheckupCountPerBen(): Flow<List<BenCheckupCount>>

    @Query("SELECT * FROM GENERAL_CHECKUP WHERE syncState = :state")
    suspend fun getCheckupsBySyncState(state: SyncState): List<GeneralCheckupCache>

    /** Moves rows from [from] to [to] only; a row changed meanwhile keeps its newer state. */
    @Query("UPDATE GENERAL_CHECKUP SET syncState = :to WHERE checkupId IN (:checkupIds) AND syncState = :from")
    suspend fun moveCheckupSyncState(checkupIds: List<String>, from: SyncState, to: SyncState)

    @Query("UPDATE GENERAL_CHECKUP SET syncState = :to WHERE syncState = :from")
    suspend fun resetCheckupSyncState(from: SyncState, to: SyncState)
}
