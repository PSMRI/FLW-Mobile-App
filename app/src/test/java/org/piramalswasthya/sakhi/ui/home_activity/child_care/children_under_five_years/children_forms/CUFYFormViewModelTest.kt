package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: CUFYFormRepository

    private lateinit var viewModel: CUFYFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = CUFYFormViewModel(repository)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial schema is null`() {
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `initial infant is null`() {
        assertNull(viewModel.infant.value)
    }

    @Test
    fun `initial isBenDead is false`() {
        assertEquals(false, viewModel.isBenDead.value)
    }

    @Test
    fun `initial saveFormState is Idle`() {
        assertEquals(CUFYFormViewModel.SaveFormState.Idle, viewModel.saveFormState.value)
    }

    @Test
    fun `initial visitDay is empty`() {
        assertEquals("", viewModel.visitDay)
    }

    // =====================================================
    // setRecordId() Tests
    // =====================================================

    @Test
    fun `setRecordId does not throw`() {
        viewModel.setRecordId(42)
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields returns empty when no schema`() {
        val result = viewModel.getVisibleFields()
        assertEquals(0, result.size)
    }

    // =====================================================
    // getMinVisitDate() Tests
    // =====================================================

    @Test
    fun `getMinVisitDate returns null when no previous date`() {
        assertNull(viewModel.getMinVisitDate())
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue does not throw when no schema`() {
        viewModel.updateFieldValue("test", "value")
    }
}
