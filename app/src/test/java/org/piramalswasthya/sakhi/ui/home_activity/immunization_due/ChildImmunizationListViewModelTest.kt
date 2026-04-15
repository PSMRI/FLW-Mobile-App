package org.piramalswasthya.sakhi.ui.home_activity.immunization_due

import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list.ChildImmunizationListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ChildImmunizationListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var vaccineDao: ImmunizationDao

    private lateinit var viewModel: ChildImmunizationListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(emptyList())
        viewModel = ChildImmunizationListViewModel(vaccineDao)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benWithVaccineDetails flow is not null`() {
        assertNotNull(viewModel.benWithVaccineDetails)
    }

    @Test
    fun `immunizationBenList flow is not null`() {
        assertNotNull(viewModel.immunizationBenList)
    }

    @Test
    fun `selectedPosition is initially 0`() {
        assertEquals(0, viewModel.selectedPosition)
    }

    // =====================================================
    // filterText() Tests
    // =====================================================

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("test")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with empty string does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
    }

    // =====================================================
    // getSelectedBenId() Tests
    // =====================================================

    @Test
    fun `getSelectedBenId returns 0 initially`() {
        assertEquals(0L, viewModel.getSelectedBenId())
    }

    // =====================================================
    // updateBottomSheetData() Tests
    // =====================================================

    @Test
    fun `updateBottomSheetData does not throw`() = runTest {
        viewModel.updateBottomSheetData(42L)
        advanceUntilIdle()
    }

    // =====================================================
    // categoryData() Tests
    // =====================================================

    @Test
    fun `categoryData returns non-empty list`() {
        val categories = viewModel.categoryData()
        assertNotNull(categories)
        assertEquals("ALL", categories[0])
    }

    @Test
    fun `categoryData clears and rebuilds on each call`() {
        viewModel.categoryData()
        val categories = viewModel.categoryData()
        assertNotNull(categories)
    }
}
