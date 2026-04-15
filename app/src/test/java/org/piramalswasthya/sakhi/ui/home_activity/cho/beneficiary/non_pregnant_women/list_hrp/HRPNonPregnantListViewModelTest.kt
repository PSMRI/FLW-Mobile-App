package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.list_hrp

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var hrpRepo: HRPRepo

    private lateinit var viewModel: HRPNonPregnantListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.hrpTrackingNonPregList } returns flowOf(emptyList())
        coEvery { hrpRepo.getAllNonPregTrack() } returns emptyList()
        viewModel = HRPNonPregnantListViewModel(recordsRepo, hrpRepo)
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

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `initial benId is null`() {
        assertNull(viewModel.benId.value)
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
    // getTrackDetails() Tests
    // =====================================================

    @Test
    fun `getTrackDetails does not throw after init`() = runTest {
        advanceUntilIdle()
        // allHRNonPregTrack may be null if init coroutine hasn't populated it
        viewModel.getTrackDetails()
    }
}
