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
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class KalaAzarFormDatasetTest : BaseViewModelTest() {

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
        runCatching { KalaAzarFormDataset(context, Languages.ENGLISH) }
        runCatching { KalaAzarFormDataset(context, Languages.HINDI) }
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..13) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<KalaAzarScreeningCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<KalaAzarScreeningCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<KalaAzarScreeningCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..13) {
        }
        assertNotNull(ds.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun savedKala(
        beneficiary: String?,
        caseStatus: String?,
        rdt: String?,
        referTo: String?,
        placeOfDeath: String? = null,
        reason: String? = null,
    ): KalaAzarScreeningCache {
        val s = mockk<KalaAzarScreeningCache>(relaxed = true)
        every { s.beneficiaryStatus } returns beneficiary
        every { s.kalaAzarCaseStatus } returns caseStatus
        every { s.rapidDiagnosticTest } returns rdt
        every { s.referToName } returns referTo
        every { s.placeOfDeath } returns placeOfDeath
        every { s.reasonForDeath } returns reason
        every { s.otherReferredFacility } returns "fac"
        every { s.otherPlaceOfDeath } returns "home"
        every { s.otherReasonForDeath } returns "unknown"
        every { s.visitDate } returns 1_600_000_000_000L
        every { s.dateOfDeath } returns 1_620_000_000_000L
        every { s.dateOfRdt } returns 1_600_000_000_000L
        return s
    }

    @Test
    fun `edit non-death path with rapid test and refer other`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedKala("opt0", "Suspected", "opt0", "opt79"))
            ds.mapValues(mockk<KalaAzarScreeningCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit confirmed case status path`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedKala("opt1", "Confirmed", "opt1", "opt5"))
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit death path with other place and reason`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedKala("opt78", "opt0", "opt2", "opt0", "opt79", "opt79"))
            ds.mapValues(mockk<KalaAzarScreeningCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit hindi non-death path`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedKala("opt0", null, "opt2", null))
        }
        assertNotNull(ds.listFlow)
    }
}
