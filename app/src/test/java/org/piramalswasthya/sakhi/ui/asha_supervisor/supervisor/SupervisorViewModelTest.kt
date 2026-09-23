package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.UserRepo

@OptIn(ExperimentalCoroutinesApi::class)
class SupervisorViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benIdGenDao: BeneficiaryIdsAvailDao

    private lateinit var viewModel: SupervisorViewModel

    @Before
    override fun setUp() {
        super.setUp()
        val mockState = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockState.name } returns "TestState"
        val mockDistrict = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockDistrict.name } returns "TestDistrict"
        val mockBlock = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockBlock.name } returns "TestBlock"
        val mockVillage = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameHindi } returns null
        every { mockVillage.nameAssamese } returns null
        every { mockVillage.nameBangla } returns null
        val mockUser = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.state } returns mockState
        every { mockUser.district } returns mockDistrict
        every { mockUser.block } returns mockBlock
        every { mockUser.villages } returns listOf(mockVillage)

        every { pref.isDevModeEnabled } returns false
        every { pref.getLoggedInUser() } returns mockUser
        every { pref.getLocationRecord() } returns null
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { pref.getProfilePicUri() } returns null
        every { database.benIdGenDao } returns benIdGenDao
        every { benIdGenDao.liveCount() } returns flowOf(0)
        every { userRepo.unProcessedRecordCount } returns flowOf(emptyList())
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = SupervisorViewModel(database, pref, userRepo)
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
    fun `currentUser is not null when logged in`() {
        assertNotNull(viewModel.currentUser)
    }

    @Test
    fun `numBenIdsAvail is not null`() {
        assertNotNull(viewModel.numBenIdsAvail)
    }

    @Test
    fun `locationRecord is accessible`() {
        // locationRecord may be null since pref.getLocationRecord() returns null
        viewModel.locationRecord
    }

    @Test
    fun `currentLanguage is ENGLISH`() {
        assertEquals(Languages.ENGLISH, viewModel.currentLanguage)
    }

    @Test
    fun `initial navigateToLoginPage is false`() {
        assertFalse(viewModel.navigateToLoginPage.value!!)
    }

    @Test
    fun `scope returns viewModelScope`() {
        assertNotNull(viewModel.scope)
    }

    // =====================================================
    // logout() Tests
    // =====================================================

    @Test
    fun `logout sets navigateToLoginPage to true`() = runTest {
        advanceUntilIdle()
        viewModel.logout()
        advanceUntilIdle()
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

    // =====================================================
    // setRange() Tests
    // =====================================================

    @Test
    fun `setRange does not throw`() = runTest {
        viewModel.setRange(1000L, 2000L)
        advanceUntilIdle()
    }

    // =====================================================
    // init userFound / villageDropdownEntries language Tests
    // =====================================================

    @Test
    fun `userName and villageList populated with ENGLISH names after init`() = runTest {
        advanceUntilIdle()
        assertEquals("TestUser", viewModel.userName)
        assertEquals("TestVillage", viewModel.villageList[0])
        assertEquals(org.piramalswasthya.sakhi.ui.service_location_activity.ServiceTypeViewModel.State.SUCCESS, viewModel.state.value)
    }

    @Test
    fun `villageList uses HINDI names when language is HINDI`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        val mockVillage = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameHindi } returns "हिन्दीगाँव"
        val mockUser = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("हिन्दीगाँव", vm.villageList[0])
    }

    @Test
    fun `villageList uses ASSAMESE names when language is ASSAMESE`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        val mockVillage = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameAssamese } returns "অসমীয়াগাঁও"
        val mockUser = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("অসমীয়াগাঁও", vm.villageList[0])
    }

    @Test
    fun `villageList uses BANGLA names when language is BANGLA`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        val mockVillage = mockk<org.piramalswasthya.sakhi.model.LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameBangla } returns "বাংলাগ্রাম"
        val mockUser = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("বাংলাগ্রাম", vm.villageList[0])
    }

    @Test
    fun `init navigates to login page when no logged in user found`() = runTest {
        every { pref.getLoggedInUser() } returns null
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals(true, vm.navigateToLoginPage.value)
    }

    // =====================================================
    // setVillage() / selectedVillageName Tests
    // =====================================================

    @Test
    fun `setVillage updates selectedVillage from user villages`() = runTest {
        advanceUntilIdle()
        viewModel.setVillage(0)
        assertEquals("TestVillage", viewModel.selectedVillage?.name)
    }

    @Test
    fun `selectedVillageName returns english name for ENGLISH language`() = runTest {
        advanceUntilIdle()
        viewModel.setVillage(0)
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        assertEquals("TestVillage", viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName falls back to name for HINDI when nameHindi is null`() = runTest {
        advanceUntilIdle()
        viewModel.setVillage(0)
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        assertEquals("TestVillage", viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName falls back to name for ASSAMESE when nameAssamese is null`() = runTest {
        advanceUntilIdle()
        viewModel.setVillage(0)
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        assertEquals("TestVillage", viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName falls back to name for BANGLA when nameBangla is null`() = runTest {
        advanceUntilIdle()
        viewModel.setVillage(0)
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        assertEquals("TestVillage", viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null when no village selected for ENGLISH`() = runTest {
        advanceUntilIdle()
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null when no village selected for HINDI`() = runTest {
        advanceUntilIdle()
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null when no village selected for ASSAMESE`() = runTest {
        advanceUntilIdle()
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName is null when no village selected for BANGLA`() = runTest {
        advanceUntilIdle()
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        assertNull(viewModel.selectedVillageName)
    }

    @Test
    fun `selectedVillageName returns localized hindi name when present`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameHindi } returns "हिन्दीगाँव"
        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        vm.setVillage(0)
        assertEquals("हिन्दीगाँव", vm.selectedVillageName)
    }

    @Test
    fun `selectedVillageName returns localized assamese name when present`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameAssamese } returns "অসমীয়াগাঁও"
        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        vm.setVillage(0)
        assertEquals("অসমীয়াগাঁও", vm.selectedVillageName)
    }

    @Test
    fun `selectedVillageName returns localized bangla name when present`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameBangla } returns "বাংলাগ্রাম"
        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        vm.setVillage(0)
        assertEquals("বাংলাগ্রাম", vm.selectedVillageName)
    }

    // =====================================================
    // villageDropdownEntries fallback (null localized name) Tests
    // =====================================================

    @Test
    fun `villageDropdownEntries falls back to name when nameHindi is null`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameHindi } returns null
        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("TestVillage", vm.villageList[0])
    }

    @Test
    fun `villageDropdownEntries falls back to name when nameAssamese is null`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.ASSAMESE
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameAssamese } returns null
        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("TestVillage", vm.villageList[0])
    }

    @Test
    fun `villageDropdownEntries falls back to name when nameBangla is null`() = runTest {
        every { pref.getCurrentLanguage() } returns Languages.BANGLA
        val mockVillage = mockk<LocationEntity>(relaxed = true)
        every { mockVillage.name } returns "TestVillage"
        every { mockVillage.nameBangla } returns null
        val mockUser = mockk<User>(relaxed = true)
        every { mockUser.name } returns "TestUser"
        every { mockUser.villages } returns listOf(mockVillage)
        every { pref.getLoggedInUser() } returns mockUser
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("TestVillage", vm.villageList[0])
    }

    // =====================================================
    // getSuperVisorSubname() Tests
    // =====================================================

    @Test
    fun `getSuperVisorSubname returns role of logged in user`() {
        val roleUser = mockk<User>(relaxed = true)
        every { roleUser.role } returns "ASHA_SUPERVISOR"
        every { pref.getLoggedInUser() } returns roleUser
        assertEquals("ASHA_SUPERVISOR", viewModel.getSuperVisorSubname())
    }

    @Test
    fun `getSuperVisorSubname returns empty string when no logged in user`() {
        every { pref.getLoggedInUser() } returns null
        assertEquals("", viewModel.getSuperVisorSubname())
    }

    // =====================================================
    // from / to flow Tests
    // =====================================================

    @Test
    fun `from flow emits initial start value`() = runTest {
        assertNotNull(viewModel.from.first())
    }

    @Test
    fun `to flow emits initial end value`() = runTest {
        assertNotNull(viewModel.to.first())
    }

    // =====================================================
    // profilePicUri Tests
    // =====================================================

    @Test
    fun `profilePicUri getter and setter delegate to pref`() {
        val uri = mockk<android.net.Uri>(relaxed = true)
        every { pref.getProfilePicUri() } returns uri
        assertEquals(uri, viewModel.profilePicUri)

        every { pref.saveProfilePicUri(uri) } returns Unit
        viewModel.profilePicUri = uri
        verify { pref.saveProfilePicUri(uri) }
    }

    // =====================================================
    // unprocessedRecordsCount Tests
    // =====================================================

    @Test
    fun `unprocessedRecordsCount is accessible`() {
        assertNotNull(viewModel.unprocessedRecordsCount)
    }

    @Test
    fun `unprocessedRecordsCount sums counts of records not yet synced`() = runTest {
        every { userRepo.unProcessedRecordCount } returns flowOf(
            listOf(
                SyncStatusCache(1, "A", SyncState.UNSYNCED, 3),
                SyncStatusCache(2, "B", SyncState.SYNCING, 2),
                SyncStatusCache(3, "C", SyncState.SYNCED, 5)
            )
        )
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals(5, vm.unprocessedRecordsCount.value)
    }

    // =====================================================
    // selectedVillage populated from currentLocation Tests
    // =====================================================

    @Test
    fun `selectedVillage populated from currentLocation when locationRecord present`() = runTest {
        val villageEntity = LocationEntity(1, "LocVillage")
        val locationRecord = LocationRecord(
            country = LocationEntity(0, "Country"),
            state = LocationEntity(0, "State"),
            district = LocationEntity(0, "District"),
            block = LocationEntity(0, "Block"),
            village = villageEntity
        )
        every { pref.getLocationRecord() } returns locationRecord
        val vm = SupervisorViewModel(database, pref, userRepo)
        advanceUntilIdle()
        assertEquals("LocVillage", vm.selectedVillage?.name)
    }
}
