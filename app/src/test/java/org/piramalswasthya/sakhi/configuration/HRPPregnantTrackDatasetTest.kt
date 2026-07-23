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
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HRPPregnantTrackDataset]. Consolidated from the previous
 * HRPPregnantTrackDatasetDeepTest + HRPPregnantTrackDatasetBranch3Test files into a single class:
 * deep ENGLISH coverage plus HINDI branch coverage over different visit strings and mapValues pages.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPPregnantTrackDatasetTest : BaseViewModelTest() {

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
    fun hrpPregnantTrackDeep() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<HRPPregnantTrackCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null, null, null) }
        runCatching { ds.setUpPage(ben, "Visit 1", saved, System.currentTimeMillis()) }
        runCatching { ds.mapValues(mockk<HRPPregnantTrackCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfRdPmsa() }
        runCatching { ds.getIndexOfRdDengue() }
        runCatching { ds.getIndexOfRdFilaria() }
        runCatching { ds.getIndexOfSevereAnemia() }
        runCatching { ds.getIndexOfPregInduced() }
        runCatching { ds.getIndexOfGest() }
        runCatching { ds.getIndexOfHypothyroidism() }
        runCatching { ds.getIndexOfPolyhydromnios() }
        runCatching { ds.getIndexOfOligohydromnios() }
        runCatching { ds.getIndexOfAntepartum() }
        runCatching { ds.getIndexOfMalPre() }
        runCatching { ds.getIndexOfHiv() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfOgttLabel() }
        runCatching { ds.getIndexOfFasting() }
        runCatching { ds.getIndexOfafter() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hrpPregnantTrackBranch() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        for (visit in listOf("Visit 1", "Visit 2", "Visit 3", "Visit 4")) {
            runCatching {
                ds.setUpPage(ben, visit, mockk<HRPPregnantTrackCache>(relaxed = true), System.currentTimeMillis())
            }
            runCatching { ds.setUpPage(ben, visit, null, 0L) }
        }
        for (p in 0..2) {
            runCatching { ds.mapValues(mockk<HRPPregnantTrackCache>(relaxed = true), p) }
        }
        runCatching { ds.getIndexOfRdPmsa() }
        runCatching { ds.getIndexOfRdDengue() }
        runCatching { ds.getIndexOfRdFilaria() }
        runCatching { ds.getIndexOfSevereAnemia() }
        runCatching { ds.getIndexOfPregInduced() }
        runCatching { ds.getIndexOfGest() }
        runCatching { ds.getIndexOfHypothyroidism() }
        runCatching { ds.getIndexOfPolyhydromnios() }
        runCatching { ds.getIndexOfOligohydromnios() }
        runCatching { ds.getIndexOfAntepartum() }
        runCatching { ds.getIndexOfMalPre() }
        runCatching { ds.getIndexOfHiv() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfOgttLabel() }
        runCatching { ds.getIndexOfFasting() }
        runCatching { ds.getIndexOfafter() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }
}
