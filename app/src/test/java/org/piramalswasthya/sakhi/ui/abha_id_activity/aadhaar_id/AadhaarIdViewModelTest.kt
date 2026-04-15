package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class AadhaarIdViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: AadhaarIdViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AadhaarIdViewModel()
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
        assertEquals(AadhaarIdViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `initial errorMessage is null`() {
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `initial navigateToAadhaarConsent is false`() {
        assertFalse(viewModel.navigateToAadhaarConsent.value!!)
    }

    @Test
    fun `initial consentChecked is false`() {
        assertFalse(viewModel.consentChecked.value!!)
    }

    @Test
    fun `initial abhaMode is NONE`() {
        assertEquals(AadhaarIdViewModel.Abha.NONE, viewModel.abhaMode.value)
    }

    // =====================================================
    // setState() Tests
    // =====================================================

    @Test
    fun `setState updates state value`() {
        viewModel.setState(AadhaarIdViewModel.State.LOADING)
        assertEquals(AadhaarIdViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `setState to SUCCESS`() {
        viewModel.setState(AadhaarIdViewModel.State.SUCCESS)
        assertEquals(AadhaarIdViewModel.State.SUCCESS, viewModel.state.value)
    }

    @Test
    fun `setState to ERROR_SERVER`() {
        viewModel.setState(AadhaarIdViewModel.State.ERROR_SERVER)
        assertEquals(AadhaarIdViewModel.State.ERROR_SERVER, viewModel.state.value)
    }

    @Test
    fun `setState to ERROR_NETWORK`() {
        viewModel.setState(AadhaarIdViewModel.State.ERROR_NETWORK)
        assertEquals(AadhaarIdViewModel.State.ERROR_NETWORK, viewModel.state.value)
    }

    @Test
    fun `setState to STATE_DETAILS_SUCCESS`() {
        viewModel.setState(AadhaarIdViewModel.State.STATE_DETAILS_SUCCESS)
        assertEquals(AadhaarIdViewModel.State.STATE_DETAILS_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `setState to ABHA_GENERATED_SUCCESS`() {
        viewModel.setState(AadhaarIdViewModel.State.ABHA_GENERATED_SUCCESS)
        assertEquals(AadhaarIdViewModel.State.ABHA_GENERATED_SUCCESS, viewModel.state.value)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state back to IDLE`() {
        viewModel.setState(AadhaarIdViewModel.State.SUCCESS)
        viewModel.resetState()
        assertEquals(AadhaarIdViewModel.State.IDLE, viewModel.state.value)
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
    // setAbha() Tests
    // =====================================================

    @Test
    fun `setAbha does not throw`() {
        viewModel.setAbha("12-3456-7890-1234")
    }

    // =====================================================
    // setBeneficiaryName() Tests
    // =====================================================

    @Test
    fun `setBeneficiaryName updates beneficiaryName`() {
        viewModel.setBeneficiaryName("John Doe")
        assertEquals("John Doe", viewModel.beneficiaryName.value)
    }

    // =====================================================
    // setConsentChecked() Tests
    // =====================================================

    @Test
    fun `setConsentChecked to true updates consentChecked`() {
        viewModel.setConsentChecked(true)
        assertTrue(viewModel.consentChecked.value!!)
    }

    @Test
    fun `setConsentChecked to false updates consentChecked`() {
        viewModel.setConsentChecked(true)
        viewModel.setConsentChecked(false)
        assertFalse(viewModel.consentChecked.value!!)
    }

    // =====================================================
    // setAbhaMode() Tests
    // =====================================================

    @Test
    fun `setAbhaMode to CREATE updates abhaMode`() {
        viewModel.setAbhaMode(AadhaarIdViewModel.Abha.CREATE)
        assertEquals(AadhaarIdViewModel.Abha.CREATE, viewModel.abhaMode.value)
    }

    @Test
    fun `setAbhaMode to SEARCH updates abhaMode`() {
        viewModel.setAbhaMode(AadhaarIdViewModel.Abha.SEARCH)
        assertEquals(AadhaarIdViewModel.Abha.SEARCH, viewModel.abhaMode.value)
    }

    @Test
    fun `setAbhaMode to NONE updates abhaMode`() {
        viewModel.setAbhaMode(AadhaarIdViewModel.Abha.CREATE)
        viewModel.setAbhaMode(AadhaarIdViewModel.Abha.NONE)
        assertEquals(AadhaarIdViewModel.Abha.NONE, viewModel.abhaMode.value)
    }

    // =====================================================
    // navigateToAadhaarConsent() Tests
    // =====================================================

    @Test
    fun `navigateToAadhaarConsent sets value to true`() {
        viewModel.navigateToAadhaarConsent(true)
        assertTrue(viewModel.navigateToAadhaarConsent.value!!)
    }

    @Test
    fun `navigateToAadhaarConsent sets value to false`() {
        viewModel.navigateToAadhaarConsent(true)
        viewModel.navigateToAadhaarConsent(false)
        assertFalse(viewModel.navigateToAadhaarConsent.value!!)
    }

    // =====================================================
    // setMobileNumber() Tests
    // =====================================================

    @Test
    fun `setMobileNumber does not throw`() {
        viewModel.setMobileNumber("9876543210")
    }

    // =====================================================
    // setAadhaarNumber() Tests
    // =====================================================

    @Test
    fun `setAadhaarNumber does not throw`() {
        viewModel.setAadhaarNumber("123456789012")
    }

    // =====================================================
    // setUserType() Tests
    // =====================================================

    @Test
    fun `setUserType updates userType`() {
        viewModel.setUserType("ASHA")
        assertEquals("ASHA", viewModel.userType.value)
    }

    // =====================================================
    // setVerificationType() Tests
    // =====================================================

    @Test
    fun `setVerificationType updates verificationType`() {
        viewModel.setVerificationType("AADHAAR_OTP")
        assertEquals("AADHAAR_OTP", viewModel.verificationType.value)
    }

    // =====================================================
    // setOtpTxnId() Tests
    // =====================================================

    @Test
    fun `setOtpTxnId does not throw`() {
        viewModel.setOtpTxnId("txn123")
    }

    // =====================================================
    // setTxnId() Tests
    // =====================================================

    @Test
    fun `setTxnId does not throw`() {
        viewModel.setTxnId("txn456")
    }

    // =====================================================
    // setOTPMsg() Tests
    // =====================================================

    @Test
    fun `setOTPMsg does not throw`() {
        viewModel.setOTPMsg("OTP sent successfully")
    }

    // =====================================================
    // setSelectedAbhaIndex() Tests
    // =====================================================

    @Test
    fun `setSelectedAbhaIndex does not throw`() {
        viewModel.setSelectedAbhaIndex("0")
    }

    // =====================================================
    // Enum Tests
    // =====================================================

    @Test
    fun `State enum has all expected values`() {
        val values = AadhaarIdViewModel.State.values()
        assertEquals(7, values.size)
    }

    @Test
    fun `Abha enum has all expected values`() {
        val values = AadhaarIdViewModel.Abha.values()
        assertEquals(3, values.size)
    }
}
