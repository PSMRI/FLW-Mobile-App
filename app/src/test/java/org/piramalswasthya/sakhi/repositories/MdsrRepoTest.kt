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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class MdsrRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var mdsrDao: MdsrDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var repo: MdsrRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = MdsrRepo(amritApiService, mdsrDao, userRepo, preferenceDao)
    }

    // =====================================================
    // saveMdsrData() Tests
    // =====================================================

    @Test
    fun `saveMdsrData saves and returns true`() = runTest {
        val mdsr = mockk<MDSRCache>()
        coEvery { mdsrDao.upsert(mdsr) } returns Unit

        val result = repo.saveMdsrData(mdsr)

        assertTrue(result)
        coVerify { mdsrDao.upsert(mdsr) }
    }

    @Test
    fun `saveMdsrData returns false on exception`() = runTest {
        val mdsr = mockk<MDSRCache>()
        coEvery { mdsrDao.upsert(mdsr) } throws RuntimeException("DB error")

        val result = repo.saveMdsrData(mdsr)

        assertEquals(false, result)
    }

    // =====================================================
    // processNewMdsr() Tests
    // =====================================================

    @Test
    fun `processNewMdsr returns true when no unprocessed records`() = runTest {
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns emptyList()

        val result = repo.processNewMdsr()

        assertEquals(true, result)
    }
}
