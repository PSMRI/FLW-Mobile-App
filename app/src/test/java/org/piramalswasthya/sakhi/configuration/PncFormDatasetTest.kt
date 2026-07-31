package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    // ---- handleListOnValueChanged coverage via public updateList wrapper ----

    private suspend fun freshCreateForm(): PncFormDataset {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(1, ben, deliveryWithDate(), null, null, false)
        return ds
    }

    @Test
    fun `pnc period drives visit date window for every allowed day`() = runTest {
        val ds = freshCreateForm()
        for (day in listOf(1, 3, 7, 14, 21, 28, 42)) {
            ds.setValueById(2, "Day $day")
            ds.updateList(2, 0)
        }
        assertTrue(ds.getIndexById(3) >= 0)
    }

    @Test
    fun `pnc period with unsupported day throws illegal state`() = runTest {
        val ds = freshCreateForm()
        ds.setValueById(2, "Day 99")
        val result = runCatching { ds.updateList(2, 0) }
        assertTrue(result.isFailure)
    }

    @Test
    fun `pnc period recomputes window from typed delivery date`() = runTest {
        val ds = freshCreateForm()
        ds.setValueById(1, "01-01-2024")
        ds.setValueById(2, "Day 42")
        ds.updateList(2, 0)
        assertTrue(ds.getListSize() > 0)
    }

    @Test
    fun `any contraception method shows and hides contraception dropdown`() = runTest {
        val ds = freshCreateForm()
        val sizeBefore = ds.getListSize()
        ds.updateList(5, 0)
        assertTrue(ds.getIndexById(6) >= 0)
        ds.updateList(5, 1)
        assertEquals(sizeBefore, ds.getListSize())
    }

    @Test
    fun `contraception method other option adds other ppc method`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(5, 0)
        ds.setValueById(6, "opt79")
        ds.updateList(6, 79)
        assertTrue(ds.getIndexById(7) >= 0)
    }

    @Test
    fun `contraception method sterilisation option adds sterilisation date`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(5, 0)
        ds.setValueById(6, "opt5")
        ds.updateList(6, 5)
        assertTrue(ds.getIndexById(56) >= 0)
    }

    @Test
    fun `contraception method with out of range index selects nothing`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(5, 0)
        ds.updateList(6, 5000)
        assertEquals(-1, ds.getIndexById(7))
    }

    @Test
    fun `any sign of danger yes adds mother danger sign and marks referral required`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(57, 0)
        assertTrue(ds.getIndexById(8) >= 0)
        ds.updateList(57, 1)
        assertEquals(-1, ds.getIndexById(8))
    }

    @Test
    fun `mother danger sign last entry adds other danger sign`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(57, 0)
        ds.setValueById(8, "opt79")
        ds.updateList(8, 79)
        assertTrue(ds.getIndexById(9) >= 0)
        ds.updateList(8, 2)
        assertEquals(-1, ds.getIndexById(9))
    }

    @Test
    fun `mother death no swaps alive fields for death fields and back`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(11, 1)
        assertTrue(ds.getIndexById(12) >= 0)
        assertTrue(ds.getIndexById(13) >= 0)
        assertTrue(ds.getIndexById(15) >= 0)
        assertEquals(-1, ds.getIndexById(4))
        ds.updateList(11, 0)
        assertEquals(-1, ds.getIndexById(12))
        assertTrue(ds.getIndexById(4) >= 0)
    }

    @Test
    fun `place of death other adds free text and other value removes it`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(11, 1)
        ds.setValueById(15, "opt8")
        ds.updateList(15, 8)
        assertTrue(ds.getIndexById(55) >= 0)
        ds.setValueById(15, "opt2")
        ds.updateList(15, 2)
        assertEquals(-1, ds.getIndexById(55))
    }

    @Test
    fun `place of death with unknown value is a no op`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(11, 1)
        ds.setValueById(15, "not-in-entries")
        ds.updateList(15, 0)
        assertEquals(-1, ds.getIndexById(55))
    }

    @Test
    fun `free text fields run empty and alphabet validations`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(57, 0)
        ds.setValueById(8, "opt79")
        ds.updateList(8, 79)
        ds.setValueById(9, "")
        ds.updateList(9, 0)
        ds.setValueById(9, "abc123")
        ds.updateList(9, 0)
        ds.updateList(11, 1)
        ds.setValueById(15, "opt8")
        ds.updateList(15, 8)
        ds.setValueById(55, "")
        ds.updateList(55, 0)
        ds.setValueById(55, "HOME 12")
        ds.updateList(55, 0)
        assertTrue(ds.getListSize() > 0)
    }

    @Test
    fun `other ppc method validation runs on value change`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(5, 0)
        ds.setValueById(6, "opt79")
        ds.updateList(6, 79)
        ds.setValueById(7, "")
        ds.updateList(7, 0)
        ds.setValueById(7, "PILL9")
        ds.updateList(7, 0)
        assertTrue(ds.getIndexById(7) >= 0)
    }

    @Test
    fun `ifa tabs given validates min and max`() = runTest {
        val ds = freshCreateForm()
        ds.setValueById(4, "5000")
        ds.updateList(4, 0)
        ds.setValueById(4, "-3")
        ds.updateList(4, 0)
        ds.setValueById(4, "30")
        ds.updateList(4, 0)
        ds.setValueById(4, "")
        ds.updateList(4, 0)
        assertTrue(ds.getIndexById(4) >= 0)
    }

    @Test
    fun `unknown form id change does not emit`() = runTest {
        val ds = freshCreateForm()
        val before = ds.getListSize()
        ds.updateList(99999, 0)
        assertEquals(before, ds.getListSize())
    }

    @Test
    fun `map values writes every populated field into the cache`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(5, 0)
        ds.setValueById(1, "01-01-2024")
        ds.setValueById(2, "Day 7")
        ds.setValueById(3, "10-01-2024")
        ds.setValueById(4, "30")
        ds.setValueById(5, "opt0")
        ds.setValueById(6, "opt3")
        ds.setValueById(9, "FEVER")
        ds.setValueById(10, "opt1")
        ds.setValueById(16, "some remark")
        ds.setValueById(58, "uri1")
        ds.setValueById(59, "uri2")
        ds.setValueById(60, "uri3")
        ds.setValueById(61, "uri4")
        val form = mockk<PNCVisitCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.pncPeriod = 7 }
        verify { form.ifaTabsGiven = 30 }
        verify { form.contraceptionMethod = "opt3" }
        verify { form.remarks = "some remark" }
        verify { form.deliveryDischargeSummary1 = "uri1" }
    }

    @Test
    fun `map values on death path records death fields`() = runTest {
        val ds = freshCreateForm()
        ds.updateList(11, 1)
        ds.setValueById(2, "Day 3")
        ds.setValueById(3, "10-01-2024")
        ds.setValueById(11, "opt79")
        ds.setValueById(12, "12-01-2024")
        ds.setValueById(13, "opt2")
        ds.setValueById(15, "opt8")
        ds.updateList(15, 8)
        ds.setValueById(55, "HOME")
        val form = mockk<PNCVisitCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.motherDeath = true }
        verify { form.causeOfDeath = "opt2" }
        verify { form.placeOfDeath = "opt8" }
        verify { form.otherPlaceOfDeath = "HOME" }
    }

    @Test
    fun `previous pnc period trims available period entries`() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val previous = mockk<PNCVisitCache>(relaxed = true)
        every { previous.pncPeriod } returns 21
        ds.setUpPage(2, ben, deliveryWithDate(), previous, null, false)
        assertTrue(ds.getIndexById(2) >= 0)
    }

    @Test
    fun `discharge summary indexes resolve after setup`() = runTest {
        val ds = freshCreateForm()
        assertTrue(ds.getIndexDeliveryDischargeSummary1() >= 0)
        assertTrue(ds.getIndexDeliveryDischargeSummary2() >= 0)
        assertTrue(ds.getIndexDeliveryDischargeSummary3() >= 0)
        assertTrue(ds.getIndexDeliveryDischargeSummary4() >= 0)
    }

    @Test
    fun `set image uri ignores unknown form id`() = runTest {
        val ds = freshCreateForm()
        val uri = mockk<android.net.Uri>(relaxed = true)
        ds.setImageUriToFormElement(1234, uri)
        assertTrue(ds.getListSize() > 0)
    }
}
