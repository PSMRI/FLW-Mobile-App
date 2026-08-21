package org.piramalswasthya.sakhi.ui.home_activity.disease_control.kala_azar.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import io.mockk.coVerify
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.KalaAzarRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class KalaAzarFormViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var kalaAzarRepo: KalaAzarRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    private lateinit var viewModel: KalaAzarFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false; every { Log.w(any(), any<String>()) } returns 0
        val realDefaultDispatcher = Dispatchers.Default
        mockkStatic(Dispatchers::class); every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns realDefaultDispatcher
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }; every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { context.resources } returns mockResources
        coEvery { benRepo.getBenFromId(any()) } returns null; coEvery { kalaAzarRepo.getKalaAzarScreening(any<Long>()) } returns null
        viewModel = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(KalaAzarFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(KalaAzarFormViewModel.State.IDLE, viewModel.state.value) }
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
    fun `init with existing ben sets benName and benAgeGender when no record exists`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns null

        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals("Jane Roe", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init with existing kala azar record sets recordExists true`() = runTest {
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Alive")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening

        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertTrue(vm.recordExists.value!!)
        assertFalse(vm.isBeneficaryStatusDeath.value!!)
    }

    @Test
    fun `init with existing kala azar record marked Death sets isBeneficaryStatusDeath true`() = runTest {
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Death")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening

        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertTrue(vm.isBeneficaryStatusDeath.value!!)
    }

    @Test
    fun `init when ben is not found leaves recordExists false`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns null

        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
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

    @Test
    fun `saveForm on non death record saves screening and sets SAVE_SUCCESS`() = runTest {
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Alive")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening
        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(KalaAzarFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { kalaAzarRepo.saveKalaAzarScreening(any()) }
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveForm on death record updates ben record and sets SAVE_SUCCESS`() = runTest {
        every { mockResources.getStringArray(R.array.benificary_case_status_kalaazar) } returns arrayOf("Alive", "Death")
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Death")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening
        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        val benRecord = benRegCache().copy(processed = "Y")
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns benRecord

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(KalaAzarFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertTrue(benRecord.isDeath)
        assertEquals("U", benRecord.processed)
        assertEquals(SyncState.UNSYNCED, benRecord.syncState)
        coVerify { benRepo.updateRecord(benRecord) }
        coVerify { kalaAzarRepo.saveKalaAzarScreening(any()) }
    }

    @Test
    fun `saveForm on death record with processed N keeps processed as N`() = runTest {
        every { mockResources.getStringArray(R.array.benificary_case_status_kalaazar) } returns arrayOf("Alive", "Death")
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Death")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening
        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        val benRecord = benRegCache().copy(processed = "N")
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns benRecord

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(KalaAzarFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertEquals("N", benRecord.processed)
    }

    @Test
    fun `saveForm on death record when maternal ben not found still saves screening`() = runTest {
        every { mockResources.getStringArray(R.array.benificary_case_status_kalaazar) } returns arrayOf("Alive", "Death")
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Death")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening
        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        coEvery { maternalHealthRepo.getBenFromId(1L) } returns null

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(KalaAzarFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
        coVerify { kalaAzarRepo.saveKalaAzarScreening(any()) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when saving screening throws`() = runTest {
        val screening = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Alive")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { kalaAzarRepo.getKalaAzarScreening(1L) } returns screening
        val vm = KalaAzarFormViewModel(savedStateHandle, preferenceDao, context, kalaAzarRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        coEvery { kalaAzarRepo.saveKalaAzarScreening(any()) } throws RuntimeException("db error")

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(KalaAzarFormViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
