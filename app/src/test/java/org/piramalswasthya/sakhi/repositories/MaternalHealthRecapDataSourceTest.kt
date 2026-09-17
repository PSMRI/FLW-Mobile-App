package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao

/**
 * Contract tests for the Maternal Health recap data source.
 *
 * IMPORTANT (honest scope): the actual row-matching — `createdBy = :userName`, the
 * `[start, end)` windows over each table's activity date, and the UNION /
 * DISTINCT-benId dedup across the five maternal tables — runs inside Room SQL. These
 * pure-JVM tests MOCK the DAO, so they only verify parameter forwarding + the
 * returned aggregate. The SQL row semantics (own vs other createdBy, dedup of a woman
 * seen across several forms, window boundaries) are covered by the instrumented Room
 * DAO test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MaternalHealthRecapDataSourceTest {

    private val dao: MaternalHealthDao = mockk()
    private val dataSource = MaternalHealthRecapDataSource(dao)

    @Test
    fun `forwards exact user and window bounds to the aggregate query`() = runTest {
        coEvery { dao.countCurrentAshaMothersSupported("meena", 1_000L, 2_000L) } returns 6
        val count = dataSource.countMothersSupported(userName = "meena", startMillis = 1_000L, endMillisExclusive = 2_000L)
        assertEquals(6, count)
        coVerify(exactly = 1) { dao.countCurrentAshaMothersSupported("meena", 1_000L, 2_000L) }
    }

    @Test
    fun `returns zero when the aggregate query finds nothing`() = runTest {
        coEvery { dao.countCurrentAshaMothersSupported(any(), any(), any()) } returns 0
        assertEquals(0, dataSource.countMothersSupported("meena", 0L, 10L))
    }
}
