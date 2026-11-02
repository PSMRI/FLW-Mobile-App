package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.UserRepo
import retrofit2.Response
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class IncentiveDashboardViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var viewModel: IncentiveDashboardViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = IncentiveDashboardViewModel(apiService, preferenceDao, userRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `dashboardData is not null`() {
        assertNotNull(viewModel.dashboardData)
    }

    private fun buildUser() = User(
        userId = 1,
        name = "Supervisor",
        userName = "sup1",
        password = "pwd",
        role = "SUPERVISOR",
        serviceMapId = 1,
        state = LocationEntity(1, "State"),
        district = LocationEntity(2, "District"),
        block = LocationEntity(3, "Block"),
        villages = emptyList()
    )

    private fun responseWith(body: String, successful: Boolean = true, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns successful
        every { response.body() } returns responseBody
        every { response.code() } returns code
        return response
    }

    @Test
    fun `getSuperVisorSubname returns role when user is logged in`() {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        assertEquals("SUPERVISOR", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `getSuperVisorSubname returns empty string when no user is logged in`() {
        every { preferenceDao.getLoggedInUser() } returns null
        assertEquals("", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `fetchDashboard sets Error when no user is logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("User not logged in", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard sets Success when parsed status is Success`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        val json = """
            {"status":"Success","statusCode":200,"errorMessage":null,
            "data":{"incentiveSummary":{"overDue":1,"rejected":0,"pending":2,"verified":3,"unclaimed":4},
            "location":{"district":"D","blockOrUlb":"B","locationType":"Rural","state":"S"},
            "totalAshaCount":5,"facilities":[],
            "supervisor":{"gender":"F","mobile":"9999999999","fullName":"Sup","userId":1}}}
        """.trimIndent()
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(json)

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Success)
        assertEquals(5, (state as DashboardUiState.Success).data.totalAshaCount)
    }

    @Test
    fun `fetchDashboard sets Error with message when status is neither Success nor auth failure`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        val json = """{"status":"Failed","statusCode":400,"errorMessage":"Bad request","data":null}"""
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(json)

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Bad request", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard sets Error with default message when errorMessage is missing`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        val json = """{"status":"Failed","statusCode":400,"errorMessage":null,"data":null}"""
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(json)

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Something went wrong", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard retries and expires session when statusCode is 401 and refresh fails`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        val json = """{"status":"Failed","statusCode":401,"errorMessage":null,"data":null}"""
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Session expired, please login again", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard retries once when statusCode is 5002 and refresh succeeds`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        val json = """{"status":"Failed","statusCode":5002,"errorMessage":null,"data":null}"""
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Session expired, please login again", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard sets Error with http code when response is unsuccessful and not 401`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(
            body = "",
            successful = false,
            code = 500
        )

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Server error: 500", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard retries once on unsuccessful 401 response when refresh succeeds`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(
            body = "",
            successful = false,
            code = 401
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Server error: 401", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard sets Error with default message when refresh fails on unsuccessful 401 response`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        coEvery { apiService.getAshaSupervisorDashboard(any()) } returns responseWith(
            body = "",
            successful = false,
            code = 401
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Server error: 401", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard sets timeout error when a SocketTimeoutException is thrown`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        coEvery { apiService.getAshaSupervisorDashboard(any()) } throws SocketTimeoutException("timeout")

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Timeout error, please try again", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `fetchDashboard sets generic error message when an unexpected exception is thrown`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        coEvery { apiService.getAshaSupervisorDashboard(any()) } throws IllegalStateException("boom")

        viewModel.fetchDashboard(8, 2026, subcenterId = 0)
        advanceUntilIdle()

        val state = viewModel.dashboardData.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("boom", (state as DashboardUiState.Error).message)
    }
}
