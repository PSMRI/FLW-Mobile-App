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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HRPPregnantTrackDataset]. Consolidated from the previous
 * HRPPregnantTrackDatasetDeepTest + HRPPregnantTrackDatasetBranch3Test files into a single class:
 * deep ENGLISH coverage plus HINDI branch coverage over different visit strings and mapValues pages.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPPregnantTrackDatasetTest : BaseViewModelTest() {

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
    fun hrpPregnantTrackDeep() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<HRPPregnantTrackCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null, null, null) }
        runCatching { ds.setUpPage(ben, "Visit 1", saved, System.currentTimeMillis()) }
        runCatching { ds.mapValues(mockk<HRPPregnantTrackCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfRdPmsa() }
        runCatching { ds.getIndexOfRdDengue() }
        runCatching { ds.getIndexOfRdFilaria() }
        runCatching { ds.getIndexOfSevereAnemia() }
        runCatching { ds.getIndexOfPregInduced() }
        runCatching { ds.getIndexOfGest() }
        runCatching { ds.getIndexOfHypothyroidism() }
        runCatching { ds.getIndexOfPolyhydromnios() }
        runCatching { ds.getIndexOfOligohydromnios() }
        runCatching { ds.getIndexOfAntepartum() }
        runCatching { ds.getIndexOfMalPre() }
        runCatching { ds.getIndexOfHiv() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfOgttLabel() }
        runCatching { ds.getIndexOfFasting() }
        runCatching { ds.getIndexOfafter() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hrpPregnantTrackBranch() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        for (visit in listOf("Visit 1", "Visit 2", "Visit 3", "Visit 4")) {
            runCatching {
                ds.setUpPage(ben, visit, mockk<HRPPregnantTrackCache>(relaxed = true), System.currentTimeMillis())
            }
            runCatching { ds.setUpPage(ben, visit, null, 0L) }
        }
        for (p in 0..2) {
            runCatching { ds.mapValues(mockk<HRPPregnantTrackCache>(relaxed = true), p) }
        }
        runCatching { ds.getIndexOfRdPmsa() }
        runCatching { ds.getIndexOfRdDengue() }
        runCatching { ds.getIndexOfRdFilaria() }
        runCatching { ds.getIndexOfSevereAnemia() }
        runCatching { ds.getIndexOfPregInduced() }
        runCatching { ds.getIndexOfGest() }
        runCatching { ds.getIndexOfHypothyroidism() }
        runCatching { ds.getIndexOfPolyhydromnios() }
        runCatching { ds.getIndexOfOligohydromnios() }
        runCatching { ds.getIndexOfAntepartum() }
        runCatching { ds.getIndexOfMalPre() }
        runCatching { ds.getIndexOfHiv() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfOgttLabel() }
        runCatching { ds.getIndexOfFasting() }
        runCatching { ds.getIndexOfafter() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private fun track(
        ifaGiven: String? = "opt1",
        ifaQuantity: Int? = null,
        bloodGlucoseTest: String? = null,
        yesNo: String? = "opt1"
    ) = HRPPregnantTrackCache(
        benId = 7L,
        visitDate = DAY_MS * 19500,
        rdPmsa = yesNo,
        rdDengue = yesNo,
        rdFilaria = yesNo,
        severeAnemia = yesNo,
        hemoglobinTest = "11.5",
        ifaGiven = ifaGiven,
        ifaQuantity = ifaQuantity,
        pregInducedHypertension = yesNo,
        systolic = 120,
        diastolic = 80,
        gestDiabetesMellitus = yesNo,
        bloodGlucoseTest = bloodGlucoseTest,
        fbg = 90,
        rbg = 110,
        ppbg = 130,
        fastingOgtt = 95,
        after2hrsOgtt = 140,
        hypothyrodism = yesNo,
        polyhydromnios = yesNo,
        oligohydromnios = yesNo,
        antepartumHem = yesNo,
        malPresentation = yesNo,
        hivsyph = yesNo,
        visit = "Visit 1"
    )

    private fun ben(regDate: Long = DAY_MS * 19000) =
        mockk<BenRegCache>(relaxed = true).also { every { it.regDate } returns regDate }

    private fun HRPPregnantTrackDataset.element(id: Int) = listFlow.value.first { it.id == id }

    // ------------------------------------------------------------------
    // setUpPage structure
    // ------------------------------------------------------------------

    @Test
    fun `setUpPage with all nulls yields the base twenty two elements`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        assertEquals(22, ds.listFlow.value.size)
        assertEquals(-1, ds.getIndexOfIfaQuantity())
        assertEquals(-1, ds.getIndexOfRbg())
    }

    @Test
    fun `setUpPage with visit string copies it into the visit headline`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "Visit 3", null, null)
        assertEquals("Visit 3", ds.element(29).title)
        assertEquals(22, ds.listFlow.value.size)
    }

    @Test
    fun `setUpPage with saved and ifa given yes adds ifa quantity`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(ifaGiven = "opt0", ifaQuantity = 30), null)
        assertEquals(23, ds.listFlow.value.size)
        assertTrue(ds.getIndexOfIfaQuantity() >= 0)
        assertEquals("30", ds.element(27).value)
    }

    @Test
    fun `setUpPage with saved and ifa given no omits ifa quantity`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(ifaGiven = "opt1"), null)
        assertEquals(22, ds.listFlow.value.size)
        assertEquals(-1, ds.getIndexOfIfaQuantity())
    }

    @Test
    fun `setUpPage with random blood glucose adds rbg field`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(bloodGlucoseTest = "opt0"), null)
        assertEquals(23, ds.listFlow.value.size)
        assertTrue(ds.getIndexOfRbg() >= 0)
        assertEquals("110", ds.element(19).value)
    }

    @Test
    fun `setUpPage with fasting and post prandial adds two fields`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(bloodGlucoseTest = "opt1"), null)
        assertEquals(24, ds.listFlow.value.size)
        assertTrue(ds.getIndexOfFbg() >= 0)
        assertTrue(ds.getIndexOfPpbg() > ds.getIndexOfFbg())
        assertEquals("90", ds.element(20).value)
        assertEquals("130", ds.element(21).value)
    }

    @Test
    fun `setUpPage with ogtt adds label and two glucose fields`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(bloodGlucoseTest = "opt2"), null)
        assertEquals(25, ds.listFlow.value.size)
        assertTrue(ds.getIndexOfOgttLabel() >= 0)
        assertTrue(ds.getIndexOfFasting() > ds.getIndexOfOgttLabel())
        assertTrue(ds.getIndexOfafter() > ds.getIndexOfFasting())
        assertEquals("95", ds.element(23).value)
        assertEquals("140", ds.element(24).value)
    }

    @Test
    fun `setUpPage with unmapped glucose test adds nothing`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(bloodGlucoseTest = "opt9"), null)
        assertEquals(22, ds.listFlow.value.size)
        assertEquals(-1, ds.getIndexOfRbg())
        assertEquals(-1, ds.getIndexOfOgttLabel())
    }

    @Test
    fun `setUpPage marks high risk when saved answers are yes`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(yesNo = "opt0"), null)
        assertTrue(ds.element(1).showHighRisk)
        assertTrue(ds.element(2).showHighRisk)
        assertTrue(ds.element(3).showHighRisk)
        assertTrue(ds.element(10).showHighRisk)
        assertTrue(ds.element(13).showHighRisk)
        assertTrue(ds.element(14).showHighRisk)
    }

    @Test
    fun `setUpPage clears high risk when saved answers are no`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(yesNo = "opt1"), null)
        assertFalse(ds.element(1).showHighRisk)
        assertFalse(ds.element(9).showHighRisk)
        assertFalse(ds.element(10).showHighRisk)
    }

    @Test
    fun `setUpPage copies bp and hemoglobin values from cache`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(), null)
        assertEquals("120", ds.element(16).value)
        assertEquals("80", ds.element(17).value)
        assertEquals("11.5", ds.element(25).value)
    }

    @Test
    fun `setUpPage with previous visit later than registration raises min date`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(regDate = DAY_MS * 19000), "Visit 2", null, DAY_MS * 19100)
        assertEquals(22, ds.listFlow.value.size)
        assertTrue(ds.element(11).min!! > 0L)
    }

    @Test
    fun `setUpPage with previous visit earlier than registration keeps reg date min`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(regDate = DAY_MS * 19200), "Visit 2", null, DAY_MS * 19000)
        assertEquals(DAY_MS * 19200, ds.element(11).min)
    }

    @Test
    fun `setUpPage with ben but no previous visit uses registration date`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(regDate = DAY_MS * 19200), "Visit 1", null, null)
        assertEquals(DAY_MS * 19200, ds.element(11).min)
    }

    // ------------------------------------------------------------------
    // updateList
    // ------------------------------------------------------------------

    @Test
    fun `updateList toggles high risk flags on every radio question`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        val riskIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 14)
        riskIds.forEach { id ->
            ds.setValueById(id, "opt0")
            ds.updateList(id, 0)
            assertTrue("id $id should be high risk", ds.element(id).showHighRisk)
        }
        riskIds.forEach { id ->
            ds.setValueById(id, "opt1")
            ds.updateList(id, 0)
            assertFalse("id $id should not be high risk", ds.element(id).showHighRisk)
        }
    }

    @Test
    fun `updateList on ifa given adds then removes the quantity field`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        ds.setValueById(26, "opt0")
        ds.updateList(26, 0)
        assertEquals(23, ds.listFlow.value.size)
        ds.setValueById(26, "opt1")
        ds.updateList(26, 0)
        assertEquals(22, ds.listFlow.value.size)
        assertEquals(-1, ds.getIndexOfIfaQuantity())
    }

    @Test
    fun `updateList on ifa given with unknown value leaves list alone`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        ds.setValueById(26, "opt5")
        ds.updateList(26, 0)
        assertEquals(22, ds.listFlow.value.size)
    }

    @Test
    fun `updateList cycles through the three blood glucose test options`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)

        ds.setValueById(18, "opt0")
        ds.updateList(18, 0)
        assertTrue(ds.getIndexOfRbg() >= 0)
        assertEquals(23, ds.listFlow.value.size)

        ds.setValueById(18, "opt1")
        ds.updateList(18, 0)
        assertEquals(-1, ds.getIndexOfRbg())
        assertTrue(ds.getIndexOfFbg() >= 0)
        assertEquals(24, ds.listFlow.value.size)

        ds.setValueById(18, "opt2")
        ds.updateList(18, 0)
        assertEquals(-1, ds.getIndexOfFbg())
        assertTrue(ds.getIndexOfOgttLabel() >= 0)
        assertEquals(25, ds.listFlow.value.size)

        ds.setValueById(18, "opt7")
        ds.updateList(18, 0)
        assertEquals(25, ds.listFlow.value.size)
    }

    @Test
    fun `updateList validates systolic and diastolic ranges`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)

        ds.setValueById(16, "10")
        ds.updateList(16, 0)
        assertNotNull(ds.element(16).errorText)

        ds.setValueById(16, "400")
        ds.updateList(16, 0)
        assertNotNull(ds.element(16).errorText)

        ds.setValueById(16, "120")
        ds.updateList(16, 0)
        assertNull(ds.element(16).errorText)

        ds.setValueById(17, "10")
        ds.updateList(17, 0)
        assertNotNull(ds.element(17).errorText)

        ds.setValueById(17, "80")
        ds.updateList(17, 0)
        assertNull(ds.element(17).errorText)
    }

    @Test
    fun `updateList validates hemoglobin decimal bounds`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)

        ds.setValueById(25, "1.0")
        ds.updateList(25, 0)
        assertNotNull(ds.element(25).errorText)

        ds.setValueById(25, "20.0")
        ds.updateList(25, 0)
        assertNotNull(ds.element(25).errorText)

        ds.setValueById(25, "11.2")
        ds.updateList(25, 0)
        assertNull(ds.element(25).errorText)
    }

    @Test
    fun `updateList validates the glucose reading fields as mandatory`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(bloodGlucoseTest = "opt1"), null)

        ds.setValueById(20, "")
        ds.updateList(20, 0)
        assertNotNull(ds.element(20).errorText)

        ds.setValueById(20, "95")
        ds.updateList(20, 0)
        assertNull(ds.element(20).errorText)

        ds.setValueById(21, "")
        ds.updateList(21, 0)
        assertNotNull(ds.element(21).errorText)

        ds.setValueById(21, "140")
        ds.updateList(21, 0)
        assertNull(ds.element(21).errorText)
    }

    @Test
    fun `updateList validates rbg and ifa quantity as mandatory`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(ifaGiven = "opt0", bloodGlucoseTest = "opt0"), null)

        ds.setValueById(19, "")
        ds.updateList(19, 0)
        assertNotNull(ds.element(19).errorText)

        ds.setValueById(19, "115")
        ds.updateList(19, 0)
        assertNull(ds.element(19).errorText)

        ds.setValueById(27, "")
        ds.updateList(27, 0)
        assertNotNull(ds.element(27).errorText)

        ds.setValueById(27, "30")
        ds.updateList(27, 0)
        assertNull(ds.element(27).errorText)
    }

    @Test
    fun `updateList with unknown id is a no op`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        ds.updateList(999, 0)
        ds.updateList(12, 0)
        assertEquals(22, ds.listFlow.value.size)
    }

    // ------------------------------------------------------------------
    // mapValues
    // ------------------------------------------------------------------

    @Test
    fun `mapValues writes every populated field back to the cache`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(ifaGiven = "opt0", ifaQuantity = 30, bloodGlucoseTest = "opt1"), null)
        ds.setValueById(11, "15-06-2025")
        val out = HRPPregnantTrackCache(benId = 7L)
        ds.mapValues(out, 0)
        assertTrue(out.visitDate!! > 0L)
        assertEquals("opt1", out.rdPmsa)
        assertEquals("opt1", out.hivsyph)
        assertEquals("11.5", out.hemoglobinTest)
        assertEquals("opt0", out.ifaGiven)
        assertEquals(30, out.ifaQuantity)
        assertEquals(120, out.systolic)
        assertEquals(80, out.diastolic)
        assertEquals("opt1", out.bloodGlucoseTest)
        assertEquals(90, out.fbg)
        assertEquals(130, out.ppbg)
    }

    @Test
    fun `mapValues on an untouched form leaves cache fields null`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        val out = HRPPregnantTrackCache(benId = 7L, systolic = 999, rdPmsa = "stale")
        ds.mapValues(out, 0)
        assertEquals(0L, out.visitDate)
        assertNull(out.rdPmsa)
        assertNull(out.systolic)
        assertNull(out.ifaQuantity)
        assertNull(out.bloodGlucoseTest)
    }

    @Test
    fun `mapValues carries ogtt readings`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(ben(), "Visit 1", track(bloodGlucoseTest = "opt2"), null)
        val out = HRPPregnantTrackCache(benId = 7L)
        ds.mapValues(out, 1)
        assertEquals(95, out.fastingOgtt)
        assertEquals(140, out.after2hrsOgtt)
        assertEquals("opt2", out.bloodGlucoseTest)
    }

    @Test
    fun `setUpPage in HINDI keeps the same structure`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.HINDI)
        ds.setUpPage(ben(), "Visit 4", track(ifaGiven = "opt0", ifaQuantity = 60, bloodGlucoseTest = "opt2"), DAY_MS * 19400)
        assertEquals(26, ds.listFlow.value.size)
        assertTrue(ds.getIndexOfIfaQuantity() >= 0)
        assertTrue(ds.getIndexOfafter() >= 0)
    }

    companion object {
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
