package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.EcrDao

/**
 * Contract tests for the Eligible Couple recap data source.
 *
 * IMPORTANT (honest scope): the actual row-matching — `createdBy = :userName`,
 * the `[start, end)` windows over `dateOfReg`/`visitDate`, and the UNION /
 * DISTINCT-benId dedup across the registration and tracking tables — runs inside
 * Room SQL. These pure-JVM tests MOCK the DAO, so they only verify parameter
 * forwarding + the returned aggregate. The SQL row semantics (own vs other
 * createdBy, dedup of a couple that is both registered and tracked, window
 * boundaries) are covered by the instrumented Room DAO test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleRecapDataSourceTest {

    private val dao: EcrDao = mockk()
    private val dataSource = EligibleCoupleRecapDataSource(dao)

    @Test
    fun `forwards exact user and window bounds to the aggregate query`() = runTest {
        coEvery { dao.countCurrentAshaEligibleCouples("meena", 1_000L, 2_000L) } returns 4
        val count = dataSource.countCouples(userName = "meena", startMillis = 1_000L, endMillisExclusive = 2_000L)
        assertEquals(4, count)
        coVerify(exactly = 1) { dao.countCurrentAshaEligibleCouples("meena", 1_000L, 2_000L) }
    }

    @Test
    fun `returns zero when the aggregate query finds nothing`() = runTest {
        coEvery { dao.countCurrentAshaEligibleCouples(any(), any(), any()) } returns 0
        assertEquals(0, dataSource.countCouples("meena", 0L, 10L))
    }
}
