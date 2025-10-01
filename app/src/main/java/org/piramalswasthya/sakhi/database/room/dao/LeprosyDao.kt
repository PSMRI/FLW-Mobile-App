package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache

@Dao
interface LeprosyDao {
    @Query("SELECT * FROM LEPROSY_SCREENING WHERE benId =:benId limit 1")
    suspend fun getLeprosyScreening(benId: Long): LeprosyScreeningCache?

    @Query("SELECT * FROM LEPROSY_SCREENING WHERE benId =:benId and (homeVisitDate = :visitDate or homeVisitDate = :visitDateGMT) limit 1")
    suspend fun getLeprosyScreening(benId: Long, visitDate: Long, visitDateGMT: Long): LeprosyScreeningCache?

    @Query("SELECT * FROM LEPROSY_SCREENING WHERE  syncState = :syncState")
    suspend fun getLeprosyScreening(syncState: SyncState): List<LeprosyScreeningCache>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLeprosyScreening(malariaScreeningCache: LeprosyScreeningCache)
}