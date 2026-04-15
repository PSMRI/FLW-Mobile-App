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
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class PncRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var maternalHealthDao: MaternalHealthDao
    @MockK private lateinit var pncDao: PncDao
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var repo: PncRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = PncRepo(amritApiService, maternalHealthDao, pncDao, database, userRepo, benDao, preferenceDao)
    }

    // =====================================================
    // getSavedPncRecord() Tests
    // =====================================================

    @Test
    fun `getSavedPncRecord returns record when exists`() = runTest {
        val pnc = mockk<PNCVisitCache>()
        coEvery { pncDao.getSavedRecord(100L, 1) } returns pnc

        val result = repo.getSavedPncRecord(100L, 1)

        assertNotNull(result)
        assertEquals(pnc, result)
    }

    @Test
    fun `getSavedPncRecord returns null when not exists`() = runTest {
        coEvery { pncDao.getSavedRecord(999L, 1) } returns null

        val result = repo.getSavedPncRecord(999L, 1)

        assertNull(result)
    }

    @Test
    fun `getSavedPncRecord passes correct visit number`() = runTest {
        coEvery { pncDao.getSavedRecord(any(), any()) } returns null

        repo.getSavedPncRecord(100L, 3)

        coVerify { pncDao.getSavedRecord(100L, 3) }
    }

    // =====================================================
    // getLastFilledPncRecord() Tests
    // =====================================================

    @Test
    fun `getLastFilledPncRecord returns record when exists`() = runTest {
        val pnc = mockk<PNCVisitCache>()
        coEvery { pncDao.getLastSavedRecord(100L) } returns pnc

        val result = repo.getLastFilledPncRecord(100L)

        assertNotNull(result)
        assertEquals(pnc, result)
    }

    @Test
    fun `getLastFilledPncRecord returns null when no records`() = runTest {
        coEvery { pncDao.getLastSavedRecord(999L) } returns null

        val result = repo.getLastFilledPncRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // persistPncRecord() Tests
    // =====================================================

    @Test
    fun `persistPncRecord calls dao insert`() = runTest {
        val pnc = mockk<PNCVisitCache>()
        coEvery { pncDao.insert(pnc) } returns Unit

        repo.persistPncRecord(pnc)

        coVerify(exactly = 1) { pncDao.insert(pnc) }
    }

    // =====================================================
    // processPncVisits() Tests
    // =====================================================

    @Test
    fun `processPncVisits throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processPncVisits()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processPncVisits returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns emptyList()

        val result = repo.processPncVisits()

        assertEquals(true, result)
    }
}
