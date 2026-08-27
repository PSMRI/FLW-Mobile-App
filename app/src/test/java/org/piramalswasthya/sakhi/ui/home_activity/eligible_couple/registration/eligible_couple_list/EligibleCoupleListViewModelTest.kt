package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_list

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithEcrDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: EligibleCoupleListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { savedStateHandle.get<String>("source") } returns "DEFAULT"
        every { recordsRepo.eligibleCoupleList } returns flowOf(emptyList())
        every { recordsRepo.eligibleCoupleMissedPeriodList } returns flowOf(emptyList())
        viewModel = EligibleCoupleListViewModel(savedStateHandle, recordsRepo)
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
    fun `filterText trims and lowercases without throwing`() = runTest {
        viewModel.filterText("  MixedCase  ")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `sort then filter combine does not throw`() = runTest {
        viewModel.setSortFilter(EcFilterType.OLDEST_FIRST)
        viewModel.filterText("x")
        advanceUntilIdle()
        assertEquals(EcFilterType.OLDEST_FIRST, viewModel.getCurrentSort())
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // benList real combine/filter/sort coverage
    // =====================================================

    private fun ben(benId: Long, name: String): BenBasicDomain =
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

    private fun ecr(benId: Long, name: String): BenWithEcrDomain = BenWithEcrDomain(ben = ben(benId, name), ecr = null)

    @Test
    fun `benList filters by matching name`() = runTest {
        val list = listOf(ecr(1L, "Alice"), ecr(2L, "Bella"))
        every { recordsRepo.eligibleCoupleList } returns flowOf(list)
        val vm = EligibleCoupleListViewModel(savedStateHandle, recordsRepo)
        vm.filterText("alice")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `benList empty when filter matches nothing`() = runTest {
        val list = listOf(ecr(1L, "Alice"))
        every { recordsRepo.eligibleCoupleList } returns flowOf(list)
        val vm = EligibleCoupleListViewModel(savedStateHandle, recordsRepo)
        vm.filterText("zzz")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `benList sorts newest first by default when filter blank`() = runTest {
        val list = listOf(ecr(1L, "Alice"), ecr(2L, "Bella"))
        every { recordsRepo.eligibleCoupleList } returns flowOf(list)
        val vm = EligibleCoupleListViewModel(savedStateHandle, recordsRepo)

        val result = vm.benList.first()

        assertEquals(2, result.size)
        assertEquals(2L, result[0].ben.benId)
        assertEquals(1L, result[1].ben.benId)
    }
}
