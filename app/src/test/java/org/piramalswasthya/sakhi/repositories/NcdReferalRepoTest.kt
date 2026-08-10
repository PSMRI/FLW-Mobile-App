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
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class NcdReferalRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var referalDao: NcdReferalDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var database: InAppDb

    private lateinit var repo: NcdReferalRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = NcdReferalRepo(referalDao, preferenceDao, userRepo, amritApiService, database)
    }

    // =====================================================
    // getReferedNCD() Tests
    // =====================================================

    @Test
    fun `getReferedNCD returns cache when exists`() = runTest {
        val cache = mockk<ReferalCache>(relaxed = true)
        coEvery { referalDao.getReferalFromBenId(1L) } returns cache
        val result = repo.getReferedNCD(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getReferedNCD returns null when not found`() = runTest {
        coEvery { referalDao.getReferalFromBenId(1L) } returns null
        val result = repo.getReferedNCD(1L)
        assertNull(result)
    }

    // =====================================================
    // saveReferedNCD() Tests
    // =====================================================

    @Test
    fun `saveReferedNCD delegates to dao`() = runTest {
        val cache = mockk<ReferalCache>(relaxed = true)
        coEvery { referalDao.upsert(cache) } returns Unit
        repo.saveReferedNCD(cache)
        coVerify { referalDao.upsert(cache) }
    }

    // =====================================================
    // pushAndUpdateNCDReferRecord() Tests
    // =====================================================

    @Test
    fun `pushAndUpdateNCDReferRecord returns when no unprocessed records`() = runTest {
        coEvery { referalDao.getAllUnprocessedReferals() } returns emptyList()
        repo.pushAndUpdateNCDReferRecord()
        coVerify { referalDao.getAllUnprocessedReferals() }
    }

    @Test
    fun `toApiDateFormat returns ISO-like timestamp string`() {
        val result = with(repo) { 0L.toApiDateFormat() }
        assertTrue(
            "Unexpected format: $result",
            result.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        )
    }

    @Test
    fun `saveReferedNCD delegates to dao upsert`() = runTest {
        val cache = mockk<ReferalCache>(relaxed = true)
        coEvery { referalDao.upsert(cache) } returns Unit

        repo.saveReferedNCD(cache)

        coVerify(exactly = 1) { referalDao.upsert(cache) }
    }

    @Test
    fun `pullAndPersistReferRecord throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pullAndPersistReferRecord()
            assertTrue("Should have thrown", false)
        } catch (e: NullPointerException) {
            // expected: userName!! on a null user
        }
        coVerify(exactly = 0) { amritApiService.getCbacReferData(any()) }
    }

}
