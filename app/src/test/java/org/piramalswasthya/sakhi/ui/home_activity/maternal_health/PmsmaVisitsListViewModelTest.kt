package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

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
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.PMSMAStatus
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaVisitsListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PmsmaVisitsListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var viewModel: PmsmaVisitsListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.getRegisteredPmsmaWomenList() } returns flowOf(emptyList())
        viewModel = PmsmaVisitsListViewModel(recordsRepo, maternalHealthRepo)
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
    fun `updateBottomSheetData then filterText combine does not throw`() = runTest {
        viewModel.updateBottomSheetData(45L)
        viewModel.filterText("name")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
        assertNotNull(viewModel.bottomSheetList)
    }

    @Test
    fun `updateBottomSheetData with different ids does not throw`() = runTest {
        viewModel.updateBottomSheetData(1L)
        viewModel.updateBottomSheetData(2L)
        advanceUntilIdle()
        assertNotNull(viewModel.bottomSheetList)
    }

    @Test
    fun `filterText empty does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    private fun benWithAnc(benId: Long, pmsma: List<PMSMAStatus>): BenWithAncListDomain {
        val entry = mockk<BenWithAncListDomain>(relaxed = true)
        every { entry.ben.benId } returns benId
        every { entry.pmsma } returns pmsma
        return entry
    }

    @Test
    fun `benList is empty until a ben id is selected`() = runTest {
        every { recordsRepo.getRegisteredPmsmaWomenList() } returns flowOf(
            listOf(benWithAnc(1L, emptyList()), benWithAnc(2L, emptyList()))
        )
        val vm = PmsmaVisitsListViewModel(recordsRepo, maternalHealthRepo)

        assertTrue(vm.benList.first().isEmpty())
    }

    @Test
    fun `benList filters to the selected ben id`() = runTest {
        every { recordsRepo.getRegisteredPmsmaWomenList() } returns flowOf(
            listOf(benWithAnc(1L, emptyList()), benWithAnc(2L, emptyList()))
        )
        val vm = PmsmaVisitsListViewModel(recordsRepo, maternalHealthRepo)

        vm.updateBottomSheetData(2L)
        advanceUntilIdle()

        val result = vm.benList.first()
        assertEquals(1, result.size)
        assertEquals(2L, result[0].ben.benId)
    }

    @Test
    fun `bottomSheetList returns pmsma of first matching ben`() = runTest {
        val status = mockk<PMSMAStatus>(relaxed = true)
        every { recordsRepo.getRegisteredPmsmaWomenList() } returns flowOf(
            listOf(benWithAnc(1L, listOf(status)))
        )
        val vm = PmsmaVisitsListViewModel(recordsRepo, maternalHealthRepo)

        vm.updateBottomSheetData(1L)
        advanceUntilIdle()

        assertEquals(1, vm.bottomSheetList.first().size)
    }

    @Test
    fun `bottomSheetList is empty when no ben is selected`() = runTest {
        every { recordsRepo.getRegisteredPmsmaWomenList() } returns flowOf(
            listOf(benWithAnc(1L, emptyList()))
        )
        val vm = PmsmaVisitsListViewModel(recordsRepo, maternalHealthRepo)

        assertTrue(vm.bottomSheetList.first().isEmpty())
    }
}
