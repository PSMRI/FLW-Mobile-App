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
    fun `setAbha updates abhaResponse`() {
        viewModel.setAbha("12-3456-7890-1234")
        assertEquals("12-3456-7890-1234", viewModel.abhaResponse)
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
    fun `setMobileNumber updates mobileNumber`() {
        viewModel.setMobileNumber("9876543210")
        assertEquals("9876543210", viewModel.mobileNumber)
    }

    @Test
    fun `mobileNumber defaults to empty string when unset`() {
        assertEquals("", viewModel.mobileNumber)
    }

    // =====================================================
    // setAadhaarNumber() Tests
    // =====================================================

    @Test
    fun `setAadhaarNumber updates aadhaarNumber`() {
        viewModel.setAadhaarNumber("123456789012")
        assertEquals("123456789012", viewModel.aadhaarNumber)
    }

    @Test
    fun `aadhaarNumber defaults to empty string when unset`() {
        assertEquals("", viewModel.aadhaarNumber)
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
    fun `setOtpTxnId updates otpTxnId`() {
        viewModel.setOtpTxnId("txn123")
        assertEquals("txn123", viewModel.otpTxnId)
    }

    @Test
    fun `otpTxnId defaults to empty string when unset`() {
        assertEquals("", viewModel.otpTxnId)
    }

    // =====================================================
    // setTxnId() Tests
    // =====================================================

    @Test
    fun `setTxnId updates txnId`() {
        viewModel.setTxnId("txn456")
        assertEquals("txn456", viewModel.txnId)
    }

    @Test
    fun `txnId defaults to empty string when unset`() {
        assertEquals("", viewModel.txnId)
    }

    // =====================================================
    // setOTPMsg() Tests
    // =====================================================

    @Test
    fun `setOTPMsg updates otpMobileNumberMessage`() {
        viewModel.setOTPMsg("OTP sent successfully")
        assertEquals("OTP sent successfully", viewModel.otpMobileNumberMessage)
    }

    @Test
    fun `otpMobileNumberMessage defaults to empty string when unset`() {
        assertEquals("", viewModel.otpMobileNumberMessage)
    }

    // =====================================================
    // setSelectedAbhaIndex() Tests
    // =====================================================

    @Test
    fun `setSelectedAbhaIndex updates selectedAbhaIndex`() {
        viewModel.setSelectedAbhaIndex("0")
        assertEquals("0", viewModel.selectedAbhaIndex)
    }

    @Test
    fun `selectedAbhaIndex defaults to empty string when unset`() {
        assertEquals("", viewModel.selectedAbhaIndex)
    }

    // =====================================================
    // aadhaarVerificationTypes Tests
    // =====================================================

    @Test
    fun `aadhaarVerificationTypes defaults to first value`() {
        assertEquals("Aadhaar No", viewModel.aadhaarVerificationTypes.value)
    }

    // =====================================================
    // selectedNavToggle Tests
    // =====================================================

    @Test
    fun `selectedNavToggle has default value and is settable`() {
        assertEquals("navHostFragmentAadhaarId", viewModel.selectedNavToggle)
        viewModel.selectedNavToggle = "otherFragment"
        assertEquals("otherFragment", viewModel.selectedNavToggle)
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
