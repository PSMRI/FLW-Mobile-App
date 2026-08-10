package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import retrofit2.HttpException
import retrofit2.Response

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

}
