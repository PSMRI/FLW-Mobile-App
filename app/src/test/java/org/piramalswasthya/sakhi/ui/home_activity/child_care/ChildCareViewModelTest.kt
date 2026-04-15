package org.piramalswasthya.sakhi.ui.home_activity.child_care

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class ChildCareViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: ChildCareViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = ChildCareViewModel()
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
