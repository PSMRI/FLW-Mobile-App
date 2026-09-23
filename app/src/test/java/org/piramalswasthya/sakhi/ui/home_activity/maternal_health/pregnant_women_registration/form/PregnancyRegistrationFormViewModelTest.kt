package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_women_registration.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PregnancyRegistrationFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var hrpRepo: HRPRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var householdRepo: HouseholdRepo
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var viewModel: PregnancyRegistrationFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns baseUser()
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns null
        coEvery { hrpRepo.getPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { ecrRepo.getLatestEctByBenId(any()) } returns null
        viewModel = PregnancyRegistrationFormViewModel(savedStateHandle, preferenceDao, context, maternalHealthRepo, ecrRepo, hrpRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(PregnancyRegistrationFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }

    private fun baseUser() = User(
        userId = 1,
        name = "Asha One",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 1,
        state = LocationEntity(10, "State"),
        district = LocationEntity(20, "District"),
        block = LocationEntity(30, "Block"),
        villages = emptyList()
    )

    private fun baseBen() = BenRegCache(
        householdId = 1L,
        beneficiaryId = 1L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 1,
        isKid = false,
        isAdult = true,
        locationRecord = LocationRecord(
            country = LocationEntity(1, "India"),
            state = LocationEntity(10, "State"),
            district = LocationEntity(20, "District"),
            block = LocationEntity(30, "Block"),
            village = LocationEntity(40, "Village")
        ),
        syncState = SyncState.UNSYNCED,
        isDraft = false
    ).apply {
        firstName = "Jane"
        lastName = "Doe"
        age = 28
        dob = System.currentTimeMillis()
    }

    private fun buildViewModel(): PregnancyRegistrationFormViewModel =
        PregnancyRegistrationFormViewModel(savedStateHandle, preferenceDao, context, maternalHealthRepo, ecrRepo, hrpRepo, benRepo)

    @Test
    fun `setRecordExist false updates recordExists to false`() {
        viewModel.setRecordExist(false)
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `getIndexOfChildLabel returns an int`() { assertNotNull(viewModel.getIndexOfChildLabel()) }

    @Test
    fun `getIndexOfPhysicalObservationLabel returns an int`() { assertNotNull(viewModel.getIndexOfPhysicalObservationLabel()) }

    @Test
    fun `getIndexOfObstetricHistoryLabel returns an int`() { assertNotNull(viewModel.getIndexOfObstetricHistoryLabel()) }

    @Test
    fun `getIndexOfEdd returns an int`() { assertNotNull(viewModel.getIndexOfEdd()) }

    @Test
    fun `getIndexofLmp returns an int`() { assertNotNull(viewModel.getIndexofLmp()) }

    @Test
    fun `getIndexOfWeeksOfPregnancy returns an int`() { assertNotNull(viewModel.getIndexOfWeeksOfPregnancy()) }

    @Test
    fun `getIndexOfPastIllness returns an int`() { assertNotNull(viewModel.getIndexOfPastIllness()) }

    @Test
    fun `getIndexOfHRP returns an int`() { assertNotNull(viewModel.getIndexOfHRP()) }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(1, 0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `saveForm fails when pregnancyRegistrationForm was never initialized`() {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PregnancyRegistrationFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm succeeds when ben exists and repos succeed`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns baseBen()
        coEvery { maternalHealthRepo.persistRegisterRecord(any()) } returns Unit
        coEvery { hrpRepo.saveRecord(any<org.piramalswasthya.sakhi.model.HRPPregnantAssessCache>()) } returns Unit
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PregnancyRegistrationFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `recordExists is true when maternalHealthRepo returns an existing registration`() {
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns baseBen()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns
            org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache(
                benId = 1L,
                createdBy = "asha1",
                updatedBy = "asha1",
                syncState = SyncState.UNSYNCED
            )
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `init populates benName and benAgeGender and recordExists is false when no saved registration exists`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns baseBen()
        coEvery { hrpRepo.getPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null
        coEvery { ecrRepo.getLatestEctByBenId(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.benName.value)
        assertNotNull(vm.benAgeGender.value)
        assertEquals(false, vm.recordExists.value)
    }
}
