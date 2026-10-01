package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.MaaMeetingEntity

@Dao
interface MaaMeetingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: MaaMeetingEntity): Long

    @Query("SELECT * FROM MAA_MEETING WHERE id = :id LIMIT 1")
    suspend fun getMaaMeetingById(id: Long): MaaMeetingEntity?

    @Query("select * from MAA_MEETING where syncState = :state")
    fun getBySyncState(state: SyncState): List<MaaMeetingEntity>

    @Query("update MAA_MEETING set syncState = :state where id = :id")
    fun updateSyncState(id: Long, state: SyncState)

    @Query("select * from MAA_MEETING")
    fun getAll(): List<MaaMeetingEntity>

    @Query("SELECT * FROM MAA_MEETING")
    fun getAllMaaData(): Flow<List<MaaMeetingEntity>>

    @Query("""
        DELETE FROM MAA_MEETING
        WHERE id != :serverId
          AND syncState = :syncedState
          AND meetingDate IS :meetingDate
          AND place IS :place
          AND participants IS :participants
          AND ashaId IS :ashaId
          AND villageName IS :villageName
    """)
    fun deleteLocalCopiesOfServerMeeting(
        serverId: Long,
        meetingDate: String?,
        place: String?,
        participants: Int?,
        ashaId: Int?,
        villageName: String?,
        syncedState: SyncState = SyncState.SYNCED
    )

    @Transaction
    fun replaceLocalCopyWithServerMeeting(
        entity: MaaMeetingEntity,
        serverId: Long,
        meetingDate: String?,
        place: String?,
        participants: Int?,
        ashaId: Int?,
        villageName: String?,
        syncedState: SyncState = SyncState.SYNCED
    ) {
        deleteLocalCopiesOfServerMeeting(
            serverId = serverId,
            meetingDate = meetingDate,
            place = place,
            participants = participants,
            ashaId = ashaId,
            villageName = villageName,
            syncedState = syncedState
        )
        insert(entity)
    }

    @Query("delete from MAA_MEETING")
    fun clearAll()

    @Query("UPDATE MAA_MEETING SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}
