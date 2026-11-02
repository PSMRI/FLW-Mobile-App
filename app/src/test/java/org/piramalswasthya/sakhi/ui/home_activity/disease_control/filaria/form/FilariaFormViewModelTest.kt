package org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.form

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.FilariaRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaFormViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var filariaRepo: FilariaRepo
    @MockK private lateinit var benRepo: BenRepo
    private lateinit var viewModel: FilariaFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No"); every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns benRegCache(); coEvery { filariaRepo.getFilariaScreening(any<Long>()) } returns null
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = FilariaFormViewModel(savedStateHandle, preferenceDao, context, filariaRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(FilariaFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(FilariaFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

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
        firstName = "Jane",
        lastName = "Roe",
        gender = Gender.FEMALE,
        age = 25
    )

    @Test
    fun `saveForm saves screening and sets SAVE_SUCCESS when ben exists`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { filariaRepo.getFilariaScreening(1L) } returns null
        val vm = FilariaFormViewModel(savedStateHandle, preferenceDao, context, filariaRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(FilariaFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { filariaRepo.saveFilariaScreening(any()) }
    }

    @Test
    fun `saveForm on existing record saves screening and sets SAVE_SUCCESS`() = runTest {
        val screening = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 10L)
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { filariaRepo.getFilariaScreening(1L) } returns screening
        val vm = FilariaFormViewModel(savedStateHandle, preferenceDao, context, filariaRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(FilariaFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { filariaRepo.saveFilariaScreening(screening) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when saving screening throws`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { filariaRepo.getFilariaScreening(1L) } returns null
        val vm = FilariaFormViewModel(savedStateHandle, preferenceDao, context, filariaRepo, benRepo)
        advanceUntilIdle()
        coEvery { filariaRepo.saveFilariaScreening(any()) } throws RuntimeException("db error")

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(FilariaFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        viewModel.updateListOnValueChanged(1, 0)
        advanceUntilIdle()
    }

    @Test
    fun `getIndexOfDate returns a value without throwing`() {
        val index = viewModel.getIndexOfDate()
        assertNotNull(index)
    }
}
