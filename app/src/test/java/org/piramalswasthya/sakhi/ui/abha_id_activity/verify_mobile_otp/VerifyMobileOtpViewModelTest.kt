package org.piramalswasthya.sakhi.ui.abha_id_activity.verify_mobile_otp

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpResponseV2
import org.piramalswasthya.sakhi.network.AbhaVerifyMobileOtpResponse
import org.piramalswasthya.sakhi.network.CreateAbhaIdResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyMobileOtpViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo

    private lateinit var viewModel: VerifyMobileOtpViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "txnId" to "test-txn-id",
        "phoneNum" to "9876543210",
        "alternatePhoneNumber" to "1234567890",
        "name" to "Test User",
        "phrAddress" to "test@abdm",
        "abhaNumber" to "12-3456-7890-1234",
        "abhaResponse" to ""
    ))

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = VerifyMobileOtpViewModel(abhaIdRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(VerifyMobileOtpViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `initial errorMessage is null`() {
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `initial showExit is false`() {
        assertFalse(viewModel.showExit.value!!)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `alternatePhoneNumberArgs is set from SavedStateHandle`() {
        assertEquals("1234567890", viewModel.alternatePhoneNumberArgs)
    }

    @Test
    fun `abhaResponse is set from SavedStateHandle`() {
        assertEquals("", viewModel.abhaResponse)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(VerifyMobileOtpViewModel.State.IDLE, viewModel.state.value)
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
    // verifyOtpClicked() Tests
    // =====================================================

    @Test
    fun `verifyOtpClicked sets state to LOADING`() {
        viewModel.verifyOtpClicked("123456")
        assertEquals(VerifyMobileOtpViewModel.State.LOADING, viewModel.state.value)
    }

    // =====================================================
    // resendOtp() Tests
    // =====================================================

    @Test
    fun `resendOtp sets state to LOADING`() {
        viewModel.resendOtp()
        assertEquals(VerifyMobileOtpViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `resendOtp sets state to OTP_GENERATED_SUCCESS on success`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.Success(
            AbhaGenerateAadhaarOtpResponseV2("txn-2", "1234567890", "otp sent")
        )
        viewModel.resendOtp()
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.OTP_GENERATED_SUCCESS, viewModel.state.value)
        assertEquals("txn-2", viewModel.txnID)
    }

    @Test
    fun `resendOtp sets state to ERROR_SERVER on error`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.Error(400, "bad request")
        viewModel.resendOtp()
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("bad request", viewModel.errorMessage.value)
    }

    @Test
    fun `resendOtp sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.NetworkError
        viewModel.resendOtp()
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // verifyMobileOtp() (via verifyOtpClicked) Tests
    // =====================================================

    @Test
    fun `verifyOtpClicked sets state to OTP_VERIFY_SUCCESS on success`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForMobileNumber(any()) } returns NetworkResult.Success(
            AbhaVerifyMobileOtpResponse("txn-3")
        )
        viewModel.verifyOtpClicked("123456")
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.OTP_VERIFY_SUCCESS, viewModel.state.value)
        assertEquals("txn-3", viewModel.txnID)
    }

    @Test
    fun `verifyOtpClicked sets state to ERROR_SERVER and showExit true on exit-browser error`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForMobileNumber(any()) } returns NetworkResult.Error(
            400, "please exit your browser and retry"
        )
        viewModel.verifyOtpClicked("123456")
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals(true, viewModel.showExit.value)
    }

    @Test
    fun `verifyOtpClicked sets state to ERROR_SERVER without showExit on other error`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForMobileNumber(any()) } returns NetworkResult.Error(400, "invalid otp")
        viewModel.verifyOtpClicked("123456")
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals(false, viewModel.showExit.value)
    }

    @Test
    fun `verifyOtpClicked sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForMobileNumber(any()) } returns NetworkResult.NetworkError
        viewModel.verifyOtpClicked("123456")
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
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
        assertEquals(VerifyMobileOtpViewModel.State.ABHA_GENERATED_SUCCESS, viewModel.state.value)
        assertEquals(response, viewModel.abha.value)
    }

    @Test
    fun `generateAbhaCard sets state to ERROR_SERVER on error`() = runTest {
        coEvery { abhaIdRepo.generateAbhaId(any()) } returns NetworkResult.Error(500, "server error")
        viewModel.generateAbhaCard()
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("server error", viewModel.errorMessage.value)
    }

    @Test
    fun `generateAbhaCard sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.generateAbhaId(any()) } returns NetworkResult.NetworkError
        viewModel.generateAbhaCard()
        advanceUntilIdle()
        assertEquals(VerifyMobileOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
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
