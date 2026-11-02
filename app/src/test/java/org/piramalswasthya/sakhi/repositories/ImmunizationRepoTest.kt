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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationPost
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [ImmunizationRepo]. Consolidated from the previously separate
 * ImmunizationRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImmunizationRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var tbDao: TBDao
    @MockK private lateinit var immunizationDao: ImmunizationDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService
    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private val isMitanin = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)

    private lateinit var repo: ImmunizationRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.w(any(), any<String>()) } returns 0
        repo = ImmunizationRepo(tbDao, immunizationDao, benDao, preferenceDao, userRepo, amritApiService)
    }

    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)

    private fun emptyErrorBody() = "".toResponseBody(jsonMediaType)

    private fun loggedIn() {
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    private fun unsyncedRecord(): ImmunizationCache {
        val record = mockk<ImmunizationCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<ImmunizationPost>(relaxed = true)
        coEvery { immunizationDao.getVaccineById(any()) } returns mockk<Vaccine>(relaxed = true)
        return record
    }

    private fun nullBodyResponse(code: Int = 200): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns null
        return response
    }

    private fun vaccineJson(id: Int, name: String) =
        """{"vaccineId":$id,"vaccineName":"$name","minAllowedAgeInMillis":0,"maxAllowedAgeInMillis":100,"category":"CHILD","immunizationService":"BIRTH"}"""

    private fun vaccinesResponseBody(vaccinesJsonArray: String): Response<ResponseBody> {
        val outer = JSONObject().apply {
            put("statusCode", 200)
            put("errorMessage", "")
            put("data", vaccinesJsonArray)
        }
        return Response.success(jsonBody(outer.toString()))
    }

    @Test
    fun `pushUnSyncedChildImmunizationRecords returns true when no unsynced`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedChildImmunizationRecords()
        assertTrue(result)
    }

    @Test
    fun `getImmunizationDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repo.getImmunizationDetailsFromServer() }
        }
    }

    @Test
    fun `getVaccineDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repo.getVaccineDetailsFromServer() }
        }
    }

    @Test
    fun `pushUnSyncedChildImmunizationRecords throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repo.pushUnSyncedChildImmunizationRecords() }
        }
    }

    @Test
    fun `getVaccineDetailsFromServer returns -1 on non-200 http code`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            Response.error(500, emptyErrorBody())

        assertEquals(-1, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `getVaccineDetailsFromServer returns 0 when no record found`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            Response.success(jsonBody("""{"statusCode":5000,"errorMessage":"No record found"}"""))

        assertEquals(0, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns 1 on 200 with empty data array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody("""{"statusCode":200,"errorMessage":"","data":"[]"}"""))

        assertEquals(1, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns 0 when no record found`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody("""{"statusCode":5000,"errorMessage":"No record found"}"""))

        assertEquals(0, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns -1 on unknown status code`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody("""{"statusCode":9999,"errorMessage":"weird"}"""))

        // unknown status throws IllegalStateException, caught -> -1
        assertEquals(-1, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns -1 on non-200 http code`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.error(500, emptyErrorBody())

        assertEquals(-1, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getVaccineDetails returns 1 on 200 with empty data array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            Response.success(jsonBody("""{"statusCode":200,"errorMessage":"","data":"[]"}"""))

        assertEquals(1, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `getVaccineDetails returns -1 on unknown status code`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            Response.success(jsonBody("""{"statusCode":9999,"errorMessage":"weird"}"""))

        assertEquals(-1, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":401,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getVaccineDetails returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `getVaccineDetails returns -1 after token refresh retry recursion`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        // socket-timeout catch recurses (discarding result), outer falls to trailing -1
        assertEquals(-1, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `push marks records synced on 200 success`() = runTest {
        loggedIn()
        val record = unsyncedRecord()
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns listOf(record)
        coEvery { amritApiService.postChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedChildImmunizationRecords())
        coVerify { immunizationDao.addImmunizationRecord(record) }
    }

    @Test
    fun `push returns true when chunk fails with unknown status code`() = runTest {
        loggedIn()
        val record = unsyncedRecord()
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns listOf(record)
        coEvery { amritApiService.postChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedChildImmunizationRecords())
    }

    @Test
    fun `push returns true when chunk fails with non-200 http`() = runTest {
        loggedIn()
        val record = unsyncedRecord()
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns listOf(record)
        coEvery { amritApiService.postChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":200}""", code = 500)

        assertTrue(repo.pushUnSyncedChildImmunizationRecords())
    }

    @Test
    fun `push returns true and refreshes token when chunk gets 401 or 5002`() = runTest {
        loggedIn()
        val record = unsyncedRecord()
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns listOf(record)
        coEvery { amritApiService.postChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedChildImmunizationRecords())
        coVerify { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `getImmunizationDetails returns -1 on null response body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getVaccineDetails returns -1 on null response body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `getImmunizationDetails returns 0 when data field missing`() = runTest {
        loggedIn()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")
        assertEquals(0, repo.getImmunizationDetailsFromServer())
    }

    @Test
    fun `getVaccineDetails returns 0 when data field missing`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")
        assertEquals(0, repo.getVaccineDetailsFromServer())
    }

    @Test
    fun `push returns true when vaccine lookup returns null`() = runTest {
        loggedIn()
        val record = mockk<ImmunizationCache>(relaxed = true)
        every { record.asPostModel() } returns mockk<ImmunizationPost>(relaxed = true)
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns listOf(record)
        coEvery { immunizationDao.getVaccineById(any()) } returns null

        assertTrue(repo.pushUnSyncedChildImmunizationRecords())
    }

    @Test
    fun `saveVaccinesFromResponse adds new vaccine when not existing`() = runTest {
        loggedIn()
        val vaccinesJson = "[${vaccineJson(1, "BCG")}]"
        coEvery { amritApiService.getAllChildVaccines(any()) } returns vaccinesResponseBody(vaccinesJson)
        coEvery { immunizationDao.getVaccineByName("BCG") } returns null
        coEvery { immunizationDao.addVaccine(any()) } returns Unit

        assertEquals(1, repo.getVaccineDetailsFromServer())
        coVerify { immunizationDao.addVaccine(any()) }
    }

    @Test
    fun `saveVaccinesFromResponse skips adding vaccine when already exists`() = runTest {
        loggedIn()
        val vaccinesJson = "[${vaccineJson(1, "BCG")}]"
        coEvery { amritApiService.getAllChildVaccines(any()) } returns vaccinesResponseBody(vaccinesJson)
        coEvery { immunizationDao.getVaccineByName("BCG") } returns mockk<Vaccine>(relaxed = true)

        assertEquals(1, repo.getVaccineDetailsFromServer())
        coVerify(exactly = 0) { immunizationDao.addVaccine(any()) }
    }

    @Test
    fun `saveVaccinesFromResponse skips mitanin-only vaccine on non-mitanin flavor`() = runTest {
        loggedIn()
        val vaccinesJson = "[${vaccineJson(1, "PCV-1")}]"
        coEvery { amritApiService.getAllChildVaccines(any()) } returns vaccinesResponseBody(vaccinesJson)

        assertEquals(1, repo.getVaccineDetailsFromServer())
        if (isMitanin) {
            coVerify(exactly = 1) { immunizationDao.getVaccineByName("PCV-1") }
        } else {
            coVerify(exactly = 0) { immunizationDao.getVaccineByName(any()) }
        }
        coVerify(exactly = 0) { immunizationDao.addVaccine(any()) }
    }

    @Test
    fun `saveVaccinesFromResponse processes mixed list skipping mitanin-only and adding others`() = runTest {
        loggedIn()
        val vaccinesJson = "[${vaccineJson(1, "PCV-1")},${vaccineJson(2, "BCG")}]"
        coEvery { amritApiService.getAllChildVaccines(any()) } returns vaccinesResponseBody(vaccinesJson)
        coEvery { immunizationDao.getVaccineByName("BCG") } returns null
        coEvery { immunizationDao.addVaccine(any()) } returns Unit

        assertEquals(1, repo.getVaccineDetailsFromServer())
        coVerify(exactly = 1) { immunizationDao.addVaccine(any()) }
        if (isMitanin) {
            coVerify(exactly = 1) { immunizationDao.getVaccineByName("PCV-1") }
        } else {
            coVerify(exactly = 0) { immunizationDao.getVaccineByName("PCV-1") }
        }
        coVerify(exactly = 1) { immunizationDao.getVaccineByName("BCG") }
    }

    @Test
    fun `getVaccineDetails returns 0 when vaccine data is malformed`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllChildVaccines(any()) } returns
            Response.success(jsonBody("""{"statusCode":200,"errorMessage":"","data":"not-a-json-array"}"""))

        assertEquals(0, repo.getVaccineDetailsFromServer())
    }

    // ---------------- saveImmunizationCacheFromResponse ----------------

    private fun immunizationEntryJson(beneficiaryId: Long = 1L, vaccineId: Int = 2) =
        """{"beneficiaryId":$beneficiaryId,"vaccineId":$vaccineId,"createdBy":"asha","modifiedBy":"asha"}"""

    private fun immunizationOuterJson(dataJson: String): String {
        val outer = JSONObject()
        outer.put("statusCode", 200)
        outer.put("errorMessage", "")
        outer.put("data", dataJson)
        return outer.toString()
    }

    @Test
    fun `getImmunizationDetails saves new record when array data has no existing cache`() = runTest {
        loggedIn()
        val dataJson = "[${immunizationEntryJson()}]"
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody(immunizationOuterJson(dataJson)))
        coEvery { immunizationDao.getImmunizationRecord(1L, 2) } returns null
        coEvery { immunizationDao.addImmunizationRecord(any()) } returns Unit

        assertEquals(1, repo.getImmunizationDetailsFromServer())

        coVerify(exactly = 1) { immunizationDao.addImmunizationRecord(any()) }
    }

    @Test
    fun `getImmunizationDetails skips save when immunization record already exists`() = runTest {
        loggedIn()
        val dataJson = "[${immunizationEntryJson()}]"
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody(immunizationOuterJson(dataJson)))
        coEvery { immunizationDao.getImmunizationRecord(1L, 2) } returns mockk(relaxed = true)

        assertEquals(1, repo.getImmunizationDetailsFromServer())

        coVerify(exactly = 0) { immunizationDao.addImmunizationRecord(any()) }
    }

    @Test
    fun `getImmunizationDetails saves record when data is object wrapper containing array`() = runTest {
        loggedIn()
        val dataJson = """{"entries":[${immunizationEntryJson()}]}"""
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody(immunizationOuterJson(dataJson)))
        coEvery { immunizationDao.getImmunizationRecord(1L, 2) } returns null
        coEvery { immunizationDao.addImmunizationRecord(any()) } returns Unit

        assertEquals(1, repo.getImmunizationDetailsFromServer())

        coVerify(exactly = 1) { immunizationDao.addImmunizationRecord(any()) }
    }

    @Test
    fun `getImmunizationDetails saves single record when data is a bare object`() = runTest {
        loggedIn()
        val dataJson = immunizationEntryJson()
        coEvery { amritApiService.getChildImmunizationDetails(any()) } returns
            Response.success(jsonBody(immunizationOuterJson(dataJson)))
        coEvery { immunizationDao.getImmunizationRecord(1L, 2) } returns null
        coEvery { immunizationDao.addImmunizationRecord(any()) } returns Unit

        assertEquals(1, repo.getImmunizationDetailsFromServer())

        coVerify(exactly = 1) { immunizationDao.addImmunizationRecord(any()) }
    }
}
