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
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class CdrRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var cdrDao: CdrDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var repo: CdrRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = CdrRepo(amritApiService, cdrDao, userRepo, preferenceDao)
    }

    // =====================================================
    // saveCdrData() Tests
    // =====================================================

    @Test
    fun `saveCdrData saves and returns true`() = runTest {
        val cdr = mockk<CDRCache>()
        coEvery { cdrDao.upsert(cdr) } returns Unit

        val result = repo.saveCdrData(cdr)

        assertTrue(result)
        coVerify { cdrDao.upsert(cdr) }
    }

    @Test
    fun `saveCdrData returns false on exception`() = runTest {
        val cdr = mockk<CDRCache>()
        coEvery { cdrDao.upsert(cdr) } throws RuntimeException("DB error")

        val result = repo.saveCdrData(cdr)

        assertEquals(false, result)
    }

    // =====================================================
    // processNewCdr() Tests
    // =====================================================

    @Test
    fun `processNewCdr returns true when no unprocessed records`() = runTest {
        coEvery { cdrDao.getAllUnprocessedCdr() } returns emptyList()

        val result = repo.processNewCdr()

        assertEquals(true, result)
    }
}
