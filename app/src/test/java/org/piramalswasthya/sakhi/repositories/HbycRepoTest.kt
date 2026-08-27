package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
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
        coEvery { hbycDao.hbycCount() } returns 0
    }

    /** Wires the household/ben/count lookups and returns [count] independent unprocessed records. */
    private fun manyUnprocessedRecords(count: Int): List<HBYCCache> {
        val caches = (1..count).map { mockk<HBYCCache>(relaxed = true) }
        coEvery { hbycDao.getAllUnprocessedHbyc() } returns caches
        coEvery { database.householdDao.getHousehold(any()) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(any(), any()) } returns mockk(relaxed = true)
        coEvery { hbycDao.hbycCount() } returns 0
        return caches
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

    @Test
    fun `updateSyncStatus no-ops and push still returns 1 when there are no unprocessed records`() = runTest {
        loggedIn()
        coEvery { hbycDao.getAllUnprocessedHbyc() } returns emptyList()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")

        assertEquals(1, repo.pushHBYCDetails())

        coVerify(exactly = 0) { hbycDao.upsert(any()) }
    }

    @Test
    fun `updateSyncStatus marks every record synced and processed when multiple records succeed`() = runTest {
        loggedIn()
        val caches = manyUnprocessedRecords(3)
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")

        assertEquals(1, repo.pushHBYCDetails())

        caches.forEach { cache ->
            verify { cache.syncState = SyncState.SYNCED }
            verify { cache.processed = "P" }
            coVerify { hbycDao.upsert(cache) }
        }
    }

    @Test
    fun `push returns 0 when updateSyncStatus throws for the only unprocessed record`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { hbycDao.upsert(any()) } throws RuntimeException("upsert failed")
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")

        assertEquals(0, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns 0 when second record fails mid-loop after first record was synced`() = runTest {
        loggedIn()
        val caches = manyUnprocessedRecords(2)
        val (first, second) = caches
        coEvery { hbycDao.upsert(second) } throws RuntimeException("upsert failed")
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")

        assertEquals(0, repo.pushHBYCDetails())

        verify { first.syncState = SyncState.SYNCED }
        verify { first.processed = "P" }
        coVerify { hbycDao.upsert(first) }
    }

    // =====================================================
    // processNewHbyc() record-loop Tests
    // =====================================================

    @Test
    fun `processNewHbyc marks single record synced when household and ben exist`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBYCCache>(relaxed = true)
        coEvery { hbycDao.getAllUnprocessedHBYC() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(any()) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(any(), any()) } returns mockk(relaxed = true)
        coEvery { hbycDao.hbycCount() } returns 3

        val result = repo.processNewHbyc()

        assertTrue(result)
        verify { cache.syncState = SyncState.SYNCING }
        coVerify { hbycDao.setSynced(cache) }
    }

    @Test
    fun `processNewHbyc skips record and continues when household is missing`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBYCCache>(relaxed = true)
        every { cache.hhId } returns 111L
        coEvery { hbycDao.getAllUnprocessedHBYC() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(111L) } returns null

        val result = repo.processNewHbyc()

        assertTrue(result)
        coVerify(exactly = 0) { hbycDao.setSynced(cache) }
    }

    @Test
    fun `processNewHbyc skips record and continues when beneficiary is missing`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val cache = mockk<HBYCCache>(relaxed = true)
        every { cache.hhId } returns 222L
        every { cache.benId } returns 333L
        coEvery { hbycDao.getAllUnprocessedHBYC() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(222L) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(222L, 333L) } returns null

        val result = repo.processNewHbyc()

        assertTrue(result)
        coVerify(exactly = 0) { hbycDao.setSynced(cache) }
    }

    @Test
    fun `processNewHbyc continues to remaining records after one fails`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val failing = mockk<HBYCCache>(relaxed = true)
        every { failing.hhId } returns 444L
        val succeeding = mockk<HBYCCache>(relaxed = true)
        every { succeeding.hhId } returns 555L
        every { succeeding.benId } returns 666L
        coEvery { hbycDao.getAllUnprocessedHBYC() } returns listOf(failing, succeeding)
        coEvery { database.householdDao.getHousehold(444L) } returns null
        coEvery { database.householdDao.getHousehold(555L) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(555L, 666L) } returns mockk(relaxed = true)
        coEvery { hbycDao.hbycCount() } returns 1

        val result = repo.processNewHbyc()

        assertTrue(result)
        coVerify(exactly = 0) { hbycDao.setSynced(failing) }
        coVerify { hbycDao.setSynced(succeeding) }
    }

    // =====================================================
    // saveChildHBYCacheFromResponse() record loop
    // =====================================================

    private fun pullPayload(dataJson: String): String {
        val escaped = dataJson.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"statusCode":200,"errorMessage":"","data":"$escaped"}"""
    }

    @Test
    fun `pull skips a record whose beneficiary is not present locally`() = runTest {
        loggedIn()
        coEvery { benDao.getBen(11L) } returns null
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse(pullPayload("""[{"beneficiaryid":11,"month":2}]"""))

        assertEquals(1, repo.getHBYCDetailsFromServer())

        coVerify(exactly = 0) { hbycDao.upsert(any()) }
    }

    @Test
    fun `pull inserts a new cache row when no local hbyc row exists for the month`() = runTest {
        loggedIn()
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.householdId } returns 71L
        every { ben.beneficiaryId } returns 12L
        coEvery { benDao.getBen(12L) } returns ben
        coEvery { hbycDao.getHbyc(71L, 12L, "3") } returns null
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse(pullPayload("""[{"beneficiaryid":12,"month":3}]"""))

        assertEquals(1, repo.getHBYCDetailsFromServer())

        coVerify(exactly = 1) { hbycDao.upsert(any()) }
    }

    @Test
    fun `pull marks an existing local hbyc row processed and synced`() = runTest {
        loggedIn()
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.householdId } returns 72L
        every { ben.beneficiaryId } returns 13L
        coEvery { benDao.getBen(13L) } returns ben
        val existing = mockk<HBYCCache>(relaxed = true)
        coEvery { hbycDao.getHbyc(72L, 13L, "4") } returns existing
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse(pullPayload("""[{"beneficiaryid":13,"month":4}]"""))

        assertEquals(1, repo.getHBYCDetailsFromServer())

        verify { existing.processed = "P" }
        verify { existing.syncState = SyncState.SYNCED }
        coVerify { hbycDao.upsert(existing) }
    }

    @Test
    fun `pull returns 0 when the data node is missing entirely`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")

        assertEquals(0, repo.getHBYCDetailsFromServer())

        coVerify(exactly = 0) { hbycDao.upsert(any()) }
    }

    @Test
    fun `pull returns 0 when the data node is not a json array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse(pullPayload("""{"beneficiaryid":14}"""))

        assertEquals(0, repo.getHBYCDetailsFromServer())

        coVerify(exactly = 0) { hbycDao.upsert(any()) }
    }

    @Test
    fun `pull returns -1 on a non-200 http status`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("{}", code = 500)

        assertEquals(-1, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `pull returns -1 on inner 5000 with a different error message`() = runTest {
        loggedIn()
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"Something else"}""")

        assertEquals(-1, repo.getHBYCDetailsFromServer())
    }

    @Test
    fun `pull returns -2 after refreshing the token and retrying`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.getHBYCFromServer(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("{}", code = 500)
        )

        assertEquals(-2, repo.getHBYCDetailsFromServer())

        coVerify(exactly = 2) { amritApiService.getHBYCFromServer(any()) }
    }

    @Test
    fun `pull returns -1 on inner 401 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("""{"statusCode":401,"errorMessage":""}""")

        assertEquals(-1, repo.getHBYCDetailsFromServer())
    }

    // =====================================================
    // pushHBYCDetails() record-build and retry branches
    // =====================================================

    @Test
    fun `push returns -1 and skips the api when a household is missing`() = runTest {
        loggedIn()
        val cache = mockk<HBYCCache>(relaxed = true)
        every { cache.hhId } returns 900L
        coEvery { hbycDao.getAllUnprocessedHbyc() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(900L) } returns null

        assertEquals(-1, repo.pushHBYCDetails())

        coVerify(exactly = 0) { amritApiService.pushHBYCToServer(any()) }
    }

    @Test
    fun `push returns -1 and skips the api when a beneficiary is missing`() = runTest {
        loggedIn()
        val cache = mockk<HBYCCache>(relaxed = true)
        every { cache.hhId } returns 901L
        every { cache.benId } returns 902L
        coEvery { hbycDao.getAllUnprocessedHbyc() } returns listOf(cache)
        coEvery { database.householdDao.getHousehold(901L) } returns mockk(relaxed = true)
        coEvery { database.benDao.getBen(901L, 902L) } returns null

        assertEquals(-1, repo.pushHBYCDetails())

        coVerify(exactly = 0) { amritApiService.pushHBYCToServer(any()) }
    }

    @Test
    fun `push returns 0 when the response omits the data node`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")

        assertEquals(0, repo.pushHBYCDetails())

        coVerify(exactly = 0) { hbycDao.upsert(any()) }
    }

    @Test
    fun `push returns -1 on a non-200 http status`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("{}", code = 500)

        assertEquals(-1, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns -1 on inner 5000 with a different error message`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"Something else"}""")

        assertEquals(-1, repo.pushHBYCDetails())
    }

    @Test
    fun `push returns -2 and falls back to a pull after refreshing the token`() = runTest {
        loggedIn()
        oneUnprocessedRecord()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.pushHBYCToServer(any()) } returns
            jsonResponse("""{"statusCode":401,"errorMessage":""}""")
        coEvery { amritApiService.getHBYCFromServer(any()) } returns
            jsonResponse("{}", code = 500)

        assertEquals(-2, repo.pushHBYCDetails())

        coVerify(exactly = 1) { amritApiService.getHBYCFromServer(any()) }
    }
}
