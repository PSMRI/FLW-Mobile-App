package org.piramalswasthya.sakhi.repositories

import android.content.Context
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var leprosyDao: LeprosyDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var context: Context

    private lateinit var repo: LeprosyRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = LeprosyRepo(leprosyDao, benDao, preferenceDao, userRepo, tmcNetworkApiService, context)
    }

    @Test
    fun `repo initializes successfully`() {
        assertNotNull(repo)
    }

    // =====================================================
    // getLeprosyScreening() Tests
    // =====================================================

    @Test
    fun `getLeprosyScreening returns cache when exists`() = runTest {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.getLeprosyScreening(1L) } returns cache
        val result = repo.getLeprosyScreening(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getLeprosyScreening returns null when not found`() = runTest {
        coEvery { leprosyDao.getLeprosyScreening(1L) } returns null
        val result = repo.getLeprosyScreening(1L)
        assertNull(result)
    }

    @Test
    fun `getLeprosyScreening with zero benId`() = runTest {
        coEvery { leprosyDao.getLeprosyScreening(0L) } returns null
        val result = repo.getLeprosyScreening(0L)
        assertNull(result)
    }

    // =====================================================
    // saveLeprosyScreening() Tests
    // =====================================================

    @Test
    fun `saveLeprosyScreening delegates to dao`() = runTest {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.saveLeprosyScreening(cache) } returns Unit
        repo.saveLeprosyScreening(cache)
        coVerify { leprosyDao.saveLeprosyScreening(cache) }
    }

    // =====================================================
    // updateLeprosyScreening() Tests
    // =====================================================

    @Test
    fun `updateLeprosyScreening delegates to dao`() = runTest {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.updateLeprosyScreening(cache) } returns Unit
        repo.updateLeprosyScreening(cache)
        coVerify { leprosyDao.updateLeprosyScreening(cache) }
    }

    // =====================================================
    // pushUnSyncedRecords() Tests
    // =====================================================

    @Test(expected = IllegalStateException::class)
    fun `getLeprosyScreeningDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getLeprosyScreeningDetailsFromServer()
    }
}
