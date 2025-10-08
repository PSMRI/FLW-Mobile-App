package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.MaaMeetingEntity

@Dao
interface MaaMeetingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: MaaMeetingEntity): Long

    @Update
    fun update(entity: MaaMeetingEntity)

    @Query("select * from MAA_MEETING order by updatedAt desc limit 1")
    fun getLatest(): MaaMeetingEntity?

    @Query("select * from MAA_MEETING where syncState = :state")
    fun getBySyncState(state: SyncState): List<MaaMeetingEntity>

    @Query("update MAA_MEETING set syncState = :state where id = :id")
    fun updateSyncState(id: Long, state: SyncState)

    @Query("select * from MAA_MEETING")
    fun getAll(): List<MaaMeetingEntity>

    @Query("delete from MAA_MEETING")
    fun clearAll()
}


