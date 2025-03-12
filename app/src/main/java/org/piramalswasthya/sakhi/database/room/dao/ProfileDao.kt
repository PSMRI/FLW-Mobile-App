package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.model.ProfileActivityCache
@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg profileActivity: ProfileActivityCache)

    @Query("select * from PROFILE_ACTIVITY where employeeId = :id")
    fun getProfileActivityById(id: Long): ProfileActivityCache?
}