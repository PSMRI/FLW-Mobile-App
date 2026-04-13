package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.infant_reg.list.InfantRegListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class InfantRegListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: InfantRegListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { savedStateHandle.get<Boolean>("onlyLowBirthWeight") } returns false
        every { recordsRepo.getListForInfantReg() } returns flowOf(emptyList())
        every { recordsRepo.getListForLowWeightInfantReg() } returns flowOf(emptyList())
        viewModel = InfantRegListViewModel(recordsRepo, savedStateHandle)
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
}
