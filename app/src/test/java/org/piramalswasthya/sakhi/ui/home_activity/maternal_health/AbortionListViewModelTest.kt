package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

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
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.list.AbortionListViewModel

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
}
