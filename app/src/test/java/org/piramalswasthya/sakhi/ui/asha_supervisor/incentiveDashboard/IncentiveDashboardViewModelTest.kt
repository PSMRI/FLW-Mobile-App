package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.UserRepo

@OptIn(ExperimentalCoroutinesApi::class)
class IncentiveDashboardViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var viewModel: IncentiveDashboardViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = IncentiveDashboardViewModel(apiService, preferenceDao, userRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `dashboardData is not null`() {
        assertNotNull(viewModel.dashboardData)
    }
}
