package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.CbacDao

/**
 * Contract tests for the CBAC recap data source.
 *
 * IMPORTANT (honest scope): the actual row-matching — the ownership predicate
 * `ashaId = :userId OR (ashaId = 0 AND createdBy = :userName)`, the
 * `[start, end)` window and `COUNT(DISTINCT benId)` — runs inside Room SQL.
 * These pure-JVM tests MOCK the DAO, so they do NOT execute that SQL; they only
 * verify the data source forwards the exact parameters and returns the DAO's
 * aggregate. The SQL row semantics (scenarios: current-user local row included,
 * downloaded ashaId=0 + matching createdBy included, different/blank createdBy
 * excluded, another ASHA excluded, boundaries, sync-independence, distinct
 * beneficiary) require a Room instrumented (androidTest) DAO test with real rows
 * — see the Phase 4.3 report.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CbacRecapDataSourceTest {

    private val dao: CbacDao = mockk()
    private val dataSource = CbacRecapDataSource(dao)

    @Test
    fun `forwards exact user, name and window bounds to the aggregate query`() = runTest {
        coEvery { dao.countCurrentAshaScreenings(42, "meena", 1_000L, 2_000L) } returns 5
        val count = dataSource.countScreeningEvents(
            userId = 42, userName = "meena", startMillis = 1_000L, endMillisExclusive = 2_000L,
        )
        assertEquals(5, count)
        coVerify(exactly = 1) { dao.countCurrentAshaScreenings(42, "meena", 1_000L, 2_000L) }
    }

    @Test
    fun `returns zero when the aggregate query finds nothing`() = runTest {
        coEvery { dao.countCurrentAshaScreenings(any(), any(), any(), any()) } returns 0
        assertEquals(0, dataSource.countScreeningEvents(42, "meena", 0L, 10L))
    }
}
