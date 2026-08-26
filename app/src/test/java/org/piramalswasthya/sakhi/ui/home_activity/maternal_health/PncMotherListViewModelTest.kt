package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.model.BenPncDomain
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.list.PncMotherListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PncMotherListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PncMotherListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.pncMotherList } returns flowOf(emptyList())
        every { recordsRepo.pncMotherNonFollowUpList } returns flowOf(emptyList())
        viewModel = PncMotherListViewModel(savedStateHandle, recordsRepo)
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
    // updateBottomSheetData() Tests
    // =====================================================

    @Test
    fun `updateBottomSheetData does not throw`() = runTest {
        viewModel.updateBottomSheetData(42L)
        advanceUntilIdle()
    }

    @Test
    fun `getCurrentSort default is NEWEST_FIRST`() {
        assertEquals(EcFilterType.NEWEST_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `setSortFilter updates current sort`() = runTest {
        viewModel.setSortFilter(EcFilterType.UNSYNCED_FIRST)
        advanceUntilIdle()
        assertEquals(EcFilterType.UNSYNCED_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `filter sort and bottom sheet combine does not throw`() = runTest {
        viewModel.filterText("mother")
        viewModel.setSortFilter(EcFilterType.OLDEST_FIRST)
        viewModel.updateBottomSheetData(0L)
        advanceUntilIdle()
        assertEquals(EcFilterType.OLDEST_FIRST, viewModel.getCurrentSort())
        assertNotNull(viewModel.benList)
        assertNotNull(viewModel.bottomSheetList)
    }

    @Test
    fun `updateBottomSheetData with non-zero id does not throw`() = runTest {
        viewModel.updateBottomSheetData(88L)
        advanceUntilIdle()
        assertNotNull(viewModel.bottomSheetList)
    }

    private fun pncEntry(benId: Long, visits: List<PNCVisitCache> = emptyList()): BenPncDomain {
        val entry = mockk<BenPncDomain>(relaxed = true)
        every { entry.ben.benId } returns benId
        every { entry.pncDate } returns benId
        every { entry.savedPncRecords } returns visits
        return entry
    }

    @Test
    fun `benList sorts newest first by default`() = runTest {
        every { recordsRepo.pncMotherList } returns flowOf(listOf(pncEntry(1L), pncEntry(2L)))
        val vm = PncMotherListViewModel(savedStateHandle, recordsRepo)

        val result = vm.benList.first()

        assertEquals(2L, result[0].ben.benId)
        assertEquals(1L, result[1].ben.benId)
    }

    @Test
    fun `bottomSheetList is empty when no ben selected`() = runTest {
        every { recordsRepo.pncMotherList } returns flowOf(listOf(pncEntry(1L)))
        val vm = PncMotherListViewModel(savedStateHandle, recordsRepo)

        assertTrue(vm.bottomSheetList.first().isEmpty())
    }

    @Test
    fun `bottomSheetList maps saved pnc records for selected ben`() = runTest {
        val visit = mockk<PNCVisitCache>(relaxed = true)
        every { visit.asDomainModel() } returns mockk(relaxed = true)
        every { recordsRepo.pncMotherList } returns flowOf(listOf(pncEntry(5L, listOf(visit))))
        val vm = PncMotherListViewModel(savedStateHandle, recordsRepo)

        vm.updateBottomSheetData(5L)
        advanceUntilIdle()

        assertEquals(1, vm.bottomSheetList.first().size)
    }
}
