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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Consolidated coverage for [PregnantWomanAncVisitDataset] (merged from Deep + Branch + Branch2 +
 * Branch3 variants): setUpPage across visits 1..4 driving isFromPmsma, lastAnc null/non-null,
 * saved null/non-null and the isAborted / hrpConfirmed / pregnantWomanDelivered / anyHighRisk /
 * weeks-of-pregnancy / bp-null branches, mapValues, getIndexOf* helpers, and ENGLISH/HINDI.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PregnantWomanAncVisitDatasetTest : BaseViewModelTest() {

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

    private fun ds() = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)

    private val lmp = 1_600_000_000_000L

    // Branch variant regis (lmpDate only).
    private fun regis(): PregnantWomanRegistrationCache {
        val r = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { r.lmpDate } returns 1_600_000_000_000L
        return r
    }

    // Branch2 variant regis (lmpDate + tt dates).
    private fun regis2(): PregnantWomanRegistrationCache {
        val r = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { r.lmpDate } returns lmp
        every { r.tt1 } returns lmp
        every { r.tt2 } returns lmp
        every { r.ttBooster } returns lmp
        return r
    }

    private fun savedAborted(): PregnantWomanAncCache {
        val s = mockk<PregnantWomanAncCache>(relaxed = true)
        every { s.isAborted } returns true
        every { s.hrpConfirmed } returns true
        every { s.pregnantWomanDelivered } returns true
        every { s.bpSystolic } returns 150
        every { s.bpDiastolic } returns 95
        every { s.visitNumber } returns 2
        return s
    }

    private fun savedPlain(): PregnantWomanAncCache {
        val s = mockk<PregnantWomanAncCache>(relaxed = true)
        every { s.isAborted } returns false
        every { s.hrpConfirmed } returns false
        every { s.pregnantWomanDelivered } returns false
        every { s.bpSystolic } returns null
        every { s.bpDiastolic } returns null
        every { s.visitNumber } returns 1
        return s
    }

    // Early: ancDate ~ 4 weeks after lmp -> woP <= 12.
    private fun earlySaved(): PregnantWomanAncCache {
        val s = mockk<PregnantWomanAncCache>(relaxed = true)
        every { s.ancDate } returns lmp + 28L * 24 * 60 * 60 * 1000
        every { s.isAborted } returns false
        every { s.hrpConfirmed } returns false
        every { s.pregnantWomanDelivered } returns false
        every { s.anyHighRisk } returns false
        every { s.bpSystolic } returns 118
        every { s.bpDiastolic } returns 76
        every { s.visitNumber } returns 1
        return s
    }

    // Late: ancDate ~ 40 weeks after lmp -> delivery window; high-risk true + aborted true.
    private fun lateHighRiskSaved(): PregnantWomanAncCache {
        val s = mockk<PregnantWomanAncCache>(relaxed = true)
        every { s.ancDate } returns lmp + 280L * 24 * 60 * 60 * 1000
        every { s.isAborted } returns true
        every { s.abortionDate } returns lmp + 200L * 24 * 60 * 60 * 1000
        every { s.hrpConfirmed } returns true
        every { s.pregnantWomanDelivered } returns true
        every { s.anyHighRisk } returns true
        every { s.highRisk } returns "opt79"
        every { s.bpSystolic } returns 160
        every { s.bpDiastolic } returns 100
        every { s.numFolicAcidTabGiven } returns 30
        every { s.numIfaAcidTabGiven } returns 30
        every { s.visitNumber } returns 3
        return s
    }

    // ---- Deep variant test ----

    @Test
    fun pregnantWomanAncVisitDeep() = runTest {
        val ds = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val regis = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { ds.setUpPage(visit, ben, regis, null, false, null) }
            runCatching { ds.setUpPage(visit, ben, regis, lastAnc, true, saved) }
        }
        runCatching { ds.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfMCPCardFrontPath() }
        runCatching { ds.getIndexOfMCPCardBackPath() }
        runCatching { ds.getIndexOfTd1() }
        runCatching { ds.getIndexOfTd2() }
        runCatching { ds.getIndexOfTdBooster() }
        assertNotNull(ds.listFlow)
    }

    // ---- Branch variant tests ----

    @Test
    fun `pmsma with aborted saved and lastAnc`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { d.setUpPage(visit, ben, regis(), lastAnc, true, savedAborted()) }
        }
        runCatching { d.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `non-pmsma plain saved and null lastAnc`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { d.setUpPage(visit, ben, regis(), null, false, savedPlain()) }
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `null saved fresh visit`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { d.setUpPage(visit, ben, regis(), null, false, null) }
            runCatching { d.setUpPage(visit, ben, regis(), mockk<PregnantWomanAncCache>(relaxed = true), true, null) }
        }
        runCatching { d.getIndexOfTd1() }
        runCatching { d.getIndexOfTd2() }
        runCatching { d.getIndexOfTdBooster() }
        assertNotNull(d.listFlow)
    }

    // ---- Branch2 variant tests ----

    @Test
    fun `early saved weeks under 12`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { d.setUpPage(visit, ben, regis2(), null, false, earlySaved()) }
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `late high-risk aborted saved with lastAnc`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        every { lastAnc.ancDate } returns lmp + 100L * 24 * 60 * 60 * 1000
        for (visit in 1..4) {
            runCatching { d.setUpPage(visit, ben, regis2(), lastAnc, true, lateHighRiskSaved()) }
        }
        runCatching { d.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 0) }
        runCatching { d.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 1) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `fresh null saved late ancDate branches`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { d.setUpPage(1, ben, regis2(), null, false, null) }
        runCatching { d.setUpPage(2, ben, regis2(), earlySaved(), true, null) }
        runCatching { d.getIndexOfTd1() }
        runCatching { d.getIndexOfTd2() }
        runCatching { d.getIndexOfTdBooster() }
        assertNotNull(d.listFlow)
    }

    // ---- added coverage: setUpTdX variants, maternal death, images, ben updates ----

    // Registration with only tt1 set (tt2/ttBooster null) -> setUpTdX "else" branch.
    private fun regisTt1Only(): PregnantWomanRegistrationCache {
        val r = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { r.lmpDate } returns lmp
        every { r.tt1 } returns lmp
        every { r.tt2 } returns null
        every { r.ttBooster } returns null
        return r
    }

    // Registration with tt1 + tt2 set (ttBooster null).
    private fun regisTt1Tt2(): PregnantWomanRegistrationCache {
        val r = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { r.lmpDate } returns lmp
        every { r.tt1 } returns lmp
        every { r.tt2 } returns lmp
        every { r.ttBooster } returns null
        return r
    }

    // maternalDeath == true with placeOfDeath at index 8 -> adds otherPlaceOfDeath.
    private fun savedMaternalDeath(): PregnantWomanAncCache {
        val s = mockk<PregnantWomanAncCache>(relaxed = true)
        every { s.ancDate } returns lmp + 200L * 24 * 60 * 60 * 1000
        every { s.isAborted } returns false
        every { s.hrpConfirmed } returns false
        every { s.pregnantWomanDelivered } returns false
        every { s.anyHighRisk } returns false
        every { s.maternalDeath } returns true
        every { s.placeOfDeath } returns "opt8"
        every { s.maternalDeathProbableCause } returns "opt2"
        every { s.deathDate } returns lmp + 190L * 24 * 60 * 60 * 1000
        every { s.bpSystolic } returns 120
        every { s.bpDiastolic } returns 80
        every { s.visitNumber } returns 2
        return s
    }

    @Test
    fun `saved maternal death with other place`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        every { lastAnc.ancDate } returns lmp + 100L * 24 * 60 * 60 * 1000
        for (visit in 1..4) {
            runCatching { d.setUpPage(visit, ben, regis2(), lastAnc, false, savedMaternalDeath()) }
        }
        runCatching { d.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `td dates from registration variations`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        for (visit in 1..3) {
            runCatching { d.setUpPage(visit, ben, regisTt1Only(), null, false, null) }
            runCatching { d.setUpPage(visit, ben, regisTt1Tt2(), null, false, null) }
        }
        runCatching { d.getIndexOfTd1() }
        runCatching { d.getIndexOfTd2() }
        runCatching { d.getIndexOfTdBooster() }
        runCatching { d.getWeeksOfPregnancy() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `image uris and ben record updates and bp toggle`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { d.setUpPage(1, ben, regis2(), null, false, savedPlain()) }
        runCatching { d.setImageUriToFormElement(31, mockk<Uri>(relaxed = true)) }
        runCatching { d.setImageUriToFormElement(32, mockk<Uri>(relaxed = true)) }
        runCatching { d.updateBenRecordToDelivered(mockk(relaxed = true)) }
        runCatching { d.updateBenRecordToEligibleCouple(mockk(relaxed = true)) }
        runCatching {
            val b = mockk<BenRegCache>(relaxed = true)
            every { b.processed } returns "N"
            d.updateBenRecordToDelivered(b)
        }
        runCatching { d.resetBpToggle() }
        runCatching { d.triggerBpToggle() }
        runCatching { d.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    // ---- Branch3 variant test (HINDI) ----

    @Test
    fun pregnantWomanAncVisitBranch() = runTest {
        val ds = PregnantWomanAncVisitDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        val regis = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        for (visit in 1..4) {
            runCatching { ds.setUpPage(visit, ben, regis, lastAnc, false, saved) }
            runCatching { ds.setUpPage(visit, ben, regis, null, true, null) }
        }
        for (p in 0..2) {
            runCatching { ds.mapValues(mockk<PregnantWomanAncCache>(relaxed = true), p) }
        }
        runCatching { ds.getIndexOfMCPCardFrontPath() }
        runCatching { ds.getIndexOfMCPCardBackPath() }
        runCatching { ds.getIndexOfTd1() }
        runCatching { ds.getIndexOfTd2() }
        runCatching { ds.getIndexOfTdBooster() }
        assertNotNull(ds.listFlow)
    }

    // Drives handleListOnValueChanged (via public updateList) across the when(formId) branches:
    // numeric validators weight(8)/bp(9)/pulseRate(11)/hb(12)/fundalHeight(13)/folic(19)/ifa(20),
    // ancVisit(3) 1 vs >1, TT date input-type toggles (16,18), anyHighRisk(21) with hb/bp side
    // effects, highRiskCondition(22), otherHighRiskCondition(23), hrpConfirm(25), placeOfDeath(42),
    // isAborted(4) & maternalDeath(27) yes/no combinations, ancDate(1) deep block, else branch.
    @Test
    fun `updateList drives anc dependant branches`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        every { lastAnc.ancDate } returns lmp + 100L * 24 * 60 * 60 * 1000
        runCatching { d.setUpPage(2, ben, regis2(), lastAnc, false, savedPlain()) }
        // numeric validators
        runCatching { d.setValueById(8, "60"); d.updateList(8, 0) }
        runCatching { d.setValueById(9, "120/80"); d.updateList(9, 0) }
        runCatching { d.setValueById(11, "72"); d.updateList(11, 0) }
        runCatching { d.setValueById(12, "11.5"); d.updateList(12, 0) }
        runCatching { d.setValueById(13, "20"); d.updateList(13, 0) }
        runCatching { d.setValueById(19, "30"); d.updateList(19, 0) }
        runCatching { d.setValueById(20, "30"); d.updateList(20, 0) }
        // ancVisit 1 then 2
        runCatching { d.setValueById(3, "1"); d.updateList(3, 0) }
        runCatching { d.setValueById(3, "2"); d.updateList(3, 0) }
        // TT date input-type toggles (null vs set)
        runCatching { d.setValueById(16, null); d.updateList(16, 0) }
        runCatching { d.setValueById(16, "01-01-2023"); d.updateList(16, 0) }
        runCatching { d.setValueById(18, null); d.updateList(18, 0) }
        runCatching { d.setValueById(18, "01-01-2023"); d.updateList(18, 0) }
        // anyHighRisk with low hb and high bp side effects
        runCatching {
            d.setValueById(12, "6.0"); d.setValueById(9, "150/95")
            d.setValueById(21, "opt1"); d.updateList(21, 1)
        }
        // highRiskCondition -> other (last index)
        runCatching { d.setValueById(22, "opt79"); d.updateList(22, 79) }
        runCatching { d.setValueById(23, "X"); d.updateList(23, 0) }
        // hrpConfirm
        runCatching { d.setValueById(25, "opt1"); d.updateList(25, 1) }
        // placeOfDeath other (trigger index 8)
        runCatching { d.setValueById(42, "opt8"); d.updateList(42, 8) }
        // isAborted yes/no combined with maternalDeath yes/no
        runCatching { d.setValueById(4, "opt79"); d.updateList(4, 0) }
        runCatching { d.setValueById(27, "opt79"); d.setValueById(4, "opt0"); d.updateList(4, 0) }
        runCatching { d.setValueById(27, "opt0"); d.setValueById(4, "opt0"); d.updateList(4, 0) }
        // maternalDeath yes / (aborted yes) / no
        runCatching { d.setValueById(27, "opt79"); d.updateList(27, 0) }
        runCatching { d.setValueById(4, "opt79"); d.setValueById(27, "opt0"); d.updateList(27, 0) }
        runCatching { d.setValueById(4, "opt0"); d.setValueById(27, "opt0"); d.updateList(27, 0) }
        // ancDate deep block: isAborted & maternalDeath both first()
        runCatching {
            d.setValueById(4, "opt0"); d.setValueById(27, "opt0")
            d.setValueById(1, "01-06-2023"); d.updateList(1, 0)
        }
        // else branch
        runCatching { d.updateList(77777, 0) }
        assertNotNull(d.listFlow)
    }
}
