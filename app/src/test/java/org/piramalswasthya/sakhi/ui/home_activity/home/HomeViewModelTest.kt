package org.piramalswasthya.sakhi.ui.home_activity.home

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.repositories.AshaProfileRepo
import org.piramalswasthya.sakhi.repositories.UserRepo

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var ashaProfileRepo: AshaProfileRepo
    @MockK private lateinit var benIdGenDao: BeneficiaryIdsAvailDao
    @MockK private lateinit var profileDao: ProfileDao

    private lateinit var viewModel: HomeViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { pref.isDevModeEnabled } returns false
        every { pref.getLoggedInUser() } returns null
        every { pref.getLocationRecord() } returns null
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getProfilePicUri() } returns null
        every { database.benIdGenDao } returns benIdGenDao
        every { database.profileDao } returns profileDao
        every { benIdGenDao.liveCount() } returns flowOf(0)
        every { userRepo.unProcessedRecordCount } returns flowOf(emptyList())
        viewModel = HomeViewModel(database, pref, userRepo, ashaProfileRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial devModeEnabled is false`() {
        assertFalse(viewModel.devModeEnabled.value!!)
    }

    @Test
    fun `currentUser is null when no user logged in`() {
        assertNull(viewModel.currentUser)
    }

    @Test
    fun `numBenIdsAvail is not null`() {
        assertNotNull(viewModel.numBenIdsAvail)
    }

    @Test
    fun `locationRecord is null when no location set`() {
        assertNull(viewModel.locationRecord)
    }

    @Test
    fun `currentLanguage returns ENGLISH`() {
        assertEquals(Languages.ENGLISH, viewModel.currentLanguage)
    }

    @Test
    fun `initial navigateToLoginPage is false`() {
        assertFalse(viewModel.navigateToLoginPage.value!!)
    }

    // =====================================================
    // logout() Tests
    // =====================================================

    @Test
    fun `logout calls deleteLoginCred and sets navigateToLoginPage`() = runTest {
        viewModel.logout()
        advanceUntilIdle()
        verify { pref.deleteLoginCred() }
        assertEquals(true, viewModel.navigateToLoginPage.value)
    }

    // =====================================================
    // navigateToLoginPageComplete() Tests
    // =====================================================

    @Test
    fun `navigateToLoginPageComplete resets flag`() {
        viewModel.navigateToLoginPageComplete()
        assertFalse(viewModel.navigateToLoginPage.value!!)
    }

    // =====================================================
    // setDevMode() / getDebMode() Tests
    // =====================================================

    @Test
    fun `setDevMode updates devModeState`() {
        every { pref.isDevModeEnabled = true } returns Unit
        viewModel.setDevMode(true)
        assertEquals(true, viewModel.devModeEnabled.value)
    }

    @Test
    fun `getDebMode returns pref value`() {
        every { pref.isDevModeEnabled } returns true
        assertEquals(true, viewModel.getDebMode())
    }
}
