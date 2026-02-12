package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.AdolescentHealthCache

@Dao
interface AdolescentHealthDao {
    @Query("SELECT * FROM ADOLESCENT_HEALTH_FORM_DATA WHERE benId =:benId AND isDraft = 0 LIMIT 1")
    suspend fun getAdolescentHealth(benId: Long): AdolescentHealthCache?

    @Query("SELECT * FROM Adolescent_Health_Form_Data WHERE benId =:benId AND isDraft = 1 LIMIT 1")
    suspend fun getDraftAdolescentHealth(benId: Long): AdolescentHealthCache?

    @Query("SELECT * FROM Adolescent_Health_Form_Data WHERE benId =:benId and (visitDate = :visitDate or visitDate = :visitDateGMT) limit 1")
    suspend fun getAdolescentHealth(benId: Long, visitDate: Long, visitDateGMT: Long): AdolescentHealthCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdolescentHealth(tbScreeningCache: AdolescentHealthCache)

    @Query("SELECT * FROM Adolescent_Health_Form_Data WHERE  syncState = :syncState")
    suspend fun getAdolescentHealth(syncState: SyncState): List<AdolescentHealthCache>

    @Query("DELETE FROM Adolescent_Health_Form_Data WHERE id = :id")
    suspend fun deleteAdolescentHealthById(id: Int)

}