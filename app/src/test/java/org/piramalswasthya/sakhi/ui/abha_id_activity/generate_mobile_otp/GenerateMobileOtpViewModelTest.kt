package org.piramalswasthya.sakhi.ui.abha_id_activity.generate_mobile_otp

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.network.AbhaCheckAndGenerateMobileOtpResponse
import org.piramalswasthya.sakhi.network.CreateAbhaIdResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateMobileOtpViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo

    private lateinit var viewModel: GenerateMobileOtpViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "txnId" to "test-txn-id",
        "mobileNumber" to "9876543210"
    ))

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = GenerateMobileOtpViewModel(abhaIdRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `txnIdFromArgs is set from SavedStateHandle`() {
        assertEquals("test-txn-id", viewModel.txnIdFromArgs)
    }

    @Test
    fun `initial errorMessage is null`() {
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(GenerateMobileOtpViewModel.State.IDLE, viewModel.state.value)
    }

    // =====================================================
    // resetErrorMessage() Tests
    // =====================================================

    @Test
    fun `resetErrorMessage sets errorMessage to null`() {
        viewModel.resetErrorMessage()
        assertNull(viewModel.errorMessage.value)
    }

    // =====================================================
    // generateOtpClicked() Tests
    // =====================================================

    @Test
    fun `generateOtpClicked sets state to LOADING`() {
        viewModel.generateOtpClicked("9876543210")
        assertEquals(GenerateMobileOtpViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `generateOtpClicked sets state to SUCCESS on success`() = runTest {
        coEvery { abhaIdRepo.checkAndGenerateOtpForMobileNumber(any()) } returns NetworkResult.Success(
            AbhaCheckAndGenerateMobileOtpResponse(false, "txn-2")
        )
        viewModel.generateOtpClicked("9876543210")
        advanceUntilIdle()
        assertEquals(GenerateMobileOtpViewModel.State.SUCCESS, viewModel.state.value)
        assertEquals("txn-2", viewModel.apiResponse.txnId)
    }

    @Test
    fun `generateOtpClicked sets state to ERROR_SERVER on error`() = runTest {
        coEvery { abhaIdRepo.checkAndGenerateOtpForMobileNumber(any()) } returns NetworkResult.Error(400, "bad request")
        viewModel.generateOtpClicked("9876543210")
        advanceUntilIdle()
        assertEquals(GenerateMobileOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("bad request", viewModel.errorMessage.value)
    }

    @Test
    fun `generateOtpClicked sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.checkAndGenerateOtpForMobileNumber(any()) } returns NetworkResult.NetworkError
        viewModel.generateOtpClicked("9876543210")
        advanceUntilIdle()
        assertEquals(GenerateMobileOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // generateAbhaCard() Tests
    // =====================================================

    @Test
    fun `generateAbhaCard sets state to ABHA_GENERATED_SUCCESS on success`() = runTest {
        val response = createAbhaIdResponse()
        coEvery { abhaIdRepo.generateAbhaId(any()) } returns NetworkResult.Success(response)
        viewModel.generateAbhaCard()
        advanceUntilIdle()
        assertEquals(GenerateMobileOtpViewModel.State.ABHA_GENERATED_SUCCESS, viewModel.state.value)
        assertEquals(response, viewModel.abha.value)
    }

    @Test
    fun `generateAbhaCard sets state to ERROR_SERVER on error`() = runTest {
        coEvery { abhaIdRepo.generateAbhaId(any()) } returns NetworkResult.Error(500, "server error")
        viewModel.generateAbhaCard()
        advanceUntilIdle()
        assertEquals(GenerateMobileOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("server error", viewModel.errorMessage.value)
    }

    @Test
    fun `generateAbhaCard sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.generateAbhaId(any()) } returns NetworkResult.NetworkError
        viewModel.generateAbhaCard()
        advanceUntilIdle()
        assertEquals(GenerateMobileOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
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
}
