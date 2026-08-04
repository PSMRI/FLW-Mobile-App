package org.piramalswasthya.sakhi.repositories

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.IncentiveActivityWithRecords
import org.piramalswasthya.sakhi.model.IncentiveCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [IncentiveRepo]. Consolidated from the previously separate
 * IncentiveRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncentiveRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var incentiveDao: IncentiveDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var context: Context
    private val recordsFlow = flowOf(emptyList<IncentiveCache>())
    private val activityFlow = flowOf(emptyList<IncentiveActivityWithRecords>())

    private lateinit var repo: IncentiveRepo

    @Before
    override fun setUp() {
        super.setUp()
        // These are captured in the constructor, so stub before building the repo.
        every { incentiveDao.getAllRecords() } returns recordsFlow
        every { incentiveDao.getAllActivity() } returns activityFlow
        repo = IncentiveRepo(amritApiService, incentiveDao, preferenceDao, userRepo, context)
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    private fun nullBodyResponse(code: Int = 200): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns null
        return response
    }

    @Test
    fun `list and activity_list expose dao flows`() {
        assertEquals(recordsFlow, repo.list)
        assertEquals(activityFlow, repo.activity_list)
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities returns true on no record found`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities saves empty master data and returns true`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords returns true on no record found`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `uploadIncentiveFiles fails when there are no valid files`() = runTest {
        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a", fileUris = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities returns false on unexpected status code`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords saves empty data and returns true when no local records`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords uses cursor timestamp when local records exist`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 5
        every { preferenceDao.lastIncentivePullTimestamp } returns 1_600_000_000_000L
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords returns false on unexpected status code`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":1234,"errorMessage":""}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `activities returns false when token refresh fails on 5002`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `activities returns true when token refresh succeeds then retry succeeds`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `activities returns true on non-200 http code`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns
            jsonResponse("""{"statusCode":200}""", code = 500)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `activities returns true on null response body`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns nullBodyResponse()

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `records returns false when token refresh fails on 401`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns
            jsonResponse("""{"statusCode":401,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `records returns true when token refresh succeeds then retry succeeds`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `records returns true on non-200 http code`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns
            jsonResponse("""{"statusCode":200}""", code = 500)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `records returns true on null response body`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns nullBodyResponse()

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities returns true on 5000 with other message`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":5000,"errorMessage":"something else"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords returns true on 5000 with other message`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":5000,"errorMessage":"something else"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `activities returns false when master data payload is malformed`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":200,"errorMessage":"","data":"garbage"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `records returns false when records data payload is malformed`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":200,"errorMessage":"","data":"garbage"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }
}
