package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_suspected.form

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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class TBSuspectedViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var tbRepo: TBRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: TBSuspectedViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

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
        every { mockResources.getStringArray(R.array.type_of_tb_case) } returns
            arrayOf("New case of TB", "Previously treated TB case", "DR-TB case")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(TBSuspectedViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(TBSuspectedViewModel.State.IDLE, viewModel.state.value) }
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
        firstName = "Amit",
        lastName = "Shah",
        gender = Gender.MALE,
        age = 35
    )

    @Test
    fun `init with existing ben sets benName and benAgeGender when no record exists`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { tbRepo.getTBSuspected(1L) } returns null

        val vm = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()

        assertEquals("Amit Shah", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init with existing TB suspected record sets recordExists true`() = runTest {
        val suspected = TBSuspectedCache(benId = 1L)
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { tbRepo.getTBSuspected(1L) } returns suspected

        val vm = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `init when ben is not found leaves recordExists false`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { tbRepo.getTBSuspected(1L) } returns null

        val vm = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        viewModel.updateListOnValueChanged(1, 0)
        advanceUntilIdle()
    }

    @Test
    fun `getAlerts delegates to the dataset without throwing`() = runTest {
        advanceUntilIdle()
        viewModel.getAlerts()
    }

    @Test
    fun `saveForm saves the suspected record and sets SAVE_SUCCESS when ben exists`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { tbRepo.getTBSuspected(1L) } returns null
        val vm = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(TBSuspectedViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { tbRepo.saveTBSuspected(any()) }
    }

    @Test
    fun `saveForm on existing record saves and sets SAVE_SUCCESS`() = runTest {
        val suspected = TBSuspectedCache(benId = 1L)
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { tbRepo.getTBSuspected(1L) } returns suspected
        val vm = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(TBSuspectedViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { tbRepo.saveTBSuspected(suspected) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when saving throws`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { tbRepo.getTBSuspected(1L) } returns null
        val vm = TBSuspectedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        advanceUntilIdle()
        coEvery { tbRepo.saveTBSuspected(any()) } throws RuntimeException("db error")

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(TBSuspectedViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
