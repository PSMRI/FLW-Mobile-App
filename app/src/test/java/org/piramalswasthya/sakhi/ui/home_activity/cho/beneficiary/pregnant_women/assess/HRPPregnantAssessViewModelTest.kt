package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.assess

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
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class HRPPregnantAssessViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var hrpRepo: HRPRepo
    @MockK private lateinit var benRepo: BenRepo
    private lateinit var viewModel: HRPPregnantAssessViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No"); every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        val realDefaultDispatcher = Dispatchers.Default
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns realDefaultDispatcher
        viewModel = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(HRPPregnantAssessViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(HRPPregnantAssessViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    private fun fullyFilledAssessCache(): HRPPregnantAssessCache {
        val cache = mockk<HRPPregnantAssessCache>(relaxed = true)
        every { cache.noOfDeliveries } returns "Yes"
        every { cache.timeLessThan18m } returns "Yes"
        every { cache.heightShort } returns "Yes"
        every { cache.age } returns "Yes"
        every { cache.rhNegative } returns "Yes"
        every { cache.homeDelivery } returns "Yes"
        every { cache.badObstetric } returns "Yes"
        every { cache.multiplePregnancy } returns "Yes"
        return cache
    }

    private fun mockBen(
        firstName: String = "Anita",
        lastName: String? = "Sharma",
        age: Int = 28,
        ageUnit: AgeUnit = AgeUnit.YEARS,
        gender: Gender = Gender.FEMALE,
        beneficiaryId: Long = 42L
    ): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns firstName
        every { ben.lastName } returns lastName
        every { ben.age } returns age
        every { ben.ageUnit } returns ageUnit
        every { ben.gender } returns gender
        every { ben.beneficiaryId } returns beneficiaryId
        return ben
    }

    @Test
    fun `init populates ben name and age gender and marks fully filled when ben and complete assess record are found`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns mockBen()
        coEvery { hrpRepo.getPregnantAssess(1L) } returns fullyFilledAssessCache()

        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        assertEquals("Anita Sharma", vm.benName.value)
        assertEquals("28 YEARS | FEMALE", vm.benAgeGender.value)
        assertTrue(vm.recordExists.value == true)
        assertTrue(vm.fullyFilled.value == true)
    }

    @Test
    fun `init blanks out last name when ben has no last name`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns mockBen(lastName = null)
        coEvery { hrpRepo.getPregnantAssess(1L) } returns null

        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        assertEquals("Anita ", vm.benName.value)
        assertFalse(vm.recordExists.value == true)
        assertFalse(vm.fullyFilled.value == true)
    }

    @Test
    fun `init marks fully filled false when an assess field is missing`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        val partial = fullyFilledAssessCache()
        every { partial.rhNegative } returns null
        coEvery { hrpRepo.getPregnantAssess(1L) } returns partial

        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        assertNull(vm.benName.value)
        assertTrue(vm.recordExists.value == true)
        assertFalse(vm.fullyFilled.value == true)
    }

    @Test
    fun `init marks record does not exist when neither ben nor assess record are found`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { hrpRepo.getPregnantAssess(1L) } returns null

        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        assertFalse(vm.recordExists.value == true)
        assertFalse(vm.fullyFilled.value == true)
        assertNull(vm.benName.value)
    }

    @Test
    fun `saveForm succeeds and updates ben when a ben and an assess record were loaded`() = runTest {
        val ben = mockBen()
        coEvery { benRepo.getBenFromId(1L) } returns ben
        coEvery { hrpRepo.getPregnantAssess(1L) } returns fullyFilledAssessCache()
        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPPregnantAssessViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { hrpRepo.saveRecord(any<HRPPregnantAssessCache>()) }
        coVerify { benRepo.updateRecord(ben) }
    }

    @Test
    fun `saveForm succeeds without updating ben when no ben was loaded`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { hrpRepo.getPregnantAssess(1L) } returns fullyFilledAssessCache()
        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPPregnantAssessViewModel.State.SAVE_SUCCESS, vm.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveForm fails when the assess record was never initialized`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { hrpRepo.getPregnantAssess(1L) } returns null
        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPPregnantAssessViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `saveForm fails when saveRecord throws`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { hrpRepo.getPregnantAssess(1L) } returns fullyFilledAssessCache()
        coEvery { hrpRepo.saveRecord(any<HRPPregnantAssessCache>()) } throws RuntimeException("boom")
        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPPregnantAssessViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `updateListOnValueChanged drives dataset updateList without throwing`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { hrpRepo.getPregnantAssess(1L) } returns null
        val vm = HRPPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
