package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import io.mockk.coEvery
import io.mockk.every
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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiaryDetailViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: BeneficiaryDetailViewModel

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)

    private val stateEntity = LocationEntity(1, "State")
    private val user = User(
        userId = 1,
        name = "Asha",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 1,
        state = stateEntity,
        district = LocationEntity(2, "District"),
        block = LocationEntity(3, "Block"),
        villages = emptyList()
    )

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = BeneficiaryDetailViewModel(apiService, preferenceDao)
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
    fun `fetchBeneficiaries emits error when user not logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Error)
        assertEquals("User not logged in", (state as BeneficiaryUiState.Error).message)
    }

    @Test
    fun `fetchBeneficiaries emits success with parsed records for statusCode 200`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        val body = """
            {"statusCode":200,"data":[{"id":1,"activityId":1,"ashaId":1,"benId":100,"amount":50,"name":"Ben1","startDate":"2026-01-01","activityDec":"desc","groupName":"grp","approvalStatus":101,"rchId":"rch1","abhaNumber":"abha1","isClaimed":true,"verifiedByUserName":"sup"}]}
        """.trimIndent()
        coEvery { apiService.getActivityDetailRecords(any()) } returns Response.success(jsonBody(body))

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Success)
        val records = (state as BeneficiaryUiState.Success).records
        assertEquals(1, records.size)
        assertEquals("Ben1", records[0].name)
    }

    @Test
    fun `fetchBeneficiaries emits empty success for statusCode 5000`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { apiService.getActivityDetailRecords(any()) } returns
            Response.success(jsonBody("""{"statusCode":5000}"""))

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Success)
        assertTrue((state as BeneficiaryUiState.Success).records.isEmpty())
    }

    @Test
    fun `fetchBeneficiaries emits error for unknown statusCode`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { apiService.getActivityDetailRecords(any()) } returns
            Response.success(jsonBody("""{"statusCode":400,"errorMessage":"bad request"}"""))

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Error)
        assertEquals("bad request", (state as BeneficiaryUiState.Error).message)
    }

    @Test
    fun `fetchBeneficiaries emits error when response body empty`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { apiService.getActivityDetailRecords(any()) } returns
            Response.success(jsonBody(""))

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Error)
        assertEquals("Empty response", (state as BeneficiaryUiState.Error).message)
    }

    @Test
    fun `fetchBeneficiaries emits error on non-successful response`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { apiService.getActivityDetailRecords(any()) } returns
            Response.error(500, jsonBody(""))

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Error)
        assertEquals("Server error: 500", (state as BeneficiaryUiState.Error).message)
    }

    @Test
    fun `fetchBeneficiaries emits error when api throws exception`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { apiService.getActivityDetailRecords(any()) } throws RuntimeException("network down")

        viewModel.fetchBeneficiaries(1, 1, 2026, 1, filterApprovalStatus = 102)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BeneficiaryUiState.Error)
        assertEquals("network down", (state as BeneficiaryUiState.Error).message)
    }
}
