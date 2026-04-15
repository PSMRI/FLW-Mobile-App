package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
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
}
