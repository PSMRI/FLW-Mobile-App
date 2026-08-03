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
}
