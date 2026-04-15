package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class NewBenRegTypeViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: NewBenRegTypeViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = NewBenRegTypeViewModel(benRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial isConsentAgreed is false`() {
        assertFalse(viewModel.isConsentAgreed)
    }

    @Test
    fun `initial hasDraftForKid is false`() {
        assertFalse(viewModel.hasDraftForKid.value!!)
    }

    @Test
    fun `initial hasDraftForGen is false`() {
        assertFalse(viewModel.hasDraftForGen.value!!)
    }

    @Test
    fun `initial navigateToNewBenKidRegistration is false`() {
        assertFalse(viewModel.navigateToNewBenKidRegistration.value!!)
    }

    @Test
    fun `initial navigateToNewBenGenRegistration is false`() {
        assertFalse(viewModel.navigateToNewBenGenRegistration.value!!)
    }

    // =====================================================
    // setConsentAgreed() Tests
    // =====================================================

    @Test
    fun `setConsentAgreed sets isConsentAgreed to true`() {
        viewModel.setConsentAgreed()
        assertTrue(viewModel.isConsentAgreed)
    }

    // =====================================================
    // checkDraft() Tests
    // =====================================================

    @Test
    fun `checkDraft resets drafts to false`() = runTest {
        viewModel.checkDraft(1L)
        advanceUntilIdle()
        assertFalse(viewModel.hasDraftForKid.value!!)
        assertFalse(viewModel.hasDraftForGen.value!!)
    }

    // =====================================================
    // navigateToNewBenRegistration() Tests
    // =====================================================

    @Test
    fun `navigateToNewBenRegistration kid sets kid flag`() = runTest {
        viewModel.navigateToNewBenRegistration(1L, false, true)
        advanceUntilIdle()
        assertTrue(viewModel.navigateToNewBenKidRegistration.value!!)
    }

    @Test
    fun `navigateToNewBenRegistration gen sets gen flag`() = runTest {
        viewModel.navigateToNewBenRegistration(1L, false, false)
        advanceUntilIdle()
        assertTrue(viewModel.navigateToNewBenGenRegistration.value!!)
    }

    // =====================================================
    // navigateCompleted() Tests
    // =====================================================

    @Test
    fun `navigateToBenKidRegistrationCompleted resets flag`() {
        viewModel.navigateToBenKidRegistrationCompleted()
        assertFalse(viewModel.navigateToNewBenKidRegistration.value!!)
    }

    @Test
    fun `navigateToBenGenRegistrationCompleted resets flag`() {
        viewModel.navigateToBenGenRegistrationCompleted()
        assertFalse(viewModel.navigateToNewBenGenRegistration.value!!)
    }
}
