package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.aadhaar_num_asha

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.network.AadhaarVerifyBioRequest
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpResponseV2
import org.piramalswasthya.sakhi.network.CreateAbhaIdResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class AadhaarNumberAshaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: AadhaarNumberAshaViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AadhaarNumberAshaViewModel(abhaIdRepo, benRepo)
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
        val state = viewModel.state.value
        assertNotNull(state)
    }

    @Test
    fun `txnId is initially null`() {
        assertNull(viewModel.txnId.value)
    }

    @Test
    fun `ben is initially null`() {
        assertNull(viewModel.ben.value)
    }

    @Test
    fun `errorMessage is initially null`() {
        assertNull(viewModel.errorMessage.value)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertNotNull(viewModel.state.value)
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
    // generateOtpClicked() / generateAadhaarOtp() Tests
    // =====================================================

    @Test
    fun `generateOtpClicked sets state to SUCCESS on success result`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.Success(
            AbhaGenerateAadhaarOtpResponseV2("txn-1", "9999999999", "otp sent")
        )
        viewModel.generateOtpClicked("123456789012")
        advanceUntilIdle()
        assertEquals(AadhaarIdViewModel.State.SUCCESS, viewModel.state.value)
        assertEquals("txn-1", viewModel.txnId.value)
        assertEquals("otp sent", viewModel.otpMobileNumberMessage.value)
    }

    @Test
    fun `generateOtpClicked sets state to ERROR_SERVER on error result`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.Error(400, "bad request")
        viewModel.generateOtpClicked("123456789012")
        advanceUntilIdle()
        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("bad request", viewModel.errorMessage.value)
    }

    @Test
    fun `generateOtpClicked sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.generateAadhaarOtpV3(any()) } returns NetworkResult.NetworkError
        viewModel.generateOtpClicked("123456789012")
        advanceUntilIdle()
        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // verifyBio() Tests
    // =====================================================

    @Test
    fun `verifyBio sets state to SUCCESS and stores response on success`() = runTest {
        val response = createAbhaIdResponse()
        coEvery { abhaIdRepo.verifyBio(any()) } returns NetworkResult.Success(response)
        viewModel.verifyBio("123456789012", "pid-data")
        advanceUntilIdle()
        assertEquals(AadhaarIdViewModel.State.SUCCESS, viewModel.state.value)
        assertEquals(response, viewModel.responseData)
        assertEquals("txn-1", viewModel.txnId.value)
        assertEquals("9999999999", viewModel.mobileNumber.value)
    }

    @Test
    fun `verifyBio sets state to ERROR_SERVER on error result`() = runTest {
        coEvery { abhaIdRepo.verifyBio(any()) } returns NetworkResult.Error(500, "server error")
        viewModel.verifyBio("123456789012", "pid-data")
        advanceUntilIdle()
        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("server error", viewModel.errorMessage.value)
    }

    @Test
    fun `verifyBio sets state to ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.verifyBio(any()) } returns NetworkResult.NetworkError
        viewModel.verifyBio("123456789012", null)
        advanceUntilIdle()
        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // getBen() Tests
    // =====================================================

    @Test
    fun `getBen sets ben to first and last name when found`() = runTest {
        val benCache = mockk<BenRegCache>(relaxed = true)
        every { benCache.firstName } returns "John"
        every { benCache.lastName } returns "Doe"
        coEvery { benRepo.getBenFromId(1L) } returns benCache
        viewModel.getBen(1L)
        advanceUntilIdle()
        assertEquals("John Doe", viewModel.ben.value)
    }

    @Test
    fun `getBen sets ben to first name only when last name is null`() = runTest {
        val benCache = mockk<BenRegCache>(relaxed = true)
        every { benCache.firstName } returns "John"
        every { benCache.lastName } returns null
        coEvery { benRepo.getBenFromId(1L) } returns benCache
        viewModel.getBen(1L)
        advanceUntilIdle()
        assertEquals("John", viewModel.ben.value)
    }

    @Test
    fun `getBen leaves ben null when beneficiary not found`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        viewModel.getBen(1L)
        advanceUntilIdle()
        assertNull(viewModel.ben.value)
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
