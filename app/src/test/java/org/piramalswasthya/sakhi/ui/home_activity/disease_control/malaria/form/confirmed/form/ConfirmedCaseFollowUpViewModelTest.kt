package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.confirmed.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenWithMalariaConfirmedDomain
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmedCaseFollowUpViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var malariaRepo: MalariaRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var recordsRepo: RecordsRepo

    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))
    private lateinit var viewModel: ConfirmedCaseFollowUpViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { recordsRepo.malariaConfirmedCasesList } returns flowOf(emptyList())
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { malariaRepo.getMalariaConfirmed(any()) } returns null
        viewModel = ConfirmedCaseFollowUpViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(ConfirmedCaseFollowUpViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `benId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.benId)
    }

    private fun locationRecord(): LocationRecord {
        val entity = LocationEntity(id = 1, name = "test")
        return LocationRecord(
            country = entity, state = entity, district = entity, block = entity, village = entity
        )
    }

    private fun benRegCache(): BenRegCache = BenRegCache(
        householdId = 10L,
        beneficiaryId = 1L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 5,
        isKid = false,
        isAdult = true,
        locationRecord = locationRecord(),
        syncState = SyncState.SYNCED,
        isDraft = false,
        firstName = "Nina",
        lastName = "Verma",
        gender = Gender.FEMALE,
        age = 28
    )

    @Test
    fun `init populates the form when the records list has no matching case`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        every { recordsRepo.malariaConfirmedCasesList } returns flowOf(emptyList())

        val vm = ConfirmedCaseFollowUpViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
        advanceUntilIdle()

        assertEquals("Nina Verma", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
        assertNotNull(vm.formList.value)
    }

    @Test
    fun `init picks the matching case slide test name from the records list`() = runTest {
        val existing = MalariaConfirmedCasesCache(benId = 1L, houseHoldDetailsId = 10L)
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { malariaRepo.getMalariaConfirmed(1L) } returns existing

        val benBasic = mockk<BenBasicDomain>(relaxed = true)
        every { benBasic.benId } returns 1L
        val matchingItem = mockk<BenWithMalariaConfirmedDomain>(relaxed = true)
        every { matchingItem.ben } returns benBasic
        every { matchingItem.slideTestName } returns "Pf"
        every { recordsRepo.malariaConfirmedCasesList } returns flowOf(listOf(matchingItem))

        val vm = ConfirmedCaseFollowUpViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertNotNull(vm.formList.value)
    }

    @Test
    fun `saveForm saves the confirmed case and sets SAVE_SUCCESS`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { malariaRepo.saveMalariaConfirmed(any()) } returns Unit
        val vm = ConfirmedCaseFollowUpViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(ConfirmedCaseFollowUpViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { malariaRepo.saveMalariaConfirmed(any()) }
    }

    @Test
    fun `saveForm posts SAVE_FAILED when the repository throws`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { malariaRepo.saveMalariaConfirmed(any()) } throws RuntimeException("boom")
        val vm = ConfirmedCaseFollowUpViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(ConfirmedCaseFollowUpViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
