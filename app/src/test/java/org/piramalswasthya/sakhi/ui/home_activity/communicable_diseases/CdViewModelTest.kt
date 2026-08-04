package org.piramalswasthya.sakhi.ui.home_activity.communicable_diseases

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest

@OptIn(ExperimentalCoroutinesApi::class)
class CdViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: CdViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = CdViewModel()
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `scope is not null`() {
        assertNotNull(viewModel.scope)
    }
}
