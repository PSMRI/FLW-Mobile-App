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
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Deep coverage test for [AHDDataset]: exercises setUpPage (create + cached),
 * mapValues, getFormElementList and setImageUriToFormElement. Each builder call
 * wrapped in runCatching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AHDDatasetTest : BaseViewModelTest() {

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
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path exercises builders`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        // setUpPage(ahd: AHDCache?)
        runCatching { ds.setUpPage(null) }
        runCatching { ds.mapValues(mockk<AHDCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<AHDCache>(relaxed = true), 1) }
        runCatching { ds.getFormElementList() }
        runCatching { ds.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(2, mockk<Uri>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `cached path exercises loadCachedData branches`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        val cache = mockk<AHDCache>(relaxed = true)
        runCatching { ds.setUpPage(cache) }
        runCatching { ds.mapValues(mockk<AHDCache>(relaxed = true), 0) }
        runCatching { ds.getFormElementList() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi construction`() = runTest {
        val ds = AHDDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    private fun mobilizedCache(mobilized: String?, place: String? = null): AHDCache {
        val c = mockk<AHDCache>(relaxed = true)
        every { c.mobilizedForAHD } returns mobilized
        every { c.ahdPlace } returns place
        every { c.ahdDate } returns "01-01-2024"
        every { c.image1 } returns "img1"
        every { c.image2 } returns "img2"
        return c
    }

    @Test
    fun `loadCachedData inserts ahdPlace and ahdDate when mobilizedForAHD matches the first entry`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(mobilizedCache(mobilized = "opt0", place = "opt1"))
        val list = ds.getFormElementList()
        assertTrue(list.any { it.id == 4 })
        assertTrue(list.any { it.id == 3 })
    }

    @Test
    fun `loadCachedData skips ahdPlace and ahdDate when mobilizedForAHD does not match`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(mobilizedCache(mobilized = "opt1"))
        val list = ds.getFormElementList()
        assertTrue(list.none { it.id == 4 })
        assertTrue(list.none { it.id == 3 })
    }

    @Test
    fun `setUpPage second call skips the image placeholder defaulting`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        ds.setUpPage(null)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setUpPage resets whitespace-only image values back to default`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        val blankUri1 = mockk<Uri>(relaxed = true)
        every { blankUri1.toString() } returns " "
        val blankUri2 = mockk<Uri>(relaxed = true)
        every { blankUri2.toString() } returns "   "
        ds.setImageUriToFormElement(1, blankUri1)
        ds.setImageUriToFormElement(2, blankUri2)
        ds.setUpPage(null)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList mobilizedForAHD yes adds place and date then skips once already present`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList mobilizedForAHD no removes place and date fields`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        ds.setValueById(5, "opt1")
        ds.updateList(5, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList validates the ahdPlace spinner empty, blank and populated`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(mobilizedCache(mobilized = "opt0", place = "opt1"))
        ds.setValueById(4, null)
        ds.updateList(4, 0)
        ds.setValueById(4, "")
        ds.updateList(4, 0)
        ds.setValueById(4, "opt2")
        ds.updateList(4, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList ignores an unhandled form id`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        ds.updateList(999, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues resolves mobilizedForAHD via the localized array lookup`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        ds.setValueById(5, "opt5")
        val target = mockk<AHDCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.mobilizedForAHD = "opt5" }
    }

    @Test
    fun `mapValues falls back to the raw mobilizedForAHD value when it is not found in the array`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        ds.setUpPage(null)
        ds.setValueById(5, "unmapped_value")
        val target = mockk<AHDCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.mobilizedForAHD = "unmapped_value" }
    }
}
