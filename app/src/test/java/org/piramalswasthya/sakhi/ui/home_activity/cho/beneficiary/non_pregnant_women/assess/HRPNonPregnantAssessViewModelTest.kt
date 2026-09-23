package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.assess

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
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantAssessViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var hrpRepo: HRPRepo
    @MockK private lateinit var benRepo: BenRepo
    private lateinit var viewModel: HRPNonPregnantAssessViewModel
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
        viewModel = HRPNonPregnantAssessViewModel(savedStateHandle, preferenceDao, context, hrpRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(HRPNonPregnantAssessViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(HRPNonPregnantAssessViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    private fun buildVm(benId: Long = 1L): HRPNonPregnantAssessViewModel =
        HRPNonPregnantAssessViewModel(
            SavedStateHandle(mapOf("benId" to benId)),
            preferenceDao,
            context,
            hrpRepo,
            benRepo
        )

    private fun mockBen(
        firstName: String = "Anita",
        lastName: String? = "Sharma",
        beneficiaryId: Long = 1L
    ): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns firstName
        every { ben.lastName } returns lastName
        every { ben.age } returns 28
        every { ben.ageUnit } returns AgeUnit.YEARS
        every { ben.gender } returns Gender.FEMALE
        every { ben.beneficiaryId } returns beneficiaryId
        return ben
    }

    private fun fullAssess(benId: Long = 1L) = HRPNonPregnantAssessCache(
        benId = benId,
        noOfDeliveries = "No",
        timeLessThan18m = "No",
        heightShort = "No",
        age = "No",
        misCarriage = "No",
        homeDelivery = "No",
        medicalIssues = "No",
        pastCSection = "No"
    )

    @Test
    fun `init populates name and age gender when a beneficiary with a last name is found`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns mockBen()

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Anita Sharma", vm.benName.value)
        assertEquals("28 YEARS | FEMALE", vm.benAgeGender.value)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns mockBen(lastName = null)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Anita ", vm.benName.value)
    }

    @Test
    fun `init reports no saved record when none exists`() = runTest {
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
        assertEquals(false, vm.fullyFilled.value)
    }

    @Test
    fun `init reports the record as fully filled when every field is present`() = runTest {
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns fullAssess()

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals(true, vm.fullyFilled.value)
    }

    @Test
    fun `init reports the record as not fully filled when a required field is missing`() = runTest {
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns fullAssess().apply { noOfDeliveries = null }

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals(false, vm.fullyFilled.value)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `saveForm succeeds and skips the beneficiary update when no beneficiary was loaded`() = runTest {
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns fullAssess()

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { hrpRepo.saveRecord(any<HRPNonPregnantAssessCache>()) }
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
        assertEquals(HRPNonPregnantAssessViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm updates the beneficiary and succeeds when a beneficiary was loaded`() = runTest {
        val ben = mockBen()
        every { ben.isHrpStatus } returns false
        every { ben.processed } returns "N"
        coEvery { benRepo.getBenFromId(any()) } returns ben

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { hrpRepo.saveRecord(any<HRPNonPregnantAssessCache>()) }
        coVerify { benRepo.updateRecord(ben) }
        assertEquals(HRPNonPregnantAssessViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when the repository throws`() = runTest {
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns fullAssess()
        coEvery { hrpRepo.saveRecord(any<HRPNonPregnantAssessCache>()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPNonPregnantAssessViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `updateBen flags high risk status and normalizes processed when not already set`() {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.isHrpStatus } returns false
        every { ben.processed } returns "X"

        viewModel.updateBen(ben)

        verify { ben.isHrpStatus = any() }
        verify { ben.processed = "U" }
        verify { ben.syncState = org.piramalswasthya.sakhi.database.room.SyncState.UNSYNCED }
    }

    @Test
    fun `updateBen leaves isHrpStatus and processed untouched when already set`() {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.isHrpStatus } returns true
        every { ben.processed } returns "N"

        viewModel.updateBen(ben)

        unmockkStatic(Dispatchers::class)
        verify(exactly = 0) { ben.isHrpStatus = any() }
        unmockkStatic(Dispatchers::class)
        verify(exactly = 0) { ben.processed = any() }
        verify { ben.syncState = org.piramalswasthya.sakhi.database.room.SyncState.UNSYNCED }
    }

    @Test
    fun `index helpers delegate to the dataset`() {
        assertNotNull(viewModel.getIndexOfChildLabel())
        assertNotNull(viewModel.getIndexOfPhysicalObservationLabel())
        assertNotNull(viewModel.getIndexOfObstetricHistoryLabel())
    }
}
