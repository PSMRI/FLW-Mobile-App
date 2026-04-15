package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class NcdViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: NcdViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = NcdViewModel()
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
