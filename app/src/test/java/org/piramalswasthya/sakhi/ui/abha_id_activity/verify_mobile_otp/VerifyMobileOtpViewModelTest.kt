package org.piramalswasthya.sakhi.ui.abha_id_activity.verify_mobile_otp

import androidx.lifecycle.SavedStateHandle
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
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
}
