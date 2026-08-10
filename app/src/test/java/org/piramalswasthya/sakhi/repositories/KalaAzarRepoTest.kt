package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import java.net.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.KalaAzarDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [KalaAzarRepo]. Consolidated from the previously separate
 * KalaAzarRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KalaAzarRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var kalaAzarDao: KalaAzarDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService
    private val api get() = amritApiService

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

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
    }

    private fun resp200(json: String): Response<ResponseBody> {
        val body = mockk<ResponseBody>()
        every { body.string() } returns json
        val resp = mockk<Response<ResponseBody>>(relaxed = true)
        every { resp.code() } returns 200
        every { resp.body() } returns body
        return resp
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

    @Test
    fun `saveKalaAzarScreening delegates to dao`() = runTest {
        val cache = mockk<KalaAzarScreeningCache>(relaxed = true)
        coEvery { kalaAzarDao.saveKalaAzarScreening(cache) } returns Unit
        repo.saveKalaAzarScreening(cache)
        coVerify { kalaAzarDao.saveKalaAzarScreening(cache) }
    }

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

    @Test
    fun `pushUnSyncedRecords returns true when no unsynced records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedRecords()
        assertTrue(result)
    }

    @Test
    fun `getKalaAzarScreeningDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        try {
            repo.getKalaAzarScreeningDetailsFromServer()
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getKalaAzarScreeningDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `getKalaAzarScreeningDetailsFromServer returns -1 on non-200 response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { api.getMalariaScreeningData(any()) } returns response
        assertEquals(-1, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `pushUnSyncedRecords throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        try {
            repo.pushUnSyncedRecords()
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":200,"data":"[]"}""")
        assertEquals(1, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found","data":"[]"}""")
        assertEquals(0, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"data":"[]"}""")
        assertEquals(-1, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -2 on 5002 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"data":"[]"}""")
        assertEquals(-2, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":999,"data":"[]"}""")
        assertEquals(-1, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `pushUnSyncedRecords marks kala azar screening synced on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<KalaAzarScreeningCache>(relaxed = true)
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { kalaAzarDao.saveKalaAzarScreening(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles kala azar 401 with token refresh`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<KalaAzarScreeningCache>(relaxed = true))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(200, """{"statusCode":401}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles kala azar 5002 without refresh`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<KalaAzarScreeningCache>(relaxed = true))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(200, """{"statusCode":5002}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles kala azar unknown status`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<KalaAzarScreeningCache>(relaxed = true))
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(200, """{"statusCode":999}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles kala azar http error`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<KalaAzarScreeningCache>(relaxed = true))
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(500)

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles kala azar exception`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<KalaAzarScreeningCache>(relaxed = true))
        coEvery { api.saveKalaAzarScreeningData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `getKalaAzarScreeningDetailsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getKalaAzarScreeningDetailsFromServer())
    }

    @Test
    fun `push returns true on 200 null body chunk`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<KalaAzarScreeningCache>(relaxed = true))
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(200, null)

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push processes multiple chunks and marks synced`() = runTest {
        loggedIn()
        coEvery { kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED) } returns
            List(21) { mockk<KalaAzarScreeningCache>(relaxed = true) }
        coEvery { api.saveKalaAzarScreeningData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 21) { kalaAzarDao.saveKalaAzarScreening(any()) }
    }

    @Test
    fun `getKalaAzarScreeningDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns resp(500)
        assertEquals(-1, repo.getKalaAzarScreeningDetailsFromServer())
    }
}
