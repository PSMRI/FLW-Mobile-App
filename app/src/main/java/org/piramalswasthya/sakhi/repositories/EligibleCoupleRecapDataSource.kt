package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.EcrDao
import javax.inject.Inject

/**
 * Monthly Recap (READ-ONLY) source for the Eligible Couple category: the count of
 * DISTINCT couples the current ASHA registered OR tracked for family planning in
 * the frozen recap window. Delegates to [EcrDao.countCurrentAshaEligibleCouples]
 * off the main thread. Returns an aggregate only — no beneficiary id/detail leaves
 * the DAO.
 */
class EligibleCoupleRecapDataSource @Inject constructor(
    private val ecrDao: EcrDao,
) {
    suspend fun countCouples(
        userName: String,
        startMillis: Long,
        endMillisExclusive: Long,
    ): Int = withContext(Dispatchers.IO) {
        ecrDao.countCurrentAshaEligibleCouples(userName, startMillis, endMillisExclusive)
    }
}
