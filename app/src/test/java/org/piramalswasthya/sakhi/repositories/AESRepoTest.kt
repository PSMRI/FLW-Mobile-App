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
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.AesDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [AESRepo]. Consolidated from AESRepoTest + ExtraTest +
 * Extra3Test + Extra4Test: getters/delegations, push no-record and no-user
 * guards, server-pull inner status branches, and the push chunk when-branches.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AESRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var aesDao: AesDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var api: AmritApiService

    private lateinit var repo: AESRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = AESRepo(aesDao, benDao, preferenceDao, userRepo, api)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    private fun nullBodyResponse(code: Int = 200): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns null
        return response
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

    // =====================================================
    // Server-pull guards and early returns
    // =====================================================

    @Test
    fun `getAESScreeningDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        try {
            repo.getAESScreeningDetailsFromServer()
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getAESScreeningDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getAESScreeningDetailsFromServer())
    }

    @Test
    fun `getAESScreeningDetailsFromServer returns -1 on non-200 response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { api.getMalariaScreeningData(any()) } returns response
        assertEquals(-1, repo.getAESScreeningDetailsFromServer())
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

    // ---------------- getAESScreeningDetailsFromServer inner branches ----------------

    @Test
    fun `pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.getAESScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getAESScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -2 on inner 5002 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getMalariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-2, repo.getAESScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on inner 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getMalariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getAESScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":8888,"errorMessage":""}""")
        assertEquals(-1, repo.getAESScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getAESScreeningDetailsFromServer())
    }

    // ---------------- pushUnSyncedRecords chunk when-branches ----------------

    @Test
    fun `push succeeds and marks synced on chunk 200`() = runTest {
        loggedIn()
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { aesDao.saveAESScreening(any()) } returns Unit
        coEvery { api.saveAESScreeningData(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on chunk 5002 token refresh`() = runTest {
        loggedIn()
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveAESScreeningData(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on unknown chunk status`() = runTest {
        loggedIn()
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAESScreeningData(any()) } returns
            jsonResponse("""{"statusCode":7777,"errorMessage":""}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on chunk http non-200`() = runTest {
        loggedIn()
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAESScreeningData(any()) } returns jsonResponse("{}", code = 500)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on chunk 200 http with null body`() = runTest {
        loggedIn()
        val cache = mockk<AESScreeningCache>(relaxed = true)
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAESScreeningData(any()) } returns nullBodyResponse()
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true when chunk push throws`() = runTest {
        loggedIn()
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<AESScreeningCache>(relaxed = true))
        coEvery { api.saveAESScreeningData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push processes multiple chunks and marks synced`() = runTest {
        loggedIn()
        coEvery { aesDao.getAESScreening(SyncState.UNSYNCED) } returns
            List(21) { mockk<AESScreeningCache>(relaxed = true) }
        coEvery { api.saveAESScreeningData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 21) { aesDao.saveAESScreening(any()) }
    }
}
