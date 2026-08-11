package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AadhaarVerifyBioRequest
import org.piramalswasthya.sakhi.network.AbhaApiService
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaGenerateMobileOtpRequest
import org.piramalswasthya.sakhi.network.AbhaResendAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaVerifyAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaVerifyMobileOtpRequest
import org.piramalswasthya.sakhi.network.AddHealthIdRecord
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.CreateAbhaIdGovRequest
import org.piramalswasthya.sakhi.network.CreateAbhaIdRequest
import org.piramalswasthya.sakhi.network.CreateHIDResponse
import org.piramalswasthya.sakhi.network.CreateHealthIdRequest
import org.piramalswasthya.sakhi.network.GenerateOtpHid
import org.piramalswasthya.sakhi.network.LoginGenerateOtpRequest
import org.piramalswasthya.sakhi.network.LoginVerifyOtpRequest
import org.piramalswasthya.sakhi.network.MapHIDtoBeneficiary
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.SearchAbhaRequest
import org.piramalswasthya.sakhi.network.ValidateOtpHid
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class AbhaIdRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var abhaApiService: AbhaApiService
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var abhaGenerated: ABHAGenratedRepo
    @MockK private lateinit var prefDao: PreferenceDao

    private lateinit var repo: AbhaIdRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = AbhaIdRepo(abhaApiService, amritApiService, userRepo, abhaGenerated, prefDao)
    }

    // ---------------- parseAbhaErrorString ----------------

    @Test
    fun `parseAbhaErrorString returns null for null input`() {
        assertNull(repo.parseAbhaErrorString(null))
    }

    @Test
    fun `parseAbhaErrorString returns null for empty input`() {
        assertNull(repo.parseAbhaErrorString(""))
    }

    @Test
    fun `parseAbhaErrorString extracts code and message`() {
        val json = """{"error":{"code":"ABDM-1114","message":"User not found."}}"""

        val result = repo.parseAbhaErrorString(json)

        assertEquals("ABDM-1114", result?.first)
        assertEquals("User not found.", result?.second)
    }

    @Test
    fun `parseAbhaErrorString returns empty pair when error object missing`() {
        val result = repo.parseAbhaErrorString("""{"foo":"bar"}""")

        assertEquals("", result?.first)
        assertEquals("", result?.second)
    }

    @Test
    fun `parseAbhaErrorString returns null for malformed json`() {
        assertNull(repo.parseAbhaErrorString("not json at all"))
    }

    // ---------------- downloadPdfCard ----------------

    @Test
    fun `downloadPdfCard delegates to abha api`() = runTest {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        coEvery { abhaApiService.getPdfCard() } returns response

        assertEquals(response, repo.downloadPdfCard())
    }

    // ---------------- no-user paths ----------------

    @Test
    fun `mapHealthIDToBeneficiary returns error when no user logged in`() = runTest {
        every { prefDao.getLoggedInUser() } returns null
        val req = mockk<MapHIDtoBeneficiary>(relaxed = true)

        val result = repo.mapHealthIDToBeneficiary(req, null)

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    @Test
    fun `addHealthIdRecord returns error when no user logged in`() = runTest {
        every { prefDao.getLoggedInUser() } returns null
        val req = mockk<AddHealthIdRecord>(relaxed = true)

        val result = repo.addHealthIdRecord(req)

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- saveAbhaModelFromRequest ----------------

    @Test
    fun `saveAbhaModelFromRequest does nothing when beneficiaryId missing`() = runTest {
        val req = mockk<MapHIDtoBeneficiary>(relaxed = true)
        every { req.beneficiaryID } returns null

        repo.saveAbhaModelFromRequest(req, null)

        coVerify(exactly = 0) { abhaGenerated.saveAbhaGenrated(any<ABHAModel>()) }
    }

    // ---------------- test helpers ----------------

    private fun jsonResponse(body: String?): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        if (body == null) {
            every { response.body() } returns null
        } else {
            val responseBody = mockk<ResponseBody>(relaxed = true)
            every { responseBody.string() } returns body
            every { response.body() } returns responseBody
        }
        return response
    }

    /** A [Response] with isSuccessful == true and the given body string. */
    private fun successAbhaResponse(body: String): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        every { response.body() } returns responseBody
        return response
    }

    /** An unsuccessful [Response] with the given http code, short-circuiting sendErrorResponse. */
    private fun serviceUnavailableResponse(): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        every { response.code() } returns 503
        return response
    }

    private fun testUser(
        userName: String = "tester",
        password: String = "pwd",
        serviceMapId: Int = 10
    ): User {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns userName
        every { user.password } returns password
        every { user.serviceMapId } returns serviceMapId
        return user
    }

    private fun createHealthIdRequest() = CreateHealthIdRequest(
        otp = null,
        txnId = null,
        address = null,
        dayOfBirth = null,
        email = null,
        profilePhoto = null,
        password = null,
        healthId = null,
        healthIdNumber = null,
        firstName = null,
        gender = null,
        lastName = null,
        middleName = null,
        monthOfBirth = null,
        name = null,
        pincode = null,
        yearOfBirth = null,
        providerServiceMapID = null,
        createdBy = null
    )

    private fun mapHidRequest(beneficiaryID: Long? = 100L) = MapHIDtoBeneficiary(
        beneficiaryRegID = 1L,
        beneficiaryID = beneficiaryID,
        healthId = "hid@sbx",
        healthIdNumber = "12-3456-7890-1234",
        providerServiceMapId = null,
        createdBy = null,
        message = null,
        txnId = "txn-1",
        ABHAProfile = null,
        isNew = false
    )

    // ---------------- createHealthIdWithUid ----------------

    @Test
    fun `createHealthIdWithUid returns success on statusCode 200`() = runTest {
        val hidJson = """{"hID":123,"healthIdNumber":"12-3456-7890-1234","name":"Test User","gender":"F","yearOfBirth":"1990","monthOfBirth":"01","dayOfBirth":"01","firstName":"Test","healthId":"testuser@sbx","lastName":"User","stateCode":"1","districtCode":"1","stateName":"MP","districtName":"Indore","mobile":"9999999999","authMethods":[],"deleted":false}"""
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", hidJson)
        }.toString()
        coEvery { amritApiService.createHid(any()) } returns jsonResponse(body)

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Success)
        val hidResponse = (result as NetworkResult.Success<*>).data as CreateHIDResponse
        assertEquals("12-3456-7890-1234", hidResponse.healthIdNumber)
    }

    @Test
    fun `createHealthIdWithUid refreshes token and retries on session expired`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        val expiredBody = JSONObject().apply {
            put("statusCode", 401)
            put("errorMessage", "Invalid login key or session is expired")
        }.toString()
        val successBody = JSONObject().apply {
            put("statusCode", 200)
            put("data", """{"hID":1,"deleted":false}""")
        }.toString()
        coEvery { amritApiService.createHid(any()) } returnsMany listOf(
            jsonResponse(expiredBody), jsonResponse(successBody)
        )

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 2) { amritApiService.createHid(any()) }
    }

    @Test
    fun `createHealthIdWithUid returns error message for other 401-family codes`() = runTest {
        val body = JSONObject().apply {
            put("statusCode", 5002)
            put("errorMessage", "Some other error")
        }.toString()
        coEvery { amritApiService.createHid(any()) } returns jsonResponse(body)

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals("Some other error", (result as NetworkResult.Error).message)
        assertEquals(0, result.code)
        coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `createHealthIdWithUid returns generic error for unrecognized statusCode`() = runTest {
        val body = """{"statusCode":400,"message":"bad request"}"""
        coEvery { amritApiService.createHid(any()) } returns jsonResponse(body)

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals(0, (result as NetworkResult.Error).code)
        assertEquals(body, result.message)
    }

    @Test
    fun `createHealthIdWithUid returns generic error when response body is null`() = runTest {
        coEvery { amritApiService.createHid(any()) } returns jsonResponse(null)

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals(0, (result as NetworkResult.Error).code)
        assertEquals("null", result.message)
    }

    @Test
    fun `createHealthIdWithUid returns network error on IOException`() = runTest {
        coEvery { amritApiService.createHid(any()) } throws IOException("no net")

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    @Test
    fun `createHealthIdWithUid returns json error for malformed response body`() = runTest {
        coEvery { amritApiService.createHid(any()) } returns jsonResponse("not-json")

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals(-2, (result as NetworkResult.Error).code)
    }

    @Test
    fun `createHealthIdWithUid returns timeout error on SocketTimeoutException`() = runTest {
        coEvery { amritApiService.createHid(any()) } throws SocketTimeoutException("timeout")

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    @Test
    fun `createHealthIdWithUid returns unknown error on generic exception`() = runTest {
        coEvery { amritApiService.createHid(any()) } throws RuntimeException("boom")

        val result = repo.createHealthIdWithUid(createHealthIdRequest())

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
        assertEquals("boom", (result as NetworkResult.Error).message)
    }

    // ---------------- mapHealthIDToBeneficiary ----------------

    @Test
    fun `mapHealthIDToBeneficiary returns success when data has benHealthID and healthId`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        val dataJson = JSONObject().apply {
            put("benHealthID", 1)
            put("healthId", "hid@sbx")
        }
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", dataJson)
        }.toString()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse(body)

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Success)
        coVerify { abhaGenerated.deleteAbhaByBenId(100L) }
    }

    @Test
    fun `mapHealthIDToBeneficiary returns error when data has response field`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        val dataJson = JSONObject().put("response", "Duplicate mapping")
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", dataJson)
        }.toString()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse(body)

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Duplicate mapping", (result as NetworkResult.Error).message)
    }

    @Test
    fun `mapHealthIDToBeneficiary returns unknown format error when data has no recognizable fields`() =
        runTest {
            every { prefDao.getLoggedInUser() } returns testUser()
            val dataJson = JSONObject().put("foo", "bar")
            val body = JSONObject().apply {
                put("statusCode", 200)
                put("data", dataJson)
            }.toString()
            coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse(body)

            val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

            assertTrue(result is NetworkResult.Error)
            assertEquals("Unknown response format", (result as NetworkResult.Error).message)
        }

    @Test
    fun `mapHealthIDToBeneficiary returns unknown format error when data missing`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        val body = JSONObject().apply { put("statusCode", 200) }.toString()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse(body)

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Unknown response format", (result as NetworkResult.Error).message)
    }

    @Test
    fun `mapHealthIDToBeneficiary refreshes token and retries on session expired`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        val expiredBody = JSONObject().apply {
            put("statusCode", 401)
            put("errorMessage", "Invalid login key or session is expired")
        }.toString()
        val dataJson = JSONObject().apply {
            put("benHealthID", 1)
            put("healthId", "hid@sbx")
        }
        val successBody = JSONObject().apply {
            put("statusCode", 200)
            put("data", dataJson)
        }.toString()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returnsMany listOf(
            jsonResponse(expiredBody), jsonResponse(successBody)
        )

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 2) { amritApiService.mapHealthIDToBeneficiary(any()) }
        coVerify(exactly = 1) { abhaGenerated.saveAbhaGenrated(any()) }
    }

    @Test
    fun `mapHealthIDToBeneficiary returns error message for other 401-family codes`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        val body = JSONObject().apply {
            put("statusCode", 5000)
            put("errorMessage", "Some backend error")
        }.toString()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse(body)

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Some backend error", (result as NetworkResult.Error).message)
        coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `mapHealthIDToBeneficiary returns generic error for unrecognized statusCode`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        val body = """{"statusCode":999,"message":"weird"}"""
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse(body)

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Error)
        assertEquals(0, (result as NetworkResult.Error).code)
        assertEquals(body, result.message)
    }

    @Test
    fun `mapHealthIDToBeneficiary saves abha model and returns network error on IOException`() =
        runTest {
            every { prefDao.getLoggedInUser() } returns testUser()
            coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } throws IOException("no net")

            val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

            assertTrue(result is NetworkResult.Error)
            assertEquals(-1, (result as NetworkResult.Error).code)
            coVerify { abhaGenerated.saveAbhaGenrated(any()) }
        }

    @Test
    fun `mapHealthIDToBeneficiary saves abha model and returns json error for malformed body`() =
        runTest {
            every { prefDao.getLoggedInUser() } returns testUser()
            coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } returns jsonResponse("not-json")

            val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

            assertTrue(result is NetworkResult.Error)
            assertEquals(-2, (result as NetworkResult.Error).code)
            coVerify { abhaGenerated.saveAbhaGenrated(any()) }
        }

    @Test
    fun `mapHealthIDToBeneficiary saves abha model and returns timeout error`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } throws
                SocketTimeoutException("timeout")

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
        coVerify { abhaGenerated.saveAbhaGenrated(any()) }
    }

    @Test
    fun `mapHealthIDToBeneficiary saves abha model and returns unknown error on generic exception`() =
        runTest {
            every { prefDao.getLoggedInUser() } returns testUser()
            coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } throws RuntimeException("boom")

            val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), null)

            assertTrue(result is NetworkResult.Error)
            assertEquals(-4, (result as NetworkResult.Error).code)
            assertEquals("boom", (result as NetworkResult.Error).message)
            coVerify { abhaGenerated.saveAbhaGenrated(any()) }
        }

    @Test
    fun `mapHealthIDToBeneficiary saves abha model using ben details when provided`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        coEvery { amritApiService.mapHealthIDToBeneficiary(any()) } throws IOException("no net")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Jane"
        every { ben.lastName } returns "Doe"

        val result = repo.mapHealthIDToBeneficiary(mapHidRequest(), ben)

        assertTrue(result is NetworkResult.Error)
        coVerify {
            abhaGenerated.saveAbhaGenrated(match {
                it.benName == "Jane" && it.benSurname == "Doe"
            })
        }
    }

    // ---------------- getStateAndDistricts ----------------

    @Test
    fun `getStateAndDistricts returns success on isSuccessful response`() = runTest {
        coEvery { abhaApiService.getStateAndDistricts() } returns successAbhaResponse("[]")

        val result = repo.getStateAndDistricts()

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `getStateAndDistricts returns timeout error on SocketTimeoutException`() = runTest {
        coEvery { abhaApiService.getStateAndDistricts() } throws SocketTimeoutException("timeout")

        val result = repo.getStateAndDistricts()

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    // ---------------- generateAadhaarOtpV3 ----------------

    @Test
    fun `generateAadhaarOtpV3 returns success on isSuccessful response`() = runTest {
        coEvery {
            abhaApiService.generateAadhaarOtpV3(any(), any(), any())
        } returns successAbhaResponse("{}")

        val result = repo.generateAadhaarOtpV3(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `generateAadhaarOtpV3 returns unknown error on generic exception`() = runTest {
        coEvery {
            abhaApiService.generateAadhaarOtpV3(any(), any(), any())
        } throws RuntimeException("boom")

        val result = repo.generateAadhaarOtpV3(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- searchAbha ----------------

    @Test
    fun `searchAbha returns success on isSuccessful response with array body`() = runTest {
        coEvery { abhaApiService.searchAbha(any(), any(), any()) } returns successAbhaResponse("[{}]")

        val result = repo.searchAbha(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `searchAbha returns network error on IOException`() = runTest {
        coEvery { abhaApiService.searchAbha(any(), any(), any()) } throws IOException("no net")

        val result = repo.searchAbha(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    // ---------------- generateAbhaOtp ----------------

    @Test
    fun `generateAbhaOtp returns success on isSuccessful response`() = runTest {
        coEvery {
            abhaApiService.loginGenerateOtp(any(), any(), any())
        } returns successAbhaResponse("{}")

        val result = repo.generateAbhaOtp(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `generateAbhaOtp returns network error object on IOException`() = runTest {
        coEvery {
            abhaApiService.loginGenerateOtp(any(), any(), any())
        } throws IOException("no net")

        val result = repo.generateAbhaOtp(mockk(relaxed = true))

        assertEquals(NetworkResult.NetworkError, result)
    }

    // ---------------- verifyAbhaOtp ----------------

    @Test
    fun `verifyAbhaOtp returns success on isSuccessful response`() = runTest {
        coEvery {
            abhaApiService.loginVerifyOtp(any(), any(), any())
        } returns successAbhaResponse("{}")

        val result = repo.verifyAbhaOtp(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `verifyAbhaOtp returns unknown error on generic exception`() = runTest {
        coEvery {
            abhaApiService.loginVerifyOtp(any(), any(), any())
        } throws RuntimeException("boom")

        val result = repo.verifyAbhaOtp(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- resendOtpForAadhaar ----------------

    @Test
    fun `resendOtpForAadhaar returns success on isSuccessful response`() = runTest {
        coEvery { abhaApiService.resendAadhaarOtp(any()) } returns successAbhaResponse("{}")

        val result = repo.resendOtpForAadhaar(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `resendOtpForAadhaar returns network error on IOException`() = runTest {
        coEvery { abhaApiService.resendAadhaarOtp(any()) } throws IOException("no net")

        val result = repo.resendOtpForAadhaar(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    // ---------------- verifyOtpForAadhaar ----------------

    @Test
    fun `verifyOtpForAadhaar returns success on isSuccessful response`() = runTest {
        coEvery {
            abhaApiService.verifyAadhaarOtp3(any(), any(), any())
        } returns successAbhaResponse("{}")

        val result = repo.verifyOtpForAadhaar(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `verifyOtpForAadhaar returns unknown error on generic exception`() = runTest {
        coEvery {
            abhaApiService.verifyAadhaarOtp3(any(), any(), any())
        } throws RuntimeException("boom")

        val result = repo.verifyOtpForAadhaar(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- printAbhaCard ----------------

    @Test
    fun `printAbhaCard returns success on isSuccessful response`() = runTest {
        coEvery { abhaApiService.printAbhaCard(any(), any()) } returns successAbhaResponse("body")

        val result = repo.printAbhaCard()

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `printAbhaCard returns network error on IOException`() = runTest {
        coEvery { abhaApiService.printAbhaCard(any(), any()) } throws IOException("no net")

        val result = repo.printAbhaCard()

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    // ---------------- checkAndGenerateOtpForMobileNumber ----------------

    @Test
    fun `checkAndGenerateOtpForMobileNumber returns success on isSuccessful response`() = runTest {
        coEvery {
            abhaApiService.checkAndGenerateMobileOtp(request = any())
        } returns successAbhaResponse("{}")

        val result = repo.checkAndGenerateOtpForMobileNumber(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `checkAndGenerateOtpForMobileNumber returns unknown error on generic exception`() = runTest {
        coEvery {
            abhaApiService.checkAndGenerateMobileOtp(request = any())
        } throws RuntimeException("boom")

        val result = repo.checkAndGenerateOtpForMobileNumber(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- verifyOtpForMobileNumber ----------------

    @Test
    fun `verifyOtpForMobileNumber returns success on isSuccessful response`() = runTest {
        coEvery {
            abhaApiService.verifyMobileOtp3(any(), any(), any())
        } returns successAbhaResponse("{}")

        val result = repo.verifyOtpForMobileNumber(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `verifyOtpForMobileNumber returns unknown error on generic exception`() = runTest {
        coEvery {
            abhaApiService.verifyMobileOtp3(any(), any(), any())
        } throws RuntimeException("boom")

        val result = repo.verifyOtpForMobileNumber(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- generateAbhaId ----------------

    @Test
    fun `generateAbhaId returns success on isSuccessful response`() = runTest {
        coEvery { abhaApiService.createAbhaId(any()) } returns successAbhaResponse("{}")

        val result = repo.generateAbhaId(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `generateAbhaId returns service unavailable error on 503`() = runTest {
        coEvery { abhaApiService.createAbhaId(any()) } returns serviceUnavailableResponse()

        val result = repo.generateAbhaId(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(503, (result as NetworkResult.Error).code)
    }

    @Test
    fun `generateAbhaId returns unknown error on generic exception`() = runTest {
        coEvery { abhaApiService.createAbhaId(any()) } throws RuntimeException("boom")

        val result = repo.generateAbhaId(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- generateAbhaIdGov ----------------

    @Test
    fun `generateAbhaIdGov returns success on isSuccessful response`() = runTest {
        coEvery { abhaApiService.createAbhaIdGov(any()) } returns successAbhaResponse("{}")

        val result = repo.generateAbhaIdGov(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `generateAbhaIdGov returns unknown error on generic exception`() = runTest {
        coEvery { abhaApiService.createAbhaIdGov(any()) } throws RuntimeException("boom")

        val result = repo.generateAbhaIdGov(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- verifyBio ----------------

    @Test
    fun `verifyBio returns success on isSuccessful response`() = runTest {
        coEvery { abhaApiService.verifyBio(any()) } returns successAbhaResponse("{}")

        val result = repo.verifyBio(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `verifyBio returns network error on IOException`() = runTest {
        coEvery { abhaApiService.verifyBio(any()) } throws IOException("no net")

        val result = repo.verifyBio(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-1, (result as NetworkResult.Error).code)
    }

    // ---------------- generateOtpHid ----------------

    @Test
    fun `generateOtpHid returns success txnId on statusCode 200`() = runTest {
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", JSONObject().put("txnId", "txn-abc"))
        }.toString()
        coEvery { amritApiService.generateOtpHealthId(any()) } returns jsonResponse(body)

        val result = repo.generateOtpHid(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
        assertEquals("txn-abc", (result as NetworkResult.Success<*>).data)
    }

    @Test
    fun `generateOtpHid refreshes token and retries on session expired`() = runTest {
        every { prefDao.getLoggedInUser() } returns testUser()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        val expiredBody = JSONObject().apply {
            put("statusCode", 401)
            put("errorMessage", "Invalid login key or session is expired")
        }.toString()
        val successBody = JSONObject().apply {
            put("statusCode", 200)
            put("data", JSONObject().put("txnId", "txn-retry"))
        }.toString()
        coEvery { amritApiService.generateOtpHealthId(any()) } returnsMany listOf(
            jsonResponse(expiredBody), jsonResponse(successBody)
        )

        val result = repo.generateOtpHid(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 2) { amritApiService.generateOtpHealthId(any()) }
    }

    @Test
    fun `generateOtpHid returns unknown error on generic exception`() = runTest {
        coEvery { amritApiService.generateOtpHealthId(any()) } throws RuntimeException("boom")

        val result = repo.generateOtpHid(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- verifyOtpAndGenerateHealthCard ----------------

    @Test
    fun `verifyOtpAndGenerateHealthCard returns success data on statusCode 200`() = runTest {
        val body = JSONObject().apply {
            put("statusCode", 200)
            put("data", JSONObject().put("data", "card-data"))
        }.toString()
        coEvery { amritApiService.verifyOtpAndGenerateHealthCard(any()) } returns jsonResponse(body)

        val result = repo.verifyOtpAndGenerateHealthCard(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Success)
        assertEquals("card-data", (result as NetworkResult.Success<*>).data)
    }

    @Test
    fun `verifyOtpAndGenerateHealthCard returns error message for other 401-family codes`() = runTest {
        val body = JSONObject().apply {
            put("statusCode", 5000)
            put("errorMessage", "Some backend error")
        }.toString()
        coEvery { amritApiService.verifyOtpAndGenerateHealthCard(any()) } returns jsonResponse(body)

        val result = repo.verifyOtpAndGenerateHealthCard(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals("Some backend error", (result as NetworkResult.Error).message)
        coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `verifyOtpAndGenerateHealthCard returns unknown error on generic exception`() = runTest {
        coEvery {
            amritApiService.verifyOtpAndGenerateHealthCard(any())
        } throws RuntimeException("boom")

        val result = repo.verifyOtpAndGenerateHealthCard(mockk(relaxed = true))

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }
}
