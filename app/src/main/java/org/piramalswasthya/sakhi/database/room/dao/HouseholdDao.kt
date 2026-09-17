package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.HouseholdBasicCache
import org.piramalswasthya.sakhi.model.HouseholdCache

@Dao
interface HouseholdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg household: HouseholdCache)

    @Update
    suspend fun update(household: HouseholdCache)

    /**
     * Monthly Recap (READ-ONLY): counts households the current ASHA registered in
     * the window [startInclusive, endExclusive). Ownership is `ashaId = :userId`
     * (local saves set the logged-in userId; downloaded rows preserve the original
     * owner's ashaId). Drafts excluded. `householdId` is the PRIMARY KEY, so each
     * registration is one row (COUNT counts it once); rows with a null/0
     * createdTimeStamp fall outside the range and are excluded. Returns an
     * aggregate only.
     */
    @Query(
        "SELECT COUNT(*) FROM HOUSEHOLD " +
                "WHERE ashaId = :userId AND isDraft = 0 " +
                "AND createdTimeStamp >= :startInclusive AND createdTimeStamp < :endExclusive"
    )
    suspend fun countCurrentAshaRegistrations(
        userId: Int,
        startInclusive: Long,
        endExclusive: Long,
    ): Int

    @Query("UPDATE HOUSEHOLD SET processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId")
    suspend fun updateHouseholdToSync(
        householdId: Long,
        proccess: String,
        updateStatus: Int
    )

    @Query("SELECT * FROM HOUSEHOLD WHERE isDraft = 1 LIMIT 1")
    suspend fun getDraftHousehold(): HouseholdCache?

    @Query("SELECT * FROM HOUSEHOLD WHERE isDraft = 0 and loc_village_Id = :selectedVillage")
    fun getAllHouseholds(selectedVillage: Int): Flow<List<HouseholdCache>>

    @Query("SELECT h.*, count(b.householdId) as numMembers FROM HOUSEHOLD as h left outer join BENEFICIARY b on b.householdId = h.householdId WHERE h.isDraft = 0 and h.loc_village_Id = :selectedVillage group by h.householdId")
    fun getAllHouseholdWithNumMembers(selectedVillage: Int): Flow<List<HouseholdBasicCache>>

    @Query("SELECT h.*, count(b.householdId) as numMembers FROM HOUSEHOLD as h left outer join BENEFICIARY b on b.householdId = h.householdId WHERE h.isDraft = 0 and h.loc_village_Id = :selectedVillage AND h.registrationType = 'Yes' group by h.householdId")
    fun getAllHouseholdForAshaFamilyMembers(selectedVillage: Int): Flow<List<HouseholdBasicCache>>

    @Query("SELECT COUNT(*) FROM HOUSEHOLD WHERE isDraft = 0 and loc_village_Id = :selectedVillage and isDeactivate =0")
    fun getAllHouseholdsCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT * FROM HOUSEHOLD WHERE householdId =:hhId LIMIT 1")
    suspend fun getHousehold(hhId: Long): HouseholdCache?

    @Query("DELETE  FROM HOUSEHOLD WHERE isDraft=1")
    suspend fun deleteDraftHousehold()

    @Query("UPDATE HOUSEHOLD SET processed = 'P' WHERE householdId in(:hhId)")
    suspend fun householdSyncedWithServer(vararg hhId: Long)

    @Query("UPDATE HOUSEHOLD SET householdId  = :newId WHERE householdId = :oldId ")
    suspend fun substituteHouseholdId(oldId: Long, newId: Long)

    @Query("SELECT * FROM household WHERE isDraft = 0 AND processed = 'N'")
    suspend fun getAllUnprocessedHousehold(): List<HouseholdCache>

}