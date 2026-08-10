package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PmsmaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var pmsmaRepo: PmsmaRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var viewModel: PmsmaViewModel
    private val savedStateHandle = SavedStateHandle(
        mapOf(
            "benId" to 1L,
            "hhId" to 1L,
            "visitNumber" to 1,
            "lastItemClick" to false
        )
    )

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns baseUser()
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null
        coEvery { benRepo.getHousehold(any()) } returns null
        coEvery { pmsmaRepo.getSavedRecord(any(), any()) } returns null
        coEvery { pmsmaRepo.getLastPmsmaVisit(any()) } returns null
        coEvery { pmsmaRepo.getActiveAncCountForBenIds(any()) } returns 0
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null
        coEvery { maternalHealthRepo.getLatestAncRecord(any()) } returns null
        viewModel = PmsmaViewModel(savedStateHandle, context, pmsmaRepo, benRepo, maternalHealthRepo, preferenceDao)
    }

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

    private val locationRecord = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(10, "State"),
        district = LocationEntity(20, "District"),
        block = LocationEntity(30, "Block"),
        village = LocationEntity(40, "Village")
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
        locationRecord = locationRecord,
        syncState = SyncState.UNSYNCED,
        isDraft = false
    ).apply {
        firstName = "Jane"
        lastName = "Doe"
        age = 28
    }

    private fun baseHousehold() = HouseholdCache(
        householdId = 1L,
        ashaId = 1,
        locationRecord = locationRecord,
        processed = "N",
        isDraft = false
    )

    private fun basePwr() = PregnantWomanRegistrationCache(
        benId = 1L,
        createdBy = "asha1",
        updatedBy = "asha1",
        syncState = SyncState.UNSYNCED
    )

    private fun baseAnc() = PregnantWomanAncCache(
        benId = 1L,
        visitNumber = 1,
        createdBy = "asha1",
        updatedBy = "asha1",
        syncState = SyncState.UNSYNCED,
        frontFilePath = null,
        backFilePath = null
    )

    private fun buildViewModel(): PmsmaViewModel =
        PmsmaViewModel(savedStateHandle, context, pmsmaRepo, benRepo, maternalHealthRepo, preferenceDao)

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(PmsmaViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }

    @Test
    fun `onHighRiskSelected true sets showHighRiskAlert`() {
        viewModel.onHighRiskSelected(true)
        assertEquals(true, viewModel.showHighRiskAlert.value)
    }

    @Test
    fun `onHighRiskSelected false does not set showHighRiskAlert`() {
        viewModel.onHighRiskSelected(false)
        assertNotEquals(true, viewModel.showHighRiskAlert.value)
    }

    @Test
    fun `setRecordExist true updates recordExists`() {
        viewModel.setRecordExist(true)
        assertEquals(true, viewModel.recordExists.value)
    }

    @Test
    fun `setRecordExist false updates recordExists`() {
        viewModel.setRecordExist(false)
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(1, 0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `init populates benName and benAgeGender when ben and household exist`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns baseBen()
        coEvery { benRepo.getHousehold(any()) } returns baseHousehold()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns basePwr()
        coEvery { maternalHealthRepo.getLatestAncRecord(any()) } returns baseAnc()
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.benName.value)
        assertNotNull(vm.benAgeGender.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `recordExists is true when pmsmaRepo returns an existing visit`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns baseBen()
        coEvery { benRepo.getHousehold(any()) } returns baseHousehold()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns basePwr()
        coEvery { pmsmaRepo.getSavedRecord(any(), any()) } returns org.piramalswasthya.sakhi.model.PMSMACache(
            benId = 1L,
            visitNumber = 1,
            visitDate = System.currentTimeMillis(),
            isActive = true,
            createdBy = "asha1",
            updatedBy = "asha1",
            syncState = SyncState.UNSYNCED
        )
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `submitForm success updates state to SUCCESS`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns baseBen()
        coEvery { benRepo.getHousehold(any()) } returns baseHousehold()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns basePwr()
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { pmsmaRepo.savePmsmaData(any()) } returns true
        vm.submitForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PmsmaViewModel.State.SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm failure updates state to FAIL`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns baseBen()
        coEvery { benRepo.getHousehold(any()) } returns baseHousehold()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns basePwr()
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { pmsmaRepo.savePmsmaData(any()) } returns false
        vm.submitForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PmsmaViewModel.State.FAIL, vm.state.value)
    }
}
