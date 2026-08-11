package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.form

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
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleTrackingFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var hrpRepo: HRPRepo

    private lateinit var viewModel: EligibleCoupleTrackingFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    private fun mockBen(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.beneficiaryId } returns 1L
        every { ben.firstName } returns "Meena"
        every { ben.lastName } returns "Devi"
        every { ben.age } returns 25
        every { ben.ageUnit } returns null
        every { ben.gender } returns null
        every { ben.genDetails } returns null
        every { ben.regDate } returns 0L
        return ben
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { context.resources } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        coEvery { benRepo.getBenFromId(any()) } returns null
        // Baseline keeps ecrRepo.getBenFromId non-null: the view model's init does
        // `ecrRepo.getBenFromId(benId)!!.benRegId` whenever no tracking record is found yet,
        // so a null beneficiary combined with no existing record would NPE inside init (uncaught).
        coEvery { ecrRepo.getBenFromId(any()) } returns mockBen()
        coEvery { ecrRepo.getLatestEctByBenId(any()) } returns null
        coEvery { ecrRepo.getAllAntraDoses(any()) } returns emptyList()
        coEvery { ecrRepo.getEct(any(), any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { benRepo.getChildCountForBen(any()) } returns 0
        viewModel = EligibleCoupleTrackingFormViewModel(savedStateHandle, preferenceDao, context, ecrRepo, benRepo, hrpRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(EligibleCoupleTrackingFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(EligibleCoupleTrackingFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    @Test
    fun `init populates ben name and age gender when beneficiary found`() = runTest {
        advanceUntilIdle()
        assertEquals("Meena Devi", viewModel.benName.value)
        assertNotNull(viewModel.benAgeGender.value)
        assertEquals(false, viewModel.recordExists.value)
        assertEquals(false, viewModel.showAntraSection.value)
    }

    @Test
    fun `init loads antra dose list`() = runTest {
        coEvery { ecrRepo.getAllAntraDoses(any()) } returns listOf(mockk(relaxed = true), mockk(relaxed = true))
        advanceUntilIdle()
        assertEquals(2, viewModel.allDoseList.value?.size)
    }

    @Test
    fun `init marks record exists and detects antra method when tracking record found`() = runTest {
        val existing = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { existing.methodOfContraception } returns "ANTRA Injection"
        coEvery { ecrRepo.getEct(any(), any()) } returns existing
        viewModel = EligibleCoupleTrackingFormViewModel(savedStateHandle, preferenceDao, context, ecrRepo, benRepo, hrpRepo)

        advanceUntilIdle()

        assertEquals(true, viewModel.recordExists.value)
        assertEquals(true, viewModel.showAntraSection.value)
    }

    @Test
    fun `init does not flag antra section for other contraception methods`() = runTest {
        val existing = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { existing.methodOfContraception } returns "Condom"
        coEvery { ecrRepo.getEct(any(), any()) } returns existing
        viewModel = EligibleCoupleTrackingFormViewModel(savedStateHandle, preferenceDao, context, ecrRepo, benRepo, hrpRepo)

        advanceUntilIdle()

        assertEquals(true, viewModel.recordExists.value)
        assertEquals(false, viewModel.showAntraSection.value)
    }

    @Test
    fun `init sets up create-mode page when ecr profile exists but no tracking record`() = runTest {
        coEvery { ecrRepo.getSavedRecord(any()) } returns mockk(relaxed = true)
        coEvery { benRepo.getChildCountForBen(any()) } returns 2

        advanceUntilIdle()

        assertNotNull(viewModel.formList)
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `saveForm succeeds and posts SAVE_SUCCESS`() = runTest {
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm posts SAVE_FAILED when repo save throws`() = runTest {
        advanceUntilIdle()
        coEvery { ecrRepo.saveEct(any()) } throws RuntimeException("boom")

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(EligibleCoupleTrackingFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw for unknown form id`() = runTest {
        advanceUntilIdle()
        viewModel.updateListOnValueChanged(-999, 0)
        advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `loadAllDoses posts an empty list by default`() = runTest {
        viewModel.loadAllDoses(1L)
        advanceUntilIdle()
        assertEquals(emptyList<Any>(), viewModel.allDoseList.value)
    }

    @Test
    fun `getIndexOfMPA and delivery discharge delegators return dataset indices`() {
        assertNotNull(viewModel.getIndexOfMPA())
        assertNotNull(viewModel.getIndexDeliveryDischargeSummary1())
        assertNotNull(viewModel.getIndexDeliveryDischargeSummary2())
        assertNotNull(viewModel.getIndexOfIsPregnant())
    }

    @Test
    fun `setCurrentDocumentFormId and getDocumentFormId round trip`() {
        viewModel.setCurrentDocumentFormId(7)
        assertEquals(7, viewModel.getDocumentFormId())
    }
}
