package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [AshaRepo]. Consolidated from AshaRepoTest + Extra2Test +
 * Extra3Test: the dao flow getter and every pullAndSaveAllAshaActivities branch
 * (5000, 200 empty, 5002 refresh success/fail, unknown status, non-200 HTTP,
 * socket-timeout retry, and null body).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AshaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var incentiveDao: IncentiveDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo

    private val recordsFlow = flowOf(emptyList<IncentiveCache>())

    private lateinit var repo: AshaRepo

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)
    private fun emptyErrorBody() = "".toResponseBody(jsonMediaType)

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { incentiveDao.getAllRecords() } returns recordsFlow
        repo = AshaRepo(amritApiService, incentiveDao, preferenceDao, userRepo)
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    private fun user() = mockk<User>(relaxed = true)

    @Test
    fun `list exposes dao flow`() {
        assertEquals(recordsFlow, repo.list)
    }

    @Test
    fun `pullAndSaveAllAshaActivities returns true on no record found`() = runTest {
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getAshaProfileData(any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllAshaActivities(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllAshaActivities saves empty master data and returns true`() = runTest {
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAshaProfileData(any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllAshaActivities(mockk<User>(relaxed = true)))
    }

    @Test
    fun `returns false when token refresh fails on 5002`() = runTest {
        val json = """{"statusCode":5002,"errorMessage":"token expired"}"""
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.success(jsonBody(json))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        // 5002 with failed refresh throws IllegalStateException, caught -> false
        assertFalse(repo.pullAndSaveAllAshaActivities(user()))
    }

    @Test
    fun `returns false on unknown status code`() = runTest {
        val json = """{"statusCode":9999,"errorMessage":"weird"}"""
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.success(jsonBody(json))

        assertFalse(repo.pullAndSaveAllAshaActivities(user()))
    }

    @Test
    fun `returns true when http code is not 200`() = runTest {
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.error(500, emptyErrorBody())

        // non-200 HTTP skips parsing, falls through to the trailing true
        assertTrue(repo.pullAndSaveAllAshaActivities(user()))
    }

    @Test
    fun `returns true when token refresh succeeds then retry succeeds`() = runTest {
        coEvery { amritApiService.getAshaProfileData(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pullAndSaveAllAshaActivities(user()))
    }

    @Test
    fun `returns true when socket timeout triggers a successful retry`() = runTest {
        var call = 0
        coEvery { amritApiService.getAshaProfileData(any()) } answers {
            if (call++ == 0) throw SocketTimeoutException("timeout")
            else jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        }

        assertTrue(repo.pullAndSaveAllAshaActivities(user()))
    }

    @Test
    fun `returns true on null response body`() = runTest {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { amritApiService.getAshaProfileData(any()) } returns response

        assertTrue(repo.pullAndSaveAllAshaActivities(user()))
    }

    @Test
    fun `saveAshaMasterData inserts new activity and skips existing activity`() = runTest {
        val data = """
            [
                {"id":1,"name":"A1","description":"d","paymentParam":"p","rate":10,"state":1,"district":1,"group":"g","groupName":"gn","fmrCode":null,"fmrCodeOld":null,"createdDate":"2024-01-01","createdBy":"u","updatedDate":"2024-01-01","updatedBy":"u"},
                {"id":2,"name":"A2","description":"d","paymentParam":"p","rate":20,"state":1,"district":1,"group":"g","groupName":"gn","fmrCode":null,"fmrCodeOld":null,"createdDate":"2024-01-01","createdBy":"u","updatedDate":"2024-01-01","updatedBy":"u"}
            ]
        """.trimIndent()
        val escapedData = data.replace("\n", "").replace("\"", "\\\"")
        val json = """{"statusCode":200,"errorMessage":"","data":"$escapedData"}"""
        coEvery { amritApiService.getAshaProfileData(any()) } returns jsonResponse(json)
        every { incentiveDao.getActivityById(1L) } returns null
        every { incentiveDao.getActivityById(2L) } returns mockk(relaxed = true)
        var insertedIds = emptyList<Long>()
        coEvery { incentiveDao.insert(*anyVararg<IncentiveActivityCache>()) } answers {
            insertedIds = (it.invocation.args[0] as Array<*>).map { activity -> (activity as IncentiveActivityCache).id }
        }

        assertTrue(repo.pullAndSaveAllAshaActivities(user()))

        coVerify(exactly = 1) { incentiveDao.insert(*anyVararg<IncentiveActivityCache>()) }
        assertEquals(listOf(1L), insertedIds)
    }
}
