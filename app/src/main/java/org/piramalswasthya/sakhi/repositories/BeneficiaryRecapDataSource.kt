package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import javax.inject.Inject

/**
 * Monthly Recap (READ-ONLY) source for the Beneficiary category: the count of
 * beneficiaries (family members) the current ASHA registered in the frozen recap
 * window. Delegates to [BenDao.countCurrentAshaRegistrations] off the main thread.
 * Returns an aggregate only — no beneficiary id/detail leaves the DAO.
 */
class BeneficiaryRecapDataSource @Inject constructor(
    private val benDao: BenDao,
) {
    suspend fun countRegistrations(
        userId: Int,
        startMillis: Long,
        endMillisExclusive: Long,
    ): Int = withContext(Dispatchers.IO) {
        benDao.countCurrentAshaRegistrations(userId, startMillis, endMillisExclusive)
    }
}
