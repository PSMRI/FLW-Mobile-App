package org.piramalswasthya.sakhi.ui.service_location_activity

import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages

// NOTE: ServiceTypeViewModel's init{} block launches
// `viewModelScope.launch { withContext(Dispatchers.IO) { ... } }` which is the
// known-brittle shape (races with test teardown under runTest / advanceUntilIdle,
// per FLW-1115 conventions). Per convention this class is intentionally NOT driven
// through advanceUntilIdle()/runTest() to reach its SUCCESS branch here - only the
// synchronous, pre-coroutine-completion surface is exercised below.
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

    @Test
    fun `selectedVillage is null before location is resolved`() {
        assertNull(viewModel.selectedVillage)
    }

    @Test
    fun `selectedVillageName is null for ENGLISH when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null for HINDI when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null for ASSAMESE when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null for BANGLA when no village selected`() {
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        assertNull(viewModel.selectedVillageName)
    }

    @Test(expected = Throwable::class)
    fun `setVillage before user is resolved throws`() {
        viewModel.setVillage(0)
    }

    @Test(expected = Throwable::class)
    fun `saveCurrentLocation before user is resolved throws`() {
        viewModel.saveCurrentLocation()
    }
}
