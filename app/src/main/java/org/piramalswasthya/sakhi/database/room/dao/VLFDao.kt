package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.VHNDCache

@Dao
interface VLFDao {

    @Query("select * from VHND where id = :id limit 1")
    fun getVHND(id: Int): VHNDCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRecord(vhndCache: VHNDCache)

    @Query("SELECT * FROM VHND")
     fun getAllVHND(): Flow<List<VHNDCache>>

}