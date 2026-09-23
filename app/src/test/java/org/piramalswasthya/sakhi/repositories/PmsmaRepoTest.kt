package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import java.net.SocketTimeoutException
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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PmsmaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var pmsmaDao: PmsmaDao

    private lateinit var repo: PmsmaRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = PmsmaRepo(preferenceDao, amritApiService, userRepo, benDao, pmsmaDao)
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

    // =====================================================
    // getPmsmaByBenId() Tests
    // =====================================================

    @Test
    fun `getPmsmaByBenId returns record when exists`() = runTest {
        val pmsma = mockk<PMSMACache>()
        coEvery { pmsmaDao.getPmsma(100L) } returns pmsma

        val result = repo.getPmsmaByBenId(100L)

        assertNotNull(result)
        assertEquals(pmsma, result)
    }

    @Test
    fun `getPmsmaByBenId returns null when not exists`() = runTest {
        coEvery { pmsmaDao.getPmsma(999L) } returns null

        val result = repo.getPmsmaByBenId(999L)

        assertNull(result)
    }

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns record when exists`() = runTest {
        val pmsma = mockk<PMSMACache>()
        coEvery { pmsmaDao.getSavedRecord(100L, 1) } returns pmsma

        val result = repo.getSavedRecord(100L, 1)

        assertNotNull(result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { pmsmaDao.getSavedRecord(999L, 1) } returns null

        val result = repo.getSavedRecord(999L, 1)

        assertNull(result)
    }

    // =====================================================
    // getLastPmsmaVisit() Tests
    // =====================================================

    @Test
    fun `getLastPmsmaVisit returns record when exists`() = runTest {
        val pmsma = mockk<PMSMACache>()
        coEvery { pmsmaDao.getLastPmsmaVisit(100L) } returns pmsma

        val result = repo.getLastPmsmaVisit(100L)

        assertNotNull(result)
    }

    @Test
    fun `getLastPmsmaVisit returns null when not exists`() = runTest {
        coEvery { pmsmaDao.getLastPmsmaVisit(999L) } returns null

        val result = repo.getLastPmsmaVisit(999L)

        assertNull(result)
    }

    // =====================================================
    // getActiveAncCountForBenIds() Tests
    // =====================================================

    @Test
    fun `getActiveAncCountForBenIds returns count`() = runTest {
        coEvery { pmsmaDao.getActiveAncCountForBenIds(100L) } returns 3

        val result = repo.getActiveAncCountForBenIds(100L)

        assertEquals(3, result)
    }

    @Test
    fun `getActiveAncCountForBenIds returns zero when none`() = runTest {
        coEvery { pmsmaDao.getActiveAncCountForBenIds(999L) } returns 0

        val result = repo.getActiveAncCountForBenIds(999L)

        assertEquals(0, result)
    }

    // =====================================================
    // savePmsmaData() Tests
    // =====================================================

    @Test
    fun `savePmsmaData saves and returns true when user logged in`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        val pmsma = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.upsert(pmsma) } returns Unit

        val result = repo.savePmsmaData(pmsma)

        assertTrue(result)
        coVerify { pmsmaDao.upsert(pmsma) }
    }

    @Test
    fun `savePmsmaData throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        val pmsma = mockk<PMSMACache>(relaxed = true)

        try {
            repo.savePmsmaData(pmsma)
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    // =====================================================
    // processNewPmsma() Tests
    // =====================================================

    @Test
    fun `processNewPmsma throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewPmsma()
            assert(false) { "Should have thrown" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewPmsma returns true when no unprocessed records`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns emptyList()

        val result = repo.processNewPmsma()

        assertEquals(true, result)
    }

    @Test
    fun `savePmsmaData returns false when dao throws`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        val pmsma = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.upsert(pmsma) } throws RuntimeException("DB error")

        assertFalse(repo.savePmsmaData(pmsma))
    }

    @Test
    fun `setToInactive updates each returned record`() = runTest {
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllPmsma(any()) } returns listOf(record)

        repo.setToInactive(setOf(1L))

        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    @Test
    fun `setToInactive does not update when nothing matches`() = runTest {
        coEvery { pmsmaDao.getAllPmsma(any()) } returns emptyList()

        repo.setToInactive(setOf(9L))

        coVerify(exactly = 0) { pmsmaDao.updatePmsmaRecord(any()) }
    }

    @Test
    fun `getPmsmaDetailsFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getPmsmaDetailsFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // ---------------- getPmsmaDetailsFromServer ----------------

    @Test
    fun `getPmsmaDetailsFromServer returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } returns
            resp(200, """{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } returns
            resp(200, """{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -1 on 5000 other message`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } returns
            resp(200, """{"statusCode":5000,"errorMessage":"boom"}""")
        assertEquals(-1, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -2 on 5002 refresh success`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.getPmsmaData(any()) } returns
            resp(200, """{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-2, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -1 on 401 refresh fail`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.getPmsmaData(any()) } returns
            resp(200, """{"statusCode":401,"errorMessage":""}""")
        assertEquals(-1, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -1 on unknown status`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } returns
            resp(200, """{"statusCode":999,"errorMessage":"x"}""")
        assertEquals(-1, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } throws SocketTimeoutException("t")
        assertEquals(-2, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } returns resp(500)
        assertEquals(-1, repo.getPmsmaDetailsFromServer())
    }

    @Test
    fun `getPmsmaDetailsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPmsmaData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getPmsmaDetailsFromServer())
    }

    // ---------------- processNewPmsma upload loop ----------------

    @Test
    fun `processNewPmsma marks record processed on 200 success`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { amritApiService.postPmsmaForm(any()) } returns
            resp(200, """{"statusCode":200,"errorMessage":""}""")

        assertTrue(repo.processNewPmsma())
        coVerify { amritApiService.postPmsmaForm(any()) }
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    @Test
    fun `processNewPmsma marks record unsynced on non-200 http`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { amritApiService.postPmsmaForm(any()) } returns resp(500)

        assertTrue(repo.processNewPmsma())
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    @Test
    fun `processNewPmsma handles 401 without refresh`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.postPmsmaForm(any()) } returns
            resp(200, """{"statusCode":401,"errorMessage":""}""")

        assertTrue(repo.processNewPmsma())
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    @Test
    fun `processNewPmsma handles unknown status`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { amritApiService.postPmsmaForm(any()) } returns
            resp(200, """{"statusCode":999,"errorMessage":""}""")

        assertTrue(repo.processNewPmsma())
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    @Test
    fun `processNewPmsma handles record that throws during mapping`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        every { record.asPostModel() } throws RuntimeException("map error")
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)

        assertTrue(repo.processNewPmsma())
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    // ---------------- companion ----------------

    @Test
    fun `getCurrentDate formats millis as iso like string`() {
        val result = PmsmaRepo.getCurrentDate(0L)
        assertTrue(result.contains("T"))
        assertTrue(result.endsWith(".000Z"))
    }

    // ---------------- postDataToAmritServer retry / dead-end branches ----------------

    @Test
    fun `processNewPmsma retries on socket timeout then exhausts retries`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { amritApiService.postPmsmaForm(any()) } throws SocketTimeoutException("t")

        assertTrue(repo.processNewPmsma())

        coVerify(exactly = 4) { amritApiService.postPmsmaForm(any()) }
        verify { record.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewPmsma does not retry when 5002 refresh throws timeout inside body parsing`() = runTest {
        // NOTE: SocketTimeoutException IS-A IOException, and the `throw SocketTimeoutException()`
        // inside the 401/5002 branch is caught by this method's own inner `catch (e: IOException)`
        // (which sits closer than the outer `catch (e: SocketTimeoutException)` retry handler), so
        // the outer retry logic never actually fires for this path - see TEST-COVERAGE-BLOCKERS.md
        // for the same class of bug elsewhere. This asserts the real (no-retry, single-call) outcome.
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.postPmsmaForm(any()) } returns
            resp(200, """{"statusCode":5002,"errorMessage":""}""")

        assertTrue(repo.processNewPmsma())

        coVerify(exactly = 1) { amritApiService.postPmsmaForm(any()) }
        verify { record.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewPmsma marks record unsynced when response has no statusCode field`() = runTest {
        loggedIn()
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { amritApiService.postPmsmaForm(any()) } returns resp(200, """{"errorMessage":"weird"}""")

        assertTrue(repo.processNewPmsma())

        verify { record.syncState = SyncState.UNSYNCED }
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    @Test
    fun `processNewPmsma marks record unsynced when user logged out mid push`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returnsMany listOf(user, null)
        val record = mockk<PMSMACache>(relaxed = true)
        coEvery { pmsmaDao.getAllUnprocessedPmsma() } returns listOf(record)
        coEvery { amritApiService.postPmsmaForm(any()) } returns
            resp(200, """{"statusCode":401,"errorMessage":""}""")

        assertTrue(repo.processNewPmsma())

        verify { record.syncState = SyncState.UNSYNCED }
        coVerify { pmsmaDao.updatePmsmaRecord(record) }
    }

    // ---------------- savePmsmaCacheFromResponse via getPmsmaDetailsFromServer ----------------

    private fun pmsmaDataOuterJson(dataJson: String): String {
        val outer = org.json.JSONObject()
        outer.put("statusCode", 200)
        outer.put("errorMessage", "")
        outer.put("data", dataJson)
        return outer.toString()
    }

    @Test
    fun `getPmsmaDetailsFromServer saves new record when ben exists and no cache yet`() = runTest {
        loggedIn()
        val dataJson = """[{"benId":1,"visitNumber":1,"isActive":true,"createdBy":"asha","updatedBy":"asha","createdDate":"2023-01-01"}]"""
        coEvery { amritApiService.getPmsmaData(any()) } returns resp(200, pmsmaDataOuterJson(dataJson))
        coEvery { benDao.getBen(1L) } returns mockk<org.piramalswasthya.sakhi.model.BenRegCache>(relaxed = true)
        coEvery { pmsmaDao.getPmsma(1L) } returns null

        assertEquals(1, repo.getPmsmaDetailsFromServer())

        coVerify(exactly = 1) { pmsmaDao.upsert(any()) }
    }

    @Test
    fun `getPmsmaDetailsFromServer skips save when pmsma cache already exists`() = runTest {
        loggedIn()
        val dataJson = """[{"benId":1,"visitNumber":1,"isActive":true,"createdBy":"asha","updatedBy":"asha","createdDate":"2023-01-01"}]"""
        coEvery { amritApiService.getPmsmaData(any()) } returns resp(200, pmsmaDataOuterJson(dataJson))
        coEvery { benDao.getBen(1L) } returns mockk<org.piramalswasthya.sakhi.model.BenRegCache>(relaxed = true)
        coEvery { pmsmaDao.getPmsma(1L) } returns mockk<PMSMACache>(relaxed = true)

        assertEquals(1, repo.getPmsmaDetailsFromServer())

        coVerify(exactly = 0) { pmsmaDao.upsert(any()) }
    }

    @Test
    fun `getPmsmaDetailsFromServer skips save when ben does not exist`() = runTest {
        loggedIn()
        val dataJson = """[{"benId":1,"visitNumber":1,"isActive":true,"createdBy":"asha","updatedBy":"asha","createdDate":"2023-01-01"}]"""
        coEvery { amritApiService.getPmsmaData(any()) } returns resp(200, pmsmaDataOuterJson(dataJson))
        coEvery { benDao.getBen(1L) } returns null
        coEvery { pmsmaDao.getPmsma(1L) } returns null

        assertEquals(1, repo.getPmsmaDetailsFromServer())

        coVerify(exactly = 0) { pmsmaDao.upsert(any()) }
    }
}
