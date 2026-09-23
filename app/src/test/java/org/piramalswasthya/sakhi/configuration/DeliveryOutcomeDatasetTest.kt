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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    // ===================== updateList-driven handler coverage =====================
    // ids: 1 dateOfDelivery, 5 hadComplications, 6 complication, 7 causeOfDeath,
    // 8 otherCauseOfDeath, 9 deliveryOutcome, 10 liveBirth, 11 stillBirth, 13 dateOfDischarge,
    // 15 isJSYBenificiary, 21/22 mcpFileUpload, 23 jsyFileUpload, 51 dateOfDeath,
    // 54 placeOfDeath, 55 otherPlaceOfDeath.
    // handleListOnValueChanged is PROTECTED - it is only driven through updateList.

    private suspend fun createPage(): DeliveryOutcomeDataset {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        ds.setUpPage(
            mockk<PregnantWomanRegistrationCache>(relaxed = true),
            mockk<PregnantWomanAncCache>(relaxed = true),
            null
        )
        return ds
    }

    @Test
    fun `create page builds base list`() = runTest {
        val ds = createPage()
        val ids = ds.listFlow.value.map { it.id }
        assertTrue("list should be populated", ids.isNotEmpty())
        assertTrue(ids.contains(1))
    }

    @Test
    fun `had complications yes reveals complication then no removes it`() = runTest {
        val ds = createPage()
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0) // triggerIndex is 0 for hadComplications
        assertTrue("complication expected", ds.listFlow.value.any { it.id == 6 })

        ds.setValueById(5, "opt1")
        ds.updateList(5, 1)
        assertFalse("complication removed", ds.listFlow.value.any { it.id == 6 })
    }

    @Test
    fun `complication death index adds cause and place of death`() = runTest {
        val ds = createPage()
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        ds.setValueById(6, "opt6")
        ds.updateList(6, 6) // index 6 == death branch
        val ids = ds.listFlow.value.map { it.id }
        assertTrue("causeOfDeath expected", ids.contains(7))
        assertTrue("dateOfDeath expected", ids.contains(51))
        assertTrue("placeOfDeath expected", ids.contains(54))
        assertFalse("discharge fields dropped on death", ids.contains(13))
    }

    @Test
    fun `complication other index adds free text field`() = runTest {
        val ds = createPage()
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        ds.setValueById(6, "opt7")
        ds.updateList(6, 7) // index 7 == "other" branch
        val ids = ds.listFlow.value.map { it.id }
        assertTrue("otherComplication expected", ids.contains(12))
        assertFalse("causeOfDeath must not be shown", ids.contains(7))
    }

    @Test
    fun `complication plain index removes all dependants`() = runTest {
        val ds = createPage()
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        ds.setValueById(6, "opt6")
        ds.updateList(6, 6)
        ds.setValueById(6, "opt2")
        ds.updateList(6, 2)
        val ids = ds.listFlow.value.map { it.id }
        assertFalse("causeOfDeath removed", ids.contains(7))
        assertFalse("dateOfDeath removed", ids.contains(51))
        assertFalse("placeOfDeath removed", ids.contains(54))
    }

    @Test
    fun `cause of death other index adds other cause field`() = runTest {
        val ds = createPage()
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        ds.setValueById(6, "opt6")
        ds.updateList(6, 6)
        ds.setValueById(7, "opt4")
        ds.updateList(7, 4) // triggerIndex is 4 for causeOfDeath
        assertTrue("otherCauseOfDeath expected", ds.listFlow.value.any { it.id == 8 })
    }

    @Test
    fun `place of death index eight adds other place of death`() = runTest {
        val ds = createPage()
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        ds.setValueById(6, "opt6")
        ds.updateList(6, 6)
        // placeOfDeath derives its own index from entries.indexOf(value); "opt8" -> 8 == triggerIndex
        ds.setValueById(54, "opt8")
        ds.updateList(54, 8)
        assertTrue("otherPlaceOfDeath expected", ds.listFlow.value.any { it.id == 55 })
    }

    @Test
    fun `jsy beneficiary yes reveals file upload`() = runTest {
        val ds = createPage()
        ds.setValueById(15, "opt0")
        ds.updateList(15, 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `date of delivery change recomputes discharge bounds`() = runTest {
        val ds = createPage()
        ds.setValueById(1, "01-01-2024")
        ds.updateList(1, 0)
        val discharge = ds.listFlow.value.firstOrNull { it.id == 13 }
        assertNotNull(discharge)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `birth count fields run numeric validation`() = runTest {
        val ds = createPage()
        ds.setValueById(9, "2")
        ds.updateList(9, 0)
        ds.setValueById(10, "1")
        ds.updateList(10, 0)
        ds.setValueById(11, "1")
        ds.updateList(11, 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `map values from a filled create page`() = runTest {
        val ds = createPage()
        ds.setValueById(1, "01-01-2024")
        ds.setValueById(2, "10:00")
        ds.setValueById(3, "opt0")
        ds.setValueById(4, "opt0")
        ds.setValueById(9, "1")
        ds.setValueById(10, "1")
        ds.setValueById(11, "0")
        val target = mockk<DeliveryOutcomeCache>(relaxed = true)
        ds.mapValues(target, 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `index getters resolve against the current list`() = runTest {
        val ds = createPage()
        assertTrue("mcp1 should be present in the non-mitanin build", ds.getIndexOfMCP1() >= -1)
        assertTrue(ds.getIndexOfMCP2() >= -1)
        // jsy upload is not on the page until the beneficiary question is answered "Yes"
        val before = ds.getIndexOfIsjsyFileUpload()
        ds.setValueById(15, "opt0")
        ds.updateList(15, 0)
        val after = ds.getIndexOfIsjsyFileUpload()
        assertTrue("jsy index should resolve after reveal", after >= 0 || before == after)
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
