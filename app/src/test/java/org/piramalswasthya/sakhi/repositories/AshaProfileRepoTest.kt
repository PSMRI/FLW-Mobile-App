package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [AshaProfileRepo]. Consolidated from AshaProfileRepoTest +
 * ExtraTest: getSavedRecord / saveRecord delegations plus the
 * postDataToAmritServer and pullAndSaveAshaProfile response-handling / guard
 * branches.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AshaProfileRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var profileDao: ProfileDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    private val context: Context = mockk(relaxed = true)

    private lateinit var repo: AshaProfileRepo

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)
    private fun emptyErrorBody() = "".toResponseBody(jsonMediaType)

    @Before
    override fun setUp() {
        super.setUp()
        repo = AshaProfileRepo(amritApiService, profileDao, preferenceDao, userRepo, context)
    }

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns record when exists`() = runTest {
        val profile = mockk<ProfileActivityCache>()
        coEvery { profileDao.getProfileActivityById(100L) } returns profile

        val result = repo.getSavedRecord(100L)

        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { profileDao.getProfileActivityById(999L) } returns null

        val result = repo.getSavedRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // saveRecord() Tests
    // =====================================================

    @Test
    fun `saveRecord calls dao insert`() = runTest {
        val profile = mockk<ProfileActivityCache>()
        coEvery { profileDao.insert(profile) } returns Unit

        repo.saveRecord(profile)

        coVerify(exactly = 1) { profileDao.insert(profile) }
    }

    // ---------------- postDataToAmritServer ----------------

    @Test
    fun `postDataToAmritServer returns false on non-200 http code`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        coEvery { amritApiService.submitAshaProfileData(cache) } returns
            Response.error(500, emptyErrorBody())

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns false when api throws`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        coEvery { amritApiService.submitAshaProfileData(cache) } throws RuntimeException("network")

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns true and skips save when employeeId missing`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        // statusCode 200 outer + inner, data parses to an all-null network model
        // whose employeeId resolves to 0, so saveProfileData returns early.
        val body = """{"statusCode":200,"data":"{}"}"""
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))

        assertTrue(repo.postDataToAmritServer(cache))
    }

    // ---------------- pullAndSaveAshaProfile ----------------

    @Test
    fun `pullAndSaveAshaProfile returns true on statusCode 5000`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.success(jsonBody("""{"statusCode":5000}"""))

        assertTrue(repo.pullAndSaveAshaProfile(user))
    }

    @Test
    fun `pullAndSaveAshaProfile returns true when no data present`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.success(jsonBody("""{"statusCode":200}"""))

        assertTrue(repo.pullAndSaveAshaProfile(user))
    }

    @Test
    fun `pullAndSaveAshaProfile returns false when api throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } throws RuntimeException("network")

        assertFalse(repo.pullAndSaveAshaProfile(user))
    }

    // =====================================================
    // Additional coverage: profile network payload helper
    // =====================================================

    private fun profileNetworkJson(
        employeeId: Int = 501,
        profileImage: String = "file:///data/user/0/pic.jpg"
    ): String {
        return JSONObject().apply {
            put("employeeId", employeeId)
            put("name", "Asha Devi")
            put("profileImage", profileImage)
            put("village", "Village1")
            put("dob", "1990-01-01")
            put("age", 34)
            put("mobileNumber", "9999999999")
            put("alternateMobileNumber", "8888888888")
            put("fatherOrSpouseName", "Ram Lal")
            put("dateOfJoining", "2015-05-05")
            put("bankAccount", "123456789")
            put("ifsc", "IFSC0001")
            put("populationCovered", 1200)
            put("choName", "Cho Name")
            put("choMobile", "7777777777")
            put("awwName", "Aww Name")
            put("awwMobile", "6666666666")
            put("anm1Name", "Anm One")
            put("anm1Mobile", "5555555555")
            put("anm2Name", "Anm Two")
            put("anm2Mobile", "4444444444")
            put("abhaNumber", "ABHA123")
            put("ashaHouseholdRegistration", "Yes")
            put("ashaFamilyMember", "No")
            put("providerServiceMapID", "PSM123")
            put("isFatherOrSpouse", true)
            put("supervisorName", "Supervisor Name")
            put("supervisorMobile", "3333333333")
        }.toString()
    }

    // ---------------- postDataToAmritServer: additional branches ----------------

    @Test
    fun `postDataToAmritServer saves record and returns true on full valid payload`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson())
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(501L) } returns null

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 1) { profileDao.insert(any()) }
    }

    @Test
    fun `postDataToAmritServer downloads server image when not local uri`() = runTest {
        mockkObject(ImageUtils)
        mockkStatic(Uri::class)
        val parsedUri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns parsedUri
        coEvery {
            ImageUtils.saveBenImageFromServerToStorage(context, "encodedServerImage", 501L)
        } returns "file:///data/user/0/downloaded.jpg"

        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson(profileImage = "encodedServerImage"))
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(501L) } returns null

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 1) {
            ImageUtils.saveBenImageFromServerToStorage(context, "encodedServerImage", 501L)
        }
        verify(exactly = 1) { preferenceDao.saveProfilePicUri(parsedUri) }
    }

    @Test
    fun `postDataToAmritServer keeps existing image when server image download fails`() = runTest {
        mockkObject(ImageUtils)
        coEvery {
            ImageUtils.saveBenImageFromServerToStorage(context, "encodedServerImage", 501L)
        } returns null

        val existing = mockk<ProfileActivityCache>(relaxed = true)
        every { existing.profileImage } returns "file:///old/image.jpg"

        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson(profileImage = "encodedServerImage"))
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(501L) } returns existing

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 1) { profileDao.insert(any()) }
    }

    @Test
    fun `postDataToAmritServer returns false when data field missing`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 200) }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns false on malformed json response`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        coEvery { amritApiService.submitAshaProfileData(cache) } returns
            Response.success(jsonBody("not a valid json"))

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns false for unhandled response status code`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 400) }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer refreshes token and gives up when retries exhausted`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 5002) }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { userRepo.refreshTokenTmc(user.userName, user.password) } returns true

        assertFalse(repo.postDataToAmritServer(cache, retryCount = 0))
    }

    @Test
    fun `postDataToAmritServer returns false when refresh token fails on 5002`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 5002) }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { userRepo.refreshTokenTmc(user.userName, user.password) } returns false

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns false when 5002 and no logged in user`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 5002) }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        every { preferenceDao.getLoggedInUser() } returns null

        assertFalse(repo.postDataToAmritServer(cache))
    }

    // ---------------- pullAndSaveAshaProfile: additional branches ----------------

    @Test
    fun `pullAndSaveAshaProfile returns true when http status code is not 200`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.error(404, emptyErrorBody())

        assertTrue(repo.pullAndSaveAshaProfile(user))
    }

    @Test
    fun `pullAndSaveAshaProfile saves record and returns true on full valid payload`() = runTest {
        val user = mockk<User>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson(employeeId = 777))
        }.toString()
        coEvery { amritApiService.getAshaProfileData(any()) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(777L) } returns null

        assertTrue(repo.pullAndSaveAshaProfile(user))

        coVerify(exactly = 1) { profileDao.insert(any()) }
    }

    @Test
    fun `pullAndSaveAshaProfile returns false when saveProfileData throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson(employeeId = 888))
        }.toString()
        coEvery { amritApiService.getAshaProfileData(any()) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(888L) } throws RuntimeException("db failure")

        assertFalse(repo.pullAndSaveAshaProfile(user))
    }

    @Test
    fun `pullAndSaveAshaProfile refreshes token and gives up when retries exhausted`() = runTest {
        val user = mockk<User>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 5002) }.toString()
        coEvery { amritApiService.getAshaProfileData(any()) } returns Response.success(jsonBody(body))
        coEvery { userRepo.refreshTokenTmc(user.userName, user.password) } returns true

        assertFalse(repo.pullAndSaveAshaProfile(user, retryCount = 0))
    }

    @Test
    fun `postDataToAmritServer falls back to existing image when server image is blank`() = runTest {
        val existing = mockk<ProfileActivityCache>(relaxed = true)
        every { existing.profileImage } returns "file:///old/existing.jpg"

        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson(profileImage = ""))
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(501L) } returns existing

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 1) { profileDao.insert(match { it.profileImage == "file:///old/existing.jpg" }) }
    }

    @Test
    fun `postDataToAmritServer defaults image to blank when server image blank and no existing record`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson(profileImage = ""))
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))
        coEvery { profileDao.getProfileActivityById(501L) } returns null

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 1) { profileDao.insert(match { it.profileImage == "" }) }
    }

    @Test
    fun `postDataToAmritServer skips save when data payload is malformed json`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", "not a valid json object")
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 0) { profileDao.insert(any()) }
    }

    @Test
    fun `pullAndSaveAshaProfile returns false when refresh token fails on 5002`() = runTest {
        val user = mockk<User>(relaxed = true)
        val body = JSONObject().apply { put("statusCode", 5002) }.toString()
        coEvery { amritApiService.getAshaProfileData(any()) } returns Response.success(jsonBody(body))
        coEvery { userRepo.refreshTokenTmc(user.userName, user.password) } returns false

        assertFalse(repo.pullAndSaveAshaProfile(user))
    }

    // ---------------- retryCount > 0 recursive retry branches ----------------

    @Test
    fun `postDataToAmritServer recursively retries after token refresh and succeeds`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        val refreshBody = JSONObject().apply { put("statusCode", 5002) }.toString()
        val successBody = JSONObject().apply {
            put("statusCode", 200)
            put("data", profileNetworkJson())
        }.toString()
        coEvery { amritApiService.submitAshaProfileData(cache) } returnsMany listOf(
            Response.success(jsonBody(refreshBody)),
            Response.success(jsonBody(successBody))
        )
        val user = mockk<User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { userRepo.refreshTokenTmc(user.userName, user.password) } returns true
        coEvery { profileDao.getProfileActivityById(501L) } returns null

        assertTrue(repo.postDataToAmritServer(cache))

        coVerify(exactly = 2) { amritApiService.submitAshaProfileData(cache) }
    }

    @Test
    fun `pullAndSaveAshaProfile recursively retries after token refresh and succeeds`() = runTest {
        val user = mockk<User>(relaxed = true)
        val refreshBody = JSONObject().apply { put("statusCode", 5002) }.toString()
        val successBody = JSONObject().apply { put("statusCode", 5000) }.toString()
        coEvery { amritApiService.getAshaProfileData(any()) } returnsMany listOf(
            Response.success(jsonBody(refreshBody)),
            Response.success(jsonBody(successBody))
        )
        coEvery { userRepo.refreshTokenTmc(user.userName, user.password) } returns true

        assertTrue(repo.pullAndSaveAshaProfile(user))

        coVerify(exactly = 2) { amritApiService.getAshaProfileData(any()) }
    }
}
