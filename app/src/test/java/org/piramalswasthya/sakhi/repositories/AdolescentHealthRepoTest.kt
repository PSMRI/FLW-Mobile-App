package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.AdolescentHealthDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [AdolescentHealthRepo]. Consolidated from
 * AdolescentHealthRepoTest + ExtraTest: getters/save delegations plus the push
 * coordinator (success + no-user) and the server-pull no-user guard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdolescentHealthRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var adolescentHealthDao: AdolescentHealthDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var tmcNetworkApiService: AmritApiService

    private lateinit var repo: AdolescentHealthRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = AdolescentHealthRepo(adolescentHealthDao, benDao, preferenceDao, userRepo, tmcNetworkApiService)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
    }

    private fun resp(code: Int, json: String? = null): Response<ResponseBody> {
        val resp = mockk<Response<ResponseBody>>(relaxed = true)
        every { resp.code() } returns code
        if (json != null) {
            val body = mockk<ResponseBody>(relaxed = true)
            every { body.string() } returns json
            every { resp.body() } returns body
        } else {
            every { resp.body() } returns null
        }
        return resp
    }

    private fun unsyncedRecords(count: Int): List<AdolescentHealthCache> =
        (1..count).map { mockk<AdolescentHealthCache>(relaxed = true) }

    // =====================================================
    // getAdolescentHealth() Tests
    // =====================================================

    @Test
    fun `getAdolescentHealth returns record when exists`() = runTest {
        val record = mockk<AdolescentHealthCache>()
        coEvery { adolescentHealthDao.getAdolescentHealth(100L) } returns record

        val result = repo.getAdolescentHealth(100L)

        assertNotNull(result)
        assertEquals(record, result)
    }

    @Test
    fun `getAdolescentHealth returns null when not exists`() = runTest {
        coEvery { adolescentHealthDao.getAdolescentHealth(999L) } returns null

        val result = repo.getAdolescentHealth(999L)

        assertNull(result)
    }

    // =====================================================
    // saveAdolescentHealth() Tests
    // =====================================================

    @Test
    fun `saveAdolescentHealth calls dao save`() = runTest {
        val record = mockk<AdolescentHealthCache>()
        coEvery { adolescentHealthDao.saveAdolescentHealth(record) } returns Unit

        repo.saveAdolescentHealth(record)

        coVerify(exactly = 1) { adolescentHealthDao.saveAdolescentHealth(record) }
    }

    // =====================================================
    // pushUnSyncedRecords() / server-pull guards
    // =====================================================

    @Test
    fun `pushUnSyncedRecords returns true when nothing to sync`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pushUnSyncedRecords()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getadolescentHealthCacheFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getadolescentHealthCacheFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // =====================================================
    // pushUnSyncedRecordsAdolescentScreening() (via pushUnSyncedRecords()) Tests
    // =====================================================

    @Test
    fun `push returns true when no unsynced screening records exist`() = runTest {
        loggedIn()
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns emptyList()

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(exactly = 0) { tmcNetworkApiService.saveAdolescentHealthData(any()) }
    }

    @Test
    fun `push marks single record synced on inner statusCode 200`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returns
            resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        verify(exactly = 1) { cache.syncState = SyncState.SYNCED }
        coVerify(exactly = 1) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push handles 401 by attempting token refresh and leaves record unsynced`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returns
            resp(200, """{"statusCode":401}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc("asha", "pwd") }
        coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push handles 5002 when token refresh fails`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returns
            resp(200, """{"statusCode":5002}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc("asha", "pwd") }
        coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push handles unknown inner statusCode as failure`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returns
            resp(200, """{"statusCode":999}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push handles http error status code as failure`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returns resp(500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push treats null response body as neither success nor failure`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returns resp(200, null)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push handles exception thrown mid-loop and continues`() = runTest {
        loggedIn()
        val cache = mockk<AdolescentHealthCache>(relaxed = true)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(cache) }
    }

    @Test
    fun `push splits records into chunks of 20 with mixed success and failure`() = runTest {
        loggedIn()
        val records = unsyncedRecords(25)
        coEvery { adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED) } returns records
        coEvery { tmcNetworkApiService.saveAdolescentHealthData(any()) } returnsMany listOf(
            resp(200, """{"statusCode":200}"""),
            resp(200, """{"statusCode":999}""")
        )

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 2) { tmcNetworkApiService.saveAdolescentHealthData(any()) }
        records.take(20).forEach {
            coVerify(exactly = 1) { adolescentHealthDao.saveAdolescentHealth(it) }
        }
        records.drop(20).forEach {
            coVerify(exactly = 0) { adolescentHealthDao.saveAdolescentHealth(it) }
        }
    }
}
