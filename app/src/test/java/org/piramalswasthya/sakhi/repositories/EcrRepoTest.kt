package org.piramalswasthya.sakhi.repositories

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.EcrDao
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EcrPost
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [EcrRepo]. Consolidated from EcrRepoTest + Extra/Extra2/Extra3:
 * getter/persist/save delegations, latest-ECT / antra-dose getters, push and pull
 * coordinators (no-user + empty-set guards, when(responseStatusCode) branches,
 * token-refresh paths) and the ECR/ECT server post methods.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EcrRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var context: Application
    @MockK private lateinit var ecrDao: EcrDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var hrpDao: HrpDao

    private lateinit var repo: EcrRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.ecrDao } returns ecrDao
        coEvery { database.benDao } returns benDao
        coEvery { database.hrpDao } returns hrpDao
        repo = EcrRepo(amritApiService, userRepo, database, preferenceDao, tmcNetworkApiService, context)
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

    private val tokenRefresh = """{"statusCode":5002,"errorMessage":""}"""
    private val unexpected = """{"statusCode":9999,"errorMessage":""}"""

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns ecr when exists`() = runTest {
        val ecr = mockk<EligibleCoupleRegCache>()
        coEvery { ecrDao.getSavedECR(100L) } returns ecr

        val result = repo.getSavedRecord(100L)

        assertNotNull(result)
        assertEquals(ecr, result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { ecrDao.getSavedECR(999L) } returns null

        val result = repo.getSavedRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // getBenFromId() Tests
    // =====================================================

    @Test
    fun `getBenFromId returns ben when exists`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L) } returns ben

        val result = repo.getBenFromId(100L)

        assertNotNull(result)
        assertEquals(ben, result)
    }

    @Test
    fun `getBenFromId returns null when not exists`() = runTest {
        coEvery { benDao.getBen(999L) } returns null

        val result = repo.getBenFromId(999L)

        assertNull(result)
    }

    // =====================================================
    // persistRecord() Tests
    // =====================================================

    @Test
    fun `persistRecord calls dao upsert`() = runTest {
        val ecr = mockk<EligibleCoupleRegCache>()
        coEvery { ecrDao.upsert(ecr) } returns Unit

        repo.persistRecord(ecr)

        coVerify(exactly = 1) { ecrDao.upsert(ecr) }
    }

    // =====================================================
    // getNoOfChildren() Tests
    // =====================================================

    @Test
    fun `getNoOfChildren returns count`() = runTest {
        coEvery { ecrDao.getNoOfChildren(100L) } returns 3

        val result = repo.getNoOfChildren(100L)

        assertEquals(3, result)
    }

    @Test
    fun `getNoOfChildren returns null when not exists`() = runTest {
        coEvery { ecrDao.getNoOfChildren(999L) } returns null

        val result = repo.getNoOfChildren(999L)

        assertNull(result)
    }

    // =====================================================
    // getEct() Tests
    // =====================================================

    @Test
    fun `getEct returns record when exists`() = runTest {
        val ect = mockk<EligibleCoupleTrackingCache>()
        coEvery { ecrDao.getEct(100L, 1000L) } returns ect

        val result = repo.getEct(100L, 1000L)

        assertNotNull(result)
        assertEquals(ect, result)
    }

    @Test
    fun `getEct returns null when not exists`() = runTest {
        coEvery { ecrDao.getEct(999L, 1000L) } returns null

        val result = repo.getEct(999L, 1000L)

        assertNull(result)
    }

    // =====================================================
    // saveEct() Tests
    // =====================================================

    @Test
    fun `saveEct calls dao upsert`() = runTest {
        val ect = mockk<EligibleCoupleTrackingCache>()
        coEvery { ecrDao.upsert(ect) } returns Unit

        repo.saveEct(ect)

        coVerify(exactly = 1) { ecrDao.upsert(ect) }
    }

    // =====================================================
    // latest-ECT / antra-dose getters
    // =====================================================

    @Test
    fun `getLatestEctByBenId delegates to dao`() = runTest {
        val ect = mockk<EligibleCoupleTrackingCache>()
        coEvery { ecrDao.getLatestEct(50L) } returns ect

        assertEquals(ect, repo.getLatestEctByBenId(50L))
        coVerify { ecrDao.getLatestEct(50L) }
    }

    @Test
    fun `getAllAntraDoses delegates to dao`() = runTest {
        val doses = listOf(mockk<EligibleCoupleTrackingCache>(relaxed = true))
        coEvery { ecrDao.getAllAntraDoses(50L) } returns doses

        assertEquals(doses, repo.getAllAntraDoses(50L))
        coVerify { ecrDao.getAllAntraDoses(50L) }
    }

    // =====================================================
    // push coordinators (empty / no-user guards)
    // =====================================================

    @Test
    fun `pushAndUpdateEcrRecord returns true when no unprocessed records`() = runTest {
        coEvery { ecrDao.getAllUnprocessedECR() } returns emptyList()

        assertTrue(repo.pushAndUpdateEcrRecord())
    }

    @Test
    fun `pushAndUpdateEctRecord returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { ecrDao.getAllUnprocessedECT() } returns emptyList()

        assertTrue(repo.pushAndUpdateEctRecord())
    }

    @Test
    fun `pushAndUpdateEctRecord throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pushAndUpdateEctRecord()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `postECRDataToAmritServer returns false for empty set`() = runTest {
        assertFalse(repo.postECRDataToAmritServer(mutableSetOf()))
    }

    @Test
    fun `postECRDataToAmritServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true)))
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pullAndPersistEcrRecord throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pullAndPersistEcrRecord()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pullAndPersistEctRecord throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pullAndPersistEctRecord()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // ---------------- pullAndPersistEcrRecord branches ----------------

    @Test
    fun `pullAndPersistEcrRecord returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.pullAndPersistEcrRecord())
    }

    @Test
    fun `pullAndPersistEcrRecord returns 1 on empty data array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":[]}"""
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.pullAndPersistEcrRecord())
    }

    @Test
    fun `pullAndPersistEcrRecord returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse("{}", code = 500)

        assertEquals(-1, repo.pullAndPersistEcrRecord())
    }

    @Test
    fun `pullAndPersistEcrRecord returns -1 on token refresh success`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.pullAndPersistEcrRecord(0))
    }

    @Test
    fun `pullAndPersistEcrRecord returns -1 on token refresh failure`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.pullAndPersistEcrRecord(0))
    }

    @Test
    fun `pullAndPersistEcrRecord returns -1 on unexpected status`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(unexpected)

        assertEquals(-1, repo.pullAndPersistEcrRecord(0))
    }

    // ---------------- pullAndPersistEctRecord branches ----------------

    @Test
    fun `pullAndPersistEctRecord returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.pullAndPersistEctRecord())
    }

    @Test
    fun `pullAndPersistEctRecord returns 1 on empty data array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":[]}"""
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.pullAndPersistEctRecord())
    }

    @Test
    fun `pullAndPersistEctRecord returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse("{}", code = 500)

        assertEquals(-1, repo.pullAndPersistEctRecord())
    }

    @Test
    fun `pullAndPersistEctRecord returns -1 on token refresh success`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.pullAndPersistEctRecord(0))
    }

    @Test
    fun `pullAndPersistEctRecord returns -1 on unexpected status`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(unexpected)

        assertEquals(-1, repo.pullAndPersistEctRecord(0))
    }

    // ---------------- postECRDataToAmritServer branches ----------------

    @Test
    fun `postECRDataToAmritServer returns true on statusCode 200`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":""}"""
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse(json)

        assertTrue(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    @Test
    fun `postECRDataToAmritServer returns false on unexpected statusCode`() = runTest {
        loggedIn()
        val json = """{"statusCode":999,"errorMessage":"weird"}"""
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse(json)

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    @Test
    fun `postECRDataToAmritServer returns false on non-200 http`() = runTest {
        loggedIn()
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse("{}", code = 500)

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    @Test
    fun `postECRDataToAmritServer returns false on token refresh success`() = runTest {
        loggedIn()
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    @Test
    fun `postECRDataToAmritServer returns false on token refresh failure`() = runTest {
        loggedIn()
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    // ---------------- pushAndUpdateEcrRecord success upload loop ----------------

    @Test
    fun `pushAndUpdateEcrRecord uploads record successfully`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { record.benId } returns 100L
        every { record.asPostModel(any()) } returns mockk<EcrPost>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECR() } returns listOf(record)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        val json = """{"errorMessage":"","statusCode":200}"""
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse(json)

        assertTrue(repo.pushAndUpdateEcrRecord())
        coVerify(atLeast = 1) { ecrDao.update(record) }
    }

    // ---------------- postECRDataToAmritServer statusCode-missing branch ----------------

    @Test
    fun `postECRDataToAmritServer returns false when statusCode missing`() = runTest {
        loggedIn()
        // Body has no statusCode key -> jsonObj.isNull("statusCode") is true ->
        // IllegalStateException is thrown and swallowed -> bad-response -> false.
        val json = """{"errorMessage":""}"""
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse(json)

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    // ---------------- pushAndUpdateEcrRecord isolation branches ----------------

    @Test
    fun `pushAndUpdateEcrRecord marks unsynced when beneficiary missing`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { record.benId } returns 100L
        coEvery { ecrDao.getAllUnprocessedECR() } returns listOf(record)
        coEvery { benDao.getBen(100L) } returns null

        assertTrue(repo.pushAndUpdateEcrRecord())
        coVerify(atLeast = 1) { ecrDao.update(record) }
    }

    @Test
    fun `pushAndUpdateEcrRecord marks unsynced when upload fails`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { record.benId } returns 100L
        every { record.asPostModel(any()) } returns mockk<EcrPost>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECR() } returns listOf(record)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { amritApiService.postEcrForm(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushAndUpdateEcrRecord())
        coVerify(atLeast = 1) { ecrDao.update(record) }
    }

    // =====================================================
    // ECR pull payload parsing (getEcrCacheFromServerResponse / getHighRiskAssess)
    // =====================================================

    /** One fully-populated ECR pull record: two children, kit details and HRP flags. */
    private fun ecrPullPayload(isRegistered: Boolean = true): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 100,
            "registrationDate": "Jan 15, 2026 10:30:00 AM",
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "updatedDate": "Jan 16, 2026 10:30:00 AM",
            "createdBy": "asha",
            "updatedBy": "asha",
            "lmpDate": "2026-01-01",
            "bankAccountNumber": 1234567890,
            "bankName": "SBI",
            "branchName": "Main",
            "ifsc": "SBIN0001234",
            "numChildren": 2,
            "dob1": "Jan 1, 2015 10:00:00 AM",
            "age1": 11,
            "gender1": "male",
            "marriageFirstChildGap": 2,
            "dob2": "Jan 1, 2018 10:00:00 AM",
            "age2": 8,
            "gender2": "female",
            "firstAndSecondChildGap": 3,
            "isRegistered": $isRegistered,
            "isKitHandedOver": true,
            "kitPhoto1": "photo1",
            "kitPhoto2": "photo2",
            "misCarriage": "yes",
            "homeDelivery": "no",
            "medicalIssues": "none",
            "pastCSection": "no",
            "isHighRisk": true
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEcrRecord saves parsed ecr and new high risk assessment`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayload())
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { ecrDao.getSavedECR(100L) } returns null
        every { hrpDao.getNonPregnantAssess(100L) } returns null

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify { ecrDao.upsert(any<EligibleCoupleRegCache>()) }
        verify { hrpDao.saveRecord(any<HRPNonPregnantAssessCache>()) }
    }

    @Test
    fun `pullAndPersistEcrRecord merges into an existing high risk assessment`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayload())
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        // ECR already stored locally -> the pulled copy must not overwrite it.
        coEvery { ecrDao.getSavedECR(100L) } returns mockk(relaxed = true)
        val existing = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        every { hrpDao.getNonPregnantAssess(100L) } returns existing

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleRegCache>()) }
        verify { existing.isHighRisk = true }
        verify { hrpDao.saveRecord(existing) }
    }

    @Test
    fun `pullAndPersistEcrRecord skips unregistered couples`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns
                jsonResponse(ecrPullPayload(isRegistered = false))
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        every { hrpDao.getNonPregnantAssess(100L) } returns null

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleRegCache>()) }
        // The high-risk assessment is parsed independently of isRegistered.
        verify { hrpDao.saveRecord(any<HRPNonPregnantAssessCache>()) }
    }

    @Test
    fun `pullAndPersistEcrRecord ignores records for unknown beneficiaries`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayload())
        coEvery { benDao.getBen(100L) } returns null

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleRegCache>()) }
        verify(exactly = 0) { hrpDao.saveRecord(any<HRPNonPregnantAssessCache>()) }
    }

    // =====================================================
    // ECT pull payload parsing (getEctCacheFromServerResponse)
    // =====================================================

    private fun ectPullPayload(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 100,
            "visitDate": "Jan 15, 2026 10:30:00 AM",
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "updatedDate": "Jan 16, 2026 10:30:00 AM",
            "createdBy": "asha",
            "updatedBy": "asha",
            "lmpDate": "2026-01-01",
            "dateOfAntraInjection": "Jan 10, 2026 10:00:00 AM",
            "dueDateOfAntraInjection": "20-04-2026",
            "methodOfContraception": "Injectable/2",
            "mpaFile": "mpa.pdf",
            "dischargeSummary1": "d1.pdf",
            "dischargeSummary2": "d2.pdf",
            "isPregnancyTestDone": "Yes",
            "isActive": true,
            "pregnancyTestResult": "Negative",
            "isPregnant": "No",
            "usingFamilyPlanning": true
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEctRecord saves parsed tracking record`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(ectPullPayload())
        coEvery { ecrDao.ectWithsameCreateDateExists(any()) } returns false
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)

        assertEquals(1, repo.pullAndPersistEctRecord())

        coVerify { ecrDao.upsert(any<EligibleCoupleTrackingCache>()) }
    }

    @Test
    fun `pullAndPersistEctRecord skips a record already stored for that date`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(ectPullPayload())
        coEvery { ecrDao.ectWithsameCreateDateExists(any()) } returns true
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)

        assertEquals(1, repo.pullAndPersistEctRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleTrackingCache>()) }
    }

    @Test
    fun `pullAndPersistEctRecord skips records for unknown beneficiaries`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(ectPullPayload())
        coEvery { ecrDao.ectWithsameCreateDateExists(any()) } returns false
        coEvery { benDao.getBen(100L) } returns null

        assertEquals(1, repo.pullAndPersistEctRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleTrackingCache>()) }
    }

    @Test
    fun `pullAndPersistEctRecord returns 0 when the payload cannot be parsed`() = runTest {
        loggedIn()
        // "data" is an object, not the expected array -> parse fails -> 0.
        val json = """{"statusCode":200,"errorMessage":"","data":{}}"""
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.pullAndPersistEctRecord())
    }

    @Test
    fun `pullAndPersistEcrRecord returns 0 when the payload cannot be parsed`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":{}}"""
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.pullAndPersistEcrRecord())
    }

    // =====================================================
    // pushAndUpdateEctRecord upload loop
    // =====================================================

    @Test
    fun `pushAndUpdateEctRecord uploads record successfully`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECT() } returns listOf(record)
        val json = """{"errorMessage":"","statusCode":200}"""
        coEvery { amritApiService.postEctForm(any()) } returns jsonResponse(json)

        assertTrue(repo.pushAndUpdateEctRecord())

        coVerify(atLeast = 2) { ecrDao.updateEligibleCoupleTracking(record) }
        coVerify { amritApiService.postEctForm(any()) }
    }

    @Test
    fun `pushAndUpdateEctRecord marks unsynced when upload fails`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECT() } returns listOf(record)
        coEvery { amritApiService.postEctForm(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushAndUpdateEctRecord())

        coVerify(atLeast = 2) { ecrDao.updateEligibleCoupleTracking(record) }
    }

    @Test
    fun `pushAndUpdateEctRecord marks unsynced on token refresh response`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECT() } returns listOf(record)
        coEvery { amritApiService.postEctForm(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushAndUpdateEctRecord())

        coVerify { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `pushAndUpdateEctRecord marks unsynced when statusCode is missing`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECT() } returns listOf(record)
        coEvery { amritApiService.postEctForm(any()) } returns jsonResponse("""{"errorMessage":""}""")

        assertTrue(repo.pushAndUpdateEctRecord())

        coVerify(atLeast = 2) { ecrDao.updateEligibleCoupleTracking(record) }
    }

    // ---------------- postECRDataToAmritServer / postECTDataToAmritServer retry exhaustion ----------------

    @Test
    fun `postECRDataToAmritServer exhausts retries when the api keeps timing out`() = runTest {
        loggedIn()
        coEvery { amritApiService.postEcrForm(any()) } throws SocketTimeoutException()

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))

        coVerify(exactly = 4) { amritApiService.postEcrForm(any()) }
    }

    @Test
    fun `pushAndUpdateEctRecord marks unsynced when the api keeps timing out`() = runTest {
        loggedIn()
        val record = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        coEvery { ecrDao.getAllUnprocessedECT() } returns listOf(record)
        coEvery { amritApiService.postEctForm(any()) } throws SocketTimeoutException()

        assertTrue(repo.pushAndUpdateEctRecord())

        coVerify(exactly = 4) { amritApiService.postEctForm(any()) }
        coVerify(atLeast = 2) { ecrDao.updateEligibleCoupleTracking(record) }
    }

    @Test
    fun `postECRDataToAmritServer returns false when response body is null`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { amritApiService.postEcrForm(any()) } returns response

        assertFalse(repo.postECRDataToAmritServer(mutableSetOf(mockk<EcrPost>(relaxed = true))))
    }

    // =====================================================
    // retry exhaustion on the pull side
    // =====================================================

    @Test
    fun `pullAndPersistEcrRecord returns -1 after exhausting retries on repeated timeout`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } throws SocketTimeoutException()

        assertEquals(-1, repo.pullAndPersistEcrRecord())

        coVerify(exactly = 4) { tmcNetworkApiService.getEcrFormData(any()) }
    }

    @Test
    fun `pullAndPersistEctRecord returns -1 after exhausting retries on repeated timeout`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } throws SocketTimeoutException()

        assertEquals(-1, repo.pullAndPersistEctRecord())

        coVerify(exactly = 4) { tmcNetworkApiService.getEctFormData(any()) }
    }

    // =====================================================
    // getEcrCacheFromServerResponse - full/minimal field coverage
    // =====================================================

    private fun ecrPullPayloadNineChildren(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 200,
            "registrationDate": "Jan 15, 2026 10:30:00 AM",
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "createdBy": "asha",
            "isRegistered": true,
            "numChildren": 9,
            "dob1": "Jan 1, 2001 10:00:00 AM", "age1": 25, "gender1": "male", "marriageFirstChildGap": 1,
            "dob2": "Jan 1, 2002 10:00:00 AM", "age2": 24, "gender2": "female", "firstAndSecondChildGap": 2,
            "dob3": "Jan 1, 2003 10:00:00 AM", "age3": 23, "gender3": "male", "secondAndThirdChildGap": 3,
            "dob4": "Jan 1, 2004 10:00:00 AM", "age4": 22, "gender4": "female", "thirdAndFourthChildGap": 4,
            "dob5": "Jan 1, 2005 10:00:00 AM", "age5": 21, "gender5": "male", "fourthAndFifthChildGap": 5,
            "dob6": "Jan 1, 2006 10:00:00 AM", "age6": 20, "gender6": "female", "fifthANdSixthChildGap": 6,
            "dob7": "Jan 1, 2007 10:00:00 AM", "age7": 19, "gender7": "male", "sixthAndSeventhChildGap": 7,
            "dob8": "Jan 1, 2008 10:00:00 AM", "age8": 18, "gender8": "female", "seventhAndEighthChildGap": 8,
            "dob9": "Jan 1, 2009 10:00:00 AM", "age9": 17, "gender9": "male", "eighthAndNinthChildGap": 9
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEcrRecord parses all nine children with alternating genders`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayloadNineChildren())
        coEvery { benDao.getBen(200L) } returns mockk(relaxed = true)
        coEvery { ecrDao.getSavedECR(200L) } returns null
        every { hrpDao.getNonPregnantAssess(200L) } returns null
        coEvery { ecrDao.upsert(match<EligibleCoupleRegCache> { it.noOfMaleChildren == 5 && it.noOfFemaleChildren == 4 }) } returns Unit

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify { ecrDao.upsert(match<EligibleCoupleRegCache> { it.noOfMaleChildren == 5 && it.noOfFemaleChildren == 4 }) }
    }

    private fun ecrPullPayloadMinimal(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 300,
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "createdBy": "asha",
            "isRegistered": true
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEcrRecord uses createdDate fallback when optional fields absent`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayloadMinimal())
        coEvery { benDao.getBen(300L) } returns mockk(relaxed = true)
        coEvery { ecrDao.getSavedECR(300L) } returns null
        every { hrpDao.getNonPregnantAssess(300L) } returns null

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify {
            ecrDao.upsert(match<EligibleCoupleRegCache> {
                it.noOfChildren == 0 && it.bankAccount == null && it.gender1 == null
            })
        }
    }

    private fun ecrPullPayloadInvalidGender(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 400,
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "createdBy": "asha",
            "isRegistered": true,
            "numChildren": 1,
            "dob1": "Jan 1, 2001 10:00:00 AM",
            "age1": 25,
            "gender1": "unknown"
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEcrRecord skips record when gender is invalid`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayloadInvalidGender())
        coEvery { benDao.getBen(400L) } returns mockk(relaxed = true)
        every { hrpDao.getNonPregnantAssess(400L) } returns null

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleRegCache>()) }
    }

    private fun ecrPullPayloadBadCreatedDate(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 500,
            "createdDate": "not-a-date",
            "createdBy": "asha",
            "isRegistered": true
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEcrRecord skips both ecr and high risk assessment when createdDate is invalid`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayloadBadCreatedDate())
        coEvery { benDao.getBen(500L) } returns mockk(relaxed = true)

        assertEquals(1, repo.pullAndPersistEcrRecord())

        coVerify(exactly = 0) { ecrDao.upsert(any<EligibleCoupleRegCache>()) }
        verify(exactly = 0) { hrpDao.saveRecord(any<HRPNonPregnantAssessCache>()) }
    }

    @Test
    fun `pullAndPersistEcrRecord defaults missing high risk flags when absent`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEcrFormData(any()) } returns jsonResponse(ecrPullPayloadMinimal())
        coEvery { benDao.getBen(300L) } returns mockk(relaxed = true)
        coEvery { ecrDao.getSavedECR(300L) } returns null
        every { hrpDao.getNonPregnantAssess(300L) } returns null

        assertEquals(1, repo.pullAndPersistEcrRecord())

        verify {
            hrpDao.saveRecord(match<HRPNonPregnantAssessCache> {
                it.isHighRisk == false && it.misCarriage == null
            })
        }
    }

    // =====================================================
    // getEctCacheFromServerResponse - additional field coverage
    // =====================================================

    private fun ectPullPayloadMinimal(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 600,
            "visitDate": "Jan 15, 2026 10:30:00 AM",
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "createdBy": "asha"
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEctRecord defaults missing optional fields`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(ectPullPayloadMinimal())
        coEvery { ecrDao.ectWithsameCreateDateExists(any()) } returns false
        coEvery { benDao.getBen(600L) } returns mockk(relaxed = true)

        assertEquals(1, repo.pullAndPersistEctRecord())

        coVerify {
            ecrDao.upsert(match<EligibleCoupleTrackingCache> {
                it.isActive == false && it.isPregnancyTestDone == null && it.methodOfContraception == null
            })
        }
    }

    private fun ectPullPayloadNoSlashContraception(): String = """
        {"statusCode":200,"errorMessage":"","data":[
          {
            "benId": 700,
            "visitDate": "Jan 15, 2026 10:30:00 AM",
            "createdDate": "Jan 15, 2026 10:30:00 AM",
            "createdBy": "asha",
            "methodOfContraception": "Condom",
            "antraDose": "manualDose"
          }
        ]}
    """.trimIndent()

    @Test
    fun `pullAndPersistEctRecord falls back to antraDose field when contraception has no slash`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getEctFormData(any()) } returns jsonResponse(ectPullPayloadNoSlashContraception())
        coEvery { ecrDao.ectWithsameCreateDateExists(any()) } returns false
        coEvery { benDao.getBen(700L) } returns mockk(relaxed = true)

        assertEquals(1, repo.pullAndPersistEctRecord())

        coVerify {
            ecrDao.upsert(match<EligibleCoupleTrackingCache> { it.antraDose == "manualDose" })
        }
    }
}
