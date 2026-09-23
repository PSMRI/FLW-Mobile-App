package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_confirmed.from

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
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TBConfirmedViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var tbRepo: TBRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: TBConfirmedViewModel
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
        every { Dispatchers.Default } returns testDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { tbRepo.getTBConfirmed(any()) } returns null
        coEvery { tbRepo.getTBSuspected(any()) } returns null
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()
        viewModel = TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(TBConfirmedViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(TBConfirmedViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    @Test
    fun `longToDateString with null millis returns empty string`() {
        assertEquals("", viewModel.longToDateString(null))
    }

    @Test
    fun `longToDateString with non-null millis returns formatted date string`() {
        val millis = 1700000000000L
        val expected = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(millis))
        assertEquals(expected, viewModel.longToDateString(millis))
    }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(1, 0)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `record exists true sets recordExists and followUpDates`() {
        val tbConfirmedTreatmentCache = TBConfirmedTreatmentCache(benId = 1L)
        coEvery { tbRepo.getTBConfirmed(any()) } returns tbConfirmedTreatmentCache
        coEvery { tbRepo.getTBSuspected(any()) } returns null
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()
        coEvery { benRepo.getBenFromId(any()) } returns null

        val localViewModel =
            TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, localViewModel.recordExists.value)
        assertNotNull(localViewModel.followUpDates.value)
    }

    @Test
    fun `saveForm with invalid default form fails validation and sets SAVE_FAILED`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        viewModel.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TBConfirmedViewModel.State.SAVE_FAILED, viewModel.state.value)
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
        firstName = "Ravi",
        lastName = "Kumar",
        gender = Gender.MALE,
        age = 45
    )

    private fun fillMandatoryFields(vm: TBConfirmedViewModel) {
        val followUpDateValue = vm.formList.value.first { it.id == 2 }.value
        vm.formList.value.first { it.id == 1 }.value = "opt0"
        vm.formList.value.first { it.id == 4 }.value = followUpDateValue
    }

    // TBConfirmedViewModel.saveForm() nests withContext(Dispatchers.Default) inside
    // withContext(Dispatchers.IO). Combining two mocked-static dispatcher levels like that
    // makes MockK's static Dispatchers mock throw ("no answer found for
    // CoroutineDispatcher(child of static Dispatchers).fold(...)") while merging the nested
    // coroutine contexts, regardless of what Dispatchers.Default is stubbed to return. The only
    // reliable way around it (without touching production code) is to drop the static mock
    // before saveForm() actually executes its nested dispatch, letting it run on the real IO/
    // Default dispatchers, and then wait in real time for the terminal state to be posted
    // (LiveData.postValue is synchronous here thanks to InstantTaskExecutorRule).
    private fun TestScope.saveFormAndAwaitResult(vm: TBConfirmedViewModel, timeoutMs: Long = 5000) {
        unmockkStatic(Dispatchers::class)
        vm.saveForm()
        advanceUntilIdle()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (vm.state.value != TBConfirmedViewModel.State.SAVE_SUCCESS &&
            vm.state.value != TBConfirmedViewModel.State.SAVE_FAILED &&
            System.currentTimeMillis() < deadline
        ) {
            Thread.sleep(5)
        }
    }

    private fun existingDeathReadyRecord(): TBConfirmedTreatmentCache {
        val now = System.currentTimeMillis()
        val start = now - 200L * 24 * 60 * 60 * 1000
        return TBConfirmedTreatmentCache(
            benId = 1L,
            regimenType = "opt0",
            treatmentStartDate = start,
            followUpDate = start,
            treatmentCompleted = true,
            treatmentOutcome = "Death"
        )
    }

    private fun todayDateString(): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())

    private fun daysAgoDateString(days: Long): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.US)
            .format(Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000))

    @Test
    fun `saveForm posts SAVE_FAILED when the repository throws while saving`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { tbRepo.getTBConfirmed(any()) } returns null
        coEvery { tbRepo.getTBSuspected(any()) } returns null
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()
        coEvery { tbRepo.saveTBConfirmed(any()) } throws RuntimeException("boom")
        val vm = TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()
        fillMandatoryFields(vm)

        saveFormAndAwaitResult(vm)

        assertEquals(TBConfirmedViewModel.State.SAVE_FAILED, vm.state.value)
        coVerify { tbRepo.saveTBConfirmed(any()) }
    }

    @Test
    fun `saveForm posts SAVE_SUCCESS and saves the record when validation passes`() = runTest {
        advanceUntilIdle()
        fillMandatoryFields(viewModel)

        saveFormAndAwaitResult(viewModel)

        assertEquals(TBConfirmedViewModel.State.SAVE_SUCCESS, viewModel.state.value)
        coVerify { tbRepo.saveTBConfirmed(any()) }
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveForm updates beneficiary death fields and saves record when treatment outcome is Death`() =
        runTest {
            every { mockResources.getStringArray(R.array.tb_treatment_outcomes) } returns
                    arrayOf("Cured", "Failed", "Lost to Follow-up (LFU)", "Death")
            val ben = benRegCache()
            coEvery { benRepo.getBenFromId(1L) } returns ben
            coEvery { tbRepo.getTBConfirmed(any()) } returns existingDeathReadyRecord()
            coEvery { tbRepo.getTBSuspected(any()) } returns null
            coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()

            val vm = TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
            advanceUntilIdle()

            vm.formList.value.first { it.id == 4 }.value = daysAgoDateString(30)
            vm.formList.value.first { it.id == 11 }.value = daysAgoDateString(5)
            vm.formList.value.first { it.id == 12 }.value = "opt0"
            vm.formList.value.first { it.id == 13 }.value = "TB complications"

            saveFormAndAwaitResult(vm)

            assertEquals(TBConfirmedViewModel.State.SAVE_SUCCESS, vm.state.value)
            assertTrue(ben.isDeath)
            assertEquals("Death", ben.isDeathValue)
            assertEquals("TB complications", ben.reasonOfDeath)
            assertEquals("opt0", ben.placeOfDeath)
            assertEquals(daysAgoDateString(5), ben.dateOfDeath)
            assertEquals("U", ben.processed)
            assertEquals(SyncState.UNSYNCED, ben.syncState)
            coVerify { benRepo.updateRecord(ben) }
            coVerify { tbRepo.saveTBConfirmed(any()) }
        }

    @Test
    fun `saveForm keeps processed as N when beneficiary record was already marked N`() = runTest {
        every { mockResources.getStringArray(R.array.tb_treatment_outcomes) } returns
                arrayOf("Cured", "Failed", "Lost to Follow-up (LFU)", "Death")
        val ben = benRegCache().apply { processed = "N" }
        coEvery { benRepo.getBenFromId(1L) } returns ben
        coEvery { tbRepo.getTBConfirmed(any()) } returns existingDeathReadyRecord()
        coEvery { tbRepo.getTBSuspected(any()) } returns null
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()

        val vm = TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()
        vm.formList.value.first { it.id == 4 }.value = daysAgoDateString(30)

        saveFormAndAwaitResult(vm)

        assertEquals(TBConfirmedViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertEquals("N", ben.processed)
        coVerify { benRepo.updateRecord(ben) }
    }
}
