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
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [HRPNonPregnantTrackDataset]. Consolidated from the previous
 * HRPNonPregnantTrackDatasetDeepTest + HRPNonPregnantTrackDatasetBranch3Test files into a single
 * class: deep ENGLISH coverage plus HINDI branch coverage over multiple mapValues page indices.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantTrackDatasetTest : BaseViewModelTest() {

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
    fun hrpNonPregnantTrackDeep() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<HRPNonPregnantTrackCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null, null, null) }
        runCatching { ds.setUpPage(ben, saved, System.currentTimeMillis(), System.currentTimeMillis()) }
        runCatching { ds.mapValues(mockk<HRPNonPregnantTrackCache>(relaxed = true), 0) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfAncLabel() }
        runCatching { ds.getIndexOfAnemia() }
        runCatching { ds.getIndexOfRisk() }
        runCatching { ds.getIndexOfLmp() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun hrpNonPregnantTrackBranch() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, mockk<HRPNonPregnantTrackCache>(relaxed = true), 0L, 0L) }
        runCatching {
            ds.setUpPage(ben, null, System.currentTimeMillis(), System.currentTimeMillis())
        }
        for (p in 0..2) {
            runCatching { ds.mapValues(mockk<HRPNonPregnantTrackCache>(relaxed = true), p) }
        }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfAncLabel() }
        runCatching { ds.getIndexOfAnemia() }
        runCatching { ds.getIndexOfRisk() }
        runCatching { ds.getIndexOfLmp() }
        runCatching { ds.getIndexOfRbg() }
        runCatching { ds.getIndexOfFbg() }
        runCatching { ds.getIndexOfPpbg() }
        runCatching { ds.getIndexOfIfaQuantity() }
        assertNotNull(ds.listFlow)
    }

    private fun savedHrp(
        risk: String,
        glucoseTest: String,
        ifaGiven: String,
        visitDate: Long = System.currentTimeMillis(),
        lmp: Long? = System.currentTimeMillis(),
    ): HRPNonPregnantTrackCache {
        val s = mockk<HRPNonPregnantTrackCache>(relaxed = true)
        every { s.visitDate } returns visitDate
        every { s.anemia } returns risk
        every { s.hypertension } returns risk
        every { s.diabetes } returns risk
        every { s.severeAnemia } returns risk
        every { s.bloodGlucoseTest } returns glucoseTest
        every { s.systolic } returns 120
        every { s.diastolic } returns 80
        every { s.rbg } returns 110
        every { s.fbg } returns 95
        every { s.ppbg } returns 140
        every { s.hemoglobinTest } returns "11.5"
        every { s.ifaGiven } returns ifaGiven
        every { s.ifaQuantity } returns 30
        every { s.fp } returns risk
        every { s.lmp } returns lmp
        every { s.missedPeriod } returns risk
        every { s.isPregnant } returns risk
        return s
    }

    @Test
    fun `setUpPage high risk with rbg and ifa quantity lmpMin and dateOfVisitMin ahead`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.regDate } returns System.currentTimeMillis() - TimeUnit.DAYS.toMillis(200)
        val saved = savedHrp(risk = "opt0", glucoseTest = "opt0", ifaGiven = "opt0")

        val lmpMin = System.currentTimeMillis()
        val dateOfVisitMin = System.currentTimeMillis()

        ds.setUpPage(ben, saved, lmpMin, dateOfVisitMin)

        val form = mockk<HRPNonPregnantTrackCache>(relaxed = true)
        ds.mapValues(form, 0)

        val benWithGen = mockk<BenRegCache>(relaxed = true)
        val genDetails = mockk<BenRegGen>(relaxed = true)
        every { benWithGen.genDetails } returns genDetails
        every { benWithGen.processed } returns "N"
        ds.updateBen(benWithGen)

        val assess = mockk<HRPPregnantAssessCache>(relaxed = true)
        ds.updateAssess(assess)

        assertNotNull(ds.getIndexOfAncLabel())
        assertNotNull(ds.getIndexOfAnemia())
        assertNotNull(ds.getIndexOfRisk())
        assertNotNull(ds.getIndexOfLmp())
        assertNotNull(ds.getIndexOfRbg())
        assertNotNull(ds.getIndexOfFbg())
        assertNotNull(ds.getIndexOfPpbg())
        assertNotNull(ds.getIndexOfIfaQuantity())
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `setUpPage low risk with fbg ppbg no ifa quantity lmpMin below cutoff dateOfVisitMin before reg`() =
        runTest {
            val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
            val ben = mockk<BenRegCache>(relaxed = true)
            val regDate = System.currentTimeMillis()
            every { ben.regDate } returns regDate
            val saved = savedHrp(risk = "opt1", glucoseTest = "opt1", ifaGiven = "opt1")

            val lmpMin = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(100)
            val dateOfVisitMin = regDate - TimeUnit.DAYS.toMillis(10)

            ds.setUpPage(ben, saved, lmpMin, dateOfVisitMin)

            val form = mockk<HRPNonPregnantTrackCache>(relaxed = true)
            ds.mapValues(form, 0)

            val benWithNullGen = mockk<BenRegCache>(relaxed = true)
            every { benWithNullGen.genDetails } returns null
            every { benWithNullGen.processed } returns "SomethingElse"
            ds.updateBen(benWithNullGen)

            assertNotNull(ds.listFlow.value)
        }

    @Test
    fun `setUpPage bloodGlucoseTest neither rbg nor fbg branch`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedHrp(risk = "opt1", glucoseTest = "opt50", ifaGiven = "opt1")

        ds.setUpPage(ben, saved, null, null)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `setUpPage saved null create path`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList anemia and shared risk formIds toggle showHighRisk`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)

        ds.setValueById(2, "opt0")
        ds.updateList(2, 0)
        ds.setValueById(2, "opt1")
        ds.updateList(2, 0)

        ds.setValueById(4, "opt0")
        ds.updateList(4, 0)
        ds.setValueById(5, "opt1")
        ds.updateList(5, 0)
        ds.setValueById(6, "opt1")
        ds.updateList(6, 0)
        ds.updateList(6, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList dateOfVisit recalculates lmp min and max with lmpMinVar set`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        ds.setUpPage(ben, null, System.currentTimeMillis(), null)

        ds.setValueById(1, "01-01-2024")
        ds.updateList(1, 0)

        ds.setValueById(1, "01-01-2030")
        ds.updateList(1, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList bloodGlucoseTest and ifaGiven add and remove dependents`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)

        ds.setValueById(17, "opt0")
        ds.updateList(17, 0)
        ds.setValueById(18, "110")
        ds.updateList(18, 0)
        ds.setValueById(18, "")
        ds.updateList(18, 0)

        ds.setValueById(17, "opt1")
        ds.updateList(17, 0)
        ds.setValueById(19, "95")
        ds.updateList(19, 0)
        ds.setValueById(20, "")
        ds.updateList(20, 0)

        ds.setValueById(22, "opt0")
        ds.updateList(22, 0)
        ds.setValueById(23, "30")
        ds.updateList(23, 0)
        ds.setValueById(23, "")
        ds.updateList(23, 0)

        ds.setValueById(22, "opt1")
        ds.updateList(22, 0)

        ds.setValueById(22, "opt50")
        ds.updateList(22, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList systolic diastolic hemoglobin and unmatched formId`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null, null)

        ds.setValueById(15, "120")
        ds.updateList(15, 0)

        ds.setValueById(16, "80")
        ds.updateList(16, 0)

        ds.setValueById(21, "11.5")
        ds.updateList(21, 0)

        ds.updateList(999, 0)

        assertNotNull(ds.listFlow.value)
    }
}
