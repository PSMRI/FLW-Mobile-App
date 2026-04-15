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

    // =====================================================
    // Extended navToChildren() Tests
    // =====================================================

    @Test
    fun `navToChildren called twice keeps flag true`() {
        viewModel.navToChildren()
        viewModel.navToChildren()
        assertTrue(viewModel.navigateToChildrenImmunization.value!!)
    }

    @Test
    fun `navToChildren does not affect mother flag`() {
        viewModel.navToChildren()
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }

    @Test
    fun `navToChildren then resetNavigation clears flag`() {
        viewModel.navToChildren()
        assertTrue(viewModel.navigateToChildrenImmunization.value!!)
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
    }

    // =====================================================
    // Extended navToMother() Tests
    // =====================================================

    @Test
    fun `navToMother called twice keeps flag true`() {
        viewModel.navToMother()
        viewModel.navToMother()
        assertTrue(viewModel.navigateToMotherImmunization.value!!)
    }

    @Test
    fun `navToMother does not affect children flag`() {
        viewModel.navToMother()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
    }

    @Test
    fun `navToMother then resetNavigation clears flag`() {
        viewModel.navToMother()
        assertTrue(viewModel.navigateToMotherImmunization.value!!)
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }

    // =====================================================
    // Combined Navigation Tests
    // =====================================================

    @Test
    fun `both flags can be true simultaneously`() {
        viewModel.navToChildren()
        viewModel.navToMother()
        assertTrue(viewModel.navigateToChildrenImmunization.value!!)
        assertTrue(viewModel.navigateToMotherImmunization.value!!)
    }

    @Test
    fun `resetNavigation after only children set clears both`() {
        viewModel.navToChildren()
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }

    @Test
    fun `resetNavigation after only mother set clears both`() {
        viewModel.navToMother()
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
        assertFalse(viewModel.navigateToMotherImmunization.value!!)
    }

    @Test
    fun `multiple instances are independent`() {
        val vm1 = ImmunizationDueTypeViewModel()
        val vm2 = ImmunizationDueTypeViewModel()
        vm1.navToChildren()
        assertFalse(vm2.navigateToChildrenImmunization.value!!)
    }

    @Test
    fun `set reset set cycle works`() {
        viewModel.navToChildren()
        assertTrue(viewModel.navigateToChildrenImmunization.value!!)
        viewModel.resetNavigation()
        assertFalse(viewModel.navigateToChildrenImmunization.value!!)
        viewModel.navToMother()
        assertTrue(viewModel.navigateToMotherImmunization.value!!)
    }
}
