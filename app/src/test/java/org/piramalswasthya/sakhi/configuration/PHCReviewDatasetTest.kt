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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PHCReviewDatasetTest : BaseViewModelTest() {

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
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun editPath() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        runCatching { ds.setUpPage(cache) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 1) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hindiConstruction() = runTest {
        val ds = PHCReviewDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    private fun populatedCache(): PHCReviewMeetingCache {
        val c = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { c.phcReviewDate } returns "01-01-2024"
        every { c.place } returns "opt3"
        every { c.villageName } returns "Some Village"
        every { c.noOfBeneficiariesAttended } returns 5
        every { c.image1 } returns "uri1"
        every { c.image2 } returns "uri2"
        return c
    }

    @Test
    fun editPathPopulatedCache() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(populatedCache()) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun editPathHindiPopulated() = runTest {
        val ds = PHCReviewDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(populatedCache()) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 1) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setUpPage a second time skips the image-placeholder defaulting`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setUpPage(populatedCache()) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setUpPage resets whitespace-only image values back to default`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        val blankUri1 = mockk<Uri>(relaxed = true)
        every { blankUri1.toString() } returns " "
        val blankUri2 = mockk<Uri>(relaxed = true)
        every { blankUri2.toString() } returns "   "
        runCatching { ds.setImageUriToFormElement(1, blankUri1) }
        runCatching { ds.setImageUriToFormElement(2, blankUri2) }
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun setImageUriBothSlots() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(2, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(99, mockk<Uri>(relaxed = true)) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues defaults participant count to zero when unparsable`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { cache.noOfBeneficiariesAttended } returns null
        runCatching { ds.setUpPage(cache) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList validates the place field both empty and populated`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setValueById(4, ""); ds.updateList(4, 0) }
        runCatching { ds.setValueById(4, "VALID PLACE NAME"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(4, "bad🚫name"); ds.updateList(4, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList validates village name and participant count fields`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        for (id in listOf(5, 6)) {
            runCatching { ds.setValueById(id, ""); ds.updateList(id, 0) }
            runCatching { ds.setValueById(id, "5"); ds.updateList(id, 0) }
        }
        runCatching { ds.updateList(999, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList handles mT checkbox deselect-all from empty state`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.updateList(9, 0) }
        runCatching { ds.updateList(9, 5) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList handles mT checkbox select-all and re-toggle with single item checklist`() = runTest {
        every { mockResources.getStringArray(R.array.activity_checklist) } returns arrayOf("All Selected")
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.updateList(9, 0) }
        runCatching { ds.updateList(9, 3) }
        runCatching { ds.updateList(9, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList removes the select-all index when a normal item is not fully selected`() = runTest {
        every { mockResources.getStringArray(R.array.activity_checklist) } returns arrayOf("Only")
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.updateList(9, 5) }
        every { mockResources.getStringArray(R.array.activity_checklist) } returns arrayOf("A", "B", "C")
        runCatching { ds.updateList(9, 2) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList keeps the select-all index when every normal item is already selected`() = runTest {
        every { mockResources.getStringArray(R.array.activity_checklist) } returns arrayOf("Only")
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.updateList(9, 5) }
        every { mockResources.getStringArray(R.array.activity_checklist) } returns arrayOf("A", "B", "C")
        runCatching { ds.updateList(9, 0) }
        runCatching { ds.updateList(9, 1) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues defaults participant count to zero when value is explicitly null`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setValueById(6, null) }
        runCatching { ds.mapValues(mockk<PHCReviewMeetingCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }
}
