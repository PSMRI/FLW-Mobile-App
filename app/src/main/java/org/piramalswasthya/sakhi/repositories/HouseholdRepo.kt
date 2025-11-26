package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import javax.inject.Inject

class HouseholdRepo @Inject constructor(
    private val dao: HouseholdDao,
    private val benDao: BenDao
) {
    suspend fun getDraftRecord(): HouseholdCache? {
        return withContext(Dispatchers.IO) {
            dao.getDraftHousehold()
        }
    }


    suspend fun getRecord(hhId: Long): HouseholdCache? {
        return withContext(Dispatchers.IO) {
            dao.getHousehold(hhId)
        }
    }

    suspend fun persistRecord(householdCache: HouseholdCache?, isFinal: Boolean = false) {
        withContext(Dispatchers.IO) {
            householdCache?.let {
                if (dao.getHousehold(it.householdId) == null)
                    dao.upsert(it)
                else
                    dao.update(it)
            }
        }
    }

    suspend fun substituteHouseholdIdForDraft(it: HouseholdCache) {
        dao.substituteHouseholdId(0, it.householdId)
        it.isDraft = false
    }

    suspend fun getAllBenOfHousehold(householdId: Long): List<BenRegCache> {
        return withContext(Dispatchers.IO) {
            benDao.getAllBenForHousehold(householdId)
        }
    }

    suspend fun deleteHouseholdDraft() {
        withContext(Dispatchers.IO) {
            dao.deleteDraftHousehold()
        }
    }

    suspend fun updateHousehold(household: HouseholdCache) {
        withContext(Dispatchers.IO) {
            dao.update(household)
        }
    }




}