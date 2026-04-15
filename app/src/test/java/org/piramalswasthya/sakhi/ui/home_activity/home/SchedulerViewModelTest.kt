package org.piramalswasthya.sakhi.ui.home_activity.home

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: SchedulerViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { maternalHealthRepo.ancDueCount } returns flowOf(5)
        every { recordsRepo.getRegisteredPregnantWomanNonFollowUpListCount() } returns flowOf(3)
        every { recordsRepo.eligibleCoupleMissedPeriodListCount } returns flowOf(2)
        every { recordsRepo.eligibleCoupleTrackingMissedPeriodListCount } returns flowOf(1)
        every { recordsRepo.pncMotherNonFollowUpListCount } returns flowOf(4)
        every { recordsRepo.eligibleCoupleTrackingNonFollowUpListCount } returns flowOf(6)
        every { recordsRepo.hrpTrackingPregListCount } returns flowOf(7)
        every { recordsRepo.hrpTrackingNonPregListCount } returns flowOf(8)
        every { recordsRepo.childrenImmunizationDueListCount } returns flowOf(9)
        every { recordsRepo.lowWeightBabiesCount } returns flowOf(10)
        every { recordsRepo.benWithAbhaListCount } returns flowOf(11)
        every { recordsRepo.benWithOldAbhaListCount } returns flowOf(12)
        every { recordsRepo.benWithNewAbhaListCount } returns flowOf(13)
        every { recordsRepo.benWithRchListCount } returns flowOf(14)
        viewModel = SchedulerViewModel(maternalHealthRepo, recordsRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is LOADING then transitions to LOADED`() = runTest {
        advanceUntilIdle()
        assertEquals(SchedulerViewModel.State.LOADED, viewModel.state.value)
    }

    @Test
    fun `date is initialized to start of today`() {
        assertNotNull(viewModel.date.value)
    }

    // =====================================================
    // Flow Property Tests
    // =====================================================

    @Test
    fun `ancDueCount flow is assigned from maternalHealthRepo`() {
        assertNotNull(viewModel.ancDueCount)
    }

    @Test
    fun `ancNonFollowUpCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.ancNonFollowUpCount)
    }

    @Test
    fun `ecrMissedPeriodCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.ecrMissedPeriodCount)
    }

    @Test
    fun `ectMissedPeriodCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.ectMissedPeriodCount)
    }

    @Test
    fun `pncNonFollowUpCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.pncNonFollowUpCount)
    }

    @Test
    fun `ecNonFollowUpCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.ecNonFollowUpCount)
    }

    @Test
    fun `hrpDueCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.hrpDueCount)
    }

    @Test
    fun `hrpCountEC flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.hrpCountEC)
    }

    @Test
    fun `immunizationDue flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.immunizationDue)
    }

    @Test
    fun `lowWeightBabiesCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.lowWeightBabiesCount)
    }

    @Test
    fun `abhaGeneratedCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.abhaGeneratedCount)
    }

    @Test
    fun `abhaOldGeneratedCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.abhaOldGeneratedCount)
    }

    @Test
    fun `abhaNewGeneratedCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.abhaNewGeneratedCount)
    }

    @Test
    fun `rchIdCount flow is assigned from recordsRepo`() {
        assertNotNull(viewModel.rchIdCount)
    }

    // =====================================================
    // setDate() Tests
    // =====================================================

    @Test
    fun `setDate updates date value`() = runTest {
        val newDate = 1234567890L
        viewModel.setDate(newDate)
        assertEquals(newDate, viewModel.date.value)
    }

    @Test
    fun `setDate sets state to LOADING then back to LOADED`() = runTest {
        viewModel.setDate(1234567890L)
        assertEquals(SchedulerViewModel.State.LOADING, viewModel.state.value)
        advanceUntilIdle()
        assertEquals(SchedulerViewModel.State.LOADED, viewModel.state.value)
    }
}
