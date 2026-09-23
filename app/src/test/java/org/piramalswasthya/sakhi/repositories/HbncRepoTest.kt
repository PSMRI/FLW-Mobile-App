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
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HbncHomeVisit
import org.piramalswasthya.sakhi.model.HbncVisitCard
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [HbncRepo]. Consolidated from the previously separate
 * HbncRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HbncRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var hbncDao: HbncDao
    @MockK private lateinit var benDao: BenDao

    private lateinit var repo: HbncRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.hbncDao } returns hbncDao
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = HbncRepo(database, amritApiService, userRepo, preferenceDao, hbncDao, benDao)
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

    @Test
    fun `getHbncRecord returns record when exists`() = runTest {
        val hbnc = mockk<HBNCCache>()
        coEvery { hbncDao.getHbnc(100L, 200L, 1) } returns hbnc

        val result = repo.getHbncRecord(100L, 200L, 1)

        assertNotNull(result)
        assertEquals(hbnc, result)
    }

    @Test
    fun `getHbncRecord returns null when not exists`() = runTest {
        coEvery { hbncDao.getHbnc(999L, 999L, 1) } returns null

        val result = repo.getHbncRecord(999L, 999L, 1)

        assertNull(result)
    }

    @Test
    fun `getHbncCard returns card when exists`() = runTest {
        val card = mockk<HBNCCache>(relaxed = true)
        coEvery { database.hbncDao.getHbnc(200L, 100L, Konstants.hbncCardDay) } returns card

        val result = repo.getHbncCard(100L, 200L)

        assertNotNull(result)
    }

    @Test
    fun `getHbncCard returns null when not exists`() = runTest {
        coEvery { hbncDao.getHbnc(any(), any(), eq(Konstants.hbncCardDay)) } returns null

        val result = repo.getHbncCard(999L, 999L)

        assertNull(result)
    }

    @Test
    fun `getFirstHomeVisit returns visit when exists`() = runTest {
        val visit = mockk<HBNCCache>(relaxed = true)
        coEvery { database.hbncDao.getHbnc(100L, 200L, 1) } returns visit

        val result = repo.getFirstHomeVisit(100L, 200L)

        assertNotNull(result)
    }

    @Test
    fun `getFirstHomeVisit returns null when not exists`() = runTest {
        coEvery { hbncDao.getHbnc(any(), any(), eq(1)) } returns null

        val result = repo.getFirstHomeVisit(999L, 999L)

        assertNull(result)
    }

    @Test
    fun `processNewHbnc throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewHbnc()
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `hbncList delegates to dao getAllHbncEntries`() {
        val flow: Flow<List<HBNCCache>> = flowOf(emptyList())
        every { hbncDao.getAllHbncEntries(200L, 100L) } returns flow

        assertEquals(flow, repo.hbncList(100L, 200L))
    }

    @Test
    fun `saveHbncData delegates to dao and returns true`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.upsert(cache) } returns Unit

        assertTrue(repo.saveHbncData(cache))
        coVerify(exactly = 1) { hbncDao.upsert(cache) }
    }

    @Test
    fun `saveHbncData throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        val cache = mockk<HBNCCache>(relaxed = true)

        try {
            repo.saveHbncData(cache)
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `saveHbncData returns false when upsert throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.upsert(cache) } throws RuntimeException("boom")

        assertFalse(repo.saveHbncData(cache))
    }

    @Test
    fun `processNewHbnc returns true when no unprocessed records`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns emptyList()

        assertEquals(true, repo.processNewHbnc())
    }

    @Test
    fun `pushHBNCDetails throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pushHBNCDetails()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `pushHBNCDetails returns 1 and skips api when nothing unprocessed`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns emptyList()

        assertEquals(1, repo.pushHBNCDetails())
        coVerify(exactly = 0) { amritApiService.pushHBNCDetailsToServer(any()) }
    }

    @Test
    fun `getHBNCDetailsFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getHBNCDetailsFromServer()
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on inner 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns
            jsonResponse("""{"statusCode":8888,"errorMessage":""}""")
        assertEquals(-1, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `push returns 1 on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.pushHBNCDetails())
    }

    @Test
    fun `push returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.pushHBNCDetails())
    }

    @Test
    fun `push returns -1 on inner 5002 when refresh fails`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.pushHBNCDetails())
    }

    @Test
    fun `push returns -1 on unknown inner status`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns
            jsonResponse("""{"statusCode":7777,"errorMessage":""}""")
        assertEquals(-1, repo.pushHBNCDetails())
    }

    @Test
    fun `push returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.pushHBNCDetails())
    }

    @Test
    fun `processNewHbnc updates record on success`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBNCCache>(relaxed = true)
        every { cache.hhId } returns 10L
        every { cache.benId } returns 20L
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        val household = mockk<HouseholdCache>(relaxed = true)
        coEvery { database.householdDao.getHousehold(10L) } returns household
        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { database.benDao.getBen(10L, 20L) } returns ben
        coEvery { hbncDao.hbncCount() } returns 3
        coEvery { hbncDao.update(cache) } returns Unit

        assertEquals(true, repo.processNewHbnc())
        coVerify(exactly = 1) { hbncDao.update(cache) }
    }

    @Test
    fun `processNewHbnc skips update when household missing`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBNCCache>(relaxed = true)
        every { cache.hhId } returns 11L
        every { cache.benId } returns 21L
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(11L) } returns null

        assertEquals(true, repo.processNewHbnc())
        coVerify(exactly = 0) { hbncDao.update(any()) }
    }

    @Test
    fun `processNewHbnc skips update when ben missing`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBNCCache>(relaxed = true)
        every { cache.hhId } returns 12L
        every { cache.benId } returns 22L
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        val household = mockk<HouseholdCache>(relaxed = true)
        coEvery { database.householdDao.getHousehold(12L) } returns household
        coEvery { database.benDao.getBen(12L, 22L) } returns null

        assertEquals(true, repo.processNewHbnc())
        coVerify(exactly = 0) { hbncDao.update(any()) }
    }

    @Test
    fun `push returns -2 and pulls from server on 401 when refresh succeeds`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns
            jsonResponse("""{"statusCode":401,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")

        assertEquals(-2, repo.pushHBNCDetails())
    }

    @Test
    fun `push returns 0 when sync status update throws`() = runTest {
        loggedIn()
        val cache = mockk<HBNCCache>(relaxed = true)
        coEvery { hbncDao.getAllUnprocessedHbnc() } returns listOf(cache)
        coEvery { amritApiService.pushHBNCDetailsToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        coEvery { hbncDao.update(any()) } throws RuntimeException("db fail")

        assertEquals(0, repo.pushHBNCDetails())
    }

    @Test
    fun `pull returns -2 and retries when refresh succeeds on 401`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":401,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )

        assertEquals(-2, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `pull returns 0 when inner data is malformed`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"not-json"}""")

        assertEquals(0, repo.getHBNCDetailsFromServer())
    }

    @Test
    fun `pull updates existing cache when found in inner 200 data`() = runTest {
        loggedIn()
        val dataJson =
            """[{"id":0,"benId":1,"hhId":2,"homeVisitDate":1,"hbncVisitCardDTO":null,"hbncPart1DTO":null,"hbncPart2DTO":null,"hbncVisitDTO":null}]"""
        val outerJson = JSONObject().apply {
            put("statusCode", 200)
            put("errorMessage", "")
            put("data", dataJson)
        }.toString()
        val existingCache = mockk<HBNCCache>(relaxed = true)
        every { existingCache.visitCard } returns null
        every { existingCache.part1 } returns null
        every { existingCache.part2 } returns null
        every { existingCache.homeVisitForm } returns null
        coEvery { hbncDao.getHbnc(2L, 1L, 1) } returns existingCache
        coEvery { hbncDao.upsert(existingCache) } returns Unit
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns jsonResponse(outerJson)

        assertEquals(1, repo.getHBNCDetailsFromServer())
        coVerify(exactly = 1) { hbncDao.upsert(existingCache) }
    }

    @Test
    fun `pull creates new cache when not found and ben exists`() = runTest {
        loggedIn()
        val dataJson =
            """[{"id":0,"benId":3,"hhId":4,"homeVisitDate":2,"hbncVisitCardDTO":null,"hbncPart1DTO":null,"hbncPart2DTO":null,"hbncVisitDTO":null}]"""
        val outerJson = JSONObject().apply {
            put("statusCode", 200)
            put("errorMessage", "")
            put("data", dataJson)
        }.toString()
        coEvery { hbncDao.getHbnc(4L, 3L, 2) } returns null
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.householdId } returns 99L
        coEvery { benDao.getBen(3L) } returns ben
        coEvery { hbncDao.upsert(any()) } returns Unit
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns jsonResponse(outerJson)

        assertEquals(1, repo.getHBNCDetailsFromServer())
        coVerify(exactly = 1) { hbncDao.upsert(any()) }
    }

    @Test
    fun `pull skips insert when cache and ben both missing`() = runTest {
        loggedIn()
        val dataJson =
            """[{"id":0,"benId":5,"hhId":6,"homeVisitDate":3,"hbncVisitCardDTO":null,"hbncPart1DTO":null,"hbncPart2DTO":null,"hbncVisitDTO":null}]"""
        val outerJson = JSONObject().apply {
            put("statusCode", 200)
            put("errorMessage", "")
            put("data", dataJson)
        }.toString()
        coEvery { hbncDao.getHbnc(6L, 5L, 3) } returns null
        coEvery { benDao.getBen(5L) } returns null
        coEvery { amritApiService.getHBNCDetailsFromServer(any()) } returns jsonResponse(outerJson)

        assertEquals(1, repo.getHBNCDetailsFromServer())
        coVerify(exactly = 0) { hbncDao.upsert(any()) }
    }
}
