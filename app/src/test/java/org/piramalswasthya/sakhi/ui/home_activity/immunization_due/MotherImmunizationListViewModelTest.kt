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
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.mother_immunization.list.MotherImmunizationListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class MotherImmunizationListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var vaccineDao: ImmunizationDao

    private lateinit var viewModel: MotherImmunizationListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(emptyList())
        viewModel = MotherImmunizationListViewModel(vaccineDao)
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
    fun `bottomSheetContent flow is not null`() {
        assertNotNull(viewModel.bottomSheetContent)
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
}
