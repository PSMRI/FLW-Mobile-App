package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.VerificationStatus
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class IncentiveVerificationViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService

    private lateinit var viewModel: IncentiveVerificationViewModel

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)

    private val successBody = """
        {
          "approvalStatus": {"rejected": 1, "pending": 2, "verified": 3},
          "data": [
            {
              "approvalStatus": 101,
              "facilityId": 10,
              "gender": "F",
              "facilityType": "PHC",
              "rejected": 0,
              "pending": 0,
              "mobile": "9999999999",
              "verified": 1,
              "fullName": "Asha One",
              "employeeId": "EMP1",
              "userId": 55,
              "totalAmount": 500,
              "facilityName": "Center A",
              "activities": [
                {
                  "approvalStatus": 101,
                  "reason": "",
                  "otherReason": "",
                  "claimedDate": "2026-01-01",
                  "approvalDate": "2026-01-02",
                  "verifiedByUserName": "Supervisor1",
                  "role": "ASHA",
                  "isClaimed": true
                }
              ]
            }
          ],
          "statusCode": 200
        }
    """.trimIndent()

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = IncentiveVerificationViewModel(apiService)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `uiState is not null`() {
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `search with empty query does not throw`() = runTest {
        viewModel.search("")
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `search with text does not throw`() = runTest {
        viewModel.search("asha")
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `refresh before init does nothing`() = runTest {
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value)
    }

    @Test
    fun `init with verified status fetches and maps workers`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.success(jsonBody(successBody))

        viewModel.init("verified", 10, 1, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerificationUiState.Success)
        val success = state as VerificationUiState.Success
        assertEquals(1, success.workers.size)
        assertEquals("Asha One", success.workers[0].name)
        assertEquals(VerificationStatus.VERIFIED, success.workers[0].status)
        assertEquals(3, success.summary.verified)
    }

    @Test
    fun `init twice does not refetch second time`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.success(jsonBody(successBody))

        viewModel.init("pending", 10, 1, 2026)
        advanceUntilIdle()
        viewModel.init("rejected", 20, 2, 2026)
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 1) { apiService.getAshaListByFacility(any()) }
    }

    @Test
    fun `refresh after init refetches data`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.success(jsonBody(successBody))

        viewModel.init("all", 10, 1, 2026)
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 2) { apiService.getAshaListByFacility(any()) }
    }

    @Test
    fun `init with unknown status defaults to ALL`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns
            Response.success(jsonBody("""{"approvalStatus":null,"data":null,"statusCode":200}"""))

        viewModel.init("some-unknown-status", 10, 1, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerificationUiState.Success)
        assertTrue((state as VerificationUiState.Success).workers.isEmpty())
    }

    @Test
    fun `init emits error when response body empty`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.success(jsonBody(""))

        viewModel.init("pending", 10, 1, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerificationUiState.Error)
        assertEquals("Empty response from server", (state as VerificationUiState.Error).message)
    }

    @Test
    fun `init emits error on parse failure`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.success(jsonBody("not-json"))

        viewModel.init("pending", 10, 1, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerificationUiState.Error)
        assertTrue((state as VerificationUiState.Error).message.startsWith("Parse error"))
    }

    @Test
    fun `init emits error on non-successful response`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.error(500, jsonBody(""))

        viewModel.init("overdue", 10, 1, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerificationUiState.Error)
        assertEquals("Server error: 500", (state as VerificationUiState.Error).message)
    }

    @Test
    fun `init emits error when api throws`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } throws RuntimeException("boom")

        viewModel.init("approved", 10, 1, 2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerificationUiState.Error)
        assertEquals("boom", (state as VerificationUiState.Error).message)
    }

    @Test
    fun `init with unclaimed status maps correctly and search filters results`() = runTest {
        coEvery { apiService.getAshaListByFacility(any()) } returns Response.success(jsonBody(successBody))

        viewModel.init("unclaimed", 10, 1, 2026)
        advanceUntilIdle()

        viewModel.search("Asha One")
        var state = viewModel.uiState.value as VerificationUiState.Success
        assertEquals(1, state.workers.size)

        viewModel.search("no-match-xyz")
        state = viewModel.uiState.value as VerificationUiState.Success
        assertTrue(state.workers.isEmpty())
    }
}
