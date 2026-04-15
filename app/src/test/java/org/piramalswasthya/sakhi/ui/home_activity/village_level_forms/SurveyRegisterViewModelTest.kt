package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.survey_register.SurveyRegisterViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyRegisterViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: SurveyRegisterViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = SurveyRegisterViewModel()
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }
}
