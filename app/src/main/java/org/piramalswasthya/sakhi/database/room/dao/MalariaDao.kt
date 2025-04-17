package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache

@Dao
interface MalariaDao {
    @Query("SELECT * FROM MALARIA_SCREENING WHERE benId =:benId limit 1")
    suspend fun getMalariaScreening(benId: Long): MalariaScreeningCache?

    @Query("SELECT * FROM MALARIA_SCREENING WHERE benId =:benId and (caseDate = :visitDate or caseDate = :visitDateGMT) limit 1")
    suspend fun getMalariaScreening(benId: Long, visitDate: Long, visitDateGMT: Long): MalariaScreeningCache?

    @Query("SELECT * FROM MALARIA_SCREENING WHERE  syncState = :syncState")
    suspend fun getMalariaScreening(syncState: SyncState): List<MalariaScreeningCache>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMalariaScreening(malariaScreeningCache: MalariaScreeningCache)


    @Query("SELECT * FROM MALARIA_CONFIRMED WHERE benId =:benId limit 1")
    suspend fun getMalariaConfirmed(benId: Long): MalariaConfirmedCasesCache?

    @Query("SELECT * FROM MALARIA_CONFIRMED WHERE benId =:benId and (dateOfDiagnosis = :visitDate or dateOfDiagnosis = :visitDateGMT) limit 1")
    suspend fun getMalariaConfirmed(benId: Long, visitDate: Long, visitDateGMT: Long): MalariaConfirmedCasesCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMalariaConfirmed(tbSuspectedCache: MalariaConfirmedCasesCache)

    @Query("SELECT * FROM MALARIA_CONFIRMED WHERE  syncState = :syncState")
    suspend fun getMalariaConfirmed(syncState: SyncState): List<MalariaConfirmedCasesCache>
}