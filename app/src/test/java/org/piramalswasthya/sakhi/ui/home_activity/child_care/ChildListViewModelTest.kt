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
}
