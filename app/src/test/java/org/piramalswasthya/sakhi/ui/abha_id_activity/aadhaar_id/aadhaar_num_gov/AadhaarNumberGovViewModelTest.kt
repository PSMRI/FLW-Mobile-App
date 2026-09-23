package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.aadhaar_num_gov

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.network.CreateAbhaIdResponse
import org.piramalswasthya.sakhi.network.DistrictCodeResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.StateCodeResponse
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class AadhaarNumberGovViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo

    private lateinit var viewModel: AadhaarNumberGovViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AadhaarNumberGovViewModel(abhaIdRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is not null`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `abha is initially null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `errorMessage is initially null`() {
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `activeState is initially null`() {
        assertNull(viewModel.activeState)
    }

    @Test
    fun `activeDistrict is initially null`() {
        assertNull(viewModel.activeDistrict)
    }

    private fun createAbhaIdResponse(token: String = "tok") = CreateAbhaIdResponse(
        token = token,
        refreshToken = "refresh",
        healthIdNumber = "12-3456-7890-1234",
        name = "Test User",
        gender = "F",
        yearOfBirth = "1990",
        monthOfBirth = "01",
        dayOfBirth = "01",
        firstName = "Test",
        healthId = "test@sbx",
        lastName = "User",
        middleName = "",
        stateCode = "1",
        districtCode = "1",
        stateName = "MP",
        districtName = "Indore",
        email = null,
        kycPhoto = null,
        profilePhoto = "",
        mobile = "9999999999",
        authMethods = arrayOf(),
        pincode = null,
        tags = null,
        alreadyExists = "false",
        new = true,
        txnId = "txn-1"
    )

    // =====================================================
    // getStates() (init) Tests
    // =====================================================

    @Test
    fun `init sets STATE_DETAILS_SUCCESS and stateCodes on success`() = runTest {
        val states = listOf(StateCodeResponse("1", "MP", listOf(DistrictCodeResponse("1", "Indore"))))
        coEvery { abhaIdRepo.getStateAndDistricts() } returns NetworkResult.Success(states)

        val vm = AadhaarNumberGovViewModel(abhaIdRepo)
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.STATE_DETAILS_SUCCESS, vm.state.value)
        assertEquals(states, vm.stateCodes.value)
    }

    @Test
    fun `init sets ERROR_SERVER and errorMessage on error`() = runTest {
        coEvery { abhaIdRepo.getStateAndDistricts() } returns NetworkResult.Error(500, "server down")

        val vm = AadhaarNumberGovViewModel(abhaIdRepo)
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, vm.state.value)
        assertEquals("server down", vm.errorMessage.value)
    }

    @Test
    fun `init sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.getStateAndDistricts() } returns NetworkResult.NetworkError

        val vm = AadhaarNumberGovViewModel(abhaIdRepo)
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, vm.state.value)
    }

    // =====================================================
    // generateAbhaCard() Tests
    // =====================================================

    @Test
    fun `generateAbhaCard sets state to LOADING immediately`() {
        viewModel.generateAbhaCard("123456789012", "Test User", "1990-01-01", "F")

        assertEquals(AadhaarIdViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `generateAbhaCard returns serialized null before the coroutine completes`() {
        val json = viewModel.generateAbhaCard("123456789012", "Test User", "1990-01-01", "F")

        assertEquals("null", json)
    }

    @Test
    fun `generateAbhaCard sets ABHA_GENERATED_SUCCESS and abha value on success`() = runTest {
        coEvery { abhaIdRepo.generateAbhaIdGov(any()) } returns
            NetworkResult.Success(createAbhaIdResponse("tok-123"))

        viewModel.generateAbhaCard("123456789012", "Test User", "1990-01-01", "F")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ABHA_GENERATED_SUCCESS, viewModel.state.value)
        assertEquals("tok-123", viewModel.abha.value?.token)
    }

    @Test
    fun `generateAbhaCard uses activeState and activeDistrict codes when set`() = runTest {
        viewModel.activeState = StateCodeResponse("7", "MP", null)
        viewModel.activeDistrict = DistrictCodeResponse("14", "Indore")
        coEvery { abhaIdRepo.generateAbhaIdGov(any()) } returns
            NetworkResult.Success(createAbhaIdResponse())

        viewModel.generateAbhaCard("123456789012", "Test User", "1990-01-01", "F")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ABHA_GENERATED_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `generateAbhaCard sets ERROR_SERVER and errorMessage on error`() = runTest {
        coEvery { abhaIdRepo.generateAbhaIdGov(any()) } returns NetworkResult.Error(400, "gov failed")

        viewModel.generateAbhaCard("123456789012", "Test User", "1990-01-01", "F")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("gov failed", viewModel.errorMessage.value)
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `generateAbhaCard sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.generateAbhaIdGov(any()) } returns NetworkResult.NetworkError

        viewModel.generateAbhaCard("123456789012", "Test User", "1990-01-01", "F")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
        assertTrue(viewModel.abha.value == null)
    }
}
