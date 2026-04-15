package org.piramalswasthya.sakhi.ui.home_activity.dashboard

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: DashboardViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = DashboardViewModel()
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }
}
