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
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.list.AbortionListViewModel
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AbortionListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: AbortionListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.getAbortionPregnantWomanList() } returns flowOf(emptyList())
        viewModel = AbortionListViewModel(recordsRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `allAbortionList flow is not null`() {
        assertNotNull(viewModel.allAbortionList)
    }

    @Test
    fun `abortionList flow is not null`() {
        assertNotNull(viewModel.abortionList)
    }

    // =====================================================
    // setYearMonth() Tests
    // =====================================================

    @Test
    fun `setYearMonth does not throw`() = runTest {
        viewModel.setYearMonth(2026, 3)
        advanceUntilIdle()
    }

    // =====================================================
    // setSearchQuery() Tests
    // =====================================================

    @Test
    fun `setSearchQuery does not throw`() = runTest {
        viewModel.setSearchQuery("test")
        advanceUntilIdle()
    }

    @Test
    fun `setSearchQuery with empty string does not throw`() = runTest {
        viewModel.setSearchQuery("")
        advanceUntilIdle()
    }

    // =====================================================
    // updateSelectedBenId() Tests
    // =====================================================

    @Test
    fun `updateSelectedBenId does not throw`() = runTest {
        viewModel.updateSelectedBenId(42L)
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
    fun `search and sort combine does not throw`() = runTest {
        viewModel.setSearchQuery("name")
        viewModel.setSortFilter(EcFilterType.AGE_WISE)
        advanceUntilIdle()
        assertEquals(EcFilterType.AGE_WISE, viewModel.getCurrentSort())
        assertNotNull(viewModel.abortionList)
    }

    @Test
    fun `setYearMonth various values do not throw`() = runTest {
        viewModel.setYearMonth(2025, 1)
        viewModel.setYearMonth(2026, 12)
        advanceUntilIdle()
        assertNotNull(viewModel.abortionList)
    }

    @Test
    fun `updateSelectedBenId does not throw_2`() = runTest {
        viewModel.updateSelectedBenId(55L)
        advanceUntilIdle()
        assertNotNull(viewModel.allAbortionList)
    }

    // =====================================================
    // abortionList real combine/filter/sort coverage
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

    private fun abortion(benId: Long, name: String, abortionDate: Long): BenWithAncListDomain =
        BenWithAncListDomain(
            ben = ben(benId, name),
            pwr = null,
            anc = emptyList(),
            pmsma = emptyList(),
            savedAncRecords = emptyList(),
            abortionDate = abortionDate,
            showAddAnc = false,
            pmsmaFillable = false,
            hasPmsma = false,
            syncState = SyncState.SYNCED
        )

    @Test
    fun `abortionList sorts newest first by default`() = runTest {
        val list = listOf(abortion(1L, "Alice", 1000L), abortion(2L, "Bella", 2000L))
        every { recordsRepo.getAbortionPregnantWomanList() } returns flowOf(list)
        val vm = AbortionListViewModel(recordsRepo)

        val result = vm.abortionList.first()

        assertEquals(2, result.size)
        assertEquals(2L, result[0].ben.benId)
        assertEquals(1L, result[1].ben.benId)
    }

    @Test
    fun `abortionList filters by matching name`() = runTest {
        val list = listOf(abortion(1L, "Alice", 1000L), abortion(2L, "Bella", 2000L))
        every { recordsRepo.getAbortionPregnantWomanList() } returns flowOf(list)
        val vm = AbortionListViewModel(recordsRepo)
        vm.setSearchQuery("alice")
        advanceUntilIdle()

        val result = vm.abortionList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `abortionList empty when search matches nothing`() = runTest {
        val list = listOf(abortion(1L, "Alice", 1000L))
        every { recordsRepo.getAbortionPregnantWomanList() } returns flowOf(list)
        val vm = AbortionListViewModel(recordsRepo)
        vm.setSearchQuery("zzz")
        advanceUntilIdle()

        val result = vm.abortionList.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `abortionList oldest first sort orders ascending`() = runTest {
        val list = listOf(abortion(1L, "Alice", 1000L), abortion(2L, "Bella", 2000L))
        every { recordsRepo.getAbortionPregnantWomanList() } returns flowOf(list)
        val vm = AbortionListViewModel(recordsRepo)
        vm.setSortFilter(EcFilterType.OLDEST_FIRST)
        advanceUntilIdle()

        val result = vm.abortionList.first()

        assertEquals(1L, result[0].ben.benId)
        assertEquals(2L, result[1].ben.benId)
    }
}
