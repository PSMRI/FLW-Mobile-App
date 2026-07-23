package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.widget.Toast
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
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyConfirmedDatasetTest : BaseViewModelTest() {

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
        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockk(relaxed = true)
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `construction ENGLISH and HINDI`() {
        runCatching { LeprosyConfirmedDataset(context, Languages.ENGLISH) }
        runCatching { LeprosyConfirmedDataset(context, Languages.HINDI) }
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path three arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null) }
        runCatching { ds.getIndexOfDate() }
        runCatching { ds.validateFollowUpDate(0L) }
        runCatching { ds.validateFollowUpDate(System.currentTimeMillis()) }
        runCatching { ds.getNextFollowUpAvailabilityMessage() }
        runCatching { ds.validateForm() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path two arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path three arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<LeprosyScreeningCache>(relaxed = true)
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved, followUp) }
        runCatching { ds.mapValues(mockk<LeprosyFollowUpCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<LeprosyFollowUpCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.validateForm() }
        runCatching { ds.validateFollowUpDate(System.currentTimeMillis()) }
        runCatching { ds.getNextFollowUpAvailabilityMessage() }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path two arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, followUp) }
        runCatching { ds.mapValues(mockk<LeprosyFollowUpCache>(relaxed = true), 0) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }
}
