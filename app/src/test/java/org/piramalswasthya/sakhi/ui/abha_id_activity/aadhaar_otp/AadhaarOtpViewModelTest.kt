package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.network.ABHAProfile
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpResponseV2
import org.piramalswasthya.sakhi.network.AbhaVerifyAadhaarOtpResponse
import org.piramalswasthya.sakhi.network.Accounts
import org.piramalswasthya.sakhi.network.LoginGenerateOtpResponse
import org.piramalswasthya.sakhi.network.LoginVerifyOtpResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.Tokens
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo

@OptIn(ExperimentalCoroutinesApi::class)
class AadhaarOtpViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo

    private lateinit var viewModel: AadhaarOtpViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "txnId" to "test-txn-id",
        "mobileNumber" to "9876543210"
    ))

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AadhaarOtpViewModel(savedStateHandle, abhaIdRepo)
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
        assertEquals(AadhaarOtpViewModel.State.IDLE, viewModel.state.value)
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
    fun `txnId is empty initially`() {
        assertEquals("", viewModel.txnId)
    }

    @Test
    fun `mobileNumber is empty initially`() {
        assertEquals("", viewModel.mobileNumber)
    }

    @Test
    fun `mobileFromArgs is set from SavedStateHandle`() {
        assertEquals("9876543210", viewModel.mobileFromArgs)
    }

    @Test
    fun `phrAddress is empty initially`() {
        assertEquals("", viewModel.phrAddress)
    }

    @Test
    fun `abhaResponse is empty initially`() {
        assertEquals("", viewModel.abhaResponse)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(AadhaarOtpViewModel.State.IDLE, viewModel.state.value)
    }

    // =====================================================
    // setMobileNumber() Tests
    // =====================================================

    @Test
    fun `setMobileNumber updates mobileNumber`() {
        viewModel.setMobileNumber("1234567890")
        assertEquals("1234567890", viewModel.mobileNumber)
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
        viewModel.verifyOtpClicked("123456", "9876543210")
        assertEquals(AadhaarOtpViewModel.State.LOADING, viewModel.state.value)
    }

    // =====================================================
    // generateOtpClicked() Tests
    // =====================================================

    @Test
    fun `generateOtpClicked sets state to LOADING`() {
        viewModel.generateOtpClicked()
        assertEquals(AadhaarOtpViewModel.State.LOADING, viewModel.state.value)
    }

    // =====================================================
    // verifyLoginOtpClicked() Tests
    // =====================================================

    @Test
    fun `verifyLoginOtpClicked sets state to LOADING`() {
        viewModel.verifyLoginOtpClicked("123456")
        assertEquals(AadhaarOtpViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `verifyOtpClicked success with middleName sets full name and OTP_VERIFY_SUCCESS`() = runTest {
        val tokens = Tokens(
            token = "tok-1",
            expiresIn = 300,
            refreshToken = "refresh-1",
            refreshExpiresIn = 1800
        )
        val response = AbhaVerifyAadhaarOtpResponse(
            message = "success",
            txnId = "txn-1",
            tokens = tokens,
            ABHAProfile = ABHAProfile(
                firstName = "John",
                middleName = "K",
                lastName = "Doe",
                mobile = "9876543210",
                phrAddress = listOf("john@abdm"),
                ABHANumber = "12-3456-7890-1234"
            )
        )
        coEvery { abhaIdRepo.verifyOtpForAadhaar(any()) } returns NetworkResult.Success(response)

        viewModel.verifyOtpClicked("123456", "9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.OTP_VERIFY_SUCCESS, viewModel.state.value)
        assertEquals("John K Doe", viewModel.name)
        assertEquals("12-3456-7890-1234", viewModel.abhaNumber)
        assertEquals("john@abdm", viewModel.phrAddress)
        assertEquals("9876543210", viewModel.mobileNumber)
        assertTrue(viewModel.abhaResponse.isNotEmpty())
        assertEquals(300, tokens.expiresIn)
        assertEquals("refresh-1", tokens.refreshToken)
        assertEquals(1800, tokens.refreshExpiresIn)
    }

    @Test
    fun `verifyOtpClicked success without middleName sets name without middleName`() = runTest {
        val response = AbhaVerifyAadhaarOtpResponse(
            message = "success",
            txnId = "txn-1",
            tokens = Tokens(token = "tok-1"),
            ABHAProfile = ABHAProfile(
                firstName = "John",
                middleName = "",
                lastName = "Doe",
                mobile = "",
                phrAddress = listOf("john@abdm"),
                ABHANumber = "12-3456-7890-1234"
            )
        )
        coEvery { abhaIdRepo.verifyOtpForAadhaar(any()) } returns NetworkResult.Success(response)

        viewModel.verifyOtpClicked("123456", "9876543210")
        advanceUntilIdle()

        assertEquals("John Doe", viewModel.name)
    }

    @Test
    fun `verifyOtpClicked error with exit browser message sets showExit true`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForAadhaar(any()) } returns
                NetworkResult.Error(403, "Please exit your browser and retry")

        viewModel.verifyOtpClicked("123456", "9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("Please exit your browser and retry", viewModel.errorMessage.value)
        assertTrue(viewModel.showExit.value!!)
    }

    @Test
    fun `verifyOtpClicked error without exit browser message keeps showExit false`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForAadhaar(any()) } returns
                NetworkResult.Error(500, "Something went wrong")

        viewModel.verifyOtpClicked("123456", "9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("Something went wrong", viewModel.errorMessage.value)
        assertFalse(viewModel.showExit.value!!)
    }

    @Test
    fun `verifyOtpClicked network error sets ERROR_NETWORK and showExit true`() = runTest {
        coEvery { abhaIdRepo.verifyOtpForAadhaar(any()) } returns NetworkResult.NetworkError

        viewModel.verifyOtpClicked("123456", "9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
        assertTrue(viewModel.showExit.value!!)
    }

    @Test
    fun `verifyLoginOtpClicked success with token sets OTP_VERIFY_SUCCESS`() = runTest {
        val response = LoginVerifyOtpResponse(
            txnId = "txn-2",
            authResult = "success",
            message = "ok",
            token = "tok-2",
            expiresIn = 3600L,
            refreshToken = "refresh-2",
            refreshExpiresIn = 7200L,
            accounts = listOf(
                Accounts(
                    ABHANumber = "12-3456-7890-1234",
                    preferredAbhaAddress = "abha@address",
                    name = "Jane Doe",
                    status = "ACTIVE",
                    profilePhoto = "",
                    mobileVerified = true
                )
            )
        )
        coEvery { abhaIdRepo.verifyAbhaOtp(any()) } returns NetworkResult.Success(response)

        viewModel.verifyLoginOtpClicked("123456")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.OTP_VERIFY_SUCCESS, viewModel.state.value)
        assertEquals("Jane Doe", viewModel.name)
        assertEquals("12-3456-7890-1234", viewModel.abhaNumber)
        assertEquals("abha@address", viewModel.phrAddress)
        assertEquals("", viewModel.mobileNumber)
    }

    @Test
    fun `verifyLoginOtpClicked success with empty token sets ERROR_SERVER`() = runTest {
        val response = LoginVerifyOtpResponse(
            txnId = "txn-2",
            authResult = "failure",
            message = "invalid otp",
            token = "",
            expiresIn = 0L,
            refreshToken = "",
            refreshExpiresIn = 0L,
            accounts = emptyList()
        )
        coEvery { abhaIdRepo.verifyAbhaOtp(any()) } returns NetworkResult.Success(response)

        viewModel.verifyLoginOtpClicked("123456")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("invalid otp", viewModel.errorMessage.value)
    }

    @Test
    fun `verifyLoginOtpClicked error with exit browser message sets showExit true`() = runTest {
        coEvery { abhaIdRepo.verifyAbhaOtp(any()) } returns
                NetworkResult.Error(403, "Please exit your browser now")

        viewModel.verifyLoginOtpClicked("123456")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertTrue(viewModel.showExit.value!!)
    }

    @Test
    fun `verifyLoginOtpClicked network error sets ERROR_NETWORK`() = runTest {
        coEvery { abhaIdRepo.verifyAbhaOtp(any()) } returns NetworkResult.NetworkError

        viewModel.verifyLoginOtpClicked("123456")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
        assertTrue(viewModel.showExit.value!!)
    }

    @Test
    fun `generateOtpClicked success sets SUCCESS and otpMobileNumberMessage`() = runTest {
        val response = AbhaGenerateAadhaarOtpResponseV2(
            txnId = "txn-3",
            mobileNumber = "9876543210",
            message = "otp sent"
        )
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.Success(response)

        viewModel.generateOtpClicked()
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.SUCCESS, viewModel.state.value)
        assertEquals("otp sent", viewModel.otpMobileNumberMessage.value)
        assertEquals("txn-3", viewModel.txnId)
    }

    @Test
    fun `generateOtpClicked error sets ERROR_SERVER`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns
                NetworkResult.Error(500, "otp generation failed")

        viewModel.generateOtpClicked()
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("otp generation failed", viewModel.errorMessage.value)
    }

    @Test
    fun `generateOtpClicked network error sets ERROR_NETWORK`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.NetworkError

        viewModel.generateOtpClicked()
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    @Test
    fun `resendCreateAadhaarOtp success updates txnId and otpMobileNumberMessage`() = runTest {
        val response = AbhaGenerateAadhaarOtpResponseV2(
            txnId = "txn-4",
            mobileNumber = "",
            message = "resent"
        )
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.Success(response)

        viewModel.resendCreateAadhaarOtp("123456789012")
        advanceUntilIdle()

        assertEquals("txn-4", viewModel.txnId)
        assertEquals("resent", viewModel.otpMobileNumberMessage.value)
    }

    @Test
    fun `resendCreateAadhaarOtp error sets ERROR_SERVER`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns
                NetworkResult.Error(500, "resend failed")

        viewModel.resendCreateAadhaarOtp("123456789012")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("resend failed", viewModel.errorMessage.value)
    }

    @Test
    fun `resendCreateAadhaarOtp network error sets ERROR_NETWORK`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.NetworkError

        viewModel.resendCreateAadhaarOtp("123456789012")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    @Test
    fun `resendOtpForSearchAbha success updates txnId`() = runTest {
        val response = LoginGenerateOtpResponse(txnId = "txn-5", message = "otp sent")
        coEvery { abhaIdRepo.generateAbhaOtp(any()) } returns NetworkResult.Success(response)

        viewModel.resendOtpForSearchAbha("0", "old-txn")
        advanceUntilIdle()

        assertEquals("txn-5", viewModel.txnId)
    }

    @Test
    fun `resendOtpForSearchAbha error sets ERROR_SERVER`() = runTest {
        coEvery { abhaIdRepo.generateAbhaOtp(any()) } returns
                NetworkResult.Error(500, "search abha otp failed")

        viewModel.resendOtpForSearchAbha("0", "old-txn")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("search abha otp failed", viewModel.errorMessage.value)
    }

    @Test
    fun `resendOtpForSearchAbha network error sets ERROR_NETWORK`() = runTest {
        coEvery { abhaIdRepo.generateAbhaOtp(any()) } returns NetworkResult.NetworkError

        viewModel.resendOtpForSearchAbha("0", "old-txn")
        advanceUntilIdle()

        assertEquals(AadhaarOtpViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }
}
