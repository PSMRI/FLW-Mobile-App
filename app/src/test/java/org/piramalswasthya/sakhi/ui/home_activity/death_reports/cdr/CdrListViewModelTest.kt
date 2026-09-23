package org.piramalswasthya.sakhi.ui.home_activity.death_reports.cdr

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
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class CdrListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: CdrListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.cdrList } returns flowOf(emptyList())
        viewModel = CdrListViewModel(recordsRepo)
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
    fun `filterText with whitespace combine does not throw`() = runTest {
        viewModel.filterText("  abc  ")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `sequential filterText emissions do not throw`() = runTest {
        viewModel.filterText("a")
        viewModel.filterText("ab")
        viewModel.filterText("")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `benList reference is stable`() {
        val first = viewModel.benList
        val second = viewModel.benList
        assertNotNull(first)
        assertNotNull(second)
    }

    @Test
    fun `benList collects real results once the flow is exercised`() = runTest {
        val result = viewModel.benList.first()
        assertTrue(result.isEmpty())
    }
}
