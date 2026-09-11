package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.BenDao

/**
 * Contract tests for the Beneficiary recap data source.
 *
 * IMPORTANT (honest scope): the actual row-matching — the `ashaId = :userId`
 * ownership, `isDraft = 0` filter and the `[start, end)` window — runs inside Room
 * SQL. These pure-JVM tests MOCK the DAO, so they only verify the data source
 * forwards the exact parameters and returns the DAO's aggregate. The SQL row
 * semantics (own vs other ASHA, draft excluded, deactivated still counted,
 * boundaries) are covered by the instrumented Room DAO test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiaryRecapDataSourceTest {

    private val dao: BenDao = mockk()
    private val dataSource = BeneficiaryRecapDataSource(dao)

    @Test
    fun `forwards exact user and window bounds to the aggregate query`() = runTest {
        coEvery { dao.countCurrentAshaRegistrations(42, 1_000L, 2_000L) } returns 5
        val count = dataSource.countRegistrations(userId = 42, startMillis = 1_000L, endMillisExclusive = 2_000L)
        assertEquals(5, count)
        coVerify(exactly = 1) { dao.countCurrentAshaRegistrations(42, 1_000L, 2_000L) }
    }

    @Test
    fun `returns zero when the aggregate query finds nothing`() = runTest {
        coEvery { dao.countCurrentAshaRegistrations(any(), any(), any()) } returns 0
        assertEquals(0, dataSource.countRegistrations(42, 0L, 10L))
    }
}
