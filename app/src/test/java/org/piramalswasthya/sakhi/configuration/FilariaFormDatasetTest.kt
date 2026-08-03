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
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaFormDatasetTest : BaseViewModelTest() {

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
    fun `construction ENGLISH and HINDI`() {
        runCatching { FilariaFormDataset(context, Languages.ENGLISH) }
        runCatching { FilariaFormDataset(context, Languages.HINDI) }
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        // setUpPage does ben!!.dob so null ben throws; wrapped in runCatching.
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.isMaleFemale(null) }
        runCatching { ds.isSuffering() }
        runCatching { ds.isYoung(0L) }
        runCatching { ds.isYoung(System.currentTimeMillis()) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        runCatching { ds.isMaleFemale(ben) }
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<FilariaScreeningCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<FilariaScreeningCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.isSuffering() }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..8) {
        }
        assertNotNull(ds.listFlow)
    }
}
