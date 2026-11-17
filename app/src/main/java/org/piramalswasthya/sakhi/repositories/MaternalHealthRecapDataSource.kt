package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import javax.inject.Inject

/**
 * Read-only Monthly Recap data source for Maternal Health. Returns the count of
 * DISTINCT mothers / pregnant women the current ASHA supported in the window via
 * ANY maternal activity (pregnancy registration, ANC, PMSMA, delivery outcome, PNC).
 *
 * Ownership rests on the ASHA's own `createdBy` (these tables carry no `ashaId`);
 * the row semantics — the `createdBy` match, the `[start, end)` windows over each
 * table's activity date, and the `UNION`/`DISTINCT benId` dedup — live in the DAO
 * SQL ([MaternalHealthDao.countCurrentAshaMothersSupported]).
 */
class MaternalHealthRecapDataSource @Inject constructor(
    private val maternalHealthDao: MaternalHealthDao,
) {
    suspend fun countMothersSupported(
        userName: String,
        startMillis: Long,
        endMillisExclusive: Long,
    ): Int = withContext(Dispatchers.IO) {
        maternalHealthDao.countCurrentAshaMothersSupported(
            userName = userName,
            startInclusive = startMillis,
            endExclusive = endMillisExclusive,
        )
    }
}
