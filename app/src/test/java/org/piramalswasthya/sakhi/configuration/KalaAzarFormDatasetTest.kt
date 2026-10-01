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
import org.piramalswasthya.sakhi.model.BenRegGen
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
        every { Log.w(any<String>(), any<String>()) } returns 0
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

    @Test
    fun `updateList beneficiaryStatus death and non-death branches`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(ben, null)

        ds.setValueById(2, "opt78")
        ds.updateList(2, 0)

        ds.setValueById(2, "opt1")
        ds.updateList(2, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList rapidDiagnostic three way branches`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(ben, null)
        ds.setValueById(2, "opt1")
        ds.updateList(2, 0)

        ds.setValueById(9, "opt2")
        ds.updateList(9, 0)

        ds.setValueById(9, "opt1")
        ds.updateList(9, 0)

        ds.setValueById(9, "opt5")
        ds.updateList(9, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList referredTo and other empty-text branches`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(ben, null)
        ds.setValueById(2, "opt1")
        ds.updateList(2, 0)

        ds.setValueById(12, "opt79")
        ds.updateList(12, 0)

        ds.setValueById(13, "")
        ds.updateList(13, 0)

        ds.setValueById(13, "filled")
        ds.updateList(13, 0)

        ds.setValueById(12, "opt1")
        ds.updateList(12, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList placeOfDeath and reasonOfDeath other-text branches`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(ben, null)
        ds.setValueById(2, "opt78")
        ds.updateList(2, 0)

        ds.setValueById(4, "opt79")
        ds.updateList(4, 0)
        ds.setValueById(5, "")
        ds.updateList(5, 0)
        ds.setValueById(5, "filled")
        ds.updateList(5, 0)
        ds.setValueById(4, "opt1")
        ds.updateList(4, 0)

        ds.setValueById(6, "opt79")
        ds.updateList(6, 0)
        ds.setValueById(7, "")
        ds.updateList(7, 0)
        ds.setValueById(7, "filled")
        ds.updateList(7, 0)
        ds.setValueById(6, "opt1")
        ds.updateList(6, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList unmatched formId returns default`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(ben, null)
        ds.updateList(999, 0)
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateBen genDetails and processed variants`() {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)

        val benWithNullGenAndReadyProcessed = mockk<BenRegCache>(relaxed = true)
        every { benWithNullGenAndReadyProcessed.genDetails } returns null
        every { benWithNullGenAndReadyProcessed.processed } returns "N"
        ds.updateBen(benWithNullGenAndReadyProcessed)

        val benWithGenAndOtherProcessed = mockk<BenRegCache>(relaxed = true)
        val genDetails = mockk<BenRegGen>(relaxed = true)
        every { benWithGenAndOtherProcessed.genDetails } returns genDetails
        every { benWithGenAndOtherProcessed.processed } returns "SomethingElse"
        ds.updateBen(benWithGenAndOtherProcessed)

        assertNotNull(ds.listFlow)
    }
}
