package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class DeathReportsViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: DeathReportsViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = DeathReportsViewModel()
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `scope returns viewModelScope`() {
        assertNotNull(viewModel.scope)
    }
}
