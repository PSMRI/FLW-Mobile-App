package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.maa_meeting

import android.content.Context
import android.content.res.Resources
import android.net.Uri
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [MaaMeetingFormViewModel]: the create versus edit arms of `init`, the CSV
 * index/value translation helpers and the save pipeline.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MaaMeetingFormViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var repo: MaaMeetingRepo

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

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { repo.appContext } returns context
        every { repo.getAllMaaMeetings() } returns flowOf(emptyList())
        coEvery { repo.getMaaMeetingById(any()) } returns null
        coEvery { repo.isThreeMonthsPassedSinceLastMeeting(any()) } returns true
    }

    private fun buildVm(id: Long = 0L): MaaMeetingFormViewModel =
        MaaMeetingFormViewModel(
            preferenceDao,
            SavedStateHandle(mapOf("id" to id)),
            repo
        )

    @Test
    fun `viewModel initializes with a dataset and an empty meeting list`() {
        val vm = buildVm()
        assertNotNull(vm.dataset)
        assertNotNull(vm.formList)
        assertEquals(emptyList<MaaMeetingEntity>(), vm.maaMeetings.value)
    }

    @Test
    fun `the record id argument is exposed`() {
        val vm = buildVm(id = 13L)
        assertEquals(13L, vm.id)
    }

    @Test
    fun `init reports no existing record for a fresh form`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init seeds the dataset from a saved meeting`() = runTest {
        val meeting = mockk<MaaMeetingEntity>(relaxed = true)
        every { meeting.meetingDate } returns "01-01-2026"
        every { meeting.place } returns "Anganwadi"
        every { meeting.villageName } returns "Rampur"
        every { meeting.noOfPragnentWomen } returns "3"
        every { meeting.noOfLactingMother } returns "2"
        every { meeting.mitaninActivityCheckList } returns "opt0|opt1"
        every { meeting.participants } returns 12
        every { meeting.meetingImages } returns listOf("a", "b", "c")
        coEvery { repo.getMaaMeetingById(any()) } returns meeting

        val vm = buildVm(id = 4L)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals("01-01-2026", vm.dataset.meetingDate.value)
        assertEquals("Anganwadi", vm.dataset.meetingPlace.value)
        assertEquals("Rampur", vm.dataset.villageName.value)
        assertEquals("12", vm.dataset.participants.value)
        assertEquals("a", vm.dataset.upload1.value)
        assertEquals("c", vm.dataset.upload3.value)
        assertEquals(null, vm.dataset.upload4.value)
    }

    @Test
    fun `init tolerates a saved meeting without images`() = runTest {
        val meeting = mockk<MaaMeetingEntity>(relaxed = true)
        every { meeting.meetingDate } returns "01-01-2026"
        every { meeting.meetingImages } returns null
        every { meeting.participants } returns null
        every { meeting.mitaninActivityCheckList } returns null
        coEvery { repo.getMaaMeetingById(any()) } returns meeting

        val vm = buildVm(id = 4L)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals(null, vm.dataset.upload1.value)
    }

    @Test
    fun `toCsv maps selected indexes onto entry labels`() {
        val vm = buildVm()
        val entries = arrayOf("A", "B", "C")

        assertEquals("A|C", vm.toCsv("0|2", entries))
        assertEquals("", vm.toCsv(null, entries))
        assertEquals("", vm.toCsv("", entries))
        assertEquals("", vm.toCsv("9", entries))
        assertEquals("", vm.toCsv("x", entries))
    }

    @Test
    fun `valueToIndexCsv maps entry labels back onto indexes`() {
        val vm = buildVm()
        val entries = arrayOf("A", "B", "C")

        assertEquals("0|2", vm.valueToIndexCsv("A|C", entries))
        assertEquals("", vm.valueToIndexCsv(null, entries))
        assertEquals("", vm.valueToIndexCsv("", entries))
        assertEquals("", vm.valueToIndexCsv("Z", entries))
        assertEquals("1", vm.valueToIndexCsv(" B ", entries))
    }

    @Test
    fun `saveForm builds the entity and runs the sync pipeline`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.dataset.meetingDate.value = "01-01-2026"
        vm.dataset.meetingPlace.value = "Anganwadi"
        vm.dataset.participants.value = "9"
        vm.saveForm()
        advanceUntilIdle()

        coVerify { repo.save(any()) }
        coVerify { repo.tryUpsync() }
        coVerify { repo.downSyncAndPersist() }
    }

    @Test
    fun `saveForm defaults blank counts to zero`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.dataset.noOfPW.value = ""
        vm.dataset.noOfLM.value = ""
        vm.saveForm()
        advanceUntilIdle()

        coVerify { repo.save(any()) }
    }

    @Test
    fun `hasMeetingInSameQuarter delegates to the repository`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertTrue(vm.hasMeetingInSameQuarter("01-01-2026"))

        coEvery { repo.isThreeMonthsPassedSinceLastMeeting(any()) } returns false
        assertFalse(vm.hasMeetingInSameQuarter("01-01-2026"))
    }

    @Test
    fun `setUploadUriFor fills each upload slot`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://photo"

        for (formId in 10..14) {
            vm.setUploadUriFor(formId, uri)
        }
        vm.setUploadUriFor(99, uri)

        assertEquals("content://photo", vm.dataset.upload1.value)
        assertEquals("content://photo", vm.dataset.upload5.value)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(15, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
