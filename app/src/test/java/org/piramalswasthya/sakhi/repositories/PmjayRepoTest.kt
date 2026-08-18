package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.PmjayDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PMJAYCache
import org.piramalswasthya.sakhi.model.User

@OptIn(ExperimentalCoroutinesApi::class)
class PmjayRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var pmjayDao: PmjayDao

    private lateinit var repo: PmjayRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { database.pmjayDao } returns pmjayDao
        repo = PmjayRepo(database, preferenceDao)
    }

    // =====================================================
    // savePmjayData() Tests
    // =====================================================

    @Test
    fun `savePmjayData returns true on success`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "testUser"
        every { preferenceDao.getLoggedInUser() } returns user

        val pmjayCache = mockk<PMJAYCache>(relaxed = true)
        coEvery { pmjayDao.upsert(pmjayCache) } returns Unit

        val result = repo.savePmjayData(pmjayCache)
        assertTrue(result)
    }

    @Test
    fun `savePmjayData throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val pmjayCache = mockk<PMJAYCache>(relaxed = true)

        try {
            repo.savePmjayData(pmjayCache)
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `savePmjayData delegates to dao upsert on success`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "asha"
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<PMJAYCache>(relaxed = true)
        coEvery { pmjayDao.upsert(cache) } returns Unit

        assertTrue(repo.savePmjayData(cache))
        coVerify(exactly = 1) { pmjayDao.upsert(cache) }
    }

    @Test
    fun `savePmjayData returns false when upsert throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "asha"
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<PMJAYCache>(relaxed = true)
        coEvery { pmjayDao.upsert(cache) } throws RuntimeException("boom")

        assertFalse(repo.savePmjayData(cache))
    }

    @Test
    fun `processNewPmjay throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewPmjay()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `processNewPmjay returns true when no unprocessed records`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { pmjayDao.getAllUnprocessedPMJAY() } returns emptyList()

        assertEquals(true, repo.processNewPmjay())
    }

    @Test
    fun `processNewPmjay returns true after building post model for a single record`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<PMJAYCache>(relaxed = true)
        coEvery { pmjayDao.getAllUnprocessedPMJAY() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(any()) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(any(), any()) } returns mockk(relaxed = true)
        coEvery { database.mdsrDao.mdsrCount() } returns 2

        val result = repo.processNewPmjay()

        assertTrue(result)
        coVerify { database.mdsrDao.mdsrCount() }
    }

    @Test
    fun `processNewPmjay throws when household is missing for a record`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<PMJAYCache>(relaxed = true)
        every { cache.hhId } returns 111L
        coEvery { pmjayDao.getAllUnprocessedPMJAY() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(111L) } returns null

        try {
            repo.processNewPmjay()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No household exists"))
        }
    }

    @Test
    fun `processNewPmjay throws when beneficiary is missing for a record`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<PMJAYCache>(relaxed = true)
        every { cache.hhId } returns 222L
        every { cache.benId } returns 333L
        coEvery { pmjayDao.getAllUnprocessedPMJAY() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(222L) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(222L, 333L) } returns null

        try {
            repo.processNewPmjay()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No beneficiary exists"))
        }
    }

    @Test
    fun `processNewPmjay builds a post model for every unprocessed record`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        val first = mockk<PMJAYCache>(relaxed = true)
        val second = mockk<PMJAYCache>(relaxed = true)
        coEvery { pmjayDao.getAllUnprocessedPMJAY() } returns listOf(first, second)
        coEvery { database.householdDao.getHousehold(any()) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(any(), any()) } returns mockk(relaxed = true)
        coEvery { database.mdsrDao.mdsrCount() } returns 0

        val result = repo.processNewPmjay()

        assertTrue(result)
        coVerify(exactly = 2) { database.householdDao.getHousehold(any()) }
    }

}
