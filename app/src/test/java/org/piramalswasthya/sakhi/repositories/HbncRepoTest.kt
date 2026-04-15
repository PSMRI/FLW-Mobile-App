package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
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
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HbncHomeVisit
import org.piramalswasthya.sakhi.model.HbncVisitCard
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class HbncRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var hbncDao: HbncDao
    @MockK private lateinit var benDao: BenDao

    private lateinit var repo: HbncRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.hbncDao } returns hbncDao
        repo = HbncRepo(database, amritApiService, userRepo, preferenceDao, hbncDao, benDao)
    }

    // =====================================================
    // getHbncRecord() Tests
    // =====================================================

    @Test
    fun `getHbncRecord returns record when exists`() = runTest {
        val hbnc = mockk<HBNCCache>()
        coEvery { hbncDao.getHbnc(100L, 200L, 1) } returns hbnc

        val result = repo.getHbncRecord(100L, 200L, 1)

        assertNotNull(result)
        assertEquals(hbnc, result)
    }

    @Test
    fun `getHbncRecord returns null when not exists`() = runTest {
        coEvery { hbncDao.getHbnc(999L, 999L, 1) } returns null

        val result = repo.getHbncRecord(999L, 999L, 1)

        assertNull(result)
    }

    // =====================================================
    // getHbncCard() Tests
    // =====================================================

    @Test
    fun `getHbncCard returns card when exists`() = runTest {
        val card = mockk<HBNCCache>(relaxed = true)
        coEvery { database.hbncDao.getHbnc(200L, 100L, Konstants.hbncCardDay) } returns card

        val result = repo.getHbncCard(100L, 200L)

        assertNotNull(result)
    }

    @Test
    fun `getHbncCard returns null when not exists`() = runTest {
        coEvery { hbncDao.getHbnc(any(), any(), eq(Konstants.hbncCardDay)) } returns null

        val result = repo.getHbncCard(999L, 999L)

        assertNull(result)
    }

    // =====================================================
    // getFirstHomeVisit() Tests
    // =====================================================

    @Test
    fun `getFirstHomeVisit returns visit when exists`() = runTest {
        val visit = mockk<HBNCCache>(relaxed = true)
        coEvery { database.hbncDao.getHbnc(100L, 200L, 1) } returns visit

        val result = repo.getFirstHomeVisit(100L, 200L)

        assertNotNull(result)
    }

    @Test
    fun `getFirstHomeVisit returns null when not exists`() = runTest {
        coEvery { hbncDao.getHbnc(any(), any(), eq(1)) } returns null

        val result = repo.getFirstHomeVisit(999L, 999L)

        assertNull(result)
    }

    // =====================================================
    // processNewHbnc() Tests
    // =====================================================

    @Test
    fun `processNewHbnc throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewHbnc()
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }
}
