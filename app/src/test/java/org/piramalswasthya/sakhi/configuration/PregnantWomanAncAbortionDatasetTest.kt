package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PregnantWomanAncAbortionDatasetTest : BaseViewModelTest() {

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
    fun pregnantWomanAncAbortionDeep() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, lastAnc, null) }
        runCatching { ds.setUpPage(ben, lastAnc, saved) }
        runCatching { ds.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfAbortionDischarge1() }
        runCatching { ds.getIndexOfAbortionDischarge2() }
        assertNotNull(ds.listFlow)
    }

    private fun PregnantWomanAncAbortionDataset.valueOf(id: Int): String? =
        listFlow.value.firstOrNull { it.id == id }?.value

    private val isMitanin = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)

    private fun regisMock(
        lmpDate: Long?,
        ancDate: Long = 1_600_000_000_000L,
        abortionDate: Long? = null,
        abortionType: String? = null,
        abortionFacility: String? = null,
    ): PregnantWomanAncCache {
        val m = mockk<PregnantWomanAncCache>(relaxed = true)
        every { m.lmpDate } returns lmpDate
        every { m.ancDate } returns ancDate
        every { m.abortionDate } returns abortionDate
        every { m.abortionType } returns abortionType
        every { m.abortionFacility } returns abortionFacility
        return m
    }

    private fun savedMock(
        isPaiucdId: Int,
        isYesOrNo: Boolean? = null,
        dateSterilisation: Long? = null,
        abortionDate: Long? = 1_650_000_000_000L,
        ancDate: Long = 1_610_000_000_000L,
        abortionType: String? = "opt1",
        abortionFacility: String? = "opt2",
        visitDate: Long? = 1_620_000_000_000L,
        serialNo: String? = "12345",
        methodOfTermination: String? = "opt3",
        terminationDoneBy: String? = "opt4",
        remarks: String? = "note",
        abortionImg1: String? = "img1",
        abortionImg2: String? = "img2",
    ): PregnantWomanAncCache {
        val m = mockk<PregnantWomanAncCache>(relaxed = true)
        every { m.isPaiucdId } returns isPaiucdId
        every { m.isYesOrNo } returns isYesOrNo
        every { m.dateSterilisation } returns dateSterilisation
        every { m.abortionDate } returns abortionDate
        every { m.ancDate } returns ancDate
        every { m.abortionType } returns abortionType
        every { m.abortionFacility } returns abortionFacility
        every { m.visitDate } returns visitDate
        every { m.serialNo } returns serialNo
        every { m.methodOfTermination } returns methodOfTermination
        every { m.terminationDoneBy } returns terminationDoneBy
        every { m.remarks } returns remarks
        every { m.abortionImg1 } returns abortionImg1
        every { m.abortionImg2 } returns abortionImg2
        return m
    }

    @Test
    fun `setUpPage create path with null lmpDate falls back to default bounds and null abortion date`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = null, abortionDate = null)
        ds.setUpPage(ben, lastAnc, null)
        assertNotEquals(-1, ds.getIndexById(2))
        assertEquals("0", ds.valueOf(2))
        assertNull(ds.valueOf(3))
    }

    @Test
    fun `setUpPage create path with lmpDate computes week count and maps abortion fields`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(
            lmpDate = 1_500_000_000_000L,
            ancDate = 1_600_000_000_000L,
            abortionDate = 1_650_000_000_000L,
            abortionType = "opt1",
            abortionFacility = "opt2",
        )
        ds.setUpPage(ben, lastAnc, null)
        assertNotNull(ds.valueOf(3))
        assertEquals("opt1", ds.valueOf(4))
        assertEquals("opt2", ds.valueOf(5))
    }

    @Test
    fun `setUpPage edit path with isPaiucdId zero omits confirmation and sterilisation fields`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 0)
        ds.setUpPage(ben, lastAnc, saved)
        assertEquals(-1, ds.getIndexById(23))
        assertEquals(-1, ds.getIndexById(24))
        assertNull(ds.valueOf(9))
    }

    @Test
    fun `setUpPage edit path with isPaiucdId one selects first option and adds confirmation only`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 1)
        ds.setUpPage(ben, lastAnc, saved)
        assertEquals("opt0", ds.valueOf(9))
        assertNotEquals(-1, ds.getIndexById(23))
        assertEquals(-1, ds.getIndexById(24))
    }

    @Test
    fun `setUpPage edit path with isPaiucdId two confirmed yes adds sterilisation date`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 2, isYesOrNo = true, dateSterilisation = 1_600_000_000_000L)
        ds.setUpPage(ben, lastAnc, saved)
        assertEquals("opt1", ds.valueOf(9))
        assertNotEquals(-1, ds.getIndexById(23))
        assertNotEquals(-1, ds.getIndexById(24))
        assertNotNull(ds.valueOf(24))
    }

    @Test
    fun `setUpPage edit path with isPaiucdId two confirmed no skips sterilisation date`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 2, isYesOrNo = false)
        ds.setUpPage(ben, lastAnc, saved)
        assertNotEquals(-1, ds.getIndexById(23))
        assertEquals(-1, ds.getIndexById(24))
    }

    @Test
    fun `setUpPage edit path maps saved fields onto visible elements`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 0)
        ds.setUpPage(ben, lastAnc, saved)
        assertEquals("12345", ds.valueOf(6))
        assertEquals("opt3", ds.valueOf(7))
        assertEquals("opt4", ds.valueOf(8))
        assertEquals("note", ds.valueOf(10))
        if (isMitanin) {
            assertNull(ds.valueOf(21))
            assertNull(ds.valueOf(22))
        } else {
            assertEquals("img1", ds.valueOf(21))
            assertEquals("img2", ds.valueOf(22))
        }
    }

    @Test
    fun `handleListOnValueChanged isPaiucd branch resets confirmation and adds it to the list`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        ds.setValueById(9, "opt1")
        ds.updateList(9, 0)
        assertNotEquals(-1, ds.getIndexById(23))
        assertNull(ds.valueOf(23))
    }

    @Test
    fun `handleListOnValueChanged serialNoAsPerAdmission validates via validateIntMinMax`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        ds.setValueById(6, "abc")
        ds.updateList(6, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `handleListOnValueChanged isYesOrNo yes with second paiucd option adds sterilisation date`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 2, isYesOrNo = false)
        ds.setUpPage(ben, lastAnc, saved)
        ds.setValueById(9, "opt1")
        ds.setValueById(23, "opt0")
        ds.updateList(23, 0)
        assertNotEquals(-1, ds.getIndexById(24))
    }

    @Test
    fun `handleListOnValueChanged isYesOrNo removes sterilisation date when paiucd is first option`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 2, isYesOrNo = true, dateSterilisation = 1_600_000_000_000L)
        ds.setUpPage(ben, lastAnc, saved)
        assertNotEquals(-1, ds.getIndexById(24))
        ds.setValueById(9, "opt0")
        ds.setValueById(23, "opt0")
        ds.updateList(23, 0)
        assertEquals(-1, ds.getIndexById(24))
    }

    @Test
    fun `handleListOnValueChanged else branch on unmapped id is a no-op`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        ds.updateList(9999, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues nulls the termination and doneBy ids when nothing was selected`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = null, abortionDate = null)
        ds.setUpPage(ben, lastAnc, null)
        val form = mockk<PregnantWomanAncCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.methodOfTerminationId = null }
        verify { form.terminationDoneById = null }
    }

    @Test
    fun `mapValues maps every saved field including termination position offsets`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        val saved = savedMock(isPaiucdId = 1, methodOfTermination = "opt3", terminationDoneBy = "opt4")
        ds.setUpPage(ben, lastAnc, saved)
        val form = mockk<PregnantWomanAncCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.serialNo = "12345" }
        verify { form.remarks = "note" }
        verify { form.abortionImg1 = "img1" }
        verify { form.abortionImg2 = "img2" }
        verify { form.isPaiucd = "opt0" }
        verify { form.isPaiucdId = 1 }
        verify { form.methodOfTerminationId = 3 }
        verify { form.terminationDoneById = 4 }
    }

    @Test
    fun `setImageUriToFormElement stores the uri string on discharge summary one`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://img1"
        ds.setImageUriToFormElement(21, uri)
        if (isMitanin) {
            assertNull(ds.valueOf(21))
        } else {
            assertEquals("content://img1", ds.valueOf(21))
        }
    }

    @Test
    fun `setImageUriToFormElement stores the uri string on discharge summary two`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://img2"
        ds.setImageUriToFormElement(22, uri)
        if (isMitanin) {
            assertNull(ds.valueOf(22))
        } else {
            assertEquals("content://img2", ds.valueOf(22))
        }
    }

    @Test
    fun `setImageUriToFormElement ignores an unmapped form id`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://ignored"
        ds.setImageUriToFormElement(999, uri)
        assertNull(ds.valueOf(21))
        assertNull(ds.valueOf(22))
    }

    @Test
    fun `bp toggle resets to false`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        ds.resetBpToggle()
        assertFalse(ds.triggerBpToggle())
    }

    @Test
    fun `getWeeksOfPregnancy and discharge indices resolve after setup`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = regisMock(lmpDate = 1_500_000_000_000L)
        ds.setUpPage(ben, lastAnc, null)
        assertEquals(1, ds.getWeeksOfPregnancy())
        if (isMitanin) {
            assertEquals(-1, ds.getIndexOfAbortionDischarge1())
            assertEquals(-1, ds.getIndexOfAbortionDischarge2())
        } else {
            assertNotEquals(-1, ds.getIndexOfAbortionDischarge1())
            assertNotEquals(-1, ds.getIndexOfAbortionDischarge2())
        }
    }
}
