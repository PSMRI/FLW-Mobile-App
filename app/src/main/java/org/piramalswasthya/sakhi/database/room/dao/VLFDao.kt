package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache

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

}