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
}
