package org.piramalswasthya.sakhi.ui.asha_supervisor.profile

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User

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
    fun `getUserDob delegates to preferenceDao`() {
        every { preferenceDao.getUserDob() } returns "01-01-1990"
        assertEquals("01-01-1990", viewModel.getUserDob())
    }

    @Test
    fun `getUserMobile delegates to preferenceDao`() {
        every { preferenceDao.getUserMobile() } returns "9999999999"
        assertEquals("9999999999", viewModel.getUserMobile())
    }

    @Test
    fun `getUserEmail delegates to preferenceDao`() {
        every { preferenceDao.getUserEmail() } returns "test@test.com"
        assertEquals("test@test.com", viewModel.getUserEmail())
    }

    @Test
    fun `getsupervisorId delegates to preferenceDao`() {
        every { preferenceDao.getSupervisorId() } returns 42
        assertEquals(42, viewModel.getsupervisorId())
    }

    @Test
    fun `getEmpId delegates to preferenceDao`() {
        every { preferenceDao.getEmployeeId() } returns "EMP123"
        assertEquals("EMP123", viewModel.getEmpId())
    }

    @Test
    fun `getSuperVisorname delegates to preferenceDao`() {
        every { preferenceDao.getSupervisorName() } returns "John Doe"
        assertEquals("John Doe", viewModel.getSuperVisorname())
    }

    @Test
    fun `getSuperVisorSubname returns role when logged in user present`() {
        val loc = LocationEntity(1, "Loc")
        val user = User(
            userId = 1,
            name = "Test",
            userName = "test",
            password = "pwd",
            role = "SUPERVISOR",
            serviceMapId = 1,
            state = loc,
            district = loc,
            block = loc,
            villages = emptyList()
        )
        every { preferenceDao.getLoggedInUser() } returns user
        assertEquals("SUPERVISOR", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `getSuperVisorSubname returns empty string when no logged in user`() {
        every { preferenceDao.getLoggedInUser() } returns null
        assertEquals("", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `getDistrict delegates to preferenceDao`() {
        every { preferenceDao.getSupervisorDistrict() } returns "District1"
        assertEquals("District1", viewModel.getDistrict())
    }

    @Test
    fun `getBlock delegates to preferenceDao`() {
        every { preferenceDao.getSupervisorBlock() } returns "Block1"
        assertEquals("Block1", viewModel.getBlock())
    }

    @Test
    fun `getState delegates to preferenceDao`() {
        every { preferenceDao.getSupervisorState() } returns "State1"
        assertEquals("State1", viewModel.getState())
    }

    @Test
    fun `getSubcenter delegates to preferenceDao`() {
        every { preferenceDao.getSupervisorSubcenter() } returns "Subcenter1"
        assertEquals("Subcenter1", viewModel.getSubcenter())
    }

    @Test
    fun `logout clears preferences and sets navigateToLoginPage`() = runTest {
        advanceUntilIdle()
        viewModel.logout()
        advanceUntilIdle()
        verify(exactly = 1) { preferenceDao.deleteForLogout() }
        verify(exactly = 1) { preferenceDao.setLastSyncedTimeStamp(Konstants.defaultTimeStamp) }
        assertEquals(true, viewModel.navigateToLoginPage.value)
    }

    @Test
    fun `navigateToLoginPageComplete resets flag to false`() {
        viewModel.navigateToLoginPageComplete()
        assertFalse(viewModel.navigateToLoginPage.value == true)
    }
}
