package org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.list

import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAdolescentDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo

/**
 * Unit tests for [AdolescentListViewModel]. `benList` is a plain text-filter combine over
 * `RecordsRepo.adolescentList`, delegating to `filterAdolescentList`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdolescentListViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var recordsRepo: RecordsRepo

    private fun entry(benId: Long, name: String) = BenWithAdolescentDomain(
        ben = BenBasicDomain(
            benId = benId,
            hhId = 1L,
            reproductiveStatusId = 0,
            regDate = "01-01-2024",
            benName = name,
            gender = "F",
            dob = 0L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false
        ),
        adolescent = null
    )

    private fun buildVm(list: List<BenWithAdolescentDomain> = emptyList()): AdolescentListViewModel {
        every { recordsRepo.adolescentList } returns flowOf(list)
        return AdolescentListViewModel(recordsRepo)
    }

    @Test
    fun `benList returns the full list when no filter text is set`() = runTest {
        val vm = buildVm(listOf(entry(1L, "Alice"), entry(2L, "Bob")))
        val result = vm.benList.first()
        assertEquals(2, result.size)
    }

    @Test
    fun `filterText narrows the list to matching beneficiaries`() = runTest {
        val vm = buildVm(listOf(entry(1L, "Alice"), entry(2L, "Bob")))
        vm.filterText("alice")
        advanceUntilIdle()
        val result = vm.benList.first()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `filterText with no matches returns an empty list`() = runTest {
        val vm = buildVm(listOf(entry(1L, "Alice")))
        vm.filterText("zzz")
        advanceUntilIdle()
        val result = vm.benList.first()
        assertEquals(0, result.size)
    }
}
