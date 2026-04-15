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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class MalariaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var malariaDao: MalariaDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var tmcNetworkApiService: AmritApiService

    private lateinit var repo: MalariaRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = MalariaRepo(malariaDao, benDao, preferenceDao, userRepo, tmcNetworkApiService)
    }

    @Test
    fun `repo initializes successfully`() {
        assertNotNull(repo)
    }

    // =====================================================
    // getLatestVisitForBen() Tests
    // =====================================================

    @Test
    fun `getLatestVisitForBen returns cache when exists`() = runTest {
        val cache = mockk<MalariaScreeningCache>(relaxed = true)
        coEvery { malariaDao.getLatestVisitForBen(1L) } returns cache
        val result = repo.getLatestVisitForBen(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getLatestVisitForBen returns null when not found`() = runTest {
        coEvery { malariaDao.getLatestVisitForBen(1L) } returns null
        val result = repo.getLatestVisitForBen(1L)
        assertNull(result)
    }

    // =====================================================
    // getlastvisitIdforBen() Tests
    // =====================================================

    @Test
    fun `getlastvisitIdforBen returns id when exists`() = runTest {
        coEvery { malariaDao.getLastVisitIdForBen(1L) } returns 42L
        val result = repo.getlastvisitIdforBen(1L)
        assertEquals(42L, result)
    }

    @Test
    fun `getlastvisitIdforBen returns null when not found`() = runTest {
        coEvery { malariaDao.getLastVisitIdForBen(1L) } returns null
        val result = repo.getlastvisitIdforBen(1L)
        assertNull(result)
    }

    // =====================================================
    // saveMalariaScreening() Tests
    // =====================================================

    @Test
    fun `saveMalariaScreening delegates to dao`() = runTest {
        val cache = mockk<MalariaScreeningCache>(relaxed = true)
        coEvery { malariaDao.saveMalariaScreening(cache) } returns Unit
        repo.saveMalariaScreening(cache)
        coVerify { malariaDao.saveMalariaScreening(cache) }
    }

    // =====================================================
    // getMalariaConfirmed() Tests
    // =====================================================

    @Test
    fun `getMalariaConfirmed returns cache when exists`() = runTest {
        val cache = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        coEvery { malariaDao.getMalariaConfirmed(1L) } returns cache
        val result = repo.getMalariaConfirmed(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getMalariaConfirmed returns null when not found`() = runTest {
        coEvery { malariaDao.getMalariaConfirmed(1L) } returns null
        val result = repo.getMalariaConfirmed(1L)
        assertNull(result)
    }

    // =====================================================
    // saveMalariaConfirmed() Tests
    // =====================================================

    @Test
    fun `saveMalariaConfirmed delegates to dao`() = runTest {
        val cache = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        coEvery { malariaDao.saveMalariaConfirmed(cache) } returns Unit
        repo.saveMalariaConfirmed(cache)
        coVerify { malariaDao.saveMalariaConfirmed(cache) }
    }

    // =====================================================
    // IRS Screening Tests
    // =====================================================

    @Test
    fun `getIRSScreening returns cache when exists`() = runTest {
        val cache = mockk<IRSRoundScreening>(relaxed = true)
        coEvery { malariaDao.getIRSScreening(1L) } returns cache
        val result = repo.getIRSScreening(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getIRSScreening returns null when not found`() = runTest {
        coEvery { malariaDao.getIRSScreening(1L) } returns null
        val result = repo.getIRSScreening(1L)
        assertNull(result)
    }

    @Test
    fun `saveIRSScreening delegates to dao`() = runTest {
        val cache = mockk<IRSRoundScreening>(relaxed = true)
        coEvery { malariaDao.saveIRSScreening(cache) } returns Unit
        repo.saveIRSScreening(cache)
        coVerify { malariaDao.saveIRSScreening(cache) }
    }

    @Test
    fun `getAllActiveIRSRecords returns empty list when none`() = runTest {
        coEvery { malariaDao.getAllActiveIRSRecords(1L) } returns emptyList()
        val result = repo.getAllActiveIRSRecords(1L)
        assertEquals(0, result.size)
    }

    @Test
    fun `getAllActiveIRSRecords returns records when exist`() = runTest {
        val records = listOf(mockk<IRSRoundScreening>(relaxed = true))
        coEvery { malariaDao.getAllActiveIRSRecords(1L) } returns records
        val result = repo.getAllActiveIRSRecords(1L)
        assertEquals(1, result.size)
    }
}
