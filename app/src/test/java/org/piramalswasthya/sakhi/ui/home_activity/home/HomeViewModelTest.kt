package org.piramalswasthya.sakhi.ui.home_activity.home

import android.content.Context
import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
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
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
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
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
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

    // =====================================================
    // saveProfilePicFromGallery() Tests
    // =====================================================

    private fun buildUser(userId: Int = 42) = User(
        userId = userId,
        name = "Asha Worker",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 1,
        state = LocationEntity(1, "State"),
        district = LocationEntity(2, "District"),
        block = LocationEntity(3, "Block"),
        villages = emptyList()
    )

    private fun newViewModelWithUser(user: User): HomeViewModel {
        every { pref.getLoggedInUser() } returns user
        every { pref.getProfilePicUri() } returns mockk(relaxed = true)
        return HomeViewModel(database, pref, userRepo, ashaProfileRepo)
    }

    @Test
    fun `saveProfilePicFromGallery does nothing when no user is logged in`() = runTest {
        val context = mockk<Context>(relaxed = true)
        val galleryUri = mockk<Uri>(relaxed = true)

        viewModel.saveProfilePicFromGallery(context, galleryUri)
        advanceUntilIdle()

        unmockkStatic(Dispatchers::class)
        verify(exactly = 0) { pref.saveProfilePicUri(any()) }
    }

    @Test
    fun `saveProfilePicFromGallery does not persist uri when image save fails`() = runTest {
        val user = buildUser()
        every { profileDao.getProfileActivityById(user.userId.toLong()) } returns null
        mockkObject(ImageUtils)
        coEvery { ImageUtils.saveBenImageFromCameraToStorage(any(), any(), any()) } returns null
        coEvery { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) } returns true
        val vm = newViewModelWithUser(user)
        advanceUntilIdle()

        val context = mockk<Context>(relaxed = true)
        val galleryUri = mockk<Uri>(relaxed = true)

        vm.saveProfilePicFromGallery(context, galleryUri)
        advanceUntilIdle()

        unmockkStatic(Dispatchers::class)
        verify(exactly = 0) { pref.saveProfilePicUri(any()) }
        unmockkObject(ImageUtils)
    }

    @Test
    fun `saveProfilePicFromGallery pulls profile and persists uri when profile is missing`() = runTest {
        val user = buildUser()
        every { profileDao.getProfileActivityById(user.userId.toLong()) } returns null
        mockkObject(ImageUtils)
        coEvery { ImageUtils.saveBenImageFromCameraToStorage(any(), any(), any()) } returns "/data/42.jpeg"
        coEvery { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) } returns true
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        val vm = newViewModelWithUser(user)
        advanceUntilIdle()

        val context = mockk<Context>(relaxed = true)
        val galleryUri = mockk<Uri>(relaxed = true)

        vm.saveProfilePicFromGallery(context, galleryUri)
        advanceUntilIdle()

        coVerify { ashaProfileRepo.pullAndSaveAshaProfile(user) }
        verify { pref.saveProfilePicUri(any()) }
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { profileDao.insert(any()) }
        unmockkStatic(Uri::class)
        unmockkObject(ImageUtils)
    }

    @Test
    fun `saveProfilePicFromGallery updates existing profile and pushes it to server`() = runTest {
        val user = buildUser()
        val profile = ProfileActivityCache(id = user.userId.toLong())
        every { profileDao.getProfileActivityById(user.userId.toLong()) } returns profile
        mockkObject(ImageUtils)
        coEvery { ImageUtils.saveBenImageFromCameraToStorage(any(), any(), any()) } returns "/data/42.jpeg"
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns "base64data"
        coEvery { ashaProfileRepo.postDataToAmritServer(any()) } returns true
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        val vm = newViewModelWithUser(user)
        advanceUntilIdle()

        val context = mockk<Context>(relaxed = true)
        val galleryUri = mockk<Uri>(relaxed = true)

        vm.saveProfilePicFromGallery(context, galleryUri)
        advanceUntilIdle()

        verify { pref.saveProfilePicUri(any()) }
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) }
        coVerify { profileDao.insert(profile) }
        coVerify { ashaProfileRepo.postDataToAmritServer(match { it.profileImage == "base64data" }) }
        assertEquals("/data/42.jpeg", profile.profileImage)
        unmockkStatic(Uri::class)
        unmockkObject(ImageUtils)
    }

    @Test
    fun `saveProfilePicFromGallery falls back to empty base64 image when encoding fails`() = runTest {
        val user = buildUser()
        val profile = ProfileActivityCache(id = user.userId.toLong())
        every { profileDao.getProfileActivityById(user.userId.toLong()) } returns profile
        mockkObject(ImageUtils)
        coEvery { ImageUtils.saveBenImageFromCameraToStorage(any(), any(), any()) } returns "/data/42.jpeg"
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns null
        coEvery { ashaProfileRepo.postDataToAmritServer(any()) } returns true
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        val vm = newViewModelWithUser(user)
        advanceUntilIdle()

        val context = mockk<Context>(relaxed = true)
        val galleryUri = mockk<Uri>(relaxed = true)

        vm.saveProfilePicFromGallery(context, galleryUri)
        advanceUntilIdle()

        coVerify { ashaProfileRepo.postDataToAmritServer(match { it.profileImage == "" }) }
        unmockkStatic(Uri::class)
        unmockkObject(ImageUtils)
    }

    // =====================================================
    // init { } profile-pic restore Tests (second inner launch)
    // =====================================================

    @Test
    fun `init restores profile pic uri from local db when profile image already present`() = runTest {
        val user = buildUser()
        val profile = ProfileActivityCache(id = user.userId.toLong(), profileImage = "/local/42.jpeg")
        every { pref.getLoggedInUser() } returns user
        every { pref.getProfilePicUri() } returns null
        every { profileDao.getProfileActivityById(user.userId.toLong()) } returns profile
        mockkStatic(Uri::class)
        val restoredUri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns restoredUri
        every { pref.saveProfilePicUri(restoredUri) } returns Unit

        val vm = HomeViewModel(database, pref, userRepo, ashaProfileRepo)
        advanceUntilIdle()

        verify { pref.saveProfilePicUri(restoredUri) }
        assertEquals(restoredUri, vm.restoredProfilePicUri.value)
        coVerify(exactly = 0) { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) }
        unmockkStatic(Dispatchers::class)
        unmockkStatic(Uri::class)
    }

    @Test
    fun `init pulls asha profile from server when local profile image is missing`() = runTest {
        val user = buildUser()
        val restoredProfile = ProfileActivityCache(id = user.userId.toLong(), profileImage = "/server/42.jpeg")
        every { pref.getLoggedInUser() } returns user
        every { pref.getProfilePicUri() } returns null
        every {
            profileDao.getProfileActivityById(user.userId.toLong())
        } returnsMany listOf(null, restoredProfile)
        coEvery { ashaProfileRepo.pullAndSaveAshaProfile(user, any()) } returns true
        mockkStatic(Uri::class)
        val restoredUri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns restoredUri
        every { pref.saveProfilePicUri(restoredUri) } returns Unit

        val vm = HomeViewModel(database, pref, userRepo, ashaProfileRepo)
        advanceUntilIdle()

        coVerify { ashaProfileRepo.pullAndSaveAshaProfile(user, any()) }
        verify { pref.saveProfilePicUri(restoredUri) }
        assertEquals(restoredUri, vm.restoredProfilePicUri.value)
        unmockkStatic(Dispatchers::class)
        unmockkStatic(Uri::class)
    }

    @Test
    fun `init leaves restoredProfilePicUri unset when no profile image is ever found`() = runTest {
        val user = buildUser()
        val emptyProfile = ProfileActivityCache(id = user.userId.toLong(), profileImage = "")
        every { pref.getLoggedInUser() } returns user
        every { pref.getProfilePicUri() } returns null
        every { profileDao.getProfileActivityById(user.userId.toLong()) } returns emptyProfile
        coEvery { ashaProfileRepo.pullAndSaveAshaProfile(user, any()) } returns false

        val vm = HomeViewModel(database, pref, userRepo, ashaProfileRepo)
        advanceUntilIdle()

        coVerify { ashaProfileRepo.pullAndSaveAshaProfile(user, any()) }
        assertNull(vm.restoredProfilePicUri.value)
        verify(exactly = 0) { pref.saveProfilePicUri(any()) }
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `init does not touch profile pic restore when a profile pic uri already exists`() = runTest {
        val user = buildUser()
        every { pref.getLoggedInUser() } returns user
        every { pref.getProfilePicUri() } returns mockk(relaxed = true)

        HomeViewModel(database, pref, userRepo, ashaProfileRepo)
        advanceUntilIdle()

        coVerify(exactly = 0) { profileDao.getProfileActivityById(any()) }
        coVerify(exactly = 0) { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) }
        unmockkStatic(Dispatchers::class)
    }
}
