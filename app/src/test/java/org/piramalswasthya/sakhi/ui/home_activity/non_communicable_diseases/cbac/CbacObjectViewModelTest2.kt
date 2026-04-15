package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.cbac

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class CbacObjectViewModelTest2 : BaseViewModelTest() {

    private lateinit var viewModel: CbacObjectViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = CbacObjectViewModel()
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }
}
