package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import android.text.TextUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYFormCardViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: CUFYFormCardViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers { firstArg<String>().all { it.isDigit() } }
        every { recordsRepo.childCard } returns flowOf(emptyList())
        viewModel = CUFYFormCardViewModel(recordsRepo)
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
        viewModel.filterType("true")
        advanceUntilIdle()
    }

    @Test
    fun `benList collects through both combine stages`() = runTest {
        val result = viewModel.benList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `benList reflects filterType true branch`() = runTest {
        viewModel.filterType("true")
        advanceUntilIdle()
        val result = viewModel.benList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getBenById invokes callback with repo result`() = runTest {
        val ben = mockk<BenBasicDomain>(relaxed = true)
        coEvery { recordsRepo.getBenById(9L) } returns ben
        var callbackResult: BenBasicDomain? = null

        viewModel.getBenById(9L) { callbackResult = it }
        advanceUntilIdle()

        assertEquals(ben, callbackResult)
    }

    @Test
    fun `getBenById invokes callback with null when repo returns null`() = runTest {
        coEvery { recordsRepo.getBenById(9L) } returns null
        var callbackResult: BenBasicDomain? = mockk(relaxed = true)

        viewModel.getBenById(9L) { callbackResult = it }
        advanceUntilIdle()

        assertNull(callbackResult)
    }

    @Test
    fun `getDobByBenIdAsync filters allBenList by benId`() = runTest {
        val matching = mockk<BenBasicDomain>(relaxed = true)
        every { matching.benId } returns 9L
        val nonMatching = mockk<BenBasicDomain>(relaxed = true)
        every { nonMatching.benId } returns 10L
        every { recordsRepo.childCard } returns flowOf(listOf(matching, nonMatching))
        val vm = CUFYFormCardViewModel(recordsRepo)
        var result: List<BenBasicDomain>? = null

        vm.getDobByBenIdAsync(9L) { result = it }
        advanceUntilIdle()

        assertEquals(1, result?.size)
        assertEquals(9L, result?.get(0)?.benId)
    }

    // =====================================================
    // benList real combine/filter coverage (kind + text stages)
    // =====================================================

    private fun ben(benId: Long, name: String, rchId: String? = null): BenBasicDomain =
        BenBasicDomain(
            benId = benId,
            hhId = 100L,
            reproductiveStatusId = 1,
            regDate = "17-03-2026",
            benName = name,
            gender = "Female",
            dob = Calendar.getInstance().apply { add(Calendar.YEAR, -4) }.timeInMillis,
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

    @Test
    fun `benList filters to rch-present only when kind is true`() = runTest {
        val withRch = ben(1L, "Alice", rchId = "12345")
        val withoutRch = ben(2L, "Bella", rchId = null)
        every { recordsRepo.childCard } returns flowOf(listOf(withRch, withoutRch))
        val vm = CUFYFormCardViewModel(recordsRepo)
        vm.filterType("true")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].benId)
    }

    @Test
    fun `benList filters by search text after kind stage`() = runTest {
        val alice = ben(1L, "Alice")
        val bella = ben(2L, "Bella")
        every { recordsRepo.childCard } returns flowOf(listOf(alice, bella))
        val vm = CUFYFormCardViewModel(recordsRepo)
        vm.filterText("alice")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].benId)
    }

    @Test
    fun `benList empty when search text matches nothing`() = runTest {
        val alice = ben(1L, "Alice")
        every { recordsRepo.childCard } returns flowOf(listOf(alice))
        val vm = CUFYFormCardViewModel(recordsRepo)
        vm.filterText("zzz")
        advanceUntilIdle()

        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }
}
