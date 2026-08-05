package org.piramalswasthya.sakhi.ui.asha_supervisor.profile

import io.mockk.every
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
class SupervisorProfileViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: SupervisorProfileViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = SupervisorProfileViewModel(preferenceDao)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `navigateToLoginPage is not null`() {
        assertNotNull(viewModel.navigateToLoginPage)
    }

    @Test
    fun `getUserGender delegates to preferenceDao`() {
        every { preferenceDao.getUserGender() } returns "Male"
        assertEquals("Male", viewModel.getUserGender())
    }

    @Test
    fun `navigateToLoginPageComplete resets flag to false`() {
        viewModel.navigateToLoginPageComplete()
        assertFalse(viewModel.navigateToLoginPage.value == true)
    }
}
