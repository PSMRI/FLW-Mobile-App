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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.InfantRegDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.infant_reg.list.InfantRegListViewModel
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class InfantRegListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: InfantRegListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { savedStateHandle.get<Boolean>("onlyLowBirthWeight") } returns false
        every { recordsRepo.getListForInfantReg() } returns flowOf(emptyList())
        every { recordsRepo.getListForLowWeightInfantReg() } returns flowOf(emptyList())
        viewModel = InfantRegListViewModel(recordsRepo, savedStateHandle)
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

    @Test
    fun `onlyLowBirthWeight defaults to false from saved state`() {
        assertFalse(viewModel.onlyLowBirthWeight)
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
    fun `filter then sort combine does not throw`() = runTest {
        viewModel.filterText("baby")
        viewModel.setSortFilter(EcFilterType.AGE_WISE)
        advanceUntilIdle()
        assertEquals(EcFilterType.AGE_WISE, viewModel.getCurrentSort())
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // benList real combine/filter/sort coverage
    // =====================================================

    private fun mother(benId: Long, name: String): BenBasicDomain =
        BenBasicDomain(
            benId = benId,
            hhId = 100L,
            reproductiveStatusId = 1,
            regDate = "17-03-2026",
            benName = name,
            gender = "Female",
            dob = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }.timeInMillis,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false
        )

    private fun doCache(benId: Long): DeliveryOutcomeCache =
        DeliveryOutcomeCache(
            benId = benId,
            isActive = true,
            createdBy = "asha",
            updatedBy = "asha",
            syncState = SyncState.SYNCED
        )

    private fun irCache(motherBenId: Long, createdDate: Long): InfantRegCache =
        InfantRegCache(
            motherBenId = motherBenId,
            isActive = true,
            babyIndex = 0,
            createdBy = "asha",
            createdDate = createdDate,
            updatedBy = "asha",
            syncState = SyncState.SYNCED
        )

    @Test
    fun `benList filters by baby name and sorts newest first`() = runTest {
        val list = listOf(
            InfantRegDomain(motherBen = mother(1L, "Alice"), babyIndex = 0, babyName = "Tiny", deliveryOutcome = doCache(1L), savedIr = irCache(1L, 1000L)),
            InfantRegDomain(motherBen = mother(2L, "Bella"), babyIndex = 0, babyName = "Junior", deliveryOutcome = doCache(2L), savedIr = irCache(2L, 2000L))
        )
        every { recordsRepo.getListForInfantReg() } returns flowOf(list)
        val vm = InfantRegListViewModel(recordsRepo, savedStateHandle)

        val result = vm.benList.first()

        assertEquals(2, result.size)
        assertEquals("Junior", result[0].babyName)
        assertEquals("Tiny", result[1].babyName)
    }

    @Test
    fun `benList filters to matching baby name only`() = runTest {
        val list = listOf(
            InfantRegDomain(motherBen = mother(1L, "Alice"), babyIndex = 0, babyName = "Tiny", deliveryOutcome = doCache(1L), savedIr = irCache(1L, 1000L)),
            InfantRegDomain(motherBen = mother(2L, "Bella"), babyIndex = 0, babyName = "Junior", deliveryOutcome = doCache(2L), savedIr = irCache(2L, 2000L))
        )
        every { recordsRepo.getListForInfantReg() } returns flowOf(list)
        val vm = InfantRegListViewModel(recordsRepo, savedStateHandle)
        vm.filterText("tiny")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals("Tiny", result[0].babyName)
    }

    @Test
    fun `benList empty when filter matches nothing`() = runTest {
        val list = listOf(InfantRegDomain(motherBen = mother(1L, "Alice"), babyIndex = 0, babyName = "Tiny", deliveryOutcome = doCache(1L), savedIr = irCache(1L, 1000L)))
        every { recordsRepo.getListForInfantReg() } returns flowOf(list)
        val vm = InfantRegListViewModel(recordsRepo, savedStateHandle)
        vm.filterText("zzz")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }
}
