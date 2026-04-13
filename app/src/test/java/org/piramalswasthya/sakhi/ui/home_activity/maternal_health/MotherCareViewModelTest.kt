package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class MotherCareViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: MotherCareViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = MotherCareViewModel()
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
