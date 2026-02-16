package org.piramalswasthya.sakhi.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.UwinCache

@Dao
interface UwinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: UwinCache)

    @Update
    suspend fun update(session: UwinCache)

    @Query("SELECT * FROM UWIN_SESSION")
    suspend fun getAllSessions(): List<UwinCache>

    @Query("SELECT * FROM UWIN_SESSION WHERE id = :id LIMIT 1")
    suspend fun getUwinById(id: Int): UwinCache?

    @Query("SELECT * FROM UWIN_SESSION WHERE syncState != :synced")
    suspend fun getUnsyncedSessions(synced: SyncState = SyncState.SYNCED): List<UwinCache>


    @Query("UPDATE UWIN_SESSION SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: Int, syncState: SyncState)

    @Query("SELECT * FROM UWIN_SESSION ORDER BY id DESC")
    fun getAllUwinRecords(): LiveData<List<UwinCache>>

    @Query("DELETE FROM UWIN_SESSION")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(newList: List<UwinCache>) {
        clearAll()
        newList.forEach { insert(it) }
    }

    @Query("UPDATE UWIN_SESSION SET syncState = 0 WHERE syncState = 1")
    suspend fun resetSyncingToUnsynced()
}