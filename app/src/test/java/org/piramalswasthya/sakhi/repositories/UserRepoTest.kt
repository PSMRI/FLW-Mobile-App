package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.crypt.CryptoUtil
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.model.Facility
import org.piramalswasthya.sakhi.model.FacilityData
import org.piramalswasthya.sakhi.model.FacilityLocation
import org.piramalswasthya.sakhi.model.PeerAtFacility
import org.piramalswasthya.sakhi.model.Supervisor
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.model.UserDetailsInResponse
import org.piramalswasthya.sakhi.model.UserNetworkResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var db: InAppDb
    @MockK private lateinit var vaccineDao: ImmunizationDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var syncDao: SyncDao
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var userRepo: UserRepo

    private val jsonMediaType = "application/json".toMediaTypeOrNull()

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(TokenInsertTmcInterceptor)
        every { TokenInsertTmcInterceptor.setToken(any()) } returns Unit
        every { TokenInsertTmcInterceptor.setJwt(any()) } returns Unit
        mockkConstructor(CryptoUtil::class)
        every { anyConstructed<CryptoUtil>().encrypt(any()) } returns "encryptedPassword"
        userRepo = UserRepo(benDao, db, vaccineDao, preferenceDao, syncDao, amritApiService)
    }

    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)

    private fun emptyErrorBody() = "".toResponseBody(jsonMediaType)

    // =====================================================
    // refreshTokenTmc() Tests
    // =====================================================

    @Test
    fun `refreshTokenTmc with no stored token returns false`() = runTest {
        every { preferenceDao.getRefreshToken() } returns null

        val result = userRepo.refreshTokenTmc("testuser", "password")

        assertFalse(result)
    }

    @Test
    fun `refreshTokenTmc success stores new tokens`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "old_refresh_token"
        coEvery { preferenceDao.registerJWTAmritToken(any()) } returns Unit
        coEvery { preferenceDao.registerRefreshToken(any()) } returns Unit
        coEvery { preferenceDao.registerAmritToken(any()) } returns Unit
        every { preferenceDao.lastAmritTokenFetchTimestamp = any() } returns Unit

        // The refresh endpoint returns tokens at the top level (see UserRepo.refreshTokenTmc
        // and TokenAuthenticator), unlike the login endpoint which nests them under "data".
        val responseJson = """{"statusCode":200,"jwtToken":"new_jwt","refreshToken":"new_refresh"}"""
        val response = Response.success(jsonBody(responseJson))
        coEvery { amritApiService.getRefreshToken(any()) } returns response

        val result = userRepo.refreshTokenTmc("testuser", "password")

        assertTrue(result)
        coVerify { preferenceDao.registerJWTAmritToken("new_jwt") }
        coVerify { preferenceDao.registerRefreshToken("new_refresh") }
    }

    @Test
    fun `refreshTokenTmc with blank jwt returns false`() = runTest {
        // A well-formed body without a jwtToken (e.g. an error payload) must not
        // be treated as a successful refresh.
        every { preferenceDao.getRefreshToken() } returns "old_refresh_token"

        val responseJson = """{"statusCode":401,"errorMessage":"Token expired"}"""
        val response = Response.success(jsonBody(responseJson))
        coEvery { amritApiService.getRefreshToken(any()) } returns response

        val result = userRepo.refreshTokenTmc("testuser", "password")

        assertFalse(result)
    }

    @Test
    fun `refreshTokenTmc HttpException returns false`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "old_refresh_token"
        val errorResponse = Response.error<String>(401, emptyErrorBody())
        coEvery { amritApiService.getRefreshToken(any()) } throws HttpException(errorResponse)

        val result = userRepo.refreshTokenTmc("testuser", "password")

        assertFalse(result)
    }

    // =====================================================
    // saveFirebaseToken() Tests
    // =====================================================

    @Test
    fun `saveFirebaseToken successful call does not throw`() = runTest {
        val response = Response.success(jsonBody("ok"))
        coEvery { amritApiService.saveFirebaseToken(any()) } returns response

        userRepo.saveFirebaseToken(1, "firebase_token", "2026-03-17")
    }

    @Test
    fun `saveFirebaseToken exception is caught silently`() = runTest {
        coEvery { amritApiService.saveFirebaseToken(any()) } throws RuntimeException("Network error")

        userRepo.saveFirebaseToken(1, "firebase_token", "2026-03-17")
    }

    // ---------------- unProcessedRecordCount ----------------

    @Test
    fun `unProcessedRecordCount exposes syncDao flow`() {
        val flow: Flow<List<SyncStatusCache>> = flowOf(emptyList())
        every { syncDao.getSyncStatus() } returns flow
        val repo = UserRepo(benDao, db, vaccineDao, preferenceDao, syncDao, amritApiService)
        assertEquals(flow, repo.unProcessedRecordCount)
    }

    // ---------------- setFacilityData guards ----------------

    @Test
    fun `setFacilityData swallows HttpException`() = runTest {
        val errorResponse = Response.error<String>(500, emptyErrorBody())
        coEvery { amritApiService.getUserDetailsById(any()) } throws HttpException(errorResponse)

        // Should complete without throwing.
        userRepo.setFacilityData(42)
    }

    @Test
    fun `setFacilityData swallows generic exception`() = runTest {
        coEvery { amritApiService.getUserDetailsById(any()) } throws RuntimeException("boom")

        userRepo.setFacilityData(42)
    }

    // ---------------- refreshTokenTmc extra branches ----------------

    @Test
    fun `refreshTokenTmc returns false on generic exception`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "stored_token"
        coEvery { amritApiService.getRefreshToken(any()) } throws RuntimeException("network")

        assertFalse(userRepo.refreshTokenTmc("user", "pass"))
    }

    @Test
    fun `refreshTokenTmc returns false on unsuccessful response`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "stored_token"
        coEvery { amritApiService.getRefreshToken(any()) } returns
            Response.error(500, emptyErrorBody())

        assertFalse(userRepo.refreshTokenTmc("user", "pass"))
    }

    // ---------------- saveFirebaseToken unsuccessful branch ----------------

    @Test
    fun `saveFirebaseToken unsuccessful response does not throw`() = runTest {
        coEvery { amritApiService.saveFirebaseToken(any()) } returns
            Response.error(400, emptyErrorBody())

        userRepo.saveFirebaseToken(1, "token", "2026-03-17")
    }

    // =====================================================
    // clearFirebaseToken() Tests
    // =====================================================

    @Test
    fun `clearFirebaseToken successful call does not throw`() = runTest {
        coEvery { amritApiService.clearFirebaseToken(any()) } returns Response.success(jsonBody("ok"))

        userRepo.clearFirebaseToken(1)
    }

    @Test
    fun `clearFirebaseToken unsuccessful response does not throw`() = runTest {
        coEvery { amritApiService.clearFirebaseToken(any()) } returns
            Response.error(400, emptyErrorBody())

        userRepo.clearFirebaseToken(1)
    }

    @Test
    fun `clearFirebaseToken exception is caught silently`() = runTest {
        coEvery { amritApiService.clearFirebaseToken(any()) } throws RuntimeException("Network error")

        userRepo.clearFirebaseToken(1)
    }

    // =====================================================
    // setFacilityData() happy-path Tests
    // =====================================================

    private fun userDetailsWithFacilityData(facilityData: FacilityData?) = UserDetailsInResponse(
        userId = 5,
        name = "Test User",
        userName = "testuser",
        stateId = 1,
        stateName = "StateX",
        workingDistrictId = 2,
        workingDistrictName = "DistrictX",
        serviceProviderId = 1,
        roleId = 1,
        roleName = "ASHA",
        providerServiceMapId = 1,
        blockId = 1,
        blockName = "BlockX",
        villageId = "1",
        villageName = "VillageX",
        facilityData = facilityData
    )

    @Test
    fun `setFacilityData saves location facility supervisor and peer lists`() = runTest {
        val facilityData = FacilityData(
            location = FacilityLocation(district = "D", blockOrUlb = "B", locationType = "L", state = "S"),
            facility = Facility(facilityId = 10, facilityType = "PHC", facilityName = "FacName"),
            supervisor = Supervisor(mobile = "888", fullName = "SupName2", userId = 99),
            peersAtFacility = listOf(
                PeerAtFacility(role = "CHO", fullName = "Cho1", mobile = "1", userId = 1),
                PeerAtFacility(role = "ANM", fullName = "Anm1", mobile = "2", userId = 2),
                PeerAtFacility(role = "OTHER", fullName = "Other1", mobile = "3", userId = 3)
            )
        )
        coEvery { amritApiService.getUserDetailsById(any()) } returns
            UserNetworkResponse(success = true, message = null, data = userDetailsWithFacilityData(facilityData))

        userRepo.setFacilityData(5)

        verify { preferenceDao.saveLocationType("L") }
        verify { preferenceDao.saveBlock("B") }
        verify { preferenceDao.saveState("S") }
        verify { preferenceDao.saveDistrict("D") }
        verify { preferenceDao.saveSupervisorSubcenter("FacName") }
        verify { preferenceDao.saveFacilityId(10) }
        verify { preferenceDao.saveSupervisorFacilityType("PHC") }
        verify { preferenceDao.saveSupervisorName("SupName2") }
        verify { preferenceDao.saveSupervisorId(99) }
        verify { preferenceDao.saveSupervisorContact("888") }
        verify { preferenceDao.saveChoList(any()) }
        verify { preferenceDao.saveAnmList(any()) }
    }

    @Test
    fun `setFacilityData with null facilityData does nothing`() = runTest {
        coEvery { amritApiService.getUserDetailsById(any()) } returns
            UserNetworkResponse(success = true, message = null, data = userDetailsWithFacilityData(null))

        userRepo.setFacilityData(5)

        verify(exactly = 0) { preferenceDao.saveLocationType(any()) }
        verify(exactly = 0) { preferenceDao.saveSupervisorSubcenter(any()) }
        verify(exactly = 0) { preferenceDao.saveSupervisorName(any()) }
        verify { preferenceDao.saveChoList(any()) }
        verify { preferenceDao.saveAnmList(any()) }
    }

    @Test
    fun `setFacilityData with missing location facility and supervisor fields falls back to defaults`() = runTest {
        val facilityData = FacilityData(
            location = FacilityLocation(district = null, blockOrUlb = null, locationType = null, state = null),
            facility = Facility(facilityId = null, facilityType = null, facilityName = null),
            supervisor = Supervisor(mobile = null, fullName = null, userId = null),
            peersAtFacility = emptyList()
        )
        coEvery { amritApiService.getUserDetailsById(any()) } returns
            UserNetworkResponse(success = true, message = null, data = userDetailsWithFacilityData(facilityData))

        userRepo.setFacilityData(5)

        verify { preferenceDao.saveLocationType("") }
        verify { preferenceDao.saveSupervisorSubcenter("") }
        verify { preferenceDao.saveFacilityId(0) }
        verify { preferenceDao.saveSupervisorName("") }
        verify { preferenceDao.saveSupervisorId(-1) }
        verify { preferenceDao.saveChoList(any()) }
        verify { preferenceDao.saveAnmList(any()) }
    }

    // =====================================================
    // refreshTokenTmc() additional branch Tests
    // =====================================================

    @Test
    fun `refreshTokenTmc does not persist refresh token when new token is blank`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "stored_token"
        val responseJson = """{"statusCode":200,"jwtToken":"jwt_only"}"""
        coEvery { amritApiService.getRefreshToken(any()) } returns Response.success(jsonBody(responseJson))

        val result = userRepo.refreshTokenTmc("user", "pass")

        assertTrue(result)
        coVerify(exactly = 0) { preferenceDao.registerRefreshToken(any()) }
    }

    @Test
    fun `refreshTokenTmc retries once on SocketTimeoutException then succeeds`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "stored_token"
        val responseJson = """{"statusCode":200,"jwtToken":"retry_jwt","refreshToken":"retry_refresh"}"""
        coEvery { amritApiService.getRefreshToken(any()) } throws SocketTimeoutException() andThen
            Response.success(jsonBody(responseJson))

        val result = userRepo.refreshTokenTmc("user", "pass")

        assertTrue(result)
        coVerify(exactly = 2) { amritApiService.getRefreshToken(any()) }
    }

    @Test
    fun `refreshTokenTmc returns false when success response body is missing`() = runTest {
        every { preferenceDao.getRefreshToken() } returns "stored_token"
        coEvery { amritApiService.getRefreshToken(any()) } returns Response.success<ResponseBody>(null)

        val result = userRepo.refreshTokenTmc("user", "pass")

        assertFalse(result)
    }

}
