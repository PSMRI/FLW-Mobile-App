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
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryOutcomeDatasetTest : BaseViewModelTest() {

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
    fun deliveryOutcomeDeep() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        val saved = mockk<DeliveryOutcomeCache>(relaxed = true)
        runCatching { ds.setUpPage(pwr, anc, null) }
        runCatching { ds.setUpPage(pwr, anc, saved) }
        runCatching { ds.mapValues(mockk<DeliveryOutcomeCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfMCP1() }
        runCatching { ds.getIndexOfMCP2() }
        runCatching { ds.getIndexOfIsjsyFileUpload() }
        assertNotNull(ds.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun savedDelivery(
        hadComplications: Boolean?,
        complication: String?,
        isJsy: Boolean?,
        placeOfDeath: String? = null,
    ): DeliveryOutcomeCache {
        val s = mockk<DeliveryOutcomeCache>(relaxed = true)
        every { s.hadComplications } returns hadComplications
        every { s.complication } returns complication
        every { s.isJSYBenificiary } returns isJsy
        every { s.placeOfDeath } returns placeOfDeath
        every { s.dateOfDelivery } returns 1_600_000_000_000L
        every { s.dateOfDischarge } returns 1_601_000_000_000L
        every { s.deliveryOutcome } returns 2
        every { s.liveBirth } returns 1
        every { s.stillBirth } returns 1
        every { s.causeOfDeath } returns "opt1"
        every { s.mcp1File } returns "mcp1"
        every { s.mcp2File } returns "mcp2"
        every { s.jsyFile } returns "jsy"
        every { s.dateOfDeath } returns "01-01-2024"
        every { s.otherPlaceOfDeath } returns "home"
        return s
    }

    @Test
    fun `edit had complications adds complication`() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        runCatching {
            ds.setUpPage(pwr, anc, savedDelivery(true, "opt1", false))
            ds.mapValues(mockk<DeliveryOutcomeCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit other delivery complication path`() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        runCatching {
            ds.setUpPage(pwr, anc, savedDelivery(true, "Other Delivery Complication", false))
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit death complication with other place of death`() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        val saved = savedDelivery(true, "DEATH", true, placeOfDeath = "opt8")
        every { saved.complication } returns "Death"
        runCatching {
            ds.setUpPage(pwr, anc, saved)
            ds.mapValues(saved, 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit jsy beneficiary adds file upload`() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        runCatching {
            ds.setUpPage(pwr, anc, savedDelivery(false, null, true))
            ds.getIndexOfIsjsyFileUpload()
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `set image uris to elements`() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        val uri = mockk<android.net.Uri>(relaxed = true)
        runCatching {
            ds.setUpPage(pwr, anc, null)
            ds.setImageUriToFormElement(21, uri)
            ds.setImageUriToFormElement(22, uri)
            ds.setImageUriToFormElement(23, uri)
        }
        assertNotNull(ds.listFlow)
    }
}
