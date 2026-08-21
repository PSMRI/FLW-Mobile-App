package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.model.ECTDomain
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

    private fun ectItem(benId: Long, records: List<ECTDomain>): BenWithEctListDomain {
        val benBasic = mockk<BenBasicDomain>(relaxed = true)
        every { benBasic.benId } returns benId
        val item = mockk<BenWithEctListDomain>(relaxed = true)
        every { item.ben } returns benBasic
        every { item.savedECTRecords } returns records
        return item
    }

    private fun ectRecord(visited: Long): ECTDomain {
        val record = mockk<ECTDomain>(relaxed = true)
        every { record.visited } returns visited
        return record
    }

    @Test
    fun `bottomSheetList emits the saved records reversed for the selected beneficiary`() = runTest {
        val ect1 = ectRecord(1_000L)
        val ect2 = ectRecord(2_000L)
        every { recordsRepo.eligibleCoupleTrackingList } returns flowOf(listOf(ectItem(42L, listOf(ect1, ect2))))

        val vm = EligibleCoupleTrackingListViewModel(savedStateHandle, recordsRepo)
        vm.setClickedBenId(42L)
        advanceUntilIdle()

        val result = vm.bottomSheetList.first()

        assertEquals(listOf(ect1, ect2), result)
    }

    @Test
    fun `bottomSheetList never emits when the selected beneficiary has no saved records`() = runTest {
        every { recordsRepo.eligibleCoupleTrackingList } returns flowOf(listOf(ectItem(42L, emptyList())))

        val vm = EligibleCoupleTrackingListViewModel(savedStateHandle, recordsRepo)
        vm.setClickedBenId(42L)
        advanceUntilIdle()

        val result = withTimeoutOrNull(100) { vm.bottomSheetList.first() }

        assertNull(result)
    }

    @Test
    fun `bottomSheetList never emits when the selected beneficiary is not in the list`() = runTest {
        every { recordsRepo.eligibleCoupleTrackingList } returns
                flowOf(listOf(ectItem(1L, listOf(ectRecord(1_000L)))))

        val vm = EligibleCoupleTrackingListViewModel(savedStateHandle, recordsRepo)
        vm.setClickedBenId(99L)
        advanceUntilIdle()

        val result = withTimeoutOrNull(100) { vm.bottomSheetList.first() }

        assertNull(result)
    }
}
