package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo

/**
 * Unit tests for [InfantListViewModel] - same combine(kind)+combine(filter) shape as
 * [org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.ChildListViewModel], but
 * `getBenById`/`getDobByBenIdAsync` resolve from a single `.first()` snapshot instead of a
 * short-circuiting collect.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InfantListViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var recordsRepo: RecordsRepo

    private fun ben(benId: Long, name: String = "TestName", rchId: String? = null, dob: Long = 0L) =
        BenBasicDomain(
            benId = benId,
            hhId = 1L,
            reproductiveStatusId = 0,
            regDate = "01-01-2024",
            benName = name,
            gender = "M",
            dob = dob,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false,
            rchId = rchId
        )

    private fun buildVm(list: List<BenBasicDomain> = emptyList()): InfantListViewModel {
        every { recordsRepo.infantList } returns flowOf(list)
        return InfantListViewModel(recordsRepo)
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers {
            val str = firstArg<CharSequence>()
            str.all { it.isDigit() }
        }
    }

    @Test
    fun `default kind false keeps the full list unfiltered`() = runTest {
        val vm = buildVm(listOf(ben(1L, rchId = "abc"), ben(2L, rchId = "12345")))
        val result = vm.benList.first()
        assertEquals(2, result.size)
    }

    @Test
    fun `filterType true keeps only bens with a digits-only rchId`() = runTest {
        val vm = buildVm(listOf(ben(1L, rchId = "abc"), ben(2L, rchId = "12345")))
        vm.filterType("true")
        advanceUntilIdle()
        val result = vm.benList.first()
        assertEquals(1, result.size)
        assertEquals(2L, result[0].benId)
    }

    @Test
    fun `filterText narrows the list by name`() = runTest {
        val vm = buildVm(listOf(ben(1L, name = "Alice"), ben(2L, name = "Bob")))
        vm.filterText("bob")
        advanceUntilIdle()
        val result = vm.benList.first()
        assertEquals(1, result.size)
        assertEquals(2L, result[0].benId)
    }

    @Test
    fun `getBenById resolves the matching beneficiary`() = runTest {
        val vm = buildVm(listOf(ben(1L, name = "Alice"), ben(2L, name = "Bob")))
        var result: BenBasicDomain? = null
        vm.getBenById(2L) { result = it }
        advanceUntilIdle()
        assertEquals(2L, result?.benId)
    }

    @Test
    fun `getBenById resolves null when nothing matches`() = runTest {
        val vm = buildVm(listOf(ben(1L)))
        var invoked = false
        vm.getBenById(999L) { invoked = true; assertNull(it) }
        advanceUntilIdle()
        assertTrue(invoked)
    }

    @Test
    fun `getDobByBenIdAsync resolves the matching beneficiary's dob`() = runTest {
        val vm = buildVm(listOf(ben(1L, dob = 5555L)))
        var dob: Long? = null
        vm.getDobByBenIdAsync(1L) { dob = it }
        advanceUntilIdle()
        assertEquals(5555L, dob)
    }

    @Test
    fun `getDobByBenIdAsync resolves null when nothing matches`() = runTest {
        val vm = buildVm(listOf(ben(1L)))
        var result: Long? = 1L
        vm.getDobByBenIdAsync(999L) { result = it }
        advanceUntilIdle()
        assertNull(result)
    }
}
