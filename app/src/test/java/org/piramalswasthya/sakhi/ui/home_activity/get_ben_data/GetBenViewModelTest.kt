package org.piramalswasthya.sakhi.ui.home_activity.get_ben_data

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
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
}
