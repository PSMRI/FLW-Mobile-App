package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_priority_list

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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithCbacCache
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class NcdPriorityListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: NcdPriorityListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.getNcdPriorityList } returns flowOf(emptyList())
        viewModel = NcdPriorityListViewModel(recordsRepo)
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
    fun `filterText with whitespace does not throw`() = runTest {
        viewModel.filterText("  priority  ")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `repeated filterText emissions do not throw`() = runTest {
        viewModel.filterText("a")
        viewModel.filterText("")
        viewModel.filterText("b")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // benList real combine/filter coverage
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

    private fun benWithCbac(benId: Long, name: String): BenWithCbacCache {
        val domain = ben(benId, name)
        val cache = mockk<BenBasicCache>(relaxed = true)
        every { cache.asBasicDomainModel() } returns domain
        return BenWithCbacCache(ben = cache, savedCbacRecords = emptyList())
    }

    @Test
    fun `benList maps and filters by matching name`() = runTest {
        val list = listOf(benWithCbac(1L, "Alice"), benWithCbac(2L, "Bella"))
        every { recordsRepo.getNcdPriorityList } returns flowOf(list)
        val vm = NcdPriorityListViewModel(recordsRepo)
        vm.filterText("alice")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].benId)
    }

    @Test
    fun `benList returns all when filter blank`() = runTest {
        val list = listOf(benWithCbac(1L, "Alice"), benWithCbac(2L, "Bella"))
        every { recordsRepo.getNcdPriorityList } returns flowOf(list)
        val vm = NcdPriorityListViewModel(recordsRepo)

        val result = vm.benList.first()

        assertEquals(2, result.size)
    }

    @Test
    fun `benList empty when filter matches nothing`() = runTest {
        val list = listOf(benWithCbac(1L, "Alice"))
        every { recordsRepo.getNcdPriorityList } returns flowOf(list)
        val vm = NcdPriorityListViewModel(recordsRepo)
        vm.filterText("zzz")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }
}
