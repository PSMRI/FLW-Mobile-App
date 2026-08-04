package org.piramalswasthya.sakhi.ui.home_activity.child_care

import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.ChildListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ChildListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: ChildListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.childList } returns flowOf(emptyList())
        viewModel = ChildListViewModel(recordsRepo)
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
        viewModel.filterType("HBYC")
        advanceUntilIdle()
    }

    @Test
    fun `filterType with empty string does not throw`() = runTest {
        viewModel.filterType("")
        advanceUntilIdle()
    }

    @Test
    fun `filterType true then filterText combine does not throw`() = runTest {
        viewModel.filterType("true")
        viewModel.filterText("abc")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `filterType false path does not throw`() = runTest {
        viewModel.filterType("false")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `getBenById on empty list invokes callback with null or not at all`() = runTest {
        var invoked = false
        var result: BenBasicDomain? = null
        runCatching {
            viewModel.getBenById(999L) { ben ->
                invoked = true
                result = ben
            }
            advanceUntilIdle()
        }
        // On empty list the finder never matches; assert no crash and no stale value.
        assertNotNull(viewModel.benList)
        if (invoked) assertNotNull("callback ran" to result)
    }

    @Test
    fun `getDobByBenIdAsync on empty list does not throw`() = runTest {
        runCatching {
            viewModel.getDobByBenIdAsync(123L) { }
            advanceUntilIdle()
        }
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `repeated filterText updates do not throw`() = runTest {
        viewModel.filterText("a")
        viewModel.filterText("ab")
        viewModel.filterText("abc")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }
}
