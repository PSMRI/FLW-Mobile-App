package org.piramalswasthya.sakhi.ui.home_activity.disease_control.aes_je.form

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
import org.piramalswasthya.sakhi.utils.HelperUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.repositories.AESRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo

@OptIn(ExperimentalCoroutinesApi::class)
class AESFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var aesRepo: AESRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: AESFormViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "benId" to 1L
    ))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.w(any(), any<String>()) } returns 0
        val realDefaultDispatcher = Dispatchers.Default
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns realDefaultDispatcher

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns ""

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { context.resources } returns mockResources
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { aesRepo.getAESScreening(any()) } returns null
        viewModel = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.benId)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(AESFormViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(AESFormViewModel.State.IDLE, viewModel.state.value)
    }

    // =====================================================
    // setRecordExist() Tests
    // =====================================================

    @Test
    fun `setRecordExist updates recordExists`() {
        viewModel.setRecordExist(true)
        assertEquals(true, viewModel.recordExists.value)
    }

    @Test
    fun `setRecordExist false updates recordExists`() {
        viewModel.setRecordExist(false)
        assertEquals(false, viewModel.recordExists.value)
    }

    // =====================================================
    // isDeath Tests
    // =====================================================

    @Test
    fun `isDeath is initially false`() {
        assertEquals(false, viewModel.isDeath)
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
        firstName = "Sam",
        lastName = "Lee",
        gender = Gender.MALE,
        age = 40
    )

    @Test
    fun `init with existing ben sets benName and benAgeGender when no record exists`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns null

        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals("Sam Lee", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init with existing AES record sets recordExists true`() = runTest {
        val screening = AESScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Alive")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening

        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertTrue(vm.recordExists.value!!)
        assertFalse(vm.isBeneficaryStatusDeath.value!!)
    }

    @Test
    fun `init with existing AES record marked Death sets isBeneficaryStatusDeath true`() = runTest {
        val screening = AESScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Death")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening

        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertTrue(vm.isBeneficaryStatusDeath.value!!)
    }

    @Test
    fun `init when ben is not found leaves recordExists false`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { aesRepo.getAESScreening(1L) } returns null

        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
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
        val screening = AESScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Alive")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening
        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AESFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { aesRepo.saveAESScreening(any()) }
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    private fun mockDeathArrays() {
        every { mockResources.getStringArray(R.array.benificary_case_status_kalaazar) } returns arrayOf("Alive", "Death", "Unknown")
        every { mockResources.getStringArray(R.array.death_place) } returns arrayOf("Home", "Hospital")
        every { mockResources.getStringArray(R.array.reason_death) } returns arrayOf("Fever", "Accident")
        every { mockResources.getStringArray(R.array.benificary_case_status) } returns arrayOf("Alive", "Death", "Fever")
    }

    private fun deathScreening(): AESScreeningCache = AESScreeningCache(
        benId = 1L,
        houseHoldDetailsId = 10L,
        beneficiaryStatus = "Death",
        dateOfDeath = 1700000000000L,
        placeOfDeath = "Home",
        reasonForDeath = "Fever"
    )

    @Test
    fun `saveForm on death record with processed N keeps processed as N`() = runTest {
        mockDeathArrays()
        val screening = deathScreening()
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening
        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        val benRecord = benRegCache().copy(processed = "N")
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns benRecord

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AESFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertTrue(vm.isDeath)
        assertEquals("N", benRecord.processed)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 1) { benRepo.updateRecord(benRecord) }
    }

    @Test
    fun `saveForm on death record when maternal ben not found still saves screening`() = runTest {
        mockDeathArrays()
        val screening = deathScreening()
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening
        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        coEvery { maternalHealthRepo.getBenFromId(1L) } returns null

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AESFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertTrue(vm.isDeath)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
        coVerify { aesRepo.saveAESScreening(any()) }
    }

    @Test
    fun `saveForm on death record with processed not N sets processed to U and populates death fields`() = runTest {
        mockDeathArrays()
        val screening = deathScreening()
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening
        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        val benRecord = benRegCache()
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns benRecord

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AESFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertTrue(vm.isDeath)
        assertTrue(benRecord.isDeath)
        assertEquals("Death", benRecord.isDeathValue)
        assertEquals("U", benRecord.processed)
        assertEquals("Fever", benRecord.reasonOfDeath)
        assertEquals(2, benRecord.reasonOfDeathId)
        assertEquals("Home", benRecord.placeOfDeath)
        assertEquals(0, benRecord.placeOfDeathId)
        assertEquals(SyncState.UNSYNCED, benRecord.syncState)
        assertNotNull(benRecord.dateOfDeath)
        assertTrue(benRecord.dateOfDeath!!.isNotEmpty())
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 1) { benRepo.updateRecord(benRecord) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when death branch benRepo update throws`() = runTest {
        mockDeathArrays()
        val screening = deathScreening()
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening
        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        val benRecord = benRegCache()
        coEvery { maternalHealthRepo.getBenFromId(1L) } returns benRecord
        coEvery { benRepo.updateRecord(any()) } throws RuntimeException("update failed")

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AESFormViewModel.State.SAVE_FAILED, vm.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { aesRepo.saveAESScreening(any()) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when saving screening throws`() = runTest {
        val screening = AESScreeningCache(benId = 1L, houseHoldDetailsId = 10L, beneficiaryStatus = "Alive")
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache()
        coEvery { aesRepo.getAESScreening(1L) } returns screening
        val vm = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()
        coEvery { aesRepo.saveAESScreening(any()) } throws RuntimeException("db error")

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AESFormViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
