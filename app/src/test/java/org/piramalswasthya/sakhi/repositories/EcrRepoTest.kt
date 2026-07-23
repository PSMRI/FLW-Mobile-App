package org.piramalswasthya.sakhi.repositories

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

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
}
