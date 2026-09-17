package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import javax.inject.Inject

/**
 * Monthly Recap (READ-ONLY) source for the Household category: the count of
 * households the current ASHA registered in the frozen recap window. Delegates to
 * [HouseholdDao.countCurrentAshaRegistrations] off the main thread. Returns an
 * aggregate only — no household id/detail leaves the DAO.
 */
class HouseholdRecapDataSource @Inject constructor(
    private val householdDao: HouseholdDao,
) {
    suspend fun countRegistrations(
        userId: Int,
        startMillis: Long,
        endMillisExclusive: Long,
    ): Int = withContext(Dispatchers.IO) {
        householdDao.countCurrentAshaRegistrations(userId, startMillis, endMillisExclusive)
    }
}
