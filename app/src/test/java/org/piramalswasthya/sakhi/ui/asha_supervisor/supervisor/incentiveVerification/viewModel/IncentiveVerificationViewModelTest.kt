package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class IncentiveVerificationViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService

    private lateinit var viewModel: IncentiveVerificationViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = IncentiveVerificationViewModel(apiService)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `uiState is not null`() {
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `search with empty query does not throw`() = runTest {
        viewModel.search("")
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `search with text does not throw`() = runTest {
        viewModel.search("asha")
        assertNotNull(viewModel.uiState)
    }
}
