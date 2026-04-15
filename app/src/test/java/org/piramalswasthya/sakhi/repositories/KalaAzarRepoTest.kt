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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.KalaAzarDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class KalaAzarRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var kalaAzarDao: KalaAzarDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var repo: KalaAzarRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = KalaAzarRepo(kalaAzarDao, benDao, preferenceDao, userRepo, amritApiService)
    }

    // =====================================================
    // getKalaAzarScreening() Tests
    // =====================================================

    @Test
    fun `getKalaAzarScreening returns cache when exists`() = runTest {
        val cache = mockk<KalaAzarScreeningCache>(relaxed = true)
        coEvery { kalaAzarDao.getKalaAzarScreening(1L) } returns cache
        val result = repo.getKalaAzarScreening(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getKalaAzarScreening returns null when not found`() = runTest {
        coEvery { kalaAzarDao.getKalaAzarScreening(1L) } returns null
        val result = repo.getKalaAzarScreening(1L)
        assertNull(result)
    }

    // =====================================================
    // saveKalaAzarScreening() Tests
    // =====================================================

    @Test
    fun `saveKalaAzarScreening delegates to dao`() = runTest {
        val cache = mockk<KalaAzarScreeningCache>(relaxed = true)
        coEvery { kalaAzarDao.saveKalaAzarScreening(cache) } returns Unit
        repo.saveKalaAzarScreening(cache)
        coVerify { kalaAzarDao.saveKalaAzarScreening(cache) }
    }

    // =====================================================
    // getKalaAzarSuspected() Tests
    // =====================================================

    @Test
    fun `getKalaAzarSuspected returns cache when exists`() = runTest {
        val cache = mockk<KalaAzarScreeningCache>(relaxed = true)
        coEvery { kalaAzarDao.getKalaAzarSuspected(1L) } returns cache
        val result = repo.getKalaAzarSuspected(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getKalaAzarSuspected returns null when not found`() = runTest {
        coEvery { kalaAzarDao.getKalaAzarSuspected(1L) } returns null
        val result = repo.getKalaAzarSuspected(1L)
        assertNull(result)
    }

    // =====================================================
    // pushUnSyncedRecords() Tests
    // =====================================================

    @Test
    fun `pushUnSyncedRecords returns true when no unsynced records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedRecords()
        assertTrue(result)
    }
}
