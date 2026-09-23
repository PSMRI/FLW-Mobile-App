package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.HomeVisitUiState
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PwAncVisitsListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var preferenceDao: PreferenceDao

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
        viewModel = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo, preferenceDao)
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

    // =====================================================
    // setSortFilter() / getCurrentSort() Tests
    // =====================================================

    @Test
    fun `getCurrentSort returns default NEWEST_FIRST`() {
        assertEquals(EcFilterType.NEWEST_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `setSortFilter updates current sort`() = runTest {
        viewModel.setSortFilter(EcFilterType.OLDEST_FIRST)
        advanceUntilIdle()
        assertEquals(EcFilterType.OLDEST_FIRST, viewModel.getCurrentSort())
    }

    // =====================================================
    // loadHomeVisitState() Tests
    // =====================================================

    @Test
    fun `loadHomeVisitState populates homeVisitState map`() = runTest {
        coEvery { recordsRepo.getHomeVisitUiState(1L) } returns HomeVisitUiState(canAddHomeVisit = true, canViewHomeVisit = false)
        viewModel.loadHomeVisitState(listOf(1L))
        advanceUntilIdle()
        assertEquals(true, viewModel.homeVisitState.value?.get(1L)?.canAddHomeVisit)
    }

    // =====================================================
    // updateDeliveryStatus() Tests
    // =====================================================

    @Test
    fun `updateDeliveryStatus delegates to maternalHealthRepo with logged in user`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userName } returns "asha1"
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery {
            maternalHealthRepo.saveDeliveryStatusFromList(any(), any(), any(), any(), any())
        } returns Unit
        viewModel.updateDeliveryStatus(1L, 2, true, 123456L)
        advanceUntilIdle()
        coVerify {
            maternalHealthRepo.saveDeliveryStatusFromList(
                benId = 1L, visitNumber = 2, isDelivered = true, userName = "asha1", delivaryDate = 123456L
            )
        }
    }

    @Test
    fun `updateDeliveryStatus uses empty username when no logged in user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        coEvery {
            maternalHealthRepo.saveDeliveryStatusFromList(any(), any(), any(), any(), any())
        } returns Unit
        viewModel.updateDeliveryStatus(1L, 1, false, 0L)
        advanceUntilIdle()
        coVerify {
            maternalHealthRepo.saveDeliveryStatusFromList(
                benId = 1L, visitNumber = 1, isDelivered = false, userName = "", delivaryDate = 0L
            )
        }
    }

    // =====================================================
    // benList / bottomSheetList emission Tests
    // =====================================================

    private fun benWithAnc(benId: Long): BenWithAncListDomain {
        val ben = mockk<BenBasicDomain>(relaxed = true)
        every { ben.benId } returns benId
        val entity = mockk<BenWithAncListDomain>(relaxed = true)
        every { entity.ben } returns ben
        every { entity.ancDate } returns 100L
        every { entity.anc } returns listOf(
            AncStatus(benId = benId, visitNumber = 1, filledWeek = 10, anyHighRisk = true, placeOfAncId = 3)
        )
        return entity
    }

    @Test
    fun `benList emits combined and sorted list`() = runTest {
        every { recordsRepo.getRegisteredPregnantWomanList() } returns flowOf(listOf(benWithAnc(1L)))
        val vm = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo, preferenceDao)
        val list = vm.benList.first()
        assertEquals(1, list.size)
    }

    @Test
    fun `benList shows high risk list when toggled on`() = runTest {
        every { recordsRepo.getRegisteredPregnantWomanList() } returns flowOf(emptyList())
        every { recordsRepo.getHighRiskPregnantWomanList() } returns flowOf(listOf(benWithAnc(2L)))
        val vm = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo, preferenceDao)
        vm.toggleHighRisk(true)
        advanceUntilIdle()
        val list = vm.benList.first()
        assertEquals(1, list.size)
    }

    @Test
    fun `bottomSheetList returns anc list for selected ben in NORMAL mode`() = runTest {
        every { recordsRepo.getRegisteredPregnantWomanList() } returns flowOf(listOf(benWithAnc(1L)))
        val vm = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo, preferenceDao)
        vm.showAncBottomSheet(1L, PwAncVisitsListViewModel.BottomSheetMode.NORMAL)
        advanceUntilIdle()
        val ancList = vm.bottomSheetList.first()
        assertTrue(ancList.isNotEmpty())
    }

    @Test
    fun `bottomSheetList returns empty for unmatched ben in NORMAL mode`() = runTest {
        every { recordsRepo.getRegisteredPregnantWomanList() } returns flowOf(listOf(benWithAnc(1L)))
        val vm = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo, preferenceDao)
        val ancList = vm.bottomSheetList.first()
        assertTrue(ancList.isEmpty())
    }

    @Test
    fun `bottomSheetList filters high risk anc entries in PMSMA mode`() = runTest {
        every { recordsRepo.getHighRiskPregnantWomanList() } returns flowOf(listOf(benWithAnc(3L)))
        val vm = PwAncVisitsListViewModel(savedStateHandle, recordsRepo, maternalHealthRepo, preferenceDao)
        vm.showAncBottomSheet(3L, PwAncVisitsListViewModel.BottomSheetMode.PMSMA)
        advanceUntilIdle()
        val ancList = vm.bottomSheetList.first()
        assertTrue(ancList.isNotEmpty())
    }
}
