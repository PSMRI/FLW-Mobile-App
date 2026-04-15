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
import org.piramalswasthya.sakhi.database.room.dao.AesDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class AESRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var aesDao: AesDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var repo: AESRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = AESRepo(aesDao, benDao, preferenceDao, userRepo, amritApiService)
    }

    // =====================================================
    // getAESScreening() Tests
    // =====================================================

    @Test
    fun `getAESScreening returns cache when exists`() = runTest {
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.getAESScreening(1L) } returns cache
        val result = repo.getAESScreening(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getAESScreening returns null when not found`() = runTest {
        coEvery { aesDao.getAESScreening(1L) } returns null
        val result = repo.getAESScreening(1L)
        assertNull(result)
    }

    // =====================================================
    // saveAESScreening() Tests
    // =====================================================

    @Test
    fun `saveAESScreening delegates to dao`() = runTest {
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.saveAESScreening(cache) } returns Unit
        repo.saveAESScreening(cache)
        coVerify { aesDao.saveAESScreening(cache) }
    }

    // =====================================================
    // pushUnSyncedRecords() Tests
    // =====================================================

    @Test
    fun `pushUnSyncedRecords returns true when no unsynced records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedRecords()
        assertTrue(result)
    }

    @Test
    fun `pushUnSyncedRecords always returns true for record-level isolation`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedRecords()
        assertTrue(result)
    }
}
