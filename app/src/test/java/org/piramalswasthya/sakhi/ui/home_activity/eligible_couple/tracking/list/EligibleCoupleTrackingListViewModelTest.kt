package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleTrackingListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: EligibleCoupleTrackingListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { savedStateHandle.get<String>("source") } returns "DEFAULT"
        every { recordsRepo.eligibleCoupleTrackingList } returns flowOf(emptyList())
        every { recordsRepo.eligibleCoupleTrackingMissedPeriodList } returns flowOf(emptyList())
        every { recordsRepo.eligibleCoupleTrackingNonFollowUpList } returns flowOf(emptyList())
        viewModel = EligibleCoupleTrackingListViewModel(savedStateHandle, recordsRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `bottomSheetList flow is not null`() {
        assertNotNull(viewModel.bottomSheetList)
    }

    @Test
    fun `scope returns viewModelScope`() {
        assertNotNull(viewModel.scope)
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
    // setClickedBenId() Tests
    // =====================================================

    @Test
    fun `setClickedBenId does not throw`() = runTest {
        viewModel.setClickedBenId(42L)
        advanceUntilIdle()
    }

    @Test
    fun `scope is available`() {
        assertNotNull(viewModel.scope)
    }

    @Test
    fun `getCurrentSort default is NEWEST_FIRST`() {
        assertEquals(EcFilterType.NEWEST_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `setSortFilter updates current sort`() = runTest {
        viewModel.setSortFilter(EcFilterType.SYNCING_FIRST)
        advanceUntilIdle()
        assertEquals(EcFilterType.SYNCING_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `setClickedBenId then filter combine does not throw`() = runTest {
        viewModel.setClickedBenId(77L)
        viewModel.filterText("  Query  ")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
        assertNotNull(viewModel.bottomSheetList)
    }

    @Test
    fun `setClickedBenId zero does not throw`() = runTest {
        viewModel.setClickedBenId(0L)
        advanceUntilIdle()
        assertNotNull(viewModel.bottomSheetList)
    }
}
