package org.piramalswasthya.sakhi.ui.home_activity.disease_control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.LeprosyViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: LeprosyViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = LeprosyViewModel()
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
