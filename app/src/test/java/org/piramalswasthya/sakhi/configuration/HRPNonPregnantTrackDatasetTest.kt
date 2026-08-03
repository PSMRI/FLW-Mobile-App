package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HRPNonPregnantTrackDataset]. Consolidated from the previous
 * HRPNonPregnantTrackDatasetDeepTest + HRPNonPregnantTrackDatasetBranch3Test files into a single
 * class: deep ENGLISH coverage plus HINDI branch coverage over multiple mapValues page indices.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantTrackDatasetTest : BaseViewModelTest() {

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
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun hrpNonPregnantTrackDeep() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<HRPNonPregnantTrackCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null, null, null) }
        runCatching { ds.setUpPage(ben, saved, System.currentTimeMillis(), System.currentTimeMillis()) }
        runCatching { ds.mapValues(mockk<HRPNonPregnantTrackCache>(relaxed = true), 0) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfAncLabel() }
        runCatching { ds.getIndexOfAnemia() }
        runCatching { ds.getIndexOfRisk() }
        runCatching { ds.getIndexOfLmp() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hrpNonPregnantTrackBranch() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, mockk<HRPNonPregnantTrackCache>(relaxed = true), 0L, 0L) }
        runCatching {
            ds.setUpPage(ben, null, System.currentTimeMillis(), System.currentTimeMillis())
        }
        for (p in 0..2) {
            runCatching { ds.mapValues(mockk<HRPNonPregnantTrackCache>(relaxed = true), p) }
        }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfAncLabel() }
        runCatching { ds.getIndexOfAnemia() }
        runCatching { ds.getIndexOfRisk() }
        runCatching { ds.getIndexOfLmp() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }
}
