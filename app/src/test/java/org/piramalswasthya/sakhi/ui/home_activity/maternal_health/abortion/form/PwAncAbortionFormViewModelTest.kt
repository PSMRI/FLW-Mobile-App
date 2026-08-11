package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.form

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
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PwAncAbortionFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: PwAncAbortionFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    private fun mockBen(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.beneficiaryId } returns 1L
        every { ben.firstName } returns "Sita"
        every { ben.lastName } returns "Sharma"
        every { ben.age } returns 28
        every { ben.ageUnit } returns null
        every { ben.gender } returns null
        every { ben.genDetails } returns null
        return ben
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.w(any(), any<String>()) } returns 0
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
        // The view model reads its beneficiary through maternalHealthRepo, not benRepo.
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns null
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns null
        // dataset.setUpPage() force-unwraps this value (`lastAnc!!`), so a null last-visit ANC
        // record would NPE inside init (uncaught) whenever no prior ANC visit exists.
        coEvery { maternalHealthRepo.getLastVisitRecordANC(any()) } returns mockk(relaxed = true)
        viewModel = PwAncAbortionFormViewModel(savedStateHandle, preferenceDao, ecrRepo, context, maternalHealthRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(PwAncAbortionFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }

    @Test
    fun `init tolerates missing beneficiary and marks record as not existing`() = runTest {
        advanceUntilIdle()
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `init populates ben name and age gender when beneficiary found`() = runTest {
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns mockBen()
        viewModel = PwAncAbortionFormViewModel(savedStateHandle, preferenceDao, ecrRepo, context, maternalHealthRepo, benRepo)
        advanceUntilIdle()
        assertEquals("Sita Sharma", viewModel.benName.value)
        assertNotNull(viewModel.benAgeGender.value)
    }

    @Test
    fun `init marks record exists when termination-done-by is present on saved anc`() = runTest {
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        every { saved.terminationDoneBy } returns "ANM"
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns saved
        viewModel = PwAncAbortionFormViewModel(savedStateHandle, preferenceDao, ecrRepo, context, maternalHealthRepo, benRepo)

        advanceUntilIdle()

        assertEquals(true, viewModel.recordExists.value)
    }

    @Test
    fun `init marks record as not existing when saved anc has no termination info`() = runTest {
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        every { saved.terminationDoneBy } returns null
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns saved

        advanceUntilIdle()

        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `saveForm fails when anc cache was never initialized`() = runTest {
        advanceUntilIdle()
        viewModel.saveForm()
        advanceUntilIdle()
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm succeeds when beneficiary exists`() = runTest {
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns mockBen()
        viewModel = PwAncAbortionFormViewModel(savedStateHandle, preferenceDao, ecrRepo, context, maternalHealthRepo, benRepo)
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm posts SAVE_FAILED when persistAncRecord throws`() = runTest {
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns mockBen()
        coEvery { maternalHealthRepo.persistAncRecord(any()) } throws RuntimeException("boom")
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(PwAncAbortionFormViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw for unknown form id`() = runTest {
        viewModel.updateListOnValueChanged(-999, 0)
        advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `getIndexOfAbortionDischarge1 and 2 and weeksOfPregnancy delegate to dataset`() {
        assertNotNull(viewModel.getIndexOfAbortionDischarge1())
        assertNotNull(viewModel.getIndexOfAbortionDischarge2())
        assertNotNull(viewModel.getIndexOfWeeksOfPregnancy())
    }

    @Test
    fun `setCurrentDocumentFormId and getDocumentFormId round trip`() {
        viewModel.setCurrentDocumentFormId(11)
        assertEquals(11, viewModel.getDocumentFormId())
    }
}
