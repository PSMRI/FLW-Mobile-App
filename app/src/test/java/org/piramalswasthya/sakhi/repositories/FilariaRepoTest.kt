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
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var filariaDao: FilariaDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var repo: FilariaRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = FilariaRepo(filariaDao, benDao, preferenceDao, userRepo, amritApiService)
    }

    // =====================================================
    // getFilariaScreening() Tests
    // =====================================================

    @Test
    fun `getFilariaScreening returns cache when exists`() = runTest {
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(1L) } returns cache
        val result = repo.getFilariaScreening(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getFilariaScreening returns null when not found`() = runTest {
        coEvery { filariaDao.getFilariaScreening(1L) } returns null
        val result = repo.getFilariaScreening(1L)
        assertNull(result)
    }

    // =====================================================
    // saveFilariaScreening() Tests
    // =====================================================

    @Test
    fun `saveFilariaScreening delegates to dao`() = runTest {
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.saveFilariaScreening(cache) } returns Unit
        repo.saveFilariaScreening(cache)
        coVerify { filariaDao.saveFilariaScreening(cache) }
    }

    // =====================================================
    // pushUnSyncedRecords() Tests
    // =====================================================

    @Test
    fun `pushUnSyncedRecords returns true when no unsynced records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedRecords()
        assertTrue(result)
    }
}
