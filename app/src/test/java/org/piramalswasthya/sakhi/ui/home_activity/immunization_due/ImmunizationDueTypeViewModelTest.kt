package org.piramalswasthya.sakhi.ui.home_activity.immunization_due

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class ImmunizationDueTypeViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: ImmunizationDueTypeViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = ImmunizationDueTypeViewModel()
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `navigateToChildrenImmunization is initially false`() {
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
    }

    @Test
    fun `navigateToMotherImmunization is initially false`() {
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }

    // =====================================================
    // navToChildren() Tests
    // =====================================================

    @Test
    fun `navToChildren sets navigateToChildrenImmunization to true`() {
        viewModel.navToChildren()
        assertTrue(viewModel.navigateToChildrenImmunization.value!!)
    }

    // =====================================================
    // navToMother() Tests
    // =====================================================

    @Test
    fun `navToMother sets navigateToMotherImmunization to true`() {
        viewModel.navToMother()
        assertTrue(viewModel.navigateToMotherImmunization.value!!)
    }

    // =====================================================
    // resetNavigation() Tests
    // =====================================================

    @Test
    fun `resetNavigation resets both navigation flags to false`() {
        viewModel.navToChildren()
        viewModel.navToMother()
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }

    @Test
    fun `resetNavigation when already false does not throw`() {
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }
}
