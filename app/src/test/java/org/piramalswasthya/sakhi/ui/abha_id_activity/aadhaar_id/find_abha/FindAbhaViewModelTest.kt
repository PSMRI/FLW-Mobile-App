package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.find_abha

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
import org.piramalswasthya.sakhi.network.Abha
import org.piramalswasthya.sakhi.network.LoginGenerateOtpResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.SearchAbhaResponse
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class FindAbhaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: FindAbhaViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FindAbhaViewModel(abhaIdRepo, benRepo)
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
    fun `txnId is initially null`() {
        assertNull(viewModel.txnId.value)
    }

    @Test
    fun `fnlTxnId is initially null`() {
        assertNull(viewModel.fnlTxnId.value)
    }

    @Test
    fun `abha is initially null`() {
        assertNull(viewModel.abha.value)
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
    fun `resetState does not throw`() {
        viewModel.resetState()
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
    // searchAbhaClicked() / searchAbha() Tests
    // =====================================================

    @Test
    fun `searchAbhaClicked sets state to LOADING`() {
        viewModel.searchAbhaClicked("9876543210")
        assertEquals(AadhaarIdViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `searchAbhaClicked sets SUCCESS state and populates txnId and abha on success`() = runTest {
        val abhaList = listOf(Abha(1, "12-3456-7890-1234", "Test User", "F"))
        coEvery { abhaIdRepo.searchAbha(any()) } returns
            NetworkResult.Success(SearchAbhaResponse("txn-1", abhaList))

        viewModel.searchAbhaClicked("9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.SUCCESS, viewModel.state.value)
        assertEquals("txn-1", viewModel.txnId.value)
        assertEquals(abhaList, viewModel.abha.value)
    }

    @Test
    fun `searchAbhaClicked sets ERROR_NETWORK when error code is -1`() = runTest {
        coEvery { abhaIdRepo.searchAbha(any()) } returns NetworkResult.Error(-1, "no internet")

        viewModel.searchAbhaClicked("9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
        assertEquals("Internet connectivity issue, try again later", viewModel.errorMessage.value)
    }

    @Test
    fun `searchAbhaClicked sets ERROR_SERVER with generic message when error code is -4`() = runTest {
        coEvery { abhaIdRepo.searchAbha(any()) } returns NetworkResult.Error(-4, "boom")

        viewModel.searchAbhaClicked("9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("Some error occurred", viewModel.errorMessage.value)
    }

    @Test
    fun `searchAbhaClicked sets ERROR_SERVER with no-abha-found message for other error codes`() =
        runTest {
            coEvery { abhaIdRepo.searchAbha(any()) } returns NetworkResult.Error(-5, "user not found")

            viewModel.searchAbhaClicked("9876543210")
            advanceUntilIdle()

            assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.state.value)
            assertEquals("No ABHA Found", viewModel.errorMessage.value)
        }

    @Test
    fun `searchAbhaClicked sets ERROR_NETWORK on NetworkError`() = runTest {
        coEvery { abhaIdRepo.searchAbha(any()) } returns NetworkResult.NetworkError

        viewModel.searchAbhaClicked("9876543210")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // generateOtpClicked() / generateAbhaOtp() Tests
    // =====================================================

    private fun primeTxnId() {
        coEvery { abhaIdRepo.searchAbha(any()) } returns
            NetworkResult.Success(SearchAbhaResponse("txn-1", emptyList()))
        viewModel.searchAbhaClicked("9876543210")
    }

    @Test
    fun `generateOtpClicked sets state to LOADING`() = runTest {
        primeTxnId()
        advanceUntilIdle()

        viewModel.generateOtpClicked("0")

        assertEquals(AadhaarIdViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `generateOtpClicked sets SUCCESS and populates fnlTxnId and otp message on success`() =
        runTest {
            primeTxnId()
            advanceUntilIdle()
            coEvery { abhaIdRepo.generateAbhaOtp(any()) } returns
                NetworkResult.Success(LoginGenerateOtpResponse("fnl-txn-1", "OTP sent"))

            viewModel.generateOtpClicked("0")
            advanceUntilIdle()

            assertEquals(AadhaarIdViewModel.State.SUCCESS, viewModel.fnlState.value)
            assertEquals("fnl-txn-1", viewModel.fnlTxnId.value)
            assertEquals("OTP sent", viewModel.otpMobileNumberMessage.value)
        }

    @Test
    fun `generateOtpClicked sets ERROR_SERVER and errorMessage on error`() = runTest {
        primeTxnId()
        advanceUntilIdle()
        coEvery { abhaIdRepo.generateAbhaOtp(any()) } returns NetworkResult.Error(400, "otp failed")

        viewModel.generateOtpClicked("0")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.fnlState.value)
        assertEquals("otp failed", viewModel.errorMessage.value)
    }

    @Test
    fun `generateOtpClicked sets ERROR_NETWORK on network error`() = runTest {
        primeTxnId()
        advanceUntilIdle()
        coEvery { abhaIdRepo.generateAbhaOtp(any()) } returns NetworkResult.NetworkError

        viewModel.generateOtpClicked("0")
        advanceUntilIdle()

        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.fnlState.value)
    }

    // =====================================================
    // getBen() Tests
    // =====================================================

    @Test
    fun `getBen sets errorMessage when beneficiary not found`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null

        viewModel.getBen(1L)
        advanceUntilIdle()

        assertEquals("Beneficiary not found", viewModel.errorMessage.value)
        assertNull(viewModel.ben.value)
    }

    @Test
    fun `getBen sets ben to first and last name when both present`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Jane"
        every { ben.lastName } returns "Doe"
        coEvery { benRepo.getBenFromId(2L) } returns ben

        viewModel.getBen(2L)
        advanceUntilIdle()

        assertEquals("Jane Doe", viewModel.ben.value)
    }

    @Test
    fun `getBen sets ben to first name only when last name is null`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Jane"
        every { ben.lastName } returns null
        coEvery { benRepo.getBenFromId(3L) } returns ben

        viewModel.getBen(3L)
        advanceUntilIdle()

        assertEquals("Jane", viewModel.ben.value)
    }

    @Test
    fun `getBen leaves ben null when both names are null`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns null
        every { ben.lastName } returns null
        coEvery { benRepo.getBenFromId(4L) } returns ben

        viewModel.getBen(4L)
        advanceUntilIdle()

        assertNull(viewModel.ben.value)
    }
}
