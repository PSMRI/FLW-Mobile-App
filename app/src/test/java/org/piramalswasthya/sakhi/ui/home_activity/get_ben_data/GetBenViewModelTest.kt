package org.piramalswasthya.sakhi.ui.home_activity.get_ben_data

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class GetBenViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: GetBenViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = GetBenViewModel(benRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(GetBenViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `numPages is initially 0`() {
        assertEquals(0, viewModel.numPages)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun `benDataList throws when accessed before getBeneficiaries`() {
        viewModel.benDataList
    }

    // =====================================================
    // getBeneficiaries Tests
    // =====================================================

    @Test
    fun `getBeneficiaries updates state to SUCCESS and populates data on non-empty list`() {
        val benDomain = mockk<BenBasicDomain>(relaxed = true)
        coEvery { benRepo.getBeneficiariesFromServer(any()) } returns Pair(3, mutableListOf(benDomain))

        viewModel.getBeneficiaries(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(GetBenViewModel.State.SUCCESS, viewModel.state.value)
        assertEquals(3, viewModel.numPages)
        assertEquals(1, viewModel.benDataList.size)
    }

    @Test
    fun `getBeneficiaries updates state to ERROR_SERVER on empty list`() {
        coEvery { benRepo.getBeneficiariesFromServer(any()) } returns Pair(0, mutableListOf())

        viewModel.getBeneficiaries(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(GetBenViewModel.State.ERROR_SERVER, viewModel.state.value)
    }
}
