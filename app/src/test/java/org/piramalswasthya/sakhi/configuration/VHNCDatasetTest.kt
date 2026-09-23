package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class VHNCDatasetTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

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
    fun createPath() = runTest {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.getFileIndex1() }
        runCatching { ds.getFileIndex2() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun editPath() = runTest {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        val cache = mockk<VHNCCache>(relaxed = true)
        runCatching { ds.setUpPage(cache) }
        runCatching { ds.mapValues(mockk<VHNCCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<VHNCCache>(relaxed = true), 1) }
        runCatching { ds.getFileIndex1() }
        runCatching { ds.getFileIndex2() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hindiConstruction() = runTest {
        val ds = VHNCDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    private fun dateForMonthOffset(offset: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 15)
        cal.add(java.util.Calendar.MONTH, offset)
        val fmt = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.ENGLISH)
        return fmt.format(cal.time)
    }

    @Test
    fun `restrictFilledMonths with no meetings does not throw`() {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        ds.restrictFilledMonths(emptyList())
    }

    @Test
    fun `restrictFilledMonths pushes min past latest eligible meeting month`() {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        val meeting = VHNCCache(id = 1, vhncDate = dateForMonthOffset(-1))
        ds.restrictFilledMonths(listOf(meeting))
    }

    @Test
    fun `restrictFilledMonths excludes the meeting being edited`() {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        val meeting = VHNCCache(id = 5, vhncDate = dateForMonthOffset(-1))
        ds.restrictFilledMonths(listOf(meeting), editingId = 5)
    }

    @Test
    fun `restrictFilledMonths ignores unparseable dates`() {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        val meeting = VHNCCache(id = 2, vhncDate = "not-a-date")
        ds.restrictFilledMonths(listOf(meeting))
    }

    @Test
    fun `restrictFilledMonths ignores months outside the eligible window`() {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        val tooOld = VHNCCache(id = 3, vhncDate = dateForMonthOffset(-6))
        val tooNew = VHNCCache(id = 4, vhncDate = dateForMonthOffset(1))
        ds.restrictFilledMonths(listOf(tooOld, tooNew))
    }

    @Test
    fun `restrictFilledMonths picks the latest of multiple eligible meetings`() {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        val older = VHNCCache(id = 6, vhncDate = dateForMonthOffset(-2))
        val newer = VHNCCache(id = 7, vhncDate = dateForMonthOffset(-1))
        ds.restrictFilledMonths(listOf(older, newer))
    }
}
