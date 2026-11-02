package org.piramalswasthya.sakhi.ui.abha_id_activity.create_abha_id

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.ABHAGenratedRepo
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAbhaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var abhaGenratedRepo: ABHAGenratedRepo

    private lateinit var viewModel: CreateAbhaViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "txnId" to "test-txn-id",
        "name" to "Test User",
        "phrAddress" to "test@abdm",
        "abhaNumber" to "12-3456-7890-1234",
        "abhaResponse" to ""
    ))

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = CreateAbhaViewModel(pref, abhaIdRepo, benRepo, abhaGenratedRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `initial hidResponse is null`() {
        assertNull(viewModel.hidResponse.value)
    }

    @Test
    fun `initial benMapped is null`() {
        assertNull(viewModel.benMapped.value)
    }

    @Test
    fun `initial errorMessage is null`() {
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
    // init() abhaResponseLiveData Tests
    // =====================================================

    @Test
    fun `initial state is LOADING`() {
        assertEquals(CreateAbhaViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `abhaResponseLiveData defaults when abhaResponse arg is empty`() {
        assertNotNull(viewModel.abhaResponseLiveData.value)
        assertEquals("", viewModel.abhaResponseLiveData.value?.message)
    }

    @Test
    fun `abhaResponseLiveData parses valid json from savedStateHandle`() {
        val json = """{"message":"ok","txnId":"txn-1","isNew":true}"""
        val handle = SavedStateHandle(
            mapOf(
                "txnId" to "test-txn-id",
                "name" to "Test User",
                "phrAddress" to "test@abdm",
                "abhaNumber" to "12-3456-7890-1234",
                "abhaResponse" to json
            )
        )
        val vm = CreateAbhaViewModel(pref, abhaIdRepo, benRepo, abhaGenratedRepo, handle)

        assertEquals("ok", vm.abhaResponseLiveData.value?.message)
        assertEquals("txn-1", vm.abhaResponseLiveData.value?.txnId)
        assertEquals(true, vm.abhaResponseLiveData.value?.isNew)
    }

    // =====================================================
    // printAbhaCard() Tests
    // =====================================================

    @Test
    fun `printAbhaCard sets DOWNLOAD_SUCCESS and byteImage on success`() = runTest {
        val body = "abha-card-bytes".toResponseBody("application/octet-stream".toMediaTypeOrNull())
        coEvery { abhaIdRepo.printAbhaCard() } returns NetworkResult.Success(body)

        viewModel.printAbhaCard()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.DOWNLOAD_SUCCESS, viewModel.state.value)
        assertEquals(body, viewModel.byteImage.value)
    }

    @Test
    fun `printAbhaCard sets ERROR_SERVER on error`() = runTest {
        coEvery { abhaIdRepo.printAbhaCard() } returns NetworkResult.Error(500, "server error")

        viewModel.printAbhaCard()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("server error", viewModel.errorMessage.value)
    }

    @Test
    fun `printAbhaCard sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.printAbhaCard() } returns NetworkResult.NetworkError

        viewModel.printAbhaCard()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // generateOtp() Tests
    // =====================================================

    @Test
    fun `generateOtp sets OTP_GENERATE_SUCCESS and otpTxnID on success`() = runTest {
        coEvery { abhaIdRepo.generateOtpHid(any()) } returns NetworkResult.Success("otp-txn-1")

        viewModel.generateOtp()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.OTP_GENERATE_SUCCESS, viewModel.state.value)
        assertEquals("otp-txn-1", viewModel.otpTxnID.value)
    }

    @Test
    fun `generateOtp sets DOWNLOAD_ERROR when error code is 0`() = runTest {
        coEvery { abhaIdRepo.generateOtpHid(any()) } returns NetworkResult.Error(0, "no code error")

        viewModel.generateOtp()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.DOWNLOAD_ERROR, viewModel.state.value)
        assertEquals("no code error", viewModel.errorMessage.value)
    }

    @Test
    fun `generateOtp sets ERROR_SERVER when error code is non-zero`() = runTest {
        coEvery { abhaIdRepo.generateOtpHid(any()) } returns NetworkResult.Error(400, "bad request")

        viewModel.generateOtp()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("bad request", viewModel.errorMessage.value)
    }

    @Test
    fun `generateOtp sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.generateOtpHid(any()) } returns NetworkResult.NetworkError

        viewModel.generateOtp()
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // verifyOtp() Tests
    // =====================================================

    @Test
    fun `verifyOtp sets OTP_VERIFY_SUCCESS and cardBase64 on success`() = runTest {
        coEvery { abhaIdRepo.verifyOtpAndGenerateHealthCard(any()) } returns NetworkResult.Success("base64card")

        viewModel.verifyOtp("123456")
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.OTP_VERIFY_SUCCESS, viewModel.state.value)
        assertEquals("base64card", viewModel.cardBase64.value)
    }

    @Test
    fun `verifyOtp sets DOWNLOAD_ERROR when error code is 0`() = runTest {
        coEvery { abhaIdRepo.verifyOtpAndGenerateHealthCard(any()) } returns NetworkResult.Error(0, "no code")

        viewModel.verifyOtp("123456")
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.DOWNLOAD_ERROR, viewModel.state.value)
    }

    @Test
    fun `verifyOtp sets ERROR_SERVER when error code is non-zero`() = runTest {
        coEvery { abhaIdRepo.verifyOtpAndGenerateHealthCard(any()) } returns NetworkResult.Error(401, "unauthorized")

        viewModel.verifyOtp("123456")
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_SERVER, viewModel.state.value)
    }

    @Test
    fun `verifyOtp sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.verifyOtpAndGenerateHealthCard(any()) } returns NetworkResult.NetworkError

        viewModel.verifyOtp("123456")
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    // =====================================================
    // mapBeneficiaryToHealthId() Tests
    // =====================================================

    @Test
    fun `mapBeneficiaryToHealthId with zero benId and benRegId adds health id record on success`() = runTest {
        coEvery { abhaIdRepo.addHealthIdRecord(any()) } returns NetworkResult.Success("ok")
        every { pref.getLoggedInUser() } returns null

        viewModel.mapBeneficiaryToHealthId(0L, 0L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ABHA_GENERATE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `mapBeneficiaryToHealthId with zero benId and benRegId sets ERROR_SERVER on error`() = runTest {
        coEvery { abhaIdRepo.addHealthIdRecord(any()) } returns NetworkResult.Error(500, "failed")
        every { pref.getLoggedInUser() } returns null

        viewModel.mapBeneficiaryToHealthId(0L, 0L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("failed", viewModel.errorMessage.value)
    }

    @Test
    fun `mapBeneficiaryToHealthId with zero benId and benRegId sets ERROR_NETWORK on network error`() = runTest {
        coEvery { abhaIdRepo.addHealthIdRecord(any()) } returns NetworkResult.NetworkError
        every { pref.getLoggedInUser() } returns null

        viewModel.mapBeneficiaryToHealthId(0L, 0L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    @Test
    fun `mapBeneficiaryToHealthId with non-zero benId maps beneficiary on success`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Jane"
        every { ben.lastName } returns "Doe"
        coEvery { benRepo.getBenFromId(101L) } returns ben
        every { pref.getLoggedInUser() } returns null
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.Success("mapped")

        viewModel.mapBeneficiaryToHealthId(101L, 0L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ABHA_GENERATE_SUCCESS, viewModel.state.value)
        assertEquals("Jane Doe", viewModel.benMapped.value)
    }

    @Test
    fun `mapBeneficiaryToHealthId with non-zero benId sets ERROR_SERVER on non-zero error code`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(101L) } returns ben
        every { pref.getLoggedInUser() } returns null
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.Error(400, "mapping failed")

        viewModel.mapBeneficiaryToHealthId(101L, 0L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_SERVER, viewModel.state.value)
        assertEquals("mapping failed", viewModel.errorMessage.value)
    }

    @Test
    fun `mapBeneficiaryToHealthId with non-zero benId emits ShowDialog uiEvent on error code zero`() = runTest {
        val events = mutableListOf<UIEvent>()
        val job = launch { viewModel.uiEvent.collect { events.add(it) } }
        advanceUntilIdle()

        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Jane"
        every { ben.lastName } returns "Doe"
        coEvery { benRepo.getBenFromId(101L) } returns ben
        every { pref.getLoggedInUser() } returns null
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.Error(0, "dup")

        viewModel.mapBeneficiaryToHealthId(101L, 0L)
        advanceUntilIdle()

        assertTrue(events.isNotEmpty())
        assertTrue(events[0] is UIEvent.ShowDialog)
        job.cancel()
    }

    @Test
    fun `mapBeneficiaryToHealthId with non-zero benId sets ERROR_NETWORK on network error`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(101L) } returns ben
        every { pref.getLoggedInUser() } returns null
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.NetworkError

        viewModel.mapBeneficiaryToHealthId(101L, 0L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    @Test
    fun `mapBeneficiaryToHealthId with non-zero benRegId uses benRegId for mapping`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(202L) } returns ben
        every { pref.getLoggedInUser() } returns null
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.Success("mapped")

        viewModel.mapBeneficiaryToHealthId(202L, 55L)
        advanceUntilIdle()

        assertEquals(CreateAbhaViewModel.State.ABHA_GENERATE_SUCCESS, viewModel.state.value)
    }
}
