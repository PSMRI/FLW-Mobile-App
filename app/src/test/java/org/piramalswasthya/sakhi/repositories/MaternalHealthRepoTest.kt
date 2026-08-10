package org.piramalswasthya.sakhi.repositories

import android.app.Application
import android.content.res.Resources
import androidx.work.WorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ANCPost
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.PwrPost
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [MaternalHealthRepo]. Consolidated from the previously separate
 * MaternalHealthRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MaternalHealthRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Application
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var maternalHealthDao: MaternalHealthDao
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var mockResources: Resources

    private lateinit var repo: MaternalHealthRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        repo = MaternalHealthRepo(
            context, amritApiService, maternalHealthDao,
            database, userRepo, benDao, preferenceDao, benRepo
        )
    }

    private fun createMockUser(): User {
        return mockk<User>(relaxed = true).also {
            every { it.userName } returns "testuser"
            every { it.userId } returns 1
            every { it.serviceMapId } returns 10
            every { it.vanId } returns 4
            every { it.password } returns "password"
        }
    }

    private suspend fun assertNoUser(block: suspend () -> Unit) {
        try {
            block()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    private fun loggedInUser(): User = mockk<User>(relaxed = true).also {
        every { it.userId } returns 42
        every { it.userName } returns "asha_user"
        every { it.password } returns "pw"
        every { preferenceDao.getLoggedInUser() } returns it
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { user.serviceMapId } returns 10
        every { preferenceDao.getLoggedInUser() } returns user
    }

    @Test
    fun `getSavedRegistrationRecord returns record when exists`() = runTest {
        val record = mockk<PregnantWomanRegistrationCache>()
        coEvery { maternalHealthDao.getSavedRecord(100L) } returns record

        val result = repo.getSavedRegistrationRecord(100L)

        assertTrue(result != null)
    }

    @Test
    fun `getSavedRegistrationRecord returns null when not exists`() = runTest {
        coEvery { maternalHealthDao.getSavedRecord(100L) } returns null

        val result = repo.getSavedRegistrationRecord(100L)

        assertTrue(result == null)
    }

    @Test
    fun `getLatestActiveRegistrationRecord delegates to dao`() = runTest {
        val record = mockk<PregnantWomanRegistrationCache>()
        coEvery { maternalHealthDao.getSavedActiveRecord(100L) } returns record

        val result = repo.getLatestActiveRegistrationRecord(100L)

        assertTrue(result != null)
        coVerify { maternalHealthDao.getSavedActiveRecord(100L) }
    }

    @Test
    fun `getSavedAncRecord returns record by benId and visitNumber`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.getSavedRecord(100L, 2) } returns record

        val result = repo.getSavedAncRecord(100L, 2)

        assertTrue(result != null)
    }

    @Test
    fun `getLatestAncRecord returns latest anc`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.getLatestAnc(100L) } returns record

        val result = repo.getLatestAncRecord(100L)

        assertTrue(result != null)
    }

    @Test
    fun `getAllActiveAncRecords returns list`() = runTest {
        val records = listOf(
            mockk<PregnantWomanAncCache>(),
            mockk<PregnantWomanAncCache>()
        )
        coEvery { maternalHealthDao.getAllActiveAncRecords(100L) } returns records

        val result = repo.getAllActiveAncRecords(100L)

        assertTrue(result.size == 2)
    }

    @Test
    fun `getAllInActiveAncRecords returns list`() = runTest {
        coEvery { maternalHealthDao.getAllInActiveAncRecords(100L) } returns emptyList()

        val result = repo.getAllInActiveAncRecords(100L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getBenFromId delegates to benDao`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L) } returns ben

        val result = repo.getBenFromId(100L)

        assertTrue(result != null)
        coVerify { benDao.getBen(100L) }
    }

    @Test
    fun `persistRegisterRecord calls dao saveRecord`() = runTest {
        val record = mockk<PregnantWomanRegistrationCache>()
        coEvery { maternalHealthDao.saveRecord(record) } returns Unit

        repo.persistRegisterRecord(record)

        coVerify(exactly = 1) { maternalHealthDao.saveRecord(record) }
    }

    @Test
    fun `persistAncRecord calls dao saveRecord`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.saveRecord(record) } returns Unit

        repo.persistAncRecord(record)

        coVerify(exactly = 1) { maternalHealthDao.saveRecord(record) }
    }

    @Test
    fun `processNewAncVisit with empty list returns true`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns emptyList()

        val result = repo.processNewAncVisit()

        assertTrue(result)
    }

    @Test
    fun `processNewAncVisit syncs records successfully`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user

        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)

        // Mock successful API response
        val successBody = """{"statusCode":200,"errorMessage":"Success","data":{}}""".toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.success(successBody)
        coEvery { amritApiService.postAncForm(any()) } returns response
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit

        val result = repo.processNewAncVisit()

        assertTrue(result)
        // Verify the record was updated to SYNCED state
        coVerify(atLeast = 1) { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) }
    }

    @Test
    fun `processNewAncVisit handles record failure with isolation`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user

        val ancRecord1 = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord1.benId } returns 100L
        every { ancRecord1.asPostModel() } returns mockk(relaxed = true)

        val ancRecord2 = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord2.benId } returns 200L
        // This one will fail - no ben exists
        coEvery { benDao.getBen(200L) } returns null

        val ancRecord3 = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord3.benId } returns 300L
        every { ancRecord3.asPostModel() } returns mockk(relaxed = true)

        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns
                listOf(ancRecord1, ancRecord2, ancRecord3)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { benDao.getBen(300L) } returns mockk(relaxed = true)

        val successBody = """{"statusCode":200,"errorMessage":"Success"}""".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { amritApiService.postAncForm(any()) } returns Response.success(successBody)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit

        val result = repo.processNewAncVisit()

        // Should still return true even though record 2 failed
        assertTrue("Process should return true even with partial failures", result)
    }

    @Test
    fun `processNewAncVisit throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewAncVisit()
            assertTrue("Should have thrown", false)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `processNewPwr with empty list returns true`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { maternalHealthDao.getAllUnprocessedPWRs() } returns emptyList()

        val result = repo.processNewPwr()

        assertTrue(result)
    }

    @Test
    fun `processNewPwr throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewPwr()
            assertTrue("Should have thrown", false)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    @Test
    fun `getSavedRecordANC delegates to dao`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.getSavedRecordANC(100L) } returns record
        assertTrue(repo.getSavedRecordANC(100L) === record)
    }

    @Test
    fun `getLastVisitRecordANC delegates to dao getLastVisitAncRecord`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.getLastVisitAncRecord(100L) } returns record
        assertTrue(repo.getLastVisitRecordANC(100L) === record)
    }

    @Test
    fun `getAllAncRecords delegates to dao getAllAncRecordsFor`() = runTest {
        coEvery { maternalHealthDao.getAllAncRecordsFor(100L) } returns emptyList()
        assertTrue(repo.getAllAncRecords(100L).isEmpty())
    }

    @Test
    fun `updateAncRecord delegates to dao updateANC`() = runTest {
        val arr = arrayOf(mockk<PregnantWomanAncCache>(relaxed = true))
        repo.updateAncRecord(arr)
        coVerify { maternalHealthDao.updateANC(*anyVararg()) }
    }

    @Test
    fun `updateExpiredPregnancyWomen delegates to benDao`() = runTest {
        repo.updateExpiredPregnancyWomen()
        coVerify { benDao.moveExpiredPregnantWomenToECT(any(), any()) }
    }

    @Test
    fun `setToInactive updates active anc and pwr records`() = runTest {
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        coEvery { maternalHealthDao.getAllActiveAncRecords(any<Set<Long>>()) } returns listOf(anc)
        coEvery { maternalHealthDao.getAllActivePwrRecords(any<Set<Long>>()) } returns listOf(pwr)

        repo.setToInactive(setOf(1L))

        coVerify { maternalHealthDao.updateANC(*anyVararg()) }
        coVerify { maternalHealthDao.updatePwr(any()) }
    }

    @Test
    fun `setToInactive does not update when nothing active`() = runTest {
        coEvery { maternalHealthDao.getAllActiveAncRecords(any<Set<Long>>()) } returns emptyList()
        coEvery { maternalHealthDao.getAllActivePwrRecords(any<Set<Long>>()) } returns emptyList()

        repo.setToInactive(setOf(1L))

        coVerify(exactly = 0) { maternalHealthDao.updateANC(*anyVararg()) }
        coVerify(exactly = 0) { maternalHealthDao.updatePwr(any()) }
    }

    @Test
    fun `postPwrToAmritServer returns false for empty set`() = runTest {
        assertFalse(repo.postPwrToAmritServer(mutableSetOf()))
    }

    @Test
    fun `postPwrToAmritServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))) }
    }

    @Test
    fun `getPwrDetailsFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getPwrDetailsFromServer() }
    }

    @Test
    fun `getAncVisitDetailsFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getAncVisitDetailsFromServer() }
    }

    @Test
    fun `getPwrDetailsFromServer returns 1 on 200 with empty data`() = runTest {
        loggedInUser()
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getPwrData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getPwrDetailsFromServer())
    }

    @Test
    fun `getPwrDetailsFromServer returns 0 on no record found`() = runTest {
        loggedInUser()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getPwrData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getPwrDetailsFromServer())
    }

    @Test
    fun `getPwrDetailsFromServer returns -2 when token refresh succeeds`() = runTest {
        loggedInUser()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.getPwrData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getPwrDetailsFromServer())
    }

    @Test
    fun `getPwrDetailsFromServer returns -1 when token refresh fails`() = runTest {
        loggedInUser()
        val json = """{"statusCode":401,"errorMessage":""}"""
        coEvery { amritApiService.getPwrData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getPwrDetailsFromServer())
    }

    @Test
    fun `getPwrDetailsFromServer returns -1 on unexpected status code`() = runTest {
        loggedInUser()
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { amritApiService.getPwrData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getPwrDetailsFromServer())
    }

    @Test
    fun `getAncVisitDetailsFromServer returns 1 on 200 with empty data`() = runTest {
        loggedInUser()
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getAncVisitDetailsFromServer())
    }

    @Test
    fun `getAncVisitDetailsFromServer returns 0 on no record found`() = runTest {
        loggedInUser()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getAncVisitDetailsFromServer())
    }

    @Test
    fun `getAncVisitDetailsFromServer returns -2 when token refresh succeeds`() = runTest {
        loggedInUser()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getAncVisitDetailsFromServer())
    }

    @Test
    fun `getAncVisitDetailsFromServer returns -1 when token refresh fails`() = runTest {
        loggedInUser()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getAncVisitDetailsFromServer())
    }

    @Test
    fun `getAncVisitDetailsFromServer returns -1 on unexpected status code`() = runTest {
        loggedInUser()
        val json = """{"statusCode":1234,"errorMessage":""}"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getAncVisitDetailsFromServer())
    }

    @Test
    fun `getCurrentDate formats millis as iso like string`() {
        val result = MaternalHealthRepo.getCurrentDate(0L)
        assertTrue(result.contains("T"))
        assertTrue(result.endsWith(".000Z"))
    }

    @Test
    fun `postPwrToAmritServer returns true on statusCode 200`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"","statusCode":200}"""
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse(json)

        assertTrue(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    @Test
    fun `postPwrToAmritServer returns false on token refresh success`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"","statusCode":5002}"""
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    @Test
    fun `postPwrToAmritServer returns false on token refresh failure`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"","statusCode":401}"""
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    @Test
    fun `postPwrToAmritServer returns false on unexpected statusCode`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"weird","statusCode":9999}"""
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse(json)

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    @Test
    fun `postPwrToAmritServer returns false on non-200 http`() = runTest {
        loggedIn()
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse("{}", code = 500)

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    @Test
    fun `processNewPwr uploads record successfully`() = runTest {
        loggedIn()
        val record = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { record.benId } returns 100L
        every { record.asPwrPost() } returns mockk<PwrPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedPWRs() } returns listOf(record)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updatePwr(any()) } returns Unit
        val json = """{"errorMessage":"","statusCode":200}"""
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse(json)

        assertTrue(repo.processNewPwr())
        coVerify(atLeast = 1) { maternalHealthDao.updatePwr(any()) }
    }

    @Test
    fun `ancDueCount flow val is built at construction`() {
        assertNotNull(repo.ancDueCount)
    }

    @Test
    fun `saveDeliveryStatusFromList returns early when beneficiary not found`() = runTest {
        coEvery { benDao.getBen(999L) } returns null

        repo.saveDeliveryStatusFromList(
            benId = 999L,
            visitNumber = 1,
            isDelivered = false,
            userName = "asha"
        )

        coVerify(exactly = 0) { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) }
    }

    private fun mockWorkManager() {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
    }

    @Test
    fun `saveDeliveryStatusFromList creates new anc cache and skips ben update when not delivered`() = runTest {
        mockWorkManager()
        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benDao.getBen(10L) } returns ben
        coEvery { maternalHealthDao.getSavedRecord(10L, 2) } returns null
        val savedSlot = slot<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.saveRecord(capture(savedSlot)) } returns Unit

        repo.saveDeliveryStatusFromList(
            benId = 10L,
            visitNumber = 2,
            isDelivered = false,
            userName = "asha1"
        )

        assertEquals("N", savedSlot.captured.processed)
        assertEquals(SyncState.UNSYNCED, savedSlot.captured.syncState)
        assertEquals(false, savedSlot.captured.pregnantWomanDelivered)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveDeliveryStatusFromList updates existing anc cache and marks ben delivered when processed is not N`() = runTest {
        mockWorkManager()
        every { mockResources.getStringArray(R.array.nbr_reproductive_status_array2) } returns
                arrayOf("Not Pregnant", "Pregnant", "Postnatal Mother")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns "P"
        coEvery { benDao.getBen(20L) } returns ben
        val existingAncCache = PregnantWomanAncCache(
            benId = 20L,
            visitNumber = 1,
            createdBy = "asha2",
            updatedBy = "asha2",
            syncState = SyncState.SYNCED,
            frontFilePath = "",
            backFilePath = "",
            processed = "P"
        )
        coEvery { maternalHealthDao.getSavedRecord(20L, 1) } returns existingAncCache
        coEvery { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { benRepo.updateRecord(any()) } returns Unit

        repo.saveDeliveryStatusFromList(
            benId = 20L,
            visitNumber = 1,
            isDelivered = true,
            userName = "asha2"
        )

        assertEquals("U", existingAncCache.processed)
        assertEquals(true, existingAncCache.pregnantWomanDelivered)
        assertEquals(SyncState.UNSYNCED, existingAncCache.syncState)
        verify { ben.processed = "U" }
        coVerify(exactly = 1) { benRepo.updateRecord(ben) }
    }

    @Test
    fun `saveDeliveryStatusFromList leaves ben processed as N when already N even though delivered`() = runTest {
        mockWorkManager()
        every { mockResources.getStringArray(R.array.nbr_reproductive_status_array2) } returns
                arrayOf("Not Pregnant", "Pregnant", "Postnatal Mother")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns "N"
        coEvery { benDao.getBen(30L) } returns ben
        val existingAncCache = PregnantWomanAncCache(
            benId = 30L,
            visitNumber = 3,
            createdBy = "asha3",
            updatedBy = "asha3",
            syncState = SyncState.SYNCED,
            frontFilePath = "",
            backFilePath = "",
            processed = "N"
        )
        coEvery { maternalHealthDao.getSavedRecord(30L, 3) } returns existingAncCache
        coEvery { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { benRepo.updateRecord(any()) } returns Unit

        repo.saveDeliveryStatusFromList(
            benId = 30L,
            visitNumber = 3,
            isDelivered = true,
            userName = "asha3"
        )

        assertEquals("N", existingAncCache.processed)
        verify(exactly = 0) { ben.processed = "U" }
        coVerify(exactly = 1) { benRepo.updateRecord(ben) }
    }

    // ---------------- pull non-200 http fall-through ----------------

    @Test
    fun `getPwrDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedInUser()
        coEvery { amritApiService.getPwrData(any()) } returns jsonResponse("{}", code = 500)

        assertEquals(-1, repo.getPwrDetailsFromServer())
    }

    @Test
    fun `getAncVisitDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedInUser()
        coEvery { amritApiService.getAncVisitsData(any()) } returns jsonResponse("{}", code = 500)

        assertEquals(-1, repo.getAncVisitDetailsFromServer())
    }

    // ---------------- postPwrToAmritServer statusCode-missing branch ----------------

    @Test
    fun `postPwrToAmritServer returns false when statusCode missing`() = runTest {
        loggedIn()
        // No statusCode key -> jsonObj.isNull("statusCode") is true -> IllegalState thrown
        // and swallowed -> bad-response -> false.
        val json = """{"errorMessage":""}"""
        coEvery { amritApiService.postPwrForm(any()) } returns jsonResponse(json)

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    // ---------------- postPwrToAmritServer additional branch coverage ----------------

    @Test
    fun `postPwrToAmritServer returns false when response body is null`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { amritApiService.postPwrForm(any()) } returns response

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    @Test
    fun `postPwrToAmritServer retries on SocketTimeoutException and succeeds on second attempt`() = runTest {
        loggedIn()
        var callCount = 0
        val successJson = """{"errorMessage":"","statusCode":200}"""
        coEvery { amritApiService.postPwrForm(any()) } coAnswers {
            callCount++
            if (callCount == 1) throw SocketTimeoutException("timed out")
            jsonResponse(successJson)
        }

        val result = repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true)))

        assertTrue(result)
        coVerify(exactly = 2) { amritApiService.postPwrForm(any()) }
    }

    @Test
    fun `postPwrToAmritServer returns false after exhausting retries on persistent SocketTimeoutException`() = runTest {
        loggedIn()
        coEvery { amritApiService.postPwrForm(any()) } throws SocketTimeoutException("timed out")

        val result = repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true)), retryCount = 1)

        assertFalse(result)
        coVerify(exactly = 2) { amritApiService.postPwrForm(any()) }
    }

    @Test
    fun `postPwrToAmritServer returns false when postPwrForm throws JSONException directly`() = runTest {
        loggedIn()
        coEvery { amritApiService.postPwrForm(any()) } throws JSONException("bad json")

        assertFalse(repo.postPwrToAmritServer(mutableSetOf(mockk<PwrPost>(relaxed = true))))
    }

    // ---------------- processNewAncVisit upload-failure isolation ----------------

    @Test
    fun `processNewAncVisit marks unsynced on unexpected statusCode`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        val json = """{"statusCode":9999,"errorMessage":"weird"}"""
        coEvery { amritApiService.postAncForm(any()) } returns jsonResponse(json)

        assertTrue(repo.processNewAncVisit())
        coVerify(atLeast = 1) { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) }
    }

    @Test
    fun `processNewAncVisit marks unsynced on non-200 http`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { amritApiService.postAncForm(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.processNewAncVisit())
        coVerify(atLeast = 1) { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) }
    }

    // ---------------- postDataToAmritServer (ANC) token-refresh / retry branches ----------------

    @Test
    fun `processNewAncVisit marks unsynced when 5002 token refresh succeeds because the retry throw is caught as IOException`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        val expired = """{"statusCode":5002,"errorMessage":"expired"}"""
        coEvery { amritApiService.postAncForm(any()) } returns jsonResponse(expired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.processNewAncVisit())

        coVerify(exactly = 1) { amritApiService.postAncForm(any()) }
        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
        verify { ancRecord.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewAncVisit marks unsynced when ANC token refresh fails on 401`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { amritApiService.postAncForm(any()) } returns
                jsonResponse("""{"statusCode":401,"errorMessage":"expired"}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.processNewAncVisit())

        coVerify(exactly = 1) { amritApiService.postAncForm(any()) }
        verify { ancRecord.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewAncVisit marks unsynced when ANC push response body is null`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { amritApiService.postAncForm(any()) } returns response

        assertTrue(repo.processNewAncVisit())

        verify { ancRecord.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewAncVisit marks unsynced when statusCode key is json null`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { amritApiService.postAncForm(any()) } returns
                jsonResponse("""{"statusCode":null,"errorMessage":""}""")

        assertTrue(repo.processNewAncVisit())

        verify { ancRecord.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun `processNewAncVisit exhausts retries when ANC push keeps timing out`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { amritApiService.postAncForm(any()) } throws SocketTimeoutException()

        assertTrue(repo.processNewAncVisit())

        coVerify(exactly = 4) { amritApiService.postAncForm(any()) }
        verify { ancRecord.syncState = SyncState.UNSYNCED }
    }

    // ---------------- postDataToAmritServer (ANC) outer JSONException branch ----------------

    @Test
    fun `processNewAncVisit marks unsynced when postAncForm throws JSONException directly`() = runTest {
        loggedIn()
        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit
        coEvery { amritApiService.postAncForm(any()) } throws JSONException("bad json")

        assertTrue(repo.processNewAncVisit())

        coVerify(exactly = 1) { amritApiService.postAncForm(any()) }
        verify { ancRecord.syncState = SyncState.UNSYNCED }
    }

    // ---------------- savePwrCacheFromResponse / isHighRisk via getPwrDetailsFromServer ----------------

    private fun pwrPullResponse(dataArrayJson: String): Response<ResponseBody> {
        val outer = JSONObject()
        outer.put("statusCode", 200)
        outer.put("errorMessage", "")
        outer.put("data", dataArrayJson)
        return jsonResponse(outer.toString())
    }

    private fun ancPullResponse(dataArrayJson: String): Response<ResponseBody> {
        val outer = JSONObject()
        outer.put("statusCode", 200)
        outer.put("errorMessage", "")
        outer.put("data", dataArrayJson)
        return jsonResponse(outer.toString())
    }

    @Test
    fun `getPwrDetailsFromServer saves new pwr cache and creates high-risk assess when none exists`() = runTest {
        loggedInUser()
        val pwrArrayJson = """[{"benId":501,"createdDate":"2024-01-01","isRegistered":true,"rhNegative":"Yes","homeDelivery":"No","badObstetric":"No","isFirstPregnancyTest":true,"lmpDate":"2024-01-01","createdBy":"asha","updatedBy":"asha","isActive":true}]"""
        coEvery { amritApiService.getPwrData(any()) } returns pwrPullResponse(pwrArrayJson)

        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benDao.getBen(501L) } returns ben
        coEvery { maternalHealthDao.getSavedRecord(501L) } returns null
        val hrpDao = mockk<HrpDao>(relaxed = true)
        every { database.hrpDao } returns hrpDao
        every { hrpDao.getPregnantAssess(501L) } returns null
        coEvery { maternalHealthDao.saveRecord(any<PregnantWomanRegistrationCache>()) } returns Unit
        val assessSlot = slot<HRPPregnantAssessCache>()
        every { hrpDao.saveRecord(capture(assessSlot)) } returns Unit

        val result = repo.getPwrDetailsFromServer()

        assertEquals(1, result)
        coVerify { maternalHealthDao.saveRecord(any<PregnantWomanRegistrationCache>()) }
        assertEquals(501L, assessSlot.captured.benId)
        assertTrue(assessSlot.captured.isHighRisk)
    }

    @Test
    fun `getPwrDetailsFromServer updates existing assess as high-risk for first-time pregnancy flag and skips duplicate pwr save`() = runTest {
        loggedInUser()
        val pwrArrayJson = """[{"benId":502,"createdDate":"2024-01-01","isRegistered":true,"rhNegative":"No","homeDelivery":"No","badObstetric":"No","isFirstPregnancyTest":false,"lmpDate":"2024-01-01","createdBy":"asha","updatedBy":"asha","isActive":true}]"""
        coEvery { amritApiService.getPwrData(any()) } returns pwrPullResponse(pwrArrayJson)

        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benDao.getBen(502L) } returns ben
        val existingCache = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        coEvery { maternalHealthDao.getSavedRecord(502L) } returns existingCache
        val existingAssess = HRPPregnantAssessCache(benId = 502L, isHighRisk = false)
        val hrpDao = mockk<HrpDao>(relaxed = true)
        every { database.hrpDao } returns hrpDao
        every { hrpDao.getPregnantAssess(502L) } returns existingAssess
        every { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) } returns Unit

        val result = repo.getPwrDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { maternalHealthDao.saveRecord(any<PregnantWomanRegistrationCache>()) }
        verify { hrpDao.saveRecord(existingAssess) }
        assertTrue(existingAssess.isHighRisk)
    }

    @Test
    fun `getPwrDetailsFromServer keeps assess as not high-risk when all risk flags are negative`() = runTest {
        loggedInUser()
        val pwrArrayJson = """[{"benId":507,"createdDate":"2024-01-01","isRegistered":true,"rhNegative":"No","homeDelivery":"No","badObstetric":"No","isFirstPregnancyTest":true,"lmpDate":"2024-01-01","createdBy":"asha","updatedBy":"asha","isActive":true}]"""
        coEvery { amritApiService.getPwrData(any()) } returns pwrPullResponse(pwrArrayJson)

        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benDao.getBen(507L) } returns ben
        val existingCache = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        coEvery { maternalHealthDao.getSavedRecord(507L) } returns existingCache
        val existingAssess = HRPPregnantAssessCache(benId = 507L, isHighRisk = false)
        val hrpDao = mockk<HrpDao>(relaxed = true)
        every { database.hrpDao } returns hrpDao
        every { hrpDao.getPregnantAssess(507L) } returns existingAssess
        every { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) } returns Unit

        val result = repo.getPwrDetailsFromServer()

        assertEquals(1, result)
        assertFalse(existingAssess.isHighRisk)
    }

    @Test
    fun `getPwrDetailsFromServer skips processing when beneficiary does not exist`() = runTest {
        loggedInUser()
        val pwrArrayJson = """[{"benId":503,"createdDate":"2024-01-01","isRegistered":true}]"""
        coEvery { amritApiService.getPwrData(any()) } returns pwrPullResponse(pwrArrayJson)
        coEvery { benDao.getBen(503L) } returns null

        val result = repo.getPwrDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { maternalHealthDao.saveRecord(any<PregnantWomanRegistrationCache>()) }
    }

    @Test
    fun `getPwrDetailsFromServer skips record with null createdDate`() = runTest {
        loggedInUser()
        val pwrArrayJson = """[{"benId":504,"isRegistered":true}]"""
        coEvery { amritApiService.getPwrData(any()) } returns pwrPullResponse(pwrArrayJson)

        val result = repo.getPwrDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { benDao.getBen(504L) }
    }

    @Test
    fun `getPwrDetailsFromServer returns 0 when pwr data payload is malformed`() = runTest {
        loggedInUser()
        coEvery { amritApiService.getPwrData(any()) } returns pwrPullResponse("not-a-json-array")

        assertEquals(0, repo.getPwrDetailsFromServer())
    }

    // ---------------- saveANCCacheFromResponse via getAncVisitDetailsFromServer ----------------

    @Test
    fun `getAncVisitDetailsFromServer saves new anc cache and skips ben update when not delivered`() = runTest {
        loggedInUser()
        val ancArrayJson = """[{"benId":601,"ancVisit":1,"isActive":true,"createdDate":"2024-01-01","isBabyDelivered":false,"createdBy":"asha","updatedBy":"asha"}]"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns ancPullResponse(ancArrayJson)

        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benDao.getBen(601L) } returns ben
        coEvery { maternalHealthDao.getSavedRecord(601L, 1) } returns null
        coEvery { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) } returns Unit

        val result = repo.getAncVisitDetailsFromServer()

        assertEquals(1, result)
        coVerify { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) }
        coVerify(exactly = 0) { benDao.updateBen(any()) }
    }

    @Test
    fun `getAncVisitDetailsFromServer marks ben as postnatal mother when baby delivered and status not already set`() = runTest {
        loggedInUser()
        val ancArrayJson = """[{"benId":602,"ancVisit":2,"isActive":true,"createdDate":"2024-01-01","isBabyDelivered":true,"createdBy":"asha","updatedBy":"asha"}]"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns ancPullResponse(ancArrayJson)

        val genDetails = BenRegGen(reproductiveStatusId = 1)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns genDetails
        coEvery { benDao.getBen(602L) } returns ben
        val existingCache = mockk<PregnantWomanAncCache>(relaxed = true)
        coEvery { maternalHealthDao.getSavedRecord(602L, 2) } returns existingCache
        coEvery { benDao.updateBen(any()) } returns Unit

        val result = repo.getAncVisitDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) }
        coVerify { benDao.updateBen(ben) }
        assertEquals("Postnatal Mother", genDetails.reproductiveStatus)
        assertEquals(3, genDetails.reproductiveStatusId)
    }

    @Test
    fun `getAncVisitDetailsFromServer skips ben update when already marked postnatal mother`() = runTest {
        loggedInUser()
        val ancArrayJson = """[{"benId":603,"ancVisit":3,"isActive":true,"createdDate":"2024-01-01","isBabyDelivered":true,"createdBy":"asha","updatedBy":"asha"}]"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns ancPullResponse(ancArrayJson)

        val genDetails = BenRegGen(reproductiveStatusId = 3)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns genDetails
        coEvery { benDao.getBen(603L) } returns ben
        coEvery { maternalHealthDao.getSavedRecord(603L, 3) } returns mockk(relaxed = true)

        val result = repo.getAncVisitDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { benDao.updateBen(any()) }
    }

    @Test
    fun `getAncVisitDetailsFromServer skips processing when beneficiary does not exist`() = runTest {
        loggedInUser()
        val ancArrayJson = """[{"benId":604,"ancVisit":1,"isActive":true,"createdDate":"2024-01-01","createdBy":"asha","updatedBy":"asha"}]"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns ancPullResponse(ancArrayJson)
        coEvery { benDao.getBen(604L) } returns null

        val result = repo.getAncVisitDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { maternalHealthDao.saveRecord(any<PregnantWomanAncCache>()) }
    }

    @Test
    fun `getAncVisitDetailsFromServer skips record with null createdDate`() = runTest {
        loggedInUser()
        val ancArrayJson = """[{"benId":605,"ancVisit":1,"isActive":true,"createdBy":"asha","updatedBy":"asha"}]"""
        coEvery { amritApiService.getAncVisitsData(any()) } returns ancPullResponse(ancArrayJson)

        val result = repo.getAncVisitDetailsFromServer()

        assertEquals(1, result)
        coVerify(exactly = 0) { benDao.getBen(605L) }
    }

    @Test
    fun `getAncVisitDetailsFromServer returns 0 when anc data payload is malformed`() = runTest {
        loggedInUser()
        coEvery { amritApiService.getAncVisitsData(any()) } returns ancPullResponse("not-a-json-array")

        assertEquals(0, repo.getAncVisitDetailsFromServer())
    }
}
