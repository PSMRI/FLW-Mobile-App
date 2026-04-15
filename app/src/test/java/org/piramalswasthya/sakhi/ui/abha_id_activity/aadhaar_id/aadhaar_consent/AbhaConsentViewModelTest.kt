package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.aadhaar_consent

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User

@OptIn(ExperimentalCoroutinesApi::class)
class AbhaConsentViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao

    private lateinit var viewModel: AbhaConsentViewModel

    @Before
    override fun setUp() {
        super.setUp()
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully with logged in user`() {
        val user = mockk<User>(relaxed = true)
        every { pref.getLoggedInUser() } returns user
        viewModel = AbhaConsentViewModel(pref)
        assertNotNull(viewModel)
        assertEquals(user, viewModel.currentUser)
    }

    @Test
    fun `viewModel initializes with null user when not logged in`() {
        every { pref.getLoggedInUser() } returns null
        viewModel = AbhaConsentViewModel(pref)
        assertNull(viewModel.currentUser)
    }
}
