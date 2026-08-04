package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_women_registration.list.PwRegistrationListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PwRegistrationListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: PwRegistrationListViewModel

    @Before
    override fun setUp() {
        super.setUp()
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
}
