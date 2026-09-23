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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.FpotDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FPOTCache
import org.piramalswasthya.sakhi.model.User

/**
 * Unit tests for [FpotRepo]. Consolidated from the previously separate
 * FpotRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FpotRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var fpotDao: FpotDao

    private lateinit var repo: FpotRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { database.fpotDao } returns fpotDao
        repo = FpotRepo(database, preferenceDao)
    }

    @Test
    fun `saveFpotData returns true on success`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "testUser"
        every { preferenceDao.getLoggedInUser() } returns user

        val fpotCache = mockk<FPOTCache>(relaxed = true)
        coEvery { fpotDao.upsert(fpotCache) } returns Unit

        val result = repo.saveFpotData(fpotCache)
        assertTrue(result)
    }

    @Test
    fun `saveFpotData throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val fpotCache = mockk<FPOTCache>(relaxed = true)

        try {
            repo.saveFpotData(fpotCache)
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `saveFpotData delegates to dao upsert on success`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "asha"
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<FPOTCache>(relaxed = true)
        coEvery { fpotDao.upsert(cache) } returns Unit

        assertTrue(repo.saveFpotData(cache))
        coVerify(exactly = 1) { fpotDao.upsert(cache) }
    }

    @Test
    fun `saveFpotData returns false when upsert throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "asha"
        every { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<FPOTCache>(relaxed = true)
        coEvery { fpotDao.upsert(cache) } throws RuntimeException("boom")

        assertFalse(repo.saveFpotData(cache))
    }
}
