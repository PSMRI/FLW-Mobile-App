package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class PmsmaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var pmsmaDao: PmsmaDao

    private lateinit var repo: PmsmaRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = PmsmaRepo(preferenceDao, amritApiService, userRepo, benDao, pmsmaDao)
    }

    // =====================================================
    // getPmsmaByBenId() Tests
    // =====================================================

    @Test
    fun `getPmsmaByBenId returns record when exists`() = runTest {
        val pmsma = mockk<PMSMACache>()
        coEvery { pmsmaDao.getPmsma(100L) } returns pmsma

        val result = repo.getPmsmaByBenId(100L)

        assertNotNull(result)
        assertEquals(pmsma, result)
    }

    @Test
    fun `getPmsmaByBenId returns null when not exists`() = runTest {
        coEvery { pmsmaDao.getPmsma(999L) } returns null

        val result = repo.getPmsmaByBenId(999L)

        assertNull(result)
    }

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns record when exists`() = runTest {
        val pmsma = mockk<PMSMACache>()
        coEvery { pmsmaDao.getSavedRecord(100L, 1) } returns pmsma

        val result = repo.getSavedRecord(100L, 1)

        assertNotNull(result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { pmsmaDao.getSavedRecord(999L, 1) } returns null

        val result = repo.getSavedRecord(999L, 1)

        assertNull(result)
    }

    // =====================================================
    // getLastPmsmaVisit() Tests
    // =====================================================

    @Test
    fun `getLastPmsmaVisit returns record when exists`() = runTest {
        val pmsma = mockk<PMSMACache>()
        coEvery { pmsmaDao.getLastPmsmaVisit(100L) } returns pmsma

        val result = repo.getLastPmsmaVisit(100L)

        assertNotNull(result)
    }

    @Test
    fun `getLastPmsmaVisit returns null when not exists`() = runTest {
        coEvery { pmsmaDao.getLastPmsmaVisit(999L) } returns null

        val result = repo.getLastPmsmaVisit(999L)

        assertNull(result)
    }

    // =====================================================
    // getActiveAncCountForBenIds() Tests
    // =====================================================

    @Test
    fun `getActiveAncCountForBenIds returns count`() = runTest {
        coEvery { pmsmaDao.getActiveAncCountForBenIds(100L) } returns 3

        val result = repo.getActiveAncCountForBenIds(100L)

        assertEquals(3, result)
    }

    @Test
    fun `getActiveAncCountForBenIds returns zero when none`() = runTest {
        coEvery { pmsmaDao.getActiveAncCountForBenIds(999L) } returns 0

        val result = repo.getActiveAncCountForBenIds(999L)

        assertEquals(0, result)
    }

    // =====================================================
    // savePmsmaData() Tests
    // =====================================================

    @Test
    fun `savePmsmaData saves and returns true when user logged in`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val pmsma = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.upsert(pmsma) } returns Unit

        val result = repo.savePmsmaData(pmsma)

        assertTrue(result)
        coVerify { pmsmaDao.upsert(pmsma) }
    }

    @Test
    fun `savePmsmaData throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        val pmsma = mockk<PMSMACache>(relaxed = true)

        try {
            repo.savePmsmaData(pmsma)
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    // =====================================================
    // processNewPmsma() Tests
    // =====================================================

    @Test
    fun `processNewPmsma throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewPmsma()
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewPmsma returns true when no unprocessed records`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns emptyList()

        val result = repo.processNewPmsma()

        assertEquals(true, result)
    }
}
