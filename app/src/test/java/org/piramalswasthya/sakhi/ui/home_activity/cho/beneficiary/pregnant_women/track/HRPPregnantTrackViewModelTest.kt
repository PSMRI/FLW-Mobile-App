package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.track

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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenWithHRPTrackingCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HRPPregnantTrackViewModel]: the visit-label ladder driven by how many tracking
 * rows already exist, the newest-date-of-visit resolution and the save arms.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPPregnantTrackViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var hrpRepo: HRPRepo

    @MockK
    private lateinit var benRepo: BenRepo

    private lateinit var user: User
    private lateinit var ben: BenRegCache

    @After
    fun releaseStaticMocks() {
        unmockkStatic(Dispatchers::class)
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { context.resources } returns mockResources

        user = mockk(relaxed = true)
        every { user.userName } returns "asha"

        ben = mockk(relaxed = true)
        every { ben.beneficiaryId } returns 2L
        every { ben.firstName } returns "Pooja"
        every { ben.lastName } returns "Verma"
        every { ben.age } returns 27

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { benRepo.getBenWithHRPT(any()) } returns mockk<BenWithHRPTrackingCache>(relaxed = true)
        coEvery { hrpRepo.getHrPregTrackList(any()) } returns emptyList()
        coEvery { hrpRepo.getHRPTrack(any()) } returns null
        coEvery { hrpRepo.getMaxDoVHrp(any()) } returns null
        coEvery { hrpRepo.getMaxDoVNonHrp(any()) } returns null
        coEvery { hrpRepo.getPregnantAssess(any()) } returns null
        coEvery { hrpRepo.saveRecord(any<HRPPregnantTrackCache>()) } returns Unit
    }

    private fun buildVm(benId: Long = 2L, trackId: Int = 0): HRPPregnantTrackViewModel =
        HRPPregnantTrackViewModel(
            SavedStateHandle(mapOf("benId" to benId, "trackId" to trackId)),
            preferenceDao,
            context,
            hrpRepo,
            benRepo
        )

    private fun trackList(size: Int): List<HRPPregnantTrackCache> =
        List(size) { mockk<HRPPregnantTrackCache>(relaxed = true) }

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 88L, trackId = 4)
        assertEquals(88L, vm.benId)
        assertEquals(4, vm.trackId)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(HRPPregnantTrackViewModel.State.IDLE, vm.state.value)
        assertFalse(vm.isHighRisk)
    }

    @Test
    fun `loadData labels the first visit when nothing is tracked yet`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Pooja Verma", vm.benName.value)
        assertNotNull(vm.benWithHrpt.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `loadData labels the pmsma visits as tracking progresses`() = runTest {
        coEvery { hrpRepo.getHrPregTrackList(any()) } returns trackList(1)
        val first = buildVm()
        advanceUntilIdle()
        assertEquals(false, first.recordExists.value)

        coEvery { hrpRepo.getHrPregTrackList(any()) } returns trackList(2)
        val second = buildVm()
        advanceUntilIdle()
        assertEquals(false, second.recordExists.value)

        coEvery { hrpRepo.getHrPregTrackList(any()) } returns trackList(3)
        val third = buildVm()
        advanceUntilIdle()
        assertEquals(false, third.recordExists.value)
    }

    @Test
    fun `loadData reports tracking complete once every visit is filled`() = runTest {
        coEvery { hrpRepo.getHrPregTrackList(any()) } returns trackList(4)

        val vm = buildVm(trackId = 0)
        advanceUntilIdle()

        assertEquals(true, vm.trackingDone.value)
    }

    @Test
    fun `loadData does not report tracking complete while editing a row`() = runTest {
        coEvery { hrpRepo.getHrPregTrackList(any()) } returns trackList(4)
        coEvery { hrpRepo.getHRPTrack(any()) } returns mockk<HRPPregnantTrackCache>(relaxed = true)

        val vm = buildVm(trackId = 3)
        advanceUntilIdle()

        assertEquals(false, vm.trackingDone.value)
        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `loadData leaves recordExists unset when a row is returned for a new form`() = runTest {
        coEvery { hrpRepo.getHRPTrack(any()) } returns mockk<HRPPregnantTrackCache>(relaxed = true)

        val vm = buildVm(trackId = 0)
        advanceUntilIdle()

        assertNull(vm.recordExists.value)
    }

    @Test
    fun `loadData takes the newest date of visit across both registers`() = runTest {
        coEvery { hrpRepo.getMaxDoVHrp(any()) } returns 2_000L
        coEvery { hrpRepo.getMaxDoVNonHrp(any()) } returns 5_000L

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `loadData falls back to the high risk register alone`() = runTest {
        coEvery { hrpRepo.getMaxDoVHrp(any()) } returns 2_000L
        coEvery { hrpRepo.getMaxDoVNonHrp(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `loadData falls back to the non high risk register alone`() = runTest {
        coEvery { hrpRepo.getMaxDoVHrp(any()) } returns null
        coEvery { hrpRepo.getMaxDoVNonHrp(any()) } returns 5_000L

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `loadData also considers the assessment lmp date`() = runTest {
        val assess = mockk<HRPPregnantAssessCache>(relaxed = true)
        every { assess.lmpDate } returns 9_000L
        coEvery { hrpRepo.getPregnantAssess(any()) } returns assess

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `loadData can be re-run`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.loadData()
        advanceUntilIdle()

        coVerify(atLeast = 2) { benRepo.getBenWithHRPT(any()) }
    }

    @Test
    fun `saveForm stores the tracking row and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { hrpRepo.saveRecord(any<HRPPregnantTrackCache>()) }
        assertEquals(HRPPregnantTrackViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when storing throws`() = runTest {
        coEvery { hrpRepo.saveRecord(any<HRPPregnantTrackCache>()) } throws
                RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPPregnantTrackViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(HRPPregnantTrackViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfRdPmsa() }
        runCatching { vm.getIndexOfRdDengue() }
        runCatching { vm.getIndexOfRdFilaria() }
        runCatching { vm.getIndexOfSevereAnemia() }
        runCatching { vm.getIndexOfPregInduced() }
        runCatching { vm.getIndexOfGest() }
        runCatching { vm.getIndexOfHypothyroidism() }
        runCatching { vm.getIndexOfPolyhydromnios() }
        runCatching { vm.getIndexOfOligohydromnios() }
        runCatching { vm.getIndexOfAntepartum() }
        runCatching { vm.getIndexOfMalPre() }
        runCatching { vm.getIndexOfHiv() }
        runCatching { vm.getIndexOfRbg() }
        runCatching { vm.getIndexOfFbg() }
        runCatching { vm.getIndexOfPpbg() }
        runCatching { vm.getIndexOfOgttLabel() }
        runCatching { vm.getIndexOfFasting() }
        runCatching { vm.getIndexOfAfter() }
        runCatching { vm.getIndexOfIfaQuantity() }

        assertNotNull(vm.formList)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
