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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [FilariaRepo]. Consolidated from FilariaRepoTest + Extra/
 * Extra2/Extra3/Extra4: getter/save delegations, push no-record / no-user
 * guards, server-pull socket-timeout / non-200 / inner-status branches, and the
 * push chunk when-branches (success, token-refresh, unknown, http error, null
 * body, exception, multi-chunk).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FilariaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var filariaDao: FilariaDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var api: AmritApiService

    private lateinit var repo: FilariaRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = FilariaRepo(filariaDao, benDao, preferenceDao, userRepo, api)
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

    // =====================================================
    // Server-pull guards and early returns
    // =====================================================

    @Test
    fun `getFilariaScreeningDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        try {
            repo.getFilariaScreeningDetailsFromServer()
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getFilariaScreeningDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getFilariaScreeningDetailsFromServer())
    }

    @Test
    fun `getFilariaScreeningDetailsFromServer returns -1 on non-200 response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { api.getMalariaScreeningData(any()) } returns response
        assertEquals(-1, repo.getFilariaScreeningDetailsFromServer())
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

    // ---------------- pull inner status when-branches ----------------

    @Test
    fun `pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":200,"data":"[]"}""")
        assertEquals(1, repo.getFilariaScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found","data":"[]"}""")
        assertEquals(0, repo.getFilariaScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"data":"[]"}""")
        assertEquals(-1, repo.getFilariaScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -2 on 5002 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"data":"[]"}""")
        assertEquals(-2, repo.getFilariaScreeningDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":999,"data":"[]"}""")
        assertEquals(-1, repo.getFilariaScreeningDetailsFromServer())
    }

    // ---------------- getter / save delegation (Extra3) ----------------

    @Test
    fun `getFilariaScreening delegates to dao`() = runTest {
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(9L) } returns cache
        assertEquals(cache, repo.getFilariaScreening(9L))
    }

    @Test
    fun `getFilariaScreening returns null when absent`() = runTest {
        coEvery { filariaDao.getFilariaScreening(9L) } returns null
        assertNull(repo.getFilariaScreening(9L))
    }

    @Test
    fun `saveFilariaScreening delegates to dao _2`() = runTest {
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.saveFilariaScreening(cache) } returns Unit
        repo.saveFilariaScreening(cache)
        coVerify { filariaDao.saveFilariaScreening(cache) }
    }

    // ---------------- pull null-body branch ----------------

    @Test
    fun `pull returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getFilariaScreeningDetailsFromServer())
    }

    // ---------------- pushUnSyncedRecords chunk when-branches ----------------

    @Test
    fun `push succeeds and marks synced on chunk 200`() = runTest {
        loggedIn()
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { filariaDao.saveFilariaScreening(any()) } returns Unit
        coEvery { api.saveFilariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on chunk 5002 token refresh`() = runTest {
        loggedIn()
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveFilariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on unknown chunk status`() = runTest {
        loggedIn()
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveFilariaScreeningData(any()) } returns
            jsonResponse("""{"statusCode":7777,"errorMessage":""}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on chunk http non-200`() = runTest {
        loggedIn()
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveFilariaScreeningData(any()) } returns jsonResponse("{}", code = 500)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true on chunk 200 http with null body`() = runTest {
        loggedIn()
        val cache = mockk<FilariaScreeningCache>(relaxed = true)
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveFilariaScreeningData(any()) } returns nullBodyResponse()
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push returns true when chunk push throws`() = runTest {
        loggedIn()
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<FilariaScreeningCache>(relaxed = true))
        coEvery { api.saveFilariaScreeningData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `push processes multiple chunks and marks synced`() = runTest {
        loggedIn()
        coEvery { filariaDao.getFilariaScreening(SyncState.UNSYNCED) } returns
            List(21) { mockk<FilariaScreeningCache>(relaxed = true) }
        coEvery { api.saveFilariaScreeningData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 21) { filariaDao.saveFilariaScreening(any()) }
    }

    // ---------------- saveAESScreeningCacheFromResponse ----------------

    private fun filariaEntryJson(): String =
        """{"benId":1,"mdaHomeVisitDate":"2024-01-15","houseHoldDetailsId":1,"createdDate":"2024-01-15","sufferingFromFilariasis":false}"""

    private fun pullOuterJson(dataJson: String): String {
        val outer = org.json.JSONObject()
        outer.put("statusCode", 200)
        outer.put("errorMessage", "")
        outer.put("data", dataJson)
        return outer.toString()
    }

    @Test
    fun `pull saves new record from array-shaped data when ben exists and no cache yet`() = runTest {
        loggedIn()
        val dataJson = "[${filariaEntryJson()}]"
        coEvery { api.getMalariaScreeningData(any()) } returns resp200(pullOuterJson(dataJson))
        coEvery { filariaDao.getFilariaScreening(1L, any(), any()) } returns null
        coEvery { benDao.getBen(1L) } returns mockk<org.piramalswasthya.sakhi.model.BenRegCache>(relaxed = true)

        assertEquals(1, repo.getFilariaScreeningDetailsFromServer())

        coVerify(exactly = 1) { filariaDao.saveFilariaScreening(any()) }
    }

    @Test
    fun `pull saves new record from object-shaped data with filariaLists wrapper`() = runTest {
        loggedIn()
        val dataJson = """{"userId":0,"filariaLists":[${filariaEntryJson()}]}"""
        coEvery { api.getMalariaScreeningData(any()) } returns resp200(pullOuterJson(dataJson))
        coEvery { filariaDao.getFilariaScreening(1L, any(), any()) } returns null
        coEvery { benDao.getBen(1L) } returns mockk<org.piramalswasthya.sakhi.model.BenRegCache>(relaxed = true)

        assertEquals(1, repo.getFilariaScreeningDetailsFromServer())

        coVerify(exactly = 1) { filariaDao.saveFilariaScreening(any()) }
    }

    @Test
    fun `pull skips save when filaria cache already exists`() = runTest {
        loggedIn()
        val dataJson = "[${filariaEntryJson()}]"
        coEvery { api.getMalariaScreeningData(any()) } returns resp200(pullOuterJson(dataJson))
        coEvery { filariaDao.getFilariaScreening(1L, any(), any()) } returns
            mockk<FilariaScreeningCache>(relaxed = true)

        assertEquals(1, repo.getFilariaScreeningDetailsFromServer())

        coVerify(exactly = 0) { filariaDao.saveFilariaScreening(any()) }
    }

    @Test
    fun `pull skips save when ben does not exist`() = runTest {
        loggedIn()
        val dataJson = "[${filariaEntryJson()}]"
        coEvery { api.getMalariaScreeningData(any()) } returns resp200(pullOuterJson(dataJson))
        coEvery { filariaDao.getFilariaScreening(1L, any(), any()) } returns null
        coEvery { benDao.getBen(1L) } returns null

        assertEquals(1, repo.getFilariaScreeningDetailsFromServer())

        coVerify(exactly = 0) { filariaDao.saveFilariaScreening(any()) }
    }

    @Test
    fun `pull swallows per-record exception during save and still reports success`() = runTest {
        loggedIn()
        val dataJson = "[${filariaEntryJson()}]"
        coEvery { api.getMalariaScreeningData(any()) } returns resp200(pullOuterJson(dataJson))
        coEvery { filariaDao.getFilariaScreening(1L, any(), any()) } returns null
        coEvery { benDao.getBen(1L) } throws RuntimeException("db down")

        assertEquals(1, repo.getFilariaScreeningDetailsFromServer())

        coVerify(exactly = 0) { filariaDao.saveFilariaScreening(any()) }
    }

    @Test
    fun `pull returns 0 when data payload is malformed`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200(pullOuterJson("not a valid json"))

        assertEquals(0, repo.getFilariaScreeningDetailsFromServer())
    }
}
