package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [HbycRepo]. Consolidated from the previously separate
 * HbycRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HbycRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var hbycDao: HbycDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var repo: HbycRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.hbycDao } returns hbycDao
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = HbycRepo(database, preferenceDao, hbycDao, benDao, userRepo, amritApiService)
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

    /** Wires the household/ben/count lookups used while building push records. */
    private fun oneUnprocessedRecord() {
        val cache = mockk<HBYCCache>(relaxed = true)
        coEvery { hbycDao.getAllUnprocessedHbyc() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(any()) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(any(), any()) } returns mockk(relaxed = true)
        coEvery { database.hbycDao.hbycCount() } returns 0
    }

    @Test
    fun `saveHbycData saves and returns true when user logged in`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val hbyc = mockk<HBYCCache>(relaxed = true)
        coEvery { hbycDao.upsert(hbyc) } returns Unit

        val result = repo.saveHbycData(hbyc)

        assertTrue(result)
        coVerify { hbycDao.upsert(hbyc) }
    }

    @Test
    fun `saveHbycData throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        val hbyc = mockk<HBYCCache>(relaxed = true)

        try {
            repo.saveHbycData(hbyc)
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewHbyc throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewHbyc()
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewHbyc returns true when no unprocessed records`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { hbycDao.getAllUnprocessedHBYC() } returns emptyList()

        val result = repo.processNewHbyc()

        assertEquals(true, result)
    }

    @Test
    fun `hbycList delegates to dao getAllHbycEntries`() {
        val flow: Flow<List<HBYCCache>> = flowOf(emptyList())
        every { hbycDao.getAllHbycEntries(200L, 100L) } returns flow

        assertEquals(flow, repo.hbycList(100L, 200L))
    }

    @Test
    fun `saveHbycData returns false when upsert throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "asha"
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBYCCache>(relaxed = true)
        coEvery { hbycDao.upsert(cache) } throws RuntimeException("boom")

        assertFalse(repo.saveHbycData(cache))
    }

    @Test
    fun `pushHBYCDetails throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pushHBYCDetails()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `getHBYCDetailsFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getHBYCDetailsFromServer()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `pushHBYCDetails skips api and does not update when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pushHBYCDetails()
        } catch (e: IllegalStateException) {
            // expected
        }
        coVerify(exactly = 0) { amritApiService.pushHBYCToServer(any()) }
    }

    @Test
    fun `pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on inner 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":8888,"errorMessage":""}""")
        assertEquals(-1, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `push returns 1 on inner 200`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns -1 on inner 5002 when refresh fails`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns -1 on unknown inner status`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":7777,"errorMessage":""}""")
        assertEquals(-1, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.pushHBYCDetails())
    }
}
