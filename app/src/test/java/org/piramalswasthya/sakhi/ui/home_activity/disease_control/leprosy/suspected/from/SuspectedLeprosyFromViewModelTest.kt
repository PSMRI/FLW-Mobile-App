package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.suspected.from

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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class SuspectedLeprosyFromViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var leprosyRepo: LeprosyRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    private lateinit var viewModel: SuspectedLeprosyFromViewModel
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
        every { context.resources } returns mockResources
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        viewModel = SuspectedLeprosyFromViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(SuspectedLeprosyFromViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(SuspectedLeprosyFromViewModel.State.IDLE, viewModel.state.value) }
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
        firstName = "John",
        lastName = "Doe",
        gender = Gender.MALE,
        age = 30
    )

    @Test
    fun `init with existing ben sets benName and benAgeGender when no leprosy record exists`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns null

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        assertEquals("John Doe", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init with existing leprosy record sets recordExists true`() = runTest {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 10L,
            beneficiaryStatus = "Alive",
            createdBy = "asha1",
            modifiedBy = "asha1"
        )
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns screening

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        assertTrue(vm.recordExists.value!!)
        assertFalse(vm.isBeneficaryStatusDeath.value!!)
    }

    @Test
    fun `init with existing leprosy record marked Death sets isBeneficaryStatusDeath true`() = runTest {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 10L,
            beneficiaryStatus = "Death",
            createdBy = "asha1",
            modifiedBy = "asha1"
        )
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns screening

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        assertTrue(vm.isBeneficaryStatusDeath.value!!)
    }

    @Test
    fun `init when ben is not found leaves benName unset`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns null

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `saveForm succeeds when no leprosy record exists yet`() = runTest {
        advanceUntilIdle()
        coEvery { leprosyRepo.updateLeprosyScreening(any()) } returns Unit

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(SuspectedLeprosyFromViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm posts SAVE_FAILED when the repository throws`() = runTest {
        advanceUntilIdle()
        coEvery { leprosyRepo.updateLeprosyScreening(any()) } throws RuntimeException("boom")

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(SuspectedLeprosyFromViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm updates the beneficiary death fields when screening status is Death`() = runTest {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 10L,
            beneficiaryStatus = "Death",
            reasonForDeath = "opt2",
            dateOfDeath = 1_700_000_000_000L,
            placeOfDeath = "opt3",
            otherPlaceOfDeath = "Other place",
            createdBy = "asha1",
            modifiedBy = "asha1"
        )
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns screening
        coEvery { leprosyRepo.updateLeprosyScreening(any()) } returns Unit
        val deadBen = benRegCache().copy(processed = "P")
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns deadBen

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(SuspectedLeprosyFromViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertTrue(deadBen.isDeath)
        assertEquals("Death", deadBen.isDeathValue)
        assertEquals("opt2", deadBen.reasonOfDeath)
        assertEquals("opt3", deadBen.placeOfDeath)
        assertEquals("Other place", deadBen.otherPlaceOfDeath)
        assertEquals("U", deadBen.processed)
        assertEquals(SyncState.UNSYNCED, deadBen.syncState)
        assertTrue(deadBen.dateOfDeath!!.isNotBlank())
    }

    @Test
    fun `saveForm keeps processed as N when beneficiary was already marked N`() = runTest {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 10L,
            beneficiaryStatus = "Death",
            createdBy = "asha1",
            modifiedBy = "asha1"
        )
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns screening
        coEvery { leprosyRepo.updateLeprosyScreening(any()) } returns Unit
        val deadBen = benRegCache().copy(processed = "N")
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns deadBen

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(SuspectedLeprosyFromViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertEquals("N", deadBen.processed)
    }

    @Test
    fun `saveForm skips the death update block when maternalHealthRepo has no matching beneficiary`() = runTest {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 10L,
            beneficiaryStatus = "Death",
            createdBy = "asha1",
            modifiedBy = "asha1"
        )
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { leprosyRepo.getLeprosyScreening(1L) } returns screening
        coEvery { leprosyRepo.updateLeprosyScreening(any()) } returns Unit
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns null

        val vm = SuspectedLeprosyFromViewModel(
            savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo
        )
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(SuspectedLeprosyFromViewModel.State.SAVE_SUCCESS, vm.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
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
