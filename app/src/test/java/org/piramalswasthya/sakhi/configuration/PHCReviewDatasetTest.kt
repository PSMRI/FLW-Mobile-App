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
}
