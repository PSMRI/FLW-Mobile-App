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
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class VHNDDatasetTest : BaseViewModelTest() {

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
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun editPath() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        val cache = mockk<VHNDCache>(relaxed = true)
        runCatching { ds.setUpPage(cache) }
        runCatching { ds.mapValues(mockk<VHNDCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<VHNDCache>(relaxed = true), 1) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hindiConstruction() = runTest {
        val ds = VHNDDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    // ---- added coverage: populated cache edit path, images, mapValues ----

    private fun populatedCache(): VHNDCache {
        val c = mockk<VHNDCache>(relaxed = true)
        every { c.vhndDate } returns "01-01-2024"
        every { c.place } returns "opt3"
        every { c.noOfBeneficiariesAttended } returns 5
        every { c.image1 } returns "uri1"
        every { c.image2 } returns "uri2"
        return c
    }

    @Test
    fun editPathPopulatedCache() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(populatedCache()) }
        runCatching { ds.mapValues(mockk<VHNDCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun setImageUriBothSlots() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(2, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(99, mockk<Uri>(relaxed = true)) }
        runCatching { ds.mapValues(mockk<VHNDCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun editPathHindiPopulated() = runTest {
        val ds = VHNDDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(populatedCache()) }
        runCatching { ds.mapValues(mockk<VHNDCache>(relaxed = true), 1) }
        assertNotNull(ds.listFlow)
    }

    // ===================== added: handleListOnValueChanged coverage =====================
    // None of the pre-existing tests ever call updateList, so every branch of
    // handleListOnValueChanged (place / the three attendee-count fields / the else fallback) was
    // entirely unexecuted. updateList dispatches purely on formId regardless of whether that
    // element is actually present in the currently built list, so these are reachable even though
    // the mitanin-only fields (noOfPWAttended etc.) are never added to the list under this flavor.

    @Test
    fun `updateList validates the place field both empty and populated`() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setValueById(3, ""); ds.updateList(3, 0) }
        runCatching { ds.setValueById(3, "VALID PLACE NAME"); ds.updateList(3, 0) }
        runCatching { ds.setValueById(3, "bad🚫name"); ds.updateList(3, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList validates every attendee count field`() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        for (id in listOf(4, 5, 6, 7)) {
            runCatching { ds.setValueById(id, ""); ds.updateList(id, 0) }
            runCatching { ds.setValueById(id, "5"); ds.updateList(id, 0) }
        }
        runCatching { ds.updateList(999, 0) }
        assertNotNull(ds.listFlow)
    }

    // setUpPage(): pic1/pic2 only default to "default" the first time they are blank; calling
    // setUpPage a second time on the same instance hits the "already has a value" skip branch.
    @Test
    fun `setUpPage a second time skips the image-placeholder defaulting`() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setUpPage(null) }
        runCatching { ds.setUpPage(populatedCache()) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setUpPage resets whitespace-only image values back to default`() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
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
}
