package org.piramalswasthya.sakhi.ui.abha_id_activity.generate_mobile_otp

import androidx.lifecycle.SavedStateHandle
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
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
}
