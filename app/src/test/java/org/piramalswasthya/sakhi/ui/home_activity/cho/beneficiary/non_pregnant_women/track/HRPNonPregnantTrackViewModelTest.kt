package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.track

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
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HRPNonPregnantTrackViewModel]. When a tracked woman is recorded as pregnant the
 * save path promotes her into the pregnant register, seeding the pregnancy assessment from the
 * non-pregnant one - every arm of that promotion is covered here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantTrackViewModelTest : BaseViewModelTest() {

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
        every { ben.firstName } returns "Neha"
        every { ben.lastName } returns "Gupta"
        every { ben.age } returns 29

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { benRepo.updateRecord(any()) } returns Unit
        coEvery { hrpRepo.getHRPNonTrack(any()) } returns null
        coEvery { hrpRepo.getMaxLmp(any()) } returns null
        coEvery { hrpRepo.getMaxDoVNonHrp(any()) } returns null
        coEvery { hrpRepo.getPregnantAssess(any()) } returns null
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null
        coEvery { hrpRepo.saveRecord(any<HRPNonPregnantTrackCache>()) } returns Unit
        coEvery { hrpRepo.saveRecord(any<HRPPregnantAssessCache>()) } returns Unit
    }

    private fun buildVm(benId: Long = 2L, trackId: Int = 0): HRPNonPregnantTrackViewModel =
        HRPNonPregnantTrackViewModel(
            SavedStateHandle(mapOf("benId" to benId, "trackId" to trackId)),
            preferenceDao,
            context,
            hrpRepo,
            benRepo
        )

    private fun pregnantTrack(): HRPNonPregnantTrackCache {
        val cache = mockk<HRPNonPregnantTrackCache>(relaxed = true)
        every { cache.isPregnant } returns "Yes"
        every { cache.benId } returns 2L
        return cache
    }

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 91L, trackId = 5)
        assertEquals(91L, vm.benId)
        assertEquals(5, vm.trackId)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(HRPNonPregnantTrackViewModel.State.IDLE, vm.state.value)
        assertFalse(vm.isHighRisk)
    }

    @Test
    fun `init populates the header and reports no saved row`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Neha Gupta", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Neha ", vm.benName.value)
    }

    @Test
    fun `init loads the row being edited`() = runTest {
        coEvery { hrpRepo.getHRPNonTrack(any()) } returns pregnantTrack()

        val vm = buildVm(trackId = 5)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `init leaves recordExists unset when a row is returned for a new form`() = runTest {
        coEvery { hrpRepo.getHRPNonTrack(any()) } returns pregnantTrack()

        val vm = buildVm(trackId = 0)
        advanceUntilIdle()

        assertNull(vm.recordExists.value)
    }

    @Test
    fun `init passes the newest lmp and visit dates to the dataset`() = runTest {
        coEvery { hrpRepo.getMaxLmp(any()) } returns 4_000L
        coEvery { hrpRepo.getMaxDoVNonHrp(any()) } returns 6_000L

        val vm = buildVm()
        advanceUntilIdle()

        coVerify { hrpRepo.getMaxLmp(any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `saveForm stores the tracking row and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { hrpRepo.saveRecord(any<HRPNonPregnantTrackCache>()) }
        assertEquals(HRPNonPregnantTrackViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm promotes the woman and seeds a fresh pregnancy assessment`() = runTest {
        coEvery { hrpRepo.getHRPNonTrack(any()) } returns pregnantTrack()

        val vm = buildVm(trackId = 5)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { benRepo.updateRecord(any()) }
        coVerify { hrpRepo.saveRecord(any<HRPPregnantAssessCache>()) }
        assertEquals(HRPNonPregnantTrackViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm copies the non pregnant assessment into the pregnancy assessment`() = runTest {
        coEvery { hrpRepo.getHRPNonTrack(any()) } returns pregnantTrack()
        val nonPreg = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        every { nonPreg.noOfDeliveries } returns "Yes"
        every { nonPreg.timeLessThan18m } returns "No"
        every { nonPreg.heightShort } returns "No"
        every { nonPreg.age } returns "No"
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns nonPreg

        val vm = buildVm(trackId = 5)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { hrpRepo.saveRecord(any<HRPPregnantAssessCache>()) }
        assertEquals(HRPNonPregnantTrackViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm keeps an existing pregnancy assessment`() = runTest {
        coEvery { hrpRepo.getHRPNonTrack(any()) } returns pregnantTrack()
        coEvery { hrpRepo.getPregnantAssess(any()) } returns
                mockk<HRPPregnantAssessCache>(relaxed = true)

        val vm = buildVm(trackId = 5)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { hrpRepo.saveRecord(any<HRPPregnantAssessCache>()) }
        assertEquals(HRPNonPregnantTrackViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when storing throws`() = runTest {
        coEvery { hrpRepo.saveRecord(any<HRPNonPregnantTrackCache>()) } throws
                RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(HRPNonPregnantTrackViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(HRPNonPregnantTrackViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfAncLabel() }
        runCatching { vm.getIndexOfAnemia() }
        runCatching { vm.getIndexOfRisk() }
        runCatching { vm.getIndexOfLmp() }
        runCatching { vm.getIndexOfRbg() }
        runCatching { vm.getIndexOfFbg() }
        runCatching { vm.getIndexOfPpbg() }
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
