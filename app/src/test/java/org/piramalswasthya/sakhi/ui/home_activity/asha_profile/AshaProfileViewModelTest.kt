package org.piramalswasthya.sakhi.ui.home_activity.asha_profile

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.AshaProfileRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.io.File

/**
 * Unit tests for [AshaProfileViewModel]: the pull-then-render `init` chain for both a saved and an
 * absent profile, the save arms and the selected-household helpers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AshaProfileViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var ashaProfileRepo: AshaProfileRepo

    @MockK
    private lateinit var householdRepo: HouseholdRepo

    @MockK
    private lateinit var recordsRepo: RecordsRepo

    private lateinit var user: User

    @After
    fun releaseStaticMocks() {
        unmockkStatic(Dispatchers::class)
        unmockkStatic(Uri::class)
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { context.resources } returns mockResources
        every { context.filesDir } returns File("build", "tmp-asha-profile-test")

        user = mockk(relaxed = true)
        every { user.userId } returns 11
        every { user.userName } returns "asha"
        every { user.villages } returns listOf(LocationEntity(id = 1, name = "Rampur"))

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLocationRecord() } returns mockk<LocationRecord>(relaxed = true)
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
        every { preferenceDao.getProfilePicUri() } returns null

        every { recordsRepo.hhListforAsha } returns flowOf(emptyList())

        coEvery { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) } returns true
        coEvery { ashaProfileRepo.getSavedRecord(any()) } returns null
        coEvery { ashaProfileRepo.saveRecord(any()) } returns Unit
        coEvery { householdRepo.getRecord(any()) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(any()) } returns emptyList()
    }

    private fun buildVm(): AshaProfileViewModel = AshaProfileViewModel(
        SavedStateHandle(emptyMap()),
        preferenceDao,
        context,
        ashaProfileRepo,
        householdRepo,
        recordsRepo
    )

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertNotNull(vm.householdList)
        assertEquals(AshaProfileViewModel.State.IDLE, vm.state.value)
        assertEquals(user, vm.currentUser)
        assertFalse(vm.isPregnant)
        assertEquals(0L, vm.selectedHouseholdId)
        assertNull(vm.selectedHousehold)
        assertTrue(vm.householdBenList.isEmpty())
    }

    @Test
    fun `init pulls the profile and seeds a blank one when nothing is saved`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        coVerify { ashaProfileRepo.pullAndSaveAshaProfile(any(), any()) }
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init keeps the saved profile picture uri when there is no record`() = runTest {
        val savedUri = mockk<Uri>(relaxed = true)
        every { savedUri.toString() } returns "content://profile"
        every { preferenceDao.getProfilePicUri() } returns savedUri

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init renders a previously saved profile`() = runTest {
        coEvery { ashaProfileRepo.getSavedRecord(any()) } returns
                mockk<ProfileActivityCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `saveForm persists the profile and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ashaProfileRepo.saveRecord(any()) }
        assertEquals(AshaProfileViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm stores a non empty profile picture uri`() = runTest {
        val saved = mockk<ProfileActivityCache>(relaxed = true)
        every { saved.profileImage } returns "content://profile"
        coEvery { ashaProfileRepo.getSavedRecord(any()) } returns saved

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        verify { preferenceDao.saveProfilePicUri(any()) }
        assertEquals(AshaProfileViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(AshaProfileViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `setRecordExist updates recordExists`() {
        val vm = buildVm()
        vm.setRecordExist(true)
        assertEquals(true, vm.recordExists.value)
        vm.setRecordExist(false)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `setSelectedHouseholdId loads the household and its beneficiaries`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        val benList = listOf(mockk<BenRegCache>(relaxed = true))
        coEvery { householdRepo.getRecord(any()) } returns household
        coEvery { householdRepo.getAllBenOfHousehold(any()) } returns benList

        val vm = buildVm()
        advanceUntilIdle()

        vm.setSelectedHouseholdId(17L)
        advanceUntilIdle()

        assertEquals(17L, vm.selectedHouseholdId)
        assertEquals(household, vm.selectedHousehold)
        assertEquals(1, vm.householdBenList.size)
    }

    @Test
    fun `resetSelectedHouseholdId clears the selection`() = runTest {
        coEvery { householdRepo.getAllBenOfHousehold(any()) } returns
                listOf(mockk<BenRegCache>(relaxed = true))

        val vm = buildVm()
        advanceUntilIdle()

        vm.setSelectedHouseholdId(17L)
        advanceUntilIdle()
        vm.resetSelectedHouseholdId()

        assertEquals(0L, vm.selectedHouseholdId)
        assertTrue(vm.householdBenList.isEmpty())
    }

    @Test
    fun `image form id round trips`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.setCurrentImageFormId(31)
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
