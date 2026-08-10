package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyFormViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var leprosyRepo: LeprosyRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    private lateinit var viewModel: LeprosyFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    private fun mockBen(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.beneficiaryId } returns 1L
        every { ben.householdId } returns 3L
        every { ben.firstName } returns "Rajesh"
        every { ben.lastName } returns null
        every { ben.age } returns 40
        every { ben.ageUnit } returns null
        every { ben.gender } returns null
        return ben
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Dispatchers::class); every { Dispatchers.IO } returns testDispatcher
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { context.resources } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }; every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        coEvery { benRepo.getBenFromId(any()) } returns null; coEvery { leprosyRepo.getLeprosyScreening(any()) } returns null
        viewModel = LeprosyFormViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(LeprosyFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(LeprosyFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    @Test
    fun `init tolerates missing beneficiary and screening record`() = runTest {
        advanceUntilIdle()
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `init populates ben name when beneficiary found`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns mockBen()
        val vm = LeprosyFormViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()
        assertEquals("Rajesh ", vm.benName.value)
        assertNotNull(vm.benAgeGender.value)
    }

    @Test
    fun `init loads existing screening record and flags death status`() = runTest {
        val screening = mockk<LeprosyScreeningCache>(relaxed = true)
        every { screening.beneficiaryStatus } returns "Death"
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screening

        val vm = LeprosyFormViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals(true, vm.isBeneficaryStatusDeath.value)
    }

    @Test
    fun `init does not flag death status for alive beneficiary`() = runTest {
        val screening = mockk<LeprosyScreeningCache>(relaxed = true)
        every { screening.beneficiaryStatus } returns "Alive"
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screening

        val vm = LeprosyFormViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals(false, vm.isBeneficaryStatusDeath.value)
    }

    @Test
    fun `saveForm fails when screening cache was never initialized`() = runTest {
        viewModel.saveForm()
        advanceUntilIdle()
        assertEquals(LeprosyFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm succeeds when beneficiary exists`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns mockBen()
        val vm = LeprosyFormViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm posts SAVE_FAILED when leprosyRepo save throws`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns mockBen()
        coEvery { leprosyRepo.saveLeprosyScreening(any()) } throws RuntimeException("boom")
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveLeprosySuspectedFormDirectlyfromCbac fails when beneficiary is missing`() = runTest {
        viewModel.saveLeprosySuspectedFormDirectlyfromCbac()
        advanceUntilIdle()
        assertEquals(LeprosyFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveLeprosySuspectedFormDirectlyfromCbac succeeds when beneficiary exists`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns mockBen()
        advanceUntilIdle()

        viewModel.saveLeprosySuspectedFormDirectlyfromCbac()
        advanceUntilIdle()

        assertEquals(LeprosyFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw for unknown form id`() = runTest {
        viewModel.updateListOnValueChanged(-999, 0)
        advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `getIndexOfDate delegates to the dataset`() {
        assertNotNull(viewModel.getIndexOfDate())
    }
}
