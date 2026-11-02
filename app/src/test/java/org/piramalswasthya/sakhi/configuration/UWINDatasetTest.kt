package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class UWINDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path builds first page`() = runTest {
        val ds = UWINDataset(context, Languages.ENGLISH)
        runCatching { ds.setFirstPage(false, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path builds first page with cache`() = runTest {
        val ds = UWINDataset(context, Languages.ENGLISH)
        val cache = mockk<UwinCache>(relaxed = true)
        runCatching { ds.setFirstPage(true, cache) }
        runCatching { ds.setFirstPage(true, null) }
        runCatching { ds.mapValues(cache, 0) }
        runCatching { ds.getUwinFileIndex1() }
        runCatching { ds.getUwinFileIndex2() }
        runCatching { ds.setImageUriToFormElement(120, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(121, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(999, mockk<Uri>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi variant construction`() = runTest {
        val ds = UWINDataset(context, Languages.HINDI)
        runCatching { ds.setFirstPage(false, null) }
        runCatching { ds.setFirstPage(true, mockk<UwinCache>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    // ===================== added: handleListOnValueChanged coverage =====================
    // Neither existing test ever calls updateList, so both branches (empty/populated) of
    // handleListOnValueChanged for uWinSessionDate and participant, including the
    // emitAlertErrorMessage side effect, were entirely unexecuted.

    @Test
    fun `updateList validates session date and participant number fields`() = runTest {
        val ds = UWINDataset(context, Languages.ENGLISH)
        ds.setFirstPage(false, null)

        ds.setValueById(117, null)
        ds.updateList(117, 0)
        assertEquals("x", ds.listFlow.value.first { it.id == 117 }.errorText)
        assertEquals("x", ds.alertErrorMessageFlow.value)

        ds.setValueById(117, "01-01-2024")
        ds.updateList(117, 0)
        assertNull(ds.listFlow.value.first { it.id == 117 }.errorText)

        ds.setValueById(119, "")
        ds.updateList(119, 0)
        assertEquals("x", ds.listFlow.value.first { it.id == 119 }.errorText)

        ds.setValueById(119, "12")
        ds.updateList(119, 0)
        assertNull(ds.listFlow.value.first { it.id == 119 }.errorText)
    }

    // ===================== added: mapValues real round-trip coverage =====================
    // The pre-existing "edit path" test called setFirstPage(true, cache) then immediately
    // setFirstPage(true, null), which resets every FormElement value to null. The subsequent
    // mapValues(cache, 0) call then threw on its very first line
    // (uWinSessionDate.value?.let{...}!! on a null value) which was silently swallowed by
    // runCatching, so every other line of mapValues (place/participant/upload files) never
    // actually executed. These tests use a real UwinCache (not a relaxed mock) so the
    // assertions verify actual computed values rather than merely "doesn't throw".

    @Test
    fun `mapValues writes real computed values back to cache after loading from cache`() = runTest {
        val ds = UWINDataset(context, Languages.ENGLISH)
        val originalDateLong = Dataset.getLongFromDate("15-03-2024")
        val cache = UwinCache(
            id = 1,
            sessionDate = originalDateLong,
            place = "opt5",
            participantsCount = 42,
            uploadedFiles1 = "file1.jpg",
            uploadedFiles2 = "file2.jpg",
            processed = "N",
            createdBy = "asha1",
            updatedBy = "asha1",
            syncState = SyncState.UNSYNCED
        )
        ds.setFirstPage(true, cache)

        val result = UwinCache(
            id = 1,
            sessionDate = 0,
            place = null,
            participantsCount = 0,
            createdBy = "asha1",
            updatedBy = "asha1",
            syncState = SyncState.UNSYNCED
        )
        ds.mapValues(result, 0)

        val expectedDate = Dataset.getLongFromDate(Dataset.getDateFromLong(originalDateLong))
        assertEquals(expectedDate, result.sessionDate)
        assertEquals("opt5", result.place)
        assertEquals(42, result.participantsCount)
        assertEquals("file1.jpg", result.uploadedFiles1)
        assertEquals("file2.jpg", result.uploadedFiles2)
    }

    @Test
    fun `mapValues nulls out upload file fields when cache values are empty strings`() = runTest {
        val ds = UWINDataset(context, Languages.ENGLISH)
        val cache = UwinCache(
            id = 3,
            sessionDate = Dataset.getLongFromDate("01-01-2024"),
            place = "opt2",
            participantsCount = 7,
            uploadedFiles1 = "",
            uploadedFiles2 = "",
            createdBy = "asha2",
            updatedBy = "asha2",
            syncState = SyncState.UNSYNCED
        )
        ds.setFirstPage(true, cache)

        val result = UwinCache(
            id = 3,
            sessionDate = 0,
            place = null,
            participantsCount = 0,
            createdBy = "asha2",
            updatedBy = "asha2",
            syncState = SyncState.UNSYNCED
        )
        ds.mapValues(result, 0)

        assertNull(result.uploadedFiles1)
        assertNull(result.uploadedFiles2)
    }

    @Test
    fun `mapValues maps place to null through the getPosition zero branch when unset`() = runTest {
        val ds = UWINDataset(context, Languages.ENGLISH)
        ds.setFirstPage(false, null)
        ds.setValueById(117, "01-01-2024")
        ds.setValueById(119, "5")

        val result = UwinCache(
            id = 2,
            sessionDate = 0,
            place = "should be overwritten",
            participantsCount = 0,
            createdBy = "a",
            updatedBy = "a",
            syncState = SyncState.UNSYNCED
        )
        ds.mapValues(result, 0)

        assertNull(result.place)
        assertEquals(5, result.participantsCount)
    }
}
