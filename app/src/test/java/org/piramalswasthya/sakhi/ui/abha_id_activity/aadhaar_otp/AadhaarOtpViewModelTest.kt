package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp

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
}
