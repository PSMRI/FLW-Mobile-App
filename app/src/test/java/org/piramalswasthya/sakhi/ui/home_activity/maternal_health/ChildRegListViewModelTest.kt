package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

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
import org.piramalswasthya.sakhi.model.ChildRegDomain
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.child_reg.list.ChildRegListViewModel
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ChildRegListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: ChildRegListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.getRegisteredInfants() } returns flowOf(emptyList())
        viewModel = ChildRegListViewModel(recordsRepo)
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
        viewModel.setSortFilter(EcFilterType.OLDEST_FIRST)
        advanceUntilIdle()
        assertEquals(EcFilterType.OLDEST_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `filter then sort combine does not throw`() = runTest {
        viewModel.filterText("child")
        viewModel.setSortFilter(EcFilterType.SYNCING_FIRST)
        advanceUntilIdle()
        assertEquals(EcFilterType.SYNCING_FIRST, viewModel.getCurrentSort())
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // benList real combine/filter/sort coverage
    // =====================================================

    private fun mother(benId: Long, name: String, familyHead: String = "Head"): BenBasicDomain =
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
            familyHeadName = familyHead,
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false
        )

    private fun infantCache(motherBenId: Long, createdDate: Long): InfantRegCache =
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
    fun `benList sorts newest first by default`() = runTest {
        val list = listOf(
            ChildRegDomain(motherBen = mother(1L, "Alice"), infant = infantCache(1L, 1000L), childBen = null),
            ChildRegDomain(motherBen = mother(2L, "Bella"), infant = infantCache(2L, 2000L), childBen = null)
        )
        every { recordsRepo.getRegisteredInfants() } returns flowOf(list)
        val vm = ChildRegListViewModel(recordsRepo)

        val result = vm.benList.first()

        assertEquals(2, result.size)
        assertEquals(2L, result[0].motherBen.benId)
        assertEquals(1L, result[1].motherBen.benId)
    }

    @Test
    fun `benList filters out mothers not matching search text`() = runTest {
        val list = listOf(ChildRegDomain(motherBen = mother(1L, "Alice"), infant = infantCache(1L, 1000L), childBen = null))
        every { recordsRepo.getRegisteredInfants() } returns flowOf(list)
        val vm = ChildRegListViewModel(recordsRepo)
        vm.filterText("zzz")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `benList matches substring and applies oldest first sort`() = runTest {
        val list = listOf(
            ChildRegDomain(motherBen = mother(1L, "Alice"), infant = infantCache(1L, 1000L), childBen = null),
            ChildRegDomain(motherBen = mother(2L, "Alicia"), infant = infantCache(2L, 2000L), childBen = null)
        )
        every { recordsRepo.getRegisteredInfants() } returns flowOf(list)
        val vm = ChildRegListViewModel(recordsRepo)
        vm.filterText("ali")
        vm.setSortFilter(EcFilterType.OLDEST_FIRST)
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(2, result.size)
        assertEquals(1L, result[0].motherBen.benId)
        assertEquals(2L, result[1].motherBen.benId)
    }
}
