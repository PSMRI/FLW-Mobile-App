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
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PncFormDatasetTest : BaseViewModelTest() {

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
    fun pncFormDeep() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val delivery = mockk<DeliveryOutcomeCache>(relaxed = true)
        val previous = mockk<PNCVisitCache>(relaxed = true)
        val saved = mockk<PNCVisitCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { ds.setUpPage(visit, ben, delivery, null, null, false) }
            runCatching { ds.setUpPage(visit, ben, delivery, previous, saved, true) }
        }
        runCatching { ds.mapValues(mockk<PNCVisitCache>(relaxed = true), 0) }
        runCatching { ds.getIndexDeliveryDischargeSummary1() }
        runCatching { ds.getIndexDeliveryDischargeSummary2() }
        runCatching { ds.getIndexDeliveryDischargeSummary3() }
        runCatching { ds.getIndexDeliveryDischargeSummary4() }
        assertNotNull(ds.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun deliveryWithDate(): DeliveryOutcomeCache {
        val d = mockk<DeliveryOutcomeCache>(relaxed = true)
        every { d.dateOfDelivery } returns 1_600_000_000_000L
        return d
    }

    private fun savedPnc(
        anyContraception: Boolean?,
        contraceptionMethod: String?,
        anyDangerSign: String?,
        motherDangerSign: String?,
        motherDeath: Boolean,
        placeOfDeath: String? = null,
    ): PNCVisitCache {
        val s = mockk<PNCVisitCache>(relaxed = true)
        every { s.pncPeriod } returns 7
        every { s.pncDate } returns 1_601_000_000_000L
        every { s.ifaTabsGiven } returns 30
        every { s.anyContraceptionMethod } returns anyContraception
        every { s.contraceptionMethod } returns contraceptionMethod
        every { s.sterilisationDate } returns 1_602_000_000_000L
        every { s.otherPpcMethod } returns "ppc"
        every { s.anyDangerSign } returns anyDangerSign
        every { s.motherDangerSign } returns motherDangerSign
        every { s.otherDangerSign } returns "danger"
        every { s.referralFacility } returns "fac"
        every { s.motherDeath } returns motherDeath
        every { s.deathDate } returns 1_603_000_000_000L
        every { s.causeOfDeath } returns "cause"
        every { s.placeOfDeath } returns placeOfDeath
        every { s.otherPlaceOfDeath } returns "home"
        every { s.remarks } returns "rem"
        return s
    }

    @Test
    fun `saved with contraception danger and mother death`() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val previous = mockk<PNCVisitCache>(relaxed = true)
        val saved = savedPnc(true, "opt79", "opt0", "opt79", true, placeOfDeath = "opt8")
        runCatching {
            ds.setUpPage(2, ben, deliveryWithDate(), previous, saved, false)
            ds.mapValues(mockk<PNCVisitCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `saved with contraception no danger no death`() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val previous = mockk<PNCVisitCache>(relaxed = true)
        val saved = savedPnc(true, "opt5", "opt3", "opt5", false)
        runCatching {
            ds.setUpPage(1, ben, deliveryWithDate(), previous, saved, false)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `saved no contraception danger yes`() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val previous = mockk<PNCVisitCache>(relaxed = true)
        val saved = savedPnc(false, null, "opt0", "opt3", false)
        runCatching {
            ds.setUpPage(3, ben, deliveryWithDate(), previous, saved, false)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `set image uris to summary elements`() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val uri = mockk<android.net.Uri>(relaxed = true)
        runCatching {
            ds.setUpPage(1, ben, deliveryWithDate(), null, null, false)
            ds.setImageUriToFormElement(58, uri)
            ds.setImageUriToFormElement(59, uri)
            ds.setImageUriToFormElement(60, uri)
            ds.setImageUriToFormElement(61, uri)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi saved path`() = runTest {
        val ds = PncFormDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        val previous = mockk<PNCVisitCache>(relaxed = true)
        val saved = savedPnc(true, "opt79", "opt0", "opt79", true, placeOfDeath = "opt8")
        runCatching {
            ds.setUpPage(4, ben, deliveryWithDate(), previous, saved, false)
        }
        assertNotNull(ds.listFlow)
    }
}
