package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class MalariaIconViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: MalariaIconViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = MalariaIconViewModel()
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
