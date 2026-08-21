package org.piramalswasthya.sakhi.repositories

import android.util.Log
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.net.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.model.MdsrPost
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

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
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        repo = MdsrRepo(amritApiService, mdsrDao, userRepo, preferenceDao)
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

    @Test
    fun `saveMdsrData forwards the exact cache to dao upsert`() = runTest {
        val mdsr = mockk<MDSRCache>(relaxed = true)
        coEvery { mdsrDao.upsert(mdsr) } returns Unit

        val result = repo.saveMdsrData(mdsr)

        assertTrue(result)
        coVerify(exactly = 1) { mdsrDao.upsert(mdsr) }
    }

    @Test
    fun `getMdsrFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getMdsrFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getMdsrFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { amritApiService.getMdsrData(any()) } throws SocketTimeoutException("timeout")

        assertEquals(-2, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns -1 on non-200 response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { amritApiService.getMdsrData(any()) } returns response

        assertEquals(-1, repo.getMdsrFromServer())
    }

    @Test
    fun `processNewMdsr uploads records and marks unsynced when server rejects`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { amritApiService.postMdsrForm(any()) } returns response

        val result = repo.processNewMdsr()

        assertTrue(result)
        coVerify { amritApiService.postMdsrForm(any()) }
        coVerify(atLeast = 1) { mdsrDao.updateMdsrRecord(record) }
    }

    // ---------------- getMdsrFromServer when-branches ----------------

    @Test
    fun `getMdsrFromServer returns 1 on 200 with empty data`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns -1 on 5000 with other message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Different"}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":401,"errorMessage":""}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns -1 on unexpected status code`() = runTest {
        loggedIn()
        val json = """{"statusCode":8888,"errorMessage":""}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getMdsrFromServer())
    }

    @Test
    fun `getMdsrFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getMdsrData(any()) } returns nullBodyResponse()

        assertEquals(-1, repo.getMdsrFromServer())
    }

    // ---------------- processNewMdsr upload success ----------------

    @Test
    fun `processNewMdsr marks synced when server accepts`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        val json = """{"statusCode":200,"errorMessage":""}"""
        coEvery { amritApiService.postMdsrForm(any()) } returns jsonResponse(json)

        val result = repo.processNewMdsr()

        assertTrue(result)
        coVerify { amritApiService.postMdsrForm(any()) }
        coVerify(atLeast = 1) { mdsrDao.updateMdsrRecord(record) }
    }

    // =====================================================
    // processNewMdsr() -> postMdsrForm() branch coverage
    // =====================================================

    @Test
    fun `processNewMdsr marks record unsynced when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
        coVerify(exactly = 0) { amritApiService.postMdsrForm(any()) }
    }

    @Test
    fun `processNewMdsr marks unsynced on unrecognized inner statusCode`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { amritApiService.postMdsrForm(any()) } returns jsonResponse(json)

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewMdsr marks unsynced when statusCode key is missing from response`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        val json = """{"errorMessage":""}"""
        coEvery { amritApiService.postMdsrForm(any()) } returns jsonResponse(json)

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewMdsr marks unsynced when inner response body is null on 200`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        coEvery { amritApiService.postMdsrForm(any()) } returns nullBodyResponse()

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewMdsr marks unsynced when server keeps returning 5002`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.postMdsrForm(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
        coVerify(exactly = 1) { amritApiService.postMdsrForm(any()) }
    }

    @Test
    fun `processNewMdsr marks unsynced on malformed json response`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        coEvery { amritApiService.postMdsrForm(any()) } returns jsonResponse("not-json")

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewMdsr marks unsynced when network call times out repeatedly`() = runTest {
        loggedIn()
        val record = mockk<MDSRCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<MdsrPost>(relaxed = true)
        coEvery { mdsrDao.getAllUnprocessedMdsr() } returns listOf(record)
        coEvery { mdsrDao.updateMdsrRecord(record) } returns Unit
        coEvery { amritApiService.postMdsrForm(any()) } throws SocketTimeoutException("timeout")

        val result = repo.processNewMdsr()

        assertTrue(result)
        verify { record.syncState = SyncState.UNSYNCED }
        coVerify(atLeast = 4) { amritApiService.postMdsrForm(any()) }
    }

    // =====================================================
    // getMdsrFromServer() -> saveMdsrCacheFromResponse() branch coverage
    // =====================================================

    @Test
    fun `getMdsrFromServer inserts new mdsr entries not already cached`() = runTest {
        loggedIn()
        val dataJson = """[{"benId":10}]"""
        val outerJson = """{"statusCode":200,"errorMessage":"","data":${Gson().toJson(dataJson)}}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(outerJson)
        coEvery { mdsrDao.getMDSR(10L) } returns null
        coEvery { mdsrDao.upsert(any()) } returns Unit

        val result = repo.getMdsrFromServer()

        assertEquals(1, result)
        coVerify { mdsrDao.upsert(match { it.benId == 10L }) }
    }

    @Test
    fun `getMdsrFromServer skips mdsr entries already cached locally`() = runTest {
        loggedIn()
        val dataJson = """[{"benId":10}]"""
        val outerJson = """{"statusCode":200,"errorMessage":"","data":${Gson().toJson(dataJson)}}"""
        coEvery { amritApiService.getMdsrData(any()) } returns jsonResponse(outerJson)
        val existing = mockk<MDSRCache>(relaxed = true)
        coEvery { mdsrDao.getMDSR(10L) } returns existing

        val result = repo.getMdsrFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { mdsrDao.upsert(any()) }
    }

}
