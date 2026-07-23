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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.CDRPost
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [CdrRepo]. Consolidated from CdrRepoTest + Extra/Extra2/Extra3:
 * saveCdrData delegation, processNewCdr empty / upload (accept & reject) paths,
 * the getCdrFromServer no-user guard, socket-timeout / non-200 early returns,
 * and every when(responseStatusCode) branch.
 */
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
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = CdrRepo(amritApiService, cdrDao, userRepo, preferenceDao)
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

    private fun nullBodyResponse(): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        return response
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

    @Test
    fun `saveCdrData forwards the exact cache to dao upsert`() = runTest {
        val cdr = mockk<CDRCache>(relaxed = true)
        coEvery { cdrDao.upsert(cdr) } returns Unit

        val result = repo.saveCdrData(cdr)

        assertTrue(result)
        coVerify(exactly = 1) { cdrDao.upsert(cdr) }
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

    @Test
    fun `processNewCdr uploads records and marks unsynced when server rejects`() = runTest {
        loggedIn()
        val record = mockk<CDRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<CDRPost>(relaxed = true)
        coEvery { cdrDao.getAllUnprocessedCdr() } returns listOf(record)
        coEvery { cdrDao.update(record) } returns Unit

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { amritApiService.postCdrForm(any()) } returns response

        val result = repo.processNewCdr()

        assertTrue(result)
        coVerify { amritApiService.postCdrForm(any()) }
        coVerify(atLeast = 1) { cdrDao.update(record) }
    }

    @Test
    fun `processNewCdr marks synced when server accepts`() = runTest {
        loggedIn()
        val record = mockk<CDRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<CDRPost>(relaxed = true)
        coEvery { cdrDao.getAllUnprocessedCdr() } returns listOf(record)
        coEvery { cdrDao.update(record) } returns Unit
        val json = """{"statusCode":200,"errorMessage":""}"""
        coEvery { amritApiService.postCdrForm(any()) } returns jsonResponse(json)

        val result = repo.processNewCdr()

        assertTrue(result)
        coVerify { amritApiService.postCdrForm(any()) }
        coVerify(atLeast = 1) { cdrDao.update(record) }
    }

    // =====================================================
    // getCdrFromServer() guards and early returns
    // =====================================================

    @Test
    fun `getCdrFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getCdrFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getCdrFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { amritApiService.getCdrData(any()) } throws SocketTimeoutException("timeout")

        assertEquals(-2, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns -1 on non-200 response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { amritApiService.getCdrData(any()) } returns response

        assertEquals(-1, repo.getCdrFromServer())
    }

    // ---------------- getCdrFromServer when-branches ----------------

    @Test
    fun `getCdrFromServer returns 1 on 200 with empty data`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getCdrData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getCdrData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns -1 on 5000 with other message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Something else"}"""
        coEvery { amritApiService.getCdrData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.getCdrData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":401,"errorMessage":""}"""
        coEvery { amritApiService.getCdrData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns -1 on unexpected status code`() = runTest {
        loggedIn()
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { amritApiService.getCdrData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getCdrFromServer())
    }

    @Test
    fun `getCdrFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getCdrData(any()) } returns nullBodyResponse()

        assertEquals(-1, repo.getCdrFromServer())
    }
}
