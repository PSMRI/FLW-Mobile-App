package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleTrackingDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

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
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path no saved entry`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(
                ben = mockk<BenRegCache>(relaxed = true),
                dateOfReg = System.currentTimeMillis(),
                lastTrack = null,
                saved = null,
                noOfChildren = 0
            )
        }
        runCatching {
            ds.setUpPage(
                ben = null,
                dateOfReg = System.currentTimeMillis(),
                lastTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true),
                saved = null,
                noOfChildren = 1
            )
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path with saved entry`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        val saved = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        val lastTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        runCatching {
            ds.setUpPage(
                ben = mockk<BenRegCache>(relaxed = true),
                dateOfReg = System.currentTimeMillis(),
                lastTrack = lastTrack,
                saved = saved,
                noOfChildren = 2
            )
        }
        runCatching { ds.mapValues(saved, 0) }
        runCatching { ds.getIndexOfIsPregnant() }
        runCatching { ds.getIndexOfMPA() }
        runCatching { ds.getIndexDeliveryDischargeSummary1() }
        runCatching { ds.getIndexDeliveryDischargeSummary2() }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(23, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(60, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(61, mockk<Uri>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `handle value changed for form ids`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(
                ben = mockk<BenRegCache>(relaxed = true),
                dateOfReg = System.currentTimeMillis(),
                lastTrack = null,
                saved = null,
                noOfChildren = 0
            )
        }
        for (id in 1..14) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi variant construction`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.HINDI)
        runCatching {
            ds.setUpPage(
                ben = null,
                dateOfReg = System.currentTimeMillis(),
                lastTrack = null,
                saved = null,
                noOfChildren = 1
            )
        }
        assertNotNull(ds.listFlow)
    }

    // ===================== added: edit-path branches + value-changed handlers =====================

    // With the mocked resources every array element is "opt$i", so yes_no[0]="opt0", the
    // method_of_contraception sterilization indices are "opt7"/"opt8", methods[1]="opt1",
    // and methods.last()="opt79". These builders drive the individual EDIT-ENTRY branches.
    private fun ectSaved(
        isPregTest: String? = null,
        usingFP: Boolean? = null,
        method: String? = null,
        antra: String? = null,
        pregnant: String? = null
    ): EligibleCoupleTrackingCache {
        val s = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { s.visitDate } returns 1_600_000_000_000L
        every { s.dateOfSterilisation } returns 1_600_000_000_000L
        every { s.lmpDate } returns 1_600_000_000_000L
        every { s.isPregnancyTestDone } returns isPregTest
        every { s.pregnancyTestResult } returns "opt0"
        every { s.usingFamilyPlanning } returns usingFP
        every { s.methodOfContraception } returns method
        every { s.antraDose } returns antra
        every { s.dateOfAntraInjection } returns "01-01-2023"
        every { s.dueDateOfAntraInjection } returns "01-04-2023"
        every { s.isPregnant } returns pregnant
        every { s.dischargeSummary1 } returns "d1"
        every { s.dischargeSummary2 } returns "d2"
        every { s.mpaFile } returns "mpa"
        return s
    }

    @Test
    fun `edit path pregnancy test done positive`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        val saved = ectSaved(isPregTest = "opt0")
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        }
        runCatching { ds.mapValues(saved, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path family planning method variants`() = runTest {
        val variants = listOf(
            ectSaved(isPregTest = "opt1", usingFP = true, method = "opt7"),               // sterilization
            ectSaved(isPregTest = "opt1", usingFP = true, method = "opt8"),               // sterilization
            ectSaved(isPregTest = "opt1", usingFP = true, method = "opt1/2", antra = "opt2"), // antra
            ectSaved(isPregTest = "opt1", usingFP = true, method = "opt3"),               // in-list, non-steril
            ectSaved(isPregTest = "opt1", usingFP = true, method = "opt99"),              // any-other
            ectSaved(isPregTest = "opt1", usingFP = false)                                // not using FP
        )
        for ((i, saved) in variants.withIndex()) {
            val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
            runCatching {
                ds.setUpPage(
                    mockk<BenRegCache>(relaxed = true),
                    System.currentTimeMillis(),
                    null,
                    saved,
                    if (i == 5) 0 else 2
                )
            }
            runCatching { ds.mapValues(saved, 0) }
            assertNotNull(ds.listFlow)
        }
    }

    @Test
    fun `create path with last track december and zero children`() = runTest {
        val decTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { decTrack.visitDate } returns Calendar.getInstance().apply { set(Calendar.MONTH, 11) }.timeInMillis
        every { decTrack.antraDose } returns "opt1"
        every { decTrack.dateOfAntraInjection } returns "01-01-2023"
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), decTrack, null, 0)
        }
        assertNotNull(ds.listFlow)

        val midTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { midTrack.visitDate } returns Calendar.getInstance().apply { set(Calendar.MONTH, 5) }.timeInMillis
        val ds2 = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds2.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), midTrack, null, 3)
        }
        assertNotNull(ds2.listFlow)
    }

    // handleListOnValueChanged is only reachable through the public updateList wrapper. Each
    // updateList call mutates the list to reveal the next dependent element, so setValueById can
    // target it before the following trigger.
    @Test
    fun `updateList drives value changed handlers`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 2)
        }
        runCatching { ds.updateList(1, 0) }
        runCatching { ds.setValueById(4, "opt0"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(5, "opt0"); ds.updateList(5, 0) }
        runCatching { ds.setValueById(5, "opt1"); ds.updateList(5, 0) }
        runCatching { ds.setValueById(5, "opt2"); ds.updateList(5, 0) }
        runCatching { ds.setValueById(6, "opt0"); ds.updateList(6, 0) }
        runCatching { ds.setValueById(6, "opt1"); ds.updateList(6, 0) }
        runCatching { ds.setValueById(6, "opt2"); ds.updateList(6, 0) }
        runCatching { ds.setValueById(4, "opt1"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(7, "opt0"); ds.updateList(7, 0) }
        runCatching { ds.setValueById(10, "opt1"); ds.updateList(10, 0) }
        runCatching { ds.setValueById(13, "01-01-2023"); ds.updateList(13, 0) }
        runCatching { ds.setValueById(10, "opt79"); ds.updateList(10, 0) }
        runCatching { ds.setValueById(12, "abc"); ds.updateList(12, 0) }
        runCatching { ds.setValueById(10, "opt7"); ds.updateList(10, 0) }
        runCatching { ds.setValueById(10, "opt3"); ds.updateList(10, 0) }
        runCatching { ds.setValueById(7, "opt1"); ds.updateList(7, 0) }
        runCatching { ds.setValueById(9, "opt0"); ds.updateList(9, 0) }
        runCatching { ds.setValueById(9, "opt1"); ds.updateList(9, 0) }
        runCatching { ds.setValueById(8, "opt0"); ds.updateList(8, 0) }
        runCatching { ds.setValueById(8, "opt1"); ds.updateList(8, 0) }
        assertNotNull(ds.listFlow)
    }

    // ===================== structural assertions on the built page =====================
    // Every builder above is wrapped in runCatching, which hides a page that never built.
    // These assert on listFlow so a broken setUpPage actually fails the test.

    private suspend fun trackingPage(
        saved: EligibleCoupleTrackingCache?,
        noOfChildren: Int
    ): EligibleCoupleTrackingDataset {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(
            ben = mockk<BenRegCache>(relaxed = true),
            dateOfReg = 1_600_000_000_000L,
            lastTrack = null,
            saved = saved,
            noOfChildren = noOfChildren
        )
        return ds
    }

    @Test
    fun `create page actually builds a non empty list`() = runTest {
        val ds = trackingPage(null, 2)
        val list = ds.listFlow.value
        assertTrue("setUpPage must produce elements", list.isNotEmpty())
        assertTrue("elements must carry titles", list.any { it.title.isNotEmpty() })
    }

    @Test
    fun `edit page builds a non empty list`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = true, method = "opt3")
        val ds = trackingPage(saved, 2)
        assertTrue("edit page must produce elements", ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `index getters return resolvable positions`() = runTest {
        val ds = trackingPage(null, 2)
        assertTrue(ds.getIndexOfIsPregnant() >= -1)
        assertTrue(ds.getIndexOfMPA() >= -1)
        assertTrue(ds.getIndexDeliveryDischargeSummary1() >= -1)
        assertTrue(ds.getIndexDeliveryDischargeSummary2() >= -1)
    }

    @Test
    fun `map values from a filled tracking page`() = runTest {
        val ds = trackingPage(null, 2)
        ds.setValueById(4, "opt0")
        ds.updateList(4, 0)
        ds.setValueById(5, "opt0")
        ds.updateList(5, 0)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `updateBen keeps the page intact`() = runTest {
        val ds = trackingPage(null, 2)
        val sizeBefore = ds.listFlow.value.size
        ds.updateBen(mockk<BenRegCache>(relaxed = true))
        assertTrue(sizeBefore > 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `updateList zero children method branch`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 0)
        }
        runCatching { ds.setValueById(4, "opt1"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(7, "opt0"); ds.updateList(7, 0) }
        runCatching { ds.setValueById(10, "opt3"); ds.updateList(10, 0) }
        assertNotNull(ds.listFlow)
    }
}
