package org.piramalswasthya.sakhi.ui.service_location_activity

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceTypeViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao

    private lateinit var viewModel: ServiceTypeViewModel

    @Before
    override fun setUp() {
        super.setUp()
        val mockState = mockk<LocationEntity>(relaxed = true)
        every { mockState.name } returns "TestState"
        val mockDistrict = mockk<LocationEntity>(relaxed = true)
        every { mockDistrict.name } returns "TestDistrict"
        val mockBlock = mockk<LocationEntity>(relaxed = true)
        every { mockBlock.name } returns "TestBlock"
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"

        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.state } returns mockState
        every { mockUser.district } returns mockDistrict
        every { mockUser.block } returns mockBlock
        every { mockUser.villages } returns listOf(mockVillage)

        every { pref.getLoggedInUser() } returns mockUser
        every { pref.getLocationRecord() } returns null
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        viewModel = ServiceTypeViewModel(pref)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `state is not null`() {
        assertNotNull(viewModel.state)
    }

    // =====================================================
    // isLocationSet() Tests
    // =====================================================

    @Test
    fun `isLocationSet returns false initially`() {
        // Init hasn't completed yet (runs on IO dispatcher)
        assertFalse(viewModel.isLocationSet())
    }
}
