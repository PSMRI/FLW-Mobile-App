package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PwAncVisitsListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var viewModel: PwAncVisitsListViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "source" to 0
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.getRegisteredPregnantWomanList() } returns flowOf(emptyList())
        every { recordsRepo.getRegisteredPregnantWomanNonFollowUpList() } returns flowOf(emptyList())
        every { recordsRepo.getDuePregnantWomanList() } returns flowOf(emptyList())
        every { recordsRepo.getHighRiskPregnantWomanList() } returns flowOf(emptyList())
        viewModel = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `bottomSheetList is not null`() {
        assertNotNull(viewModel.bottomSheetList)
    }

    @Test
    fun `homeVisitState is not null`() {
        assertNotNull(viewModel.homeVisitState)
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
    // toggleHighRisk() Tests
    // =====================================================

    @Test
    fun `toggleHighRisk true does not throw`() = runTest {
        viewModel.toggleHighRisk(true)
        advanceUntilIdle()
    }

    @Test
    fun `toggleHighRisk false does not throw`() = runTest {
        viewModel.toggleHighRisk(false)
        advanceUntilIdle()
    }

    // =====================================================
    // showAncBottomSheet() Tests
    // =====================================================

    @Test
    fun `showAncBottomSheet NORMAL mode does not throw`() = runTest {
        viewModel.showAncBottomSheet(1L, PwAncVisitsListViewModel.BottomSheetMode.NORMAL)
        advanceUntilIdle()
    }

    @Test
    fun `showAncBottomSheet PMSMA mode does not throw`() = runTest {
        viewModel.showAncBottomSheet(1L, PwAncVisitsListViewModel.BottomSheetMode.PMSMA)
        advanceUntilIdle()
    }
}
