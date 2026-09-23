package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import android.text.TextUtils
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
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
import org.piramalswasthya.sakhi.model.BenWithPwrDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_women_registration.list.PwRegistrationListViewModel
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class PwRegistrationListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: PwRegistrationListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers { firstArg<String>().all { it.isDigit() } }
        every { recordsRepo.getPregnantWomenList() } returns flowOf(emptyList())
        every { recordsRepo.getPregnantWomenWithRchList() } returns flowOf(emptyList())
        viewModel = PwRegistrationListViewModel(recordsRepo)
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

    // =====================================================
    // filterType() Tests
    // =====================================================

    @Test
    fun `filterType does not throw`() = runTest {
        viewModel.filterType("registered")
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
    fun `setSortFilter to AGE_WISE updates current sort`() = runTest {
        viewModel.setSortFilter(EcFilterType.AGE_WISE)
        advanceUntilIdle()
        assertEquals(EcFilterType.AGE_WISE, viewModel.getCurrentSort())
    }

    @Test
    fun `filterType true then filterText combine does not throw`() = runTest {
        viewModel.filterType("true")
        viewModel.filterText("  Name  ")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `filterType false path does not throw`() = runTest {
        viewModel.filterType("false")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // benList real combine/filter/sort coverage
    // =====================================================

    private fun ben(benId: Long, name: String, rchId: String? = null): BenBasicDomain =
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
            rchId = rchId,
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false
        )

    private fun pwr(benId: Long, name: String, rchId: String? = null): BenWithPwrDomain =
        BenWithPwrDomain(ben = ben(benId, name, rchId), pwr = null)

    @Test
    fun `benList returns all when kind is false`() = runTest {
        val list = listOf(pwr(1L, "Alice", rchId = "12345"), pwr(2L, "Bella", rchId = null))
        every { recordsRepo.getPregnantWomenList() } returns flowOf(list)
        val vm = PwRegistrationListViewModel(recordsRepo)

        val result = vm.benList.first()

        assertEquals(2, result.size)
    }

    @Test
    fun `benList filters to rch-present only when kind is true`() = runTest {
        val list = listOf(pwr(1L, "Alice", rchId = "12345"), pwr(2L, "Bella", rchId = null))
        every { recordsRepo.getPregnantWomenList() } returns flowOf(list)
        val vm = PwRegistrationListViewModel(recordsRepo)
        vm.filterType("true")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `benList filters by search text`() = runTest {
        val list = listOf(pwr(1L, "Alice"), pwr(2L, "Bella"))
        every { recordsRepo.getPregnantWomenList() } returns flowOf(list)
        val vm = PwRegistrationListViewModel(recordsRepo)
        vm.filterText("alice")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `benList empty when search matches nothing`() = runTest {
        val list = listOf(pwr(1L, "Alice"))
        every { recordsRepo.getPregnantWomenList() } returns flowOf(list)
        val vm = PwRegistrationListViewModel(recordsRepo)
        vm.filterText("zzz")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }
}
