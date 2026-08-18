package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao

/**
 * Contract tests for the Immunization recap data source.
 *
 * IMPORTANT (honest scope): the actual row-matching — `createdBy = :userName` and the
 * `[start, end)` window over the vaccination `date` — runs inside Room SQL. These
 * pure-JVM tests MOCK the DAO, so they only verify parameter forwarding + the returned
 * aggregate. The SQL row semantics (own vs other createdBy, per-dose counting,
 * NULL-date exclusion, composite-PK no-duplication, window boundaries) are covered by
 * the instrumented Room DAO test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImmunizationRecapDataSourceTest {

    private val dao: ImmunizationDao = mockk()
    private val dataSource = ImmunizationRecapDataSource(dao)

    @Test
    fun `forwards exact user and window bounds to the aggregate query`() = runTest {
        coEvery { dao.countCurrentAshaDosesAdministered("meena", 1_000L, 2_000L) } returns 12
        val count = dataSource.countDosesAdministered(userName = "meena", startMillis = 1_000L, endMillisExclusive = 2_000L)
        assertEquals(12, count)
        coVerify(exactly = 1) { dao.countCurrentAshaDosesAdministered("meena", 1_000L, 2_000L) }
    }

    @Test
    fun `returns zero when the aggregate query finds nothing`() = runTest {
        coEvery { dao.countCurrentAshaDosesAdministered(any(), any(), any()) } returns 0
        assertEquals(0, dataSource.countDosesAdministered("meena", 0L, 10L))
    }
}
