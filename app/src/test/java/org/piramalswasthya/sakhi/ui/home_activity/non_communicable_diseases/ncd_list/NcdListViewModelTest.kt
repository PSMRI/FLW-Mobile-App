package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_list

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
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class NcdListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: NcdListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.allBenList } returns flowOf(emptyList())
        viewModel = NcdListViewModel(recordsRepo)
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

    @Test
    fun `abha is initially null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `benId is initially null`() {
        assertNull(viewModel.benId.value)
    }

    @Test
    fun `benRegId is initially null`() {
        assertNull(viewModel.benRegId.value)
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
    // resetBenRegId() Tests
    // =====================================================

    @Test
    fun `resetBenRegId sets benRegId to null`() {
        viewModel.resetBenRegId()
        assertNull(viewModel.benRegId.value)
    }
}
