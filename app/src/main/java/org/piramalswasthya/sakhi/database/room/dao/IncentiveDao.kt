package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveActivityWithRecords
import org.piramalswasthya.sakhi.model.IncentiveCache
import org.piramalswasthya.sakhi.model.IncentiveRecordCache

@Dao
interface IncentiveDao {


    @Upsert
    suspend fun insert(vararg incentiveActivity: IncentiveActivityCache)

    @Upsert
    suspend fun insert(vararg incentiveRecord: IncentiveRecordCache)

    @Query("select * from INCENTIVE_RECORD")
    fun getAllRecords(): Flow<List<IncentiveCache>>

    @Query("select * from INCENTIVE_ACTIVITY")
    fun getAllActivity() : Flow<List<IncentiveActivityWithRecords>>

    @Query("select * from INCENTIVE_RECORD where id = :recordId")
    fun getRecordById(recordId: Long): IncentiveRecordCache?

    @Query("select * from INCENTIVE_RECORD where activityId = :activityId and createdDate = :createdDate")
    fun getRecordByActivityAndCreatedDate(
        activityId: Long,
        createdDate: Long
    ): IncentiveRecordCache?

    @Query("select * from INCENTIVE_ACTIVITY where id = :id")
    fun getActivityById(id: Long): IncentiveActivityCache?


}