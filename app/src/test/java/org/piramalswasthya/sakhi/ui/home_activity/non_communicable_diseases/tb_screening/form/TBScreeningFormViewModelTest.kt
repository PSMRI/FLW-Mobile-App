package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.database.shared_preferences.ReferralStatusManager
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class TBScreeningFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var tbRepo: TBRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var referralStatusManager: ReferralStatusManager
    @MockK private lateinit var ncdReferalRepo: NcdReferalRepo

    private lateinit var viewModel: TBScreeningFormViewModel
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
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { tbRepo.getTBScreening(any()) } returns null
        viewModel = TBScreeningFormViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo, referralStatusManager, ncdReferalRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(TBScreeningFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(TBScreeningFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

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
        firstName = "John"
        lastName = "Doe"
        age = 30
    }

    private fun buildViewModel(): TBScreeningFormViewModel =
        TBScreeningFormViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo, referralStatusManager, ncdReferalRepo)

    @Test
    fun `markReferralCompleted then isReferralAlreadyDone returns true for that type`() {
        viewModel.markReferralCompleted(TBScreeningFormViewModel.ReferralType.TB)
        assertTrue(viewModel.isReferralAlreadyDone(TBScreeningFormViewModel.ReferralType.TB))
    }

    @Test
    fun `isReferralAlreadyDone returns false for a type not marked`() {
        viewModel.markReferralCompleted(TBScreeningFormViewModel.ReferralType.TB)
        assertEquals(false, viewModel.isReferralAlreadyDone(TBScreeningFormViewModel.ReferralType.LEPROSY))
    }

    @Test
    fun `addReferral adds new referral and marks as referred`() {
        val referral = ReferalCache(benId = 1L, referralReason = "TB Suspected", syncState = SyncState.UNSYNCED)
        viewModel.addReferral(referral)
        assertEquals(1, viewModel.referralList.value?.size)
        assertEquals(referral, viewModel.referralCache)
        verify { referralStatusManager.markAsReferred(1L, TBScreeningFormViewModel.ReferralType.TB.name) }
    }

    @Test
    fun `addReferral ignores duplicate referral with same reason`() {
        val referral1 = ReferalCache(benId = 1L, referralReason = "TB Suspected", syncState = SyncState.UNSYNCED)
        val referral2 = ReferalCache(benId = 1L, referralReason = "TB Suspected", syncState = SyncState.UNSYNCED)
        viewModel.addReferral(referral1)
        viewModel.addReferral(referral2)
        assertEquals(1, viewModel.referralList.value?.size)
    }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(1, 0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `saveForm fails when tbScreeningCache was never initialized`() {
        viewModel.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TBScreeningFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm succeeds when ben exists and save does not throw`() {
        coEvery { benRepo.getBenFromId(any()) } returns baseBen()
        coEvery { tbRepo.getTBScreening(any()) } returns null
        coEvery { tbRepo.saveTBScreening(any()) } returns Unit
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        vm.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TBScreeningFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveFormDirectlyfromCbac succeeds when ben exists`() {
        coEvery { benRepo.getBenFromId(any()) } returns baseBen()
        coEvery { tbRepo.getTBScreening(any()) } returns null
        coEvery { tbRepo.saveTBScreening(any()) } returns Unit
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.saveFormDirectlyfromCbac()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TBScreeningFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveFormDirectlyfromCbac fails when ben does not exist`() {
        coEvery { benRepo.getBenFromId(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.saveFormDirectlyfromCbac()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TBScreeningFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `getAlerts sets referToHwcFacility without crashing`() {
        testDispatcher.scheduler.advanceUntilIdle()
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        viewModel.getAlerts()
        assertNotNull(viewModel)
    }

    @Test
    fun `getIndexOfDate returns an int`() {
        testDispatcher.scheduler.advanceUntilIdle()
        val index = viewModel.getIndexOfDate()
        assertTrue(index >= -1)
    }

    @Test
    fun `addReferral dedups by reason then grows list for a new reason`() {
        val referral1 = ReferalCache(benId = 1L, referralReason = "TB Suspected", syncState = SyncState.UNSYNCED)
        val referral2 = ReferalCache(benId = 1L, referralReason = "TB Suspected", syncState = SyncState.UNSYNCED)
        viewModel.addReferral(referral1)
        viewModel.addReferral(referral2)
        assertEquals(1, viewModel.referralList.value?.size)
        assertEquals(referral1, viewModel.referralCache)

        val referral3 = ReferalCache(benId = 1L, referralReason = "Leprosy Suspected", syncState = SyncState.UNSYNCED)
        viewModel.addReferral(referral3)
        assertEquals(2, viewModel.referralList.value?.size)
    }

    @Test
    fun `recordExists is true when tbRepo returns an existing screening`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { benRepo.getBenFromId(any()) } returns baseBen()
        coEvery { tbRepo.getTBScreening(any()) } returns TBScreeningCache(benId = 1L)
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.recordExists.value)
    }
}
