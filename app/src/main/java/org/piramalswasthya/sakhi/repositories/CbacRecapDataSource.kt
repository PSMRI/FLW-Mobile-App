package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import javax.inject.Inject

/**
 * Read-only recap view over CBAC (NCD screening) records for one ASHA.
 *
 * Delegates to an aggregate DAO query that counts DISTINCT beneficiaries with a
 * completed CBAC screening in the recap window, owned by the current ASHA:
 *   - locally authored rows (ashaId == userId), OR
 *   - server-downloaded rows (ashaId == 0 AND createdBy == userName).
 * Sync state is not filtered (offline-first). Only an aggregate Int is returned;
 * no beneficiary id ever leaves the DAO.
 */
class CbacRecapDataSource @Inject constructor(
    private val cbacDao: CbacDao,
) {
    /**
     * @param startMillis inclusive window start
     * @param endMillisExclusive exclusive window end
     */
    suspend fun countScreeningEvents(
        userId: Int,
        userName: String,
        startMillis: Long,
        endMillisExclusive: Long,
    ): Int = withContext(Dispatchers.IO) {
        cbacDao.countCurrentAshaScreenings(
            userId = userId,
            userName = userName,
            startInclusive = startMillis,
            endExclusive = endMillisExclusive,
        )
    }
}
