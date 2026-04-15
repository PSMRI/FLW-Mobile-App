package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class HbycRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var hbycDao: HbycDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var repo: HbycRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.hbycDao } returns hbycDao
        repo = HbycRepo(database, preferenceDao, hbycDao, benDao, userRepo, amritApiService)
    }

    // =====================================================
    // saveHbycData() Tests
    // =====================================================

    @Test
    fun `saveHbycData saves and returns true when user logged in`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val hbyc = mockk<HBYCCache>(relaxed = true)
        coEvery { hbycDao.upsert(hbyc) } returns Unit

        val result = repo.saveHbycData(hbyc)

        assertTrue(result)
        coVerify { hbycDao.upsert(hbyc) }
    }

    @Test
    fun `saveHbycData throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        val hbyc = mockk<HBYCCache>(relaxed = true)

        try {
            repo.saveHbycData(hbyc)
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    // =====================================================
    // processNewHbyc() Tests
    // =====================================================

    @Test
    fun `processNewHbyc throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewHbyc()
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewHbyc returns true when no unprocessed records`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { hbycDao.getAllUnprocessedHBYC() } returns emptyList()

        val result = repo.processNewHbyc()

        assertEquals(true, result)
    }
}
