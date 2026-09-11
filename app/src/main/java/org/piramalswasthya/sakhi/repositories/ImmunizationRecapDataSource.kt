package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import javax.inject.Inject

/**
 * Read-only Monthly Recap data source for Immunization. Returns the count of vaccine
 * DOSES the current ASHA administered in the window (child + mother alike). Ownership
 * rests on the ASHA's own `createdBy` (the IMMUNIZATION table has no `ashaId`); the
 * row semantics — the `createdBy` match and the `[start, end)` window over the
 * user-entered vaccination `date` — live in the DAO SQL
 * ([ImmunizationDao.countCurrentAshaDosesAdministered]). The composite PK makes the
 * count re-sync-proof.
 */
class ImmunizationRecapDataSource @Inject constructor(
    private val immunizationDao: ImmunizationDao,
) {
    suspend fun countDosesAdministered(
        userName: String,
        startMillis: Long,
        endMillisExclusive: Long,
    ): Int = withContext(Dispatchers.IO) {
        immunizationDao.countCurrentAshaDosesAdministered(
            userName = userName,
            startInclusive = startMillis,
            endExclusive = endMillisExclusive,
        )
    }
}
