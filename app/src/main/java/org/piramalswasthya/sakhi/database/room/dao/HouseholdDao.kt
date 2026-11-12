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

    @Query("UPDATE HOUSEHOLD SET processed = :proccess , serverUpdatedStatus =:updateStatus WHERE householdId = :householdId")
    suspend fun updateHouseholdToSync(
        householdId: Long,
        proccess: String,
        updateStatus: Int
    )

    // Household ID 0 is the shared parent row for non-household beneficiaries,
    // not a resumable household-registration draft.
    @Query("SELECT * FROM HOUSEHOLD WHERE isDraft = 1 AND householdId != 0 LIMIT 1")
    suspend fun getDraftHousehold(): HouseholdCache?

    @Query("SELECT * FROM HOUSEHOLD WHERE isDraft = 0 and loc_village_Id = :selectedVillage ORDER BY MAX(IFNULL(updatedTimeStamp, 0), IFNULL(createdTimeStamp, 0)) DESC, householdId DESC")
    fun getAllHouseholds(selectedVillage: Int): Flow<List<HouseholdCache>>

    @Query("SELECT h.*, count(b.householdId) as numMembers FROM HOUSEHOLD as h left outer join BENEFICIARY b on b.householdId = h.householdId WHERE h.isDraft = 0 and h.loc_village_Id = :selectedVillage group by h.householdId ORDER BY MAX(IFNULL(h.updatedTimeStamp, 0), IFNULL(h.createdTimeStamp, 0)) DESC, h.householdId DESC")
    fun getAllHouseholdWithNumMembers(selectedVillage: Int): Flow<List<HouseholdBasicCache>>

    @Query("SELECT h.*, count(b.householdId) as numMembers FROM HOUSEHOLD as h left outer join BENEFICIARY b on b.householdId = h.householdId WHERE h.isDraft = 0 and h.loc_village_Id = :selectedVillage AND h.registrationType = 'Yes' group by h.householdId ORDER BY MAX(IFNULL(h.updatedTimeStamp, 0), IFNULL(h.createdTimeStamp, 0)) DESC, h.householdId DESC")
    fun getAllHouseholdForAshaFamilyMembers(selectedVillage: Int): Flow<List<HouseholdBasicCache>>

    @Query("SELECT COUNT(*) FROM HOUSEHOLD WHERE isDraft = 0 and loc_village_Id = :selectedVillage and isDeactivate =0")
    fun getAllHouseholdsCount(selectedVillage: Int): Flow<Int>

    @Query("SELECT * FROM HOUSEHOLD WHERE householdId =:hhId LIMIT 1")
    suspend fun getHousehold(hhId: Long): HouseholdCache?

    // Never delete the household-0 placeholder: BENEFICIARY references it with
    // ON DELETE CASCADE, and all non-household beneficiaries use that parent.
    @Query("DELETE FROM HOUSEHOLD WHERE isDraft = 1 AND householdId != 0")
    suspend fun deleteDraftHousehold()

    @Query("UPDATE HOUSEHOLD SET processed = 'P' WHERE householdId in(:hhId)")
    suspend fun householdSyncedWithServer(vararg hhId: Long)

    // ID 0 is shared by every non-household beneficiary. Updating its primary
    // key would cascade those beneficiaries into a newly registered household.
    @Query("UPDATE HOUSEHOLD SET householdId = :newId WHERE householdId = :oldId AND householdId != 0")
    suspend fun substituteHouseholdId(oldId: Long, newId: Long)

    @Query("SELECT * FROM household WHERE isDraft = 0 AND processed = 'N'")
    suspend fun getAllUnprocessedHousehold(): List<HouseholdCache>

}
