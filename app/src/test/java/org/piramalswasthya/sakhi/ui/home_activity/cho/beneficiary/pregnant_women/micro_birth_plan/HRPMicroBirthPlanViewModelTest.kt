package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.micro_birth_plan

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class HRPMicroBirthPlanViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var hrpRepo: HRPRepo
    @MockK private lateinit var benRepo: BenRepo
    private lateinit var viewModel: HRPMicroBirthPlanViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    private val locationRecord = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(10, "State"),
        district = LocationEntity(20, "District"),
        block = LocationEntity(30, "Block"),
        village = LocationEntity(40, "Village")
    )

    private fun baseBen(lastName: String? = "Doe") = BenRegCache(
        householdId = 1L,
        beneficiaryId = 1L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 1,
        isKid = false,
        isAdult = true,
        locationRecord = locationRecord,
        syncState = SyncState.UNSYNCED,
        isDraft = false
    ).apply {
        firstName = "Jane"
        this.lastName = lastName
        age = 28
        ageUnit = AgeUnit.YEARS
        gender = Gender.FEMALE
        genDetails = BenRegGen(spouseName = "John")
    }

    private fun buildViewModel(): HRPMicroBirthPlanViewModel =
        HRPMicroBirthPlanViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No"); every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        viewModel = HRPMicroBirthPlanViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(HRPMicroBirthPlanViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(HRPMicroBirthPlanViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    @Test
    fun `init populates benName benAgeGender and benDetails when a beneficiary with a last name is found`() {
        coEvery { benRepo.getBenFromId(any()) } returns baseBen(lastName = "Doe")
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Jane Doe", vm.benName.value)
        assertEquals("28 YEARS | FEMALE", vm.benAgeGender.value)
        assertEquals("John", vm.benDetails.spouseName)
    }

    @Test
    fun `init omits the last name when the beneficiary has none`() {
        coEvery { benRepo.getBenFromId(any()) } returns baseBen(lastName = null)
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Jane ", vm.benName.value)
    }

    @Test
    fun `init leaves benName null when no beneficiary is found`() {
        coEvery { benRepo.getBenFromId(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(vm.benName.value)
    }

    @Test
    fun `init sets recordExists true and reuses the existing micro birth plan cache when one is stored`() {
        coEvery { hrpRepo.getMicroBirthPlan(any()) } returns HRPMicroBirthPlanCache(
            benId = 1L,
            nearestSc = "Existing PHC"
        )
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.recordExists.value)
        assertEquals("Existing PHC", vm._microBirthPlanCache.nearestSc)
    }

    @Test
    fun `init sets recordExists false when no micro birth plan cache is stored`() {
        coEvery { hrpRepo.getMicroBirthPlan(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init stores the pregnant assess cache returned by hrpReo`() {
        coEvery { hrpRepo.getPregnantAssess(any()) } returns HRPPregnantAssessCache(
            benId = 1L,
            noOfDeliveries = "2"
        )
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("2", vm._hRPPregnantAssessCache?.noOfDeliveries)
    }

    @Test
    fun `setRecordExists true updates recordExists`() {
        viewModel.setRecordExists(true)
        assertEquals(true, viewModel.recordExists.value)
    }

    @Test
    fun `setRecordExists false updates recordExists`() {
        viewModel.setRecordExists(false)
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(2, 0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `saveForm succeeds and persists the micro birth plan cache when it is initialized`() {
        coEvery { benRepo.getBenFromId(any()) } returns baseBen()
        coEvery { hrpRepo.getMicroBirthPlan(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HRPMicroBirthPlanViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertEquals("U", vm._microBirthPlanCache.processed)
        assertEquals(SyncState.UNSYNCED, vm._microBirthPlanCache.syncState)
        assertEquals(true, vm.isHighRisk)
        coVerify(exactly = 1) { hrpRepo.saveRecord(any<HRPMicroBirthPlanCache>()) }
    }

    @Test
    fun `saveForm sets state to SAVE_FAILED when the micro birth plan cache was never initialized`() {
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { hrpRepo.getMicroBirthPlan(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HRPMicroBirthPlanViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
