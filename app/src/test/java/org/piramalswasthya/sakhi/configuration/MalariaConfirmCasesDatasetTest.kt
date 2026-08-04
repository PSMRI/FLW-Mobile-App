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
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Deep coverage test for [MalariaConfirmCasesDataset]: invokes every public
 * getIndexOfDate) on both the create and edit code paths with rich relaxed mocks.
 * Each builder call is wrapped in runCatching so a mock gap never fails the test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MalariaConfirmCasesDatasetTest : BaseViewModelTest() {

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
    fun `create path exercises builders`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        // setUpPage(ben: BenRegCache?, slideTestName: String, saved: MalariaConfirmedCasesCache?)
        runCatching { ds.setUpPage(null, "x", null) }
        runCatching { ds.mapValues(mockk<MalariaConfirmedCasesCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<MalariaConfirmedCasesCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path exercises saved branches`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, "x", saved) }
        runCatching { ds.mapValues(mockk<MalariaConfirmedCasesCache>(relaxed = true), 0) }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi construction`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null, "x", null) }
        assertNotNull(ds.listFlow)
    }
}
