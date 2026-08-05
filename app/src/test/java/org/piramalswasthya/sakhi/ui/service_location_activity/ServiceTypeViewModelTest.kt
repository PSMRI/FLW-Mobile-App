package org.piramalswasthya.sakhi.ui.service_location_activity

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceTypeViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao

    private lateinit var viewModel: ServiceTypeViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = ServiceTypeViewModel(pref)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is LOADING`() {
        assertEquals(ServiceTypeViewModel.State.LOADING, viewModel.state.value)
    }

    @Test
    fun `isLocationSet returns false while loading`() {
        assertFalse(viewModel.isLocationSet())
    }

    @Test
    fun `isNoUserFound live data is not null`() {
        assertNotNull(viewModel.isNoUserFound)
    }
}
