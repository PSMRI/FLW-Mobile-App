package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import java.net.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TBRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var tbDao: TBDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var api: AmritApiService

    private lateinit var repo: TBRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = TBRepo(tbDao, benDao, preferenceDao, userRepo, api)
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

    private fun onlyScreening(cache: TBScreeningCache) {
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns emptyList()
    }

    // ---------------- push: suspected / confirmed chunk branches ----------------

    private fun onlySuspected(cache: TBSuspectedCache) {
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns emptyList()
    }

    private fun onlyConfirmed(cache: TBConfirmedTreatmentCache) {
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns listOf(cache)
    }

    // =====================================================
    // getTBScreening() Tests
    // =====================================================

    @Test
    fun `getTBScreening returns record when exists`() = runTest {
        val screening = mockk<TBScreeningCache>()
        coEvery { tbDao.getTbScreening(100L) } returns screening

        val result = repo.getTBScreening(100L)

        assertNotNull(result)
        assertEquals(screening, result)
    }

    @Test
    fun `getTBScreening returns null when not exists`() = runTest {
        coEvery { tbDao.getTbScreening(999L) } returns null

        val result = repo.getTBScreening(999L)

        assertNull(result)
    }

    // =====================================================
    // saveTBScreening() Tests
    // =====================================================

    @Test
    fun `saveTBScreening calls dao`() = runTest {
        val screening = mockk<TBScreeningCache>()
        coEvery { tbDao.saveTbScreening(screening) } returns Unit

        repo.saveTBScreening(screening)

        coVerify(exactly = 1) { tbDao.saveTbScreening(screening) }
    }

    // =====================================================
    // getTBSuspected() Tests
    // =====================================================

    @Test
    fun `getTBSuspected returns record when exists`() = runTest {
        val suspected = mockk<TBSuspectedCache>()
        coEvery { tbDao.getTbSuspected(100L) } returns suspected

        val result = repo.getTBSuspected(100L)

        assertNotNull(result)
        assertEquals(suspected, result)
    }

    @Test
    fun `getTBSuspected returns null when not exists`() = runTest {
        coEvery { tbDao.getTbSuspected(999L) } returns null

        val result = repo.getTBSuspected(999L)

        assertNull(result)
    }

    // =====================================================
    // saveTBSuspected() Tests
    // =====================================================

    @Test
    fun `saveTBSuspected calls dao`() = runTest {
        val suspected = mockk<TBSuspectedCache>()
        coEvery { tbDao.saveTbSuspected(suspected) } returns Unit

        repo.saveTBSuspected(suspected)

        coVerify(exactly = 1) { tbDao.saveTbSuspected(suspected) }
    }

    // =====================================================
    // getTBConfirmed() Tests
    // =====================================================

    @Test
    fun `getTBConfirmed returns record when exists`() = runTest {
        val confirmed = mockk<TBConfirmedTreatmentCache>()
        coEvery { tbDao.getTbConfirmed(100L) } returns confirmed

        val result = repo.getTBConfirmed(100L)

        assertNotNull(result)
        assertEquals(confirmed, result)
    }

    @Test
    fun `getTBConfirmed returns null when not exists`() = runTest {
        coEvery { tbDao.getTbConfirmed(999L) } returns null

        val result = repo.getTBConfirmed(999L)

        assertNull(result)
    }

    // =====================================================
    // saveTBConfirmed() Tests
    // =====================================================

    @Test
    fun `saveTBConfirmed calls dao`() = runTest {
        val confirmed = mockk<TBConfirmedTreatmentCache>()
        coEvery { tbDao.saveTbConfirmed(confirmed) } returns Unit

        repo.saveTBConfirmed(confirmed)

        coVerify(exactly = 1) { tbDao.saveTbConfirmed(confirmed) }
    }

    // =====================================================
    // getAllFollowUpsForBeneficiary() Tests
    // =====================================================

    @Test
    fun `getAllFollowUps returns list when exists`() = runTest {
        val list = listOf(mockk<TBConfirmedTreatmentCache>(), mockk<TBConfirmedTreatmentCache>())
        coEvery { tbDao.getAllFollowUpsForBeneficiary(100L) } returns list

        val result = repo.getAllFollowUpsForBeneficiary(100L)

        assertEquals(2, result.size)
    }

    @Test
    fun `getAllFollowUps returns empty for unknown ben`() = runTest {
        coEvery { tbDao.getAllFollowUpsForBeneficiary(999L) } returns emptyList()

        val result = repo.getAllFollowUpsForBeneficiary(999L)

        assertTrue(result.isEmpty())
    }

    // ---------------- screening ----------------

    @Test
    fun `screening pull returns 1 on inner 200 with empty object`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":200,"errorMessage":"","data":"{}"}""")
        assertEquals(1, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -2 on 401 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":401,"errorMessage":""}""")
        assertEquals(-2, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":999,"errorMessage":"x"}""")
        assertEquals(-1, repo.getTBScreeningDetailsFromServer())
    }

    // ---------------- suspected ----------------

    @Test
    fun `suspected pull returns 1 on inner 200 with empty object`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns
            resp200("""{"statusCode":200,"errorMessage":"","data":"{}"}""")
        assertEquals(1, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `suspected pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `suspected pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getTBSuspectedData(any()) } returns
            resp200("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getTbSuspectedDetailsFromServer())
    }

    // ---------------- confirmed ----------------

    @Test
    fun `confirmed pull returns 1 on inner 200 with empty object`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":200,"errorMessage":"","data":"{}"}""")
        assertEquals(1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":999,"errorMessage":"x"}""")
        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    // ---------------- push: screening ----------------

    @Test
    fun `pushUnSyncedRecords marks tb screening synced on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<TBScreeningCache>(relaxed = true)
        onlyScreening(cache)
        coEvery { api.saveTBScreeningData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { tbDao.saveTbScreening(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles tb screening 401 refresh`() = runTest {
        loggedIn()
        onlyScreening(mockk(relaxed = true))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveTBScreeningData(any()) } returns resp(200, """{"statusCode":401}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles tb screening unknown status`() = runTest {
        loggedIn()
        onlyScreening(mockk(relaxed = true))
        coEvery { api.saveTBScreeningData(any()) } returns resp(200, """{"statusCode":999}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles tb screening http error`() = runTest {
        loggedIn()
        onlyScreening(mockk(relaxed = true))
        coEvery { api.saveTBScreeningData(any()) } returns resp(500)

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles tb screening exception`() = runTest {
        loggedIn()
        onlyScreening(mockk(relaxed = true))
        coEvery { api.saveTBScreeningData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

    // ---------------- push: suspected ----------------

    @Test
    fun `pushUnSyncedRecords marks tb suspected synced on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<TBSuspectedCache>(relaxed = true)
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveTBSuspectedData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { tbDao.saveTbSuspected(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles tb suspected 5002 refresh`() = runTest {
        loggedIn()
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns
            listOf(mockk<TBSuspectedCache>(relaxed = true))
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveTBSuspectedData(any()) } returns resp(200, """{"statusCode":5002}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    // ---------------- push: confirmed ----------------

    @Test
    fun `pushUnSyncedRecords marks tb confirmed synced on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<TBConfirmedTreatmentCache>(relaxed = true)
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveTBConfirmedData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { tbDao.saveTbConfirmed(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles tb confirmed http error`() = runTest {
        loggedIn()
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns
            listOf(mockk<TBConfirmedTreatmentCache>(relaxed = true))
        coEvery { api.saveTBConfirmedData(any()) } returns resp(500)

        assertTrue(repo.pushUnSyncedRecords())
    }

    // ---------------- pull: suspected remaining branches ----------------

    @Test
    fun `getTbSuspectedDetailsFromServer returns -2 on 401 refresh success`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getTBSuspectedData(any()) } returns
            resp(200, """{"statusCode":401,"errorMessage":""}""")
        assertEquals(-2, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `getTbSuspectedDetailsFromServer returns -1 on unknown status`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns
            resp(200, """{"statusCode":999,"errorMessage":"x"}""")
        assertEquals(-1, repo.getTbSuspectedDetailsFromServer())
    }

    // ---------------- pull: null body ----------------

    @Test
    fun `getTBScreeningDetailsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getTBScreeningDetailsFromServer())
    }

    // ---------------- confirmed pull remaining branches ----------------

    @Test
    fun `confirmed pull returns -2 on 5002 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getTBConfirmedData() } returns
            resp(200, """{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-2, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns resp(500)
        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns resp(200, null)
        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    // ---------------- suspected pull remaining guards ----------------

    @Test
    fun `suspected pull returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns resp(500)
        assertEquals(-1, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `suspected pull returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `pushUnSyncedRecords handles tb suspected http error`() = runTest {
        loggedIn()
        onlySuspected(mockk(relaxed = true))
        coEvery { api.saveTBSuspectedData(any()) } returns resp(500)

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords marks tb suspected synced on inner 200_2`() = runTest {
        loggedIn()
        val cache = mockk<TBSuspectedCache>(relaxed = true)
        onlySuspected(cache)
        coEvery { api.saveTBSuspectedData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { tbDao.saveTbSuspected(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles tb confirmed 401 refresh`() = runTest {
        loggedIn()
        onlyConfirmed(mockk(relaxed = true))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveTBConfirmedData(any()) } returns resp(200, """{"statusCode":401}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles tb confirmed unknown status`() = runTest {
        loggedIn()
        onlyConfirmed(mockk(relaxed = true))
        coEvery { api.saveTBConfirmedData(any()) } returns resp(200, """{"statusCode":999}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    // ---------------- screening ----------------

    @Test
    fun `getTBScreeningDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        try {
            repo.getTBScreeningDetailsFromServer()
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getTBScreeningDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `getTBScreeningDetailsFromServer returns -1 on non-200 response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { api.getTBScreeningData(any()) } returns response
        assertEquals(-1, repo.getTBScreeningDetailsFromServer())
    }

    // ---------------- suspected ----------------

    @Test
    fun `getTbSuspectedDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        try {
            repo.getTbSuspectedDetailsFromServer()
            fail("Should have thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getTbSuspectedDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getTbSuspectedDetailsFromServer())
    }

    // ---------------- confirmed ----------------

    @Test
    fun `getTbConfirmedDetailsFromServer returns -1 when no user`() = runTest {
        // no-user throw happens inside the try block, caught and mapped to -1
        every { preferenceDao.getLoggedInUser() } returns null
        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `getTbConfirmedDetailsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getTbConfirmedDetailsFromServer())
    }

    // ---------------- push coordinator ----------------

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

    // =====================================================
    // pull payload parsing (saveTB*CacheFromResponse)
    // =====================================================

    private fun envelope(inner: String, statusCode: Int = 200, errorMessage: String = "") =
        JSONObject()
            .put("statusCode", statusCode)
            .put("errorMessage", errorMessage)
            .put("data", inner)
            .toString()

    private val screeningInner =
        """{"tbScreeningList":[{"id":1,"benId":100,"visitDate":"Jul 22, 2023 8:17:23 AM","coughMoreThan2Weeks":true,"bloodInSputum":false}]}"""

    private val suspectedInner =
        """{"tbSuspectedList":[{"id":1,"benId":100,"visitDate":"Jul 22, 2023 8:17:23 AM","isSputumCollected":true,"nikshayId":"NK1"}]}"""

    private val confirmedInner =
        """{"tbConfirmedCases":[{"id":1,"benId":100,"regimenType":"DS","treatmentStartDate":"2026-01-05","followUpDate":"2026-02-05"}]}"""

    @Test
    fun `screening pull saves a parsed record for a known beneficiary`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns resp200(envelope(screeningInner))
        coEvery { tbDao.getTbScreening(100L, any(), any()) } returns null
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)

        assertEquals(1, repo.getTBScreeningDetailsFromServer())

        coVerify { tbDao.saveTbScreening(any<TBScreeningCache>()) }
    }

    @Test
    fun `screening pull keeps an already stored record untouched`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns resp200(envelope(screeningInner))
        coEvery { tbDao.getTbScreening(100L, any(), any()) } returns mockk(relaxed = true)

        assertEquals(1, repo.getTBScreeningDetailsFromServer())

        coVerify(exactly = 0) { tbDao.saveTbScreening(any<TBScreeningCache>()) }
    }

    @Test
    fun `screening pull skips records for unknown beneficiaries`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns resp200(envelope(screeningInner))
        coEvery { tbDao.getTbScreening(100L, any(), any()) } returns null
        coEvery { benDao.getBen(100L) } returns null

        assertEquals(1, repo.getTBScreeningDetailsFromServer())

        coVerify(exactly = 0) { tbDao.saveTbScreening(any<TBScreeningCache>()) }
    }

    @Test
    fun `screening pull skips records without a visit date`() = runTest {
        loggedIn()
        val inner = """{"tbScreeningList":[{"id":1,"benId":100}]}"""
        coEvery { api.getTBScreeningData(any()) } returns resp200(envelope(inner))

        assertEquals(1, repo.getTBScreeningDetailsFromServer())

        coVerify(exactly = 0) { tbDao.saveTbScreening(any<TBScreeningCache>()) }
    }

    @Test
    fun `screening pull returns 0 when the nested payload is malformed`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":200,"errorMessage":"","data":{}}""")

        assertEquals(0, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on status 5000 with another message`() = runTest {
        loggedIn()
        coEvery { api.getTBScreeningData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"Something else"}""")

        assertEquals(-1, repo.getTBScreeningDetailsFromServer())
    }

    @Test
    fun `suspected pull saves a parsed record for a known beneficiary`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns resp200(envelope(suspectedInner))
        coEvery { tbDao.getTbSuspected(100L, any(), any()) } returns null
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)

        assertEquals(1, repo.getTbSuspectedDetailsFromServer())

        coVerify { tbDao.saveTbSuspected(any<TBSuspectedCache>()) }
    }

    @Test
    fun `suspected pull keeps an already stored record untouched`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns resp200(envelope(suspectedInner))
        coEvery { tbDao.getTbSuspected(100L, any(), any()) } returns mockk(relaxed = true)

        assertEquals(1, repo.getTbSuspectedDetailsFromServer())

        coVerify(exactly = 0) { tbDao.saveTbSuspected(any<TBSuspectedCache>()) }
    }

    @Test
    fun `suspected pull skips records for unknown beneficiaries`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns resp200(envelope(suspectedInner))
        coEvery { tbDao.getTbSuspected(100L, any(), any()) } returns null
        coEvery { benDao.getBen(100L) } returns null

        assertEquals(1, repo.getTbSuspectedDetailsFromServer())

        coVerify(exactly = 0) { tbDao.saveTbSuspected(any<TBSuspectedCache>()) }
    }

    @Test
    fun `suspected pull returns 0 when the nested payload is malformed`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns
            resp200("""{"statusCode":200,"errorMessage":"","data":{}}""")

        assertEquals(0, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `suspected pull returns -1 on status 5000 with another message`() = runTest {
        loggedIn()
        coEvery { api.getTBSuspectedData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"Something else"}""")

        assertEquals(-1, repo.getTbSuspectedDetailsFromServer())
    }

    @Test
    fun `confirmed pull saves every parsed treatment record`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns resp200(envelope(confirmedInner))

        assertEquals(1, repo.getTbConfirmedDetailsFromServer())

        coVerify { tbDao.saveTbConfirmed(any<TBConfirmedTreatmentCache>()) }
    }

    @Test
    fun `confirmed pull keeps going when one record cannot be stored`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns resp200(envelope(confirmedInner))
        coEvery { tbDao.saveTbConfirmed(any<TBConfirmedTreatmentCache>()) } throws
            RuntimeException("constraint")

        assertEquals(1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull tolerates a malformed nested payload`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":200,"errorMessage":"","data":"not-json"}""")

        assertEquals(1, repo.getTbConfirmedDetailsFromServer())

        coVerify(exactly = 0) { tbDao.saveTbConfirmed(any<TBConfirmedTreatmentCache>()) }
    }

    @Test
    fun `confirmed pull returns -1 on status 5000 with another message`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":5000,"errorMessage":"Something else"}""")

        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on an unexpected failure`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } throws RuntimeException("boom")

        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on 401 which is not handled`() = runTest {
        loggedIn()
        coEvery { api.getTBConfirmedData() } returns
            resp200("""{"statusCode":401,"errorMessage":""}""")

        assertEquals(-1, repo.getTbConfirmedDetailsFromServer())
    }

    // =====================================================
    // push chunking across more than one chunk
    // =====================================================

    @Test
    fun `pushUnSyncedRecords splits tb screening records into chunks of twenty`() = runTest {
        loggedIn()
        val caches = List(25) { mockk<TBScreeningCache>(relaxed = true) }
        coEvery { tbDao.getTBScreening(SyncState.UNSYNCED) } returns caches
        coEvery { tbDao.getTbSuspected(SyncState.UNSYNCED) } returns emptyList()
        coEvery { tbDao.getTbConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveTBScreeningData(any()) } returns resp(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 2) { api.saveTBScreeningData(any()) }
    }

    @Test
    fun `pushUnSyncedRecords tolerates a null body on the tb screening push`() = runTest {
        loggedIn()
        onlyScreening(mockk(relaxed = true))
        coEvery { api.saveTBScreeningData(any()) } returns resp(200, null)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tbDao.saveTbScreening(any<TBScreeningCache>()) }
    }

    @Test
    fun `pushUnSyncedRecords tolerates a null body on the tb suspected push`() = runTest {
        loggedIn()
        onlySuspected(mockk(relaxed = true))
        coEvery { api.saveTBSuspectedData(any()) } returns resp(200, null)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tbDao.saveTbSuspected(any<TBSuspectedCache>()) }
    }

    @Test
    fun `pushUnSyncedRecords tolerates a null body on the tb confirmed push`() = runTest {
        loggedIn()
        onlyConfirmed(mockk(relaxed = true))
        coEvery { api.saveTBConfirmedData(any()) } returns resp(200, null)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tbDao.saveTbConfirmed(any<TBConfirmedTreatmentCache>()) }
    }

    @Test
    fun `pushUnSyncedRecords keeps tb suspected unsynced when the push throws`() = runTest {
        loggedIn()
        onlySuspected(mockk(relaxed = true))
        coEvery { api.saveTBSuspectedData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords keeps tb confirmed unsynced when the push throws`() = runTest {
        loggedIn()
        onlyConfirmed(mockk(relaxed = true))
        coEvery { api.saveTBConfirmedData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

}
