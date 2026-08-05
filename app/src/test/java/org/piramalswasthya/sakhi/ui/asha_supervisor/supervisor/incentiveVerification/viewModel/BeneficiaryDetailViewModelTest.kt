package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiaryDetailViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: BeneficiaryDetailViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = BeneficiaryDetailViewModel(apiService, preferenceDao)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `uiState is not null`() {
        assertNotNull(viewModel.uiState)
    }
}
