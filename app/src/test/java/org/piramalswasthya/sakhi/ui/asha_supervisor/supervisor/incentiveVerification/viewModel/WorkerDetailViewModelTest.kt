package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerDetailViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: WorkerDetailViewModel

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)

    private val user: User = io.mockk.mockk(relaxed = true)

    private val successBody = """
        {
          "statusCode": 200,
          "data": [
            {
              "activityId": 1,
              "activityDec": "Home visit",
              "groupName": "grpA",
              "amount": 100,
              "claimCount": 2,
              "isDefaultActivity": false,
              "totalAmount": 200,
              "isDefault": false,
              "approvalStatus": 102
            }
          ]
        }
    """.trimIndent()

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        viewModel = WorkerDetailViewModel(apiService, preferenceDao)
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
    fun `actionState is not null`() {
        assertNotNull(viewModel.actionState)
    }

    @Test
    fun `getSuperVisorSubname returns role when logged in`() {
        every { preferenceDao.getLoggedInUser() } returns user
        every { user.role } returns "SUPERVISOR"

        assertEquals("SUPERVISOR", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `getSuperVisorSubname returns empty when not logged in`() {
        every { preferenceDao.getLoggedInUser() } returns null

        assertEquals("", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `init emits error when user not logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        every { preferenceDao.getStateId() } returns 1

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Error)
        assertEquals("User not logged in", (state as WorkerDetailUiState.Error).message)
    }

    @Test
    fun `init emits success with parsed claimed incentives`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns Response.success(jsonBody(successBody))

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Success)
        val records = (state as WorkerDetailUiState.Success).records
        assertEquals(1, records.size)
        assertEquals("Home visit", records[0].activityDec)
    }

    /**
     * Verbatim claimedIncentiveByUser payload (10-Sep-2026) after the backend added `isApproved`:
     * the Monthly honorarium comes back pre-approved, the two register-filling activities do not.
     * Note it also no longer carries `isDefaultActivity`.
     */
    private val isApprovedBody = """
        {"data":[
          {"activityDec":"Filling 5 prescribed types of information in the Mitanin register","approvalStatus":102,"activityId":128,"incentiveId":99516,"totalAmount":1000,"groupName":"Administrative and Fixed Payments","isDefault":true,"amount":1000,"claimCount":1,"isApproved":false},
          {"activityDec":"Filling prescribed information in the Mitanin register","approvalStatus":102,"activityId":113,"incentiveId":99517,"totalAmount":500,"groupName":"Administrative and Fixed Payments","isDefault":true,"amount":500,"claimCount":1,"isApproved":false},
          {"activityDec":"Monthly honorarium","approvalStatus":102,"activityId":140,"incentiveId":99515,"totalAmount":2200,"groupName":"Administrative and Fixed Payments","isDefault":true,"amount":2200,"claimCount":1,"isApproved":true}
        ],"statusCode":200,"errorMessage":"Success","status":"Success"}
    """.trimIndent()

    @Test
    fun `init parses isApproved per row`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns Response.success(jsonBody(isApprovedBody))

        viewModel.init(1, 8, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val records = (viewModel.uiState.value as WorkerDetailUiState.Success).records
        assertEquals(3, records.size)

        val honorarium = records.first { it.activityId == 140 }
        assertTrue(honorarium.isApproved)
        assertEquals(2200, honorarium.totalAmount)
        assertEquals(99515, honorarium.incentiveId)

        assertFalse(records.first { it.activityId == 128 }.isApproved)
        assertFalse(records.first { it.activityId == 113 }.isApproved)
    }

    @Test
    fun `isApproved defaults to false when the field is absent`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns Response.success(jsonBody(successBody))

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val records = (viewModel.uiState.value as WorkerDetailUiState.Success).records
        assertFalse(records[0].isApproved)
    }

    @Test
    fun `init emits empty success for statusCode 5000`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns
            Response.success(jsonBody("""{"statusCode":5000}"""))

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Success)
        assertTrue((state as WorkerDetailUiState.Success).records.isEmpty())
    }

    @Test
    fun `init emits error for unknown statusCode`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns
            Response.success(jsonBody("""{"statusCode":400,"errorMessage":"bad"}"""))

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Error)
        assertEquals("bad", (state as WorkerDetailUiState.Error).message)
    }

    @Test
    fun `init emits error when response body empty`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns Response.success(jsonBody(""))

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Error)
        assertEquals("Empty response", (state as WorkerDetailUiState.Error).message)
    }

    @Test
    fun `init emits error on non-successful response`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } returns Response.error(500, jsonBody(""))

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Error)
        assertEquals("Server error: 500", (state as WorkerDetailUiState.Error).message)
    }

    @Test
    fun `init emits error when api throws`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getStateId() } returns 5
        coEvery { apiService.getClaimedIncentiveByUser(any()) } throws RuntimeException("network fail")

        viewModel.init(1, 1, 2026, approvalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WorkerDetailUiState.Error)
        assertEquals("network fail", (state as WorkerDetailUiState.Error).message)
    }

    @Test
    fun `verifyActivities emits success message on approval`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } returns Response.success(
            jsonBody("""{"statusCode":200,"updatedRecords":3}""")
        )

        viewModel.verifyActivities(1, listOf(10L, 20L, 30L))
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Success)
        assertEquals("Successfully verified 3 records", (state as ActionState.Success).message)
    }

    @Test
    fun `verifyActivities emits error when statusCode not 200`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } returns Response.success(
            jsonBody("""{"statusCode":400}""")
        )

        viewModel.verifyActivities(1, listOf(10L))
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Verification failed", (state as ActionState.Error).message)
    }

    @Test
    fun `verifyActivities emits error on non-successful response`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } returns Response.error(500, jsonBody(""))

        viewModel.verifyActivities(1, listOf(10L))
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Server error: 500", (state as ActionState.Error).message)
    }

    @Test
    fun `verifyActivities emits error when api throws`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } throws RuntimeException("verify boom")

        viewModel.verifyActivities(1, listOf(10L))
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("verify boom", (state as ActionState.Error).message)
    }

    @Test
    fun `rejectActivities emits success message on rejection`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } returns Response.success(
            jsonBody("""{"statusCode":200,"updatedRecords":2}""")
        )

        viewModel.rejectActivities(1, listOf(10L, 20L), "reason", "other")
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Success)
        assertEquals("Successfully rejected 2 records", (state as ActionState.Success).message)
    }

    @Test
    fun `rejectActivities emits error when statusCode not 200`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } returns Response.success(
            jsonBody("""{"statusCode":400}""")
        )

        viewModel.rejectActivities(1, listOf(10L), "reason", "other")
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Rejection failed", (state as ActionState.Error).message)
    }

    @Test
    fun `rejectActivities emits error on non-successful response`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } returns Response.error(404, jsonBody(""))

        viewModel.rejectActivities(1, listOf(10L), "reason", "other")
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Server error: 404", (state as ActionState.Error).message)
    }

    @Test
    fun `rejectActivities emits error when api throws`() = runTest {
        coEvery { apiService.updateApprovalStatus(any()) } throws RuntimeException("reject boom")

        viewModel.rejectActivities(1, listOf(10L), "reason", "other")
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("reject boom", (state as ActionState.Error).message)
    }
}
