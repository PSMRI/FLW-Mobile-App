package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.form

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
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
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

    private fun mockAncCache(
        isPaiucdId: Int = 0,
        isYesOrNo: Boolean = false,
        dateSterilisation: Long? = null,
        isAborted: Boolean = false,
        terminationDoneBy: String? = null
    ): PregnantWomanAncCache {
        val cache = mockk<PregnantWomanAncCache>(relaxed = true)
        every { cache.isPaiucdId } returns isPaiucdId
        every { cache.isYesOrNo } returns isYesOrNo
        every { cache.dateSterilisation } returns dateSterilisation
        every { cache.isAborted } returns isAborted
        every { cache.terminationDoneBy } returns terminationDoneBy
        return cache
    }

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
    fun `saveForm marks beneficiary sterilised and saves ect record when dateSterilisation is present`() = runTest {
        val savedAnc = mockAncCache(isPaiucdId = 2, isYesOrNo = true, dateSterilisation = 5000L)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns "N"
        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns ben
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        coVerify { benRepo.updateRecord(ben) }
        verify(exactly = 0) { ben.processed = "U" }
        coVerify { ecrRepo.saveEct(any()) }
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm marks unprocessed beneficiary as updated-pending sync during sterilisation save`() = runTest {
        val savedAnc = mockAncCache(isPaiucdId = 2, isYesOrNo = true, dateSterilisation = 5000L)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns "U"
        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns ben
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        verify { ben.processed = "U" }
        coVerify { benRepo.updateRecord(ben) }
    }

    @Test
    fun `saveForm skips ect save when dateSterilisation is null`() = runTest {
        val savedAnc = mockAncCache(isPaiucdId = 2, isYesOrNo = true, dateSterilisation = null)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        val ben = mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns ben
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        coVerify(exactly = 0) { ecrRepo.saveEct(any()) }
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm skips benRepo update when no matching beneficiary found for sterilisation`() = runTest {
        val savedAnc = mockAncCache(isPaiucdId = 2, isYesOrNo = true, dateSterilisation = 5000L)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns null
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
        coVerify(exactly = 0) { ecrRepo.saveEct(any()) }
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm deactivates registration and inactive anc records when isAborted is true`() = runTest {
        val savedAnc = mockAncCache(isAborted = true)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        val registrationRecord = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { registrationRecord.processed } returns "U"
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns registrationRecord
        val inactiveRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { inactiveRecord.processed } returns "N"
        coEvery { maternalHealthRepo.getAllInActiveAncRecords(any()) } returns listOf(inactiveRecord)
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        verify { registrationRecord.active = false }
        verify { registrationRecord.processed = "U" }
        coVerify { maternalHealthRepo.persistRegisterRecord(registrationRecord) }
        verify { inactiveRecord.isActive = false }
        coVerify { maternalHealthRepo.updateAncRecord(arrayOf(inactiveRecord)) }
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm does not re-flag already-processed registration or anc records when isAborted is true`() = runTest {
        val savedAnc = mockAncCache(isAborted = true)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        val registrationRecord = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { registrationRecord.processed } returns "N"
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns registrationRecord
        val inactiveRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { inactiveRecord.processed } returns "N"
        coEvery { maternalHealthRepo.getAllInActiveAncRecords(any()) } returns listOf(inactiveRecord)
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        verify(exactly = 0) { registrationRecord.processed = "U" }
        verify(exactly = 0) { inactiveRecord.processed = "U" }
        coVerify { maternalHealthRepo.persistRegisterRecord(registrationRecord) }
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm skips registration deactivation when no registration record exists`() = runTest {
        val savedAnc = mockAncCache(isAborted = true)
        coEvery { maternalHealthRepo.getSavedRecordANC(any()) } returns savedAnc
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null
        coEvery { maternalHealthRepo.getAllInActiveAncRecords(any()) } returns emptyList()
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        coVerify(exactly = 0) { maternalHealthRepo.persistRegisterRecord(any()) }
        assertEquals(PwAncAbortionFormViewModel.State.SAVE_SUCCESS, viewModel.state.value)
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
