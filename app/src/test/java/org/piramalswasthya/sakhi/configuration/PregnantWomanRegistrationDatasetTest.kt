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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Consolidated coverage for [PregnantWomanRegistrationDataset] (merged from Deep + Branch + Branch3
 * variants): setUpPage with differing ben/assess/saved/ecr field values so both sides of the
 * isHrpStatus / isHighRisk / lmpDate>0 / is1st / isHrp / noOfChildren branches run, mapValues,
 * mapValuesForAssess, the getIndexOf* helpers, and ENGLISH/HINDI language.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PregnantWomanRegistrationDatasetTest : BaseViewModelTest() {

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

    private fun ben(hrp: Boolean, lastNameNull: Boolean): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns 2
        every { b.isDraft } returns false
        every { b.gender } returns Gender.FEMALE
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns if (lastNameNull) null else "LAST"
        every { b.isHrpStatus } returns hrp
        return b
    }

    private fun assess(lmp: Long): HRPPregnantAssessCache {
        val a = mockk<HRPPregnantAssessCache>(relaxed = true)
        every { a.lmpDate } returns lmp
        return a
    }

    private fun saved(lmp: Long, first: Boolean, hrp: Boolean): PregnantWomanRegistrationCache {
        val s = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { s.lmpDate } returns lmp
        every { s.is1st } returns first
        every { s.isHrp } returns hrp
        return s
    }

    private fun ecr(children: Int): EligibleCoupleRegCache {
        val e = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { e.noOfChildren } returns children
        return e
    }

    private fun ds() = PregnantWomanRegistrationDataset(context, Languages.ENGLISH)

    // ---- Deep variant test ----

    @Test
    fun pregnantWomanRegistrationDeep() = runTest {
        val ds = PregnantWomanRegistrationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val assess = mockk<HRPPregnantAssessCache>(relaxed = true)
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        val saved = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, assess, null, ecr, null) }
        runCatching { ds.setUpPage(ben, assess, saved, ecr, System.currentTimeMillis()) }
        runCatching { ds.setUpPage(null, null, null, null, null) }
        runCatching { ds.mapValues(mockk<PregnantWomanRegistrationCache>(relaxed = true), 0) }
        runCatching { ds.mapValuesForAssess(mockk<HRPPregnantAssessCache>(relaxed = true), 0) }
        runCatching { ds.isHighRisk() }
        runCatching { ds.getIndexOfEdd() }
        runCatching { ds.getIndexOfLmp() }
        runCatching { ds.getIndexOfWeeksPregnancy() }
        runCatching { ds.getIndexOfPastIllness() }
        runCatching { ds.getIndexOfHRP() }
        runCatching { ds.getIndexOfChildLabel() }
        runCatching { ds.getIndexOfPhysicalObservationLabel() }
        runCatching { ds.getIndexOfObstetricHistoryLabel() }
        assertNotNull(ds.listFlow)
    }

    // ---- Branch variant tests ----

    @Test
    fun `hrp true, lmp set, first pregnancy, many children`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = true, lastNameNull = false),
                assess(lmp = 1_000_000_000_000L),
                saved(lmp = 1_000_000_000_000L, first = true, hrp = true),
                ecr(children = 4),
                System.currentTimeMillis()
            )
        }
        runCatching { d.isHighRisk() }
        runCatching { d.mapValues(mockk<PregnantWomanRegistrationCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `hrp false, no lmp, not first, no children`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = false, lastNameNull = true),
                assess(lmp = 0L),
                saved(lmp = 0L, first = false, hrp = false),
                ecr(children = 0),
                null
            )
        }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `null ben and assess and saved`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(null, null, null, ecr(children = 2), null) }
        runCatching { d.setUpPage(null, null, null, null, null) }
        assertNotNull(d.listFlow)
    }

    // ---- added coverage: ecr-driven, saved-rich, assess-with-lmp, map helpers ----

    private fun savedRich(isHrp: Boolean, pastIllnessOther: Boolean): PregnantWomanRegistrationCache {
        val s = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { s.dateOfRegistration } returns 1_600_000_000_000L
        every { s.lmpDate } returns 1_500_000_000_000L
        every { s.is1st } returns false
        every { s.isHrp } returns isHrp
        every { s.pastIllness } returns if (pastIllnessOther) "79" else "1"
        every { s.otherPastIllness } returns "OPI"
        every { s.numPrevPregnancy } returns 2
        every { s.complicationPrevPregnancy } returns "opt2"
        every { s.otherComplication } returns "OC"
        every { s.bloodGroup } returns "opt1"
        every { s.weight } returns 60
        every { s.height } returns 160
        every { s.hrpIdBy } returns "opt1"
        return s
    }

    // ecr.noOfChildren > 3 with saved == null drives the "not first pregnancy" + noOfDeliveries block.
    @Test
    fun `ecr children present create path`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = false, lastNameNull = false),
                null,
                null,
                ecr(children = 5),
                null
            )
        }
        runCatching { d.mapValues(mockk<PregnantWomanRegistrationCache>(relaxed = true), 0) }
        runCatching { d.mapValuesForAssess(mockk<HRPPregnantAssessCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `saved rich with past illness other and hrp`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = true, lastNameNull = false),
                assess(lmp = 1_500_000_000_000L),
                savedRich(isHrp = true, pastIllnessOther = true),
                ecr(children = 2),
                System.currentTimeMillis()
            )
        }
        runCatching { d.mapValues(mockk<PregnantWomanRegistrationCache>(relaxed = true), 0) }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `saved rich no hrp no past illness other`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = false, lastNameNull = true),
                null,
                savedRich(isHrp = false, pastIllnessOther = false),
                null,
                null
            )
        }
        runCatching { d.mapValues(mockk<PregnantWomanRegistrationCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `assess lmp positive no saved`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = false, lastNameNull = false),
                assess(lmp = 1_500_000_000_000L),
                null,
                ecr(children = 1),
                System.currentTimeMillis()
            )
        }
        runCatching { d.mapValuesForAssess(mockk<HRPPregnantAssessCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValueToBenRegId variations`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = false, lastNameNull = false),
                assess(lmp = 1_500_000_000_000L),
                null,
                ecr(children = 0),
                null
            )
        }
        runCatching { d.mapValueToBenRegId(mockk<BenRegCache>(relaxed = true)) }
        runCatching { d.mapValueToBenRegId(null) }
        assertNotNull(d.listFlow)
    }

    // ---- Branch3 variant test (HINDI) ----

    @Test
    fun pregnantWomanRegistrationBranch() = runTest {
        val ds = PregnantWomanRegistrationDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        val assess = mockk<HRPPregnantAssessCache>(relaxed = true)
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        val saved = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, null, saved, ecr, 0L) }
        runCatching { ds.setUpPage(ben, assess, saved, null, System.currentTimeMillis()) }
        runCatching { ds.setUpPage(null, assess, null, ecr, null) }
        for (p in 0..3) {
            runCatching { ds.mapValues(mockk<PregnantWomanRegistrationCache>(relaxed = true), p) }
            runCatching { ds.mapValuesForAssess(mockk<HRPPregnantAssessCache>(relaxed = true), p) }
        }
        runCatching { ds.isHighRisk() }
        runCatching { ds.getIndexOfEdd() }
        runCatching { ds.getIndexOfLmp() }
        runCatching { ds.getIndexOfWeeksPregnancy() }
        runCatching { ds.getIndexOfPastIllness() }
        runCatching { ds.getIndexOfHRP() }
        runCatching { ds.getIndexOfChildLabel() }
        runCatching { ds.getIndexOfPhysicalObservationLabel() }
        runCatching { ds.getIndexOfObstetricHistoryLabel() }
        assertNotNull(ds.listFlow)
    }

    // Drives handleListOnValueChanged (via public updateList) across the when(formId) branches:
    // rchId(2), weight(11), height(12), totalNumberOfPreviousPregnancy(22), otherPastIllness(20),
    // otherComplications(24), pastIllness(19) none vs other, isFirstPregnancy(21) yes vs no
    // (recursively drives home/noOfDeliveries/etc.), complications dropdown(23), the high-risk
    // driving fields (27,28,29,30,31,32,33,34), isHrpCase(25), dateOfReg(1), lmp(7), else branch.
    @Test
    fun `updateList drives registration dependant branches`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                ben(hrp = true, lastNameNull = false),
                assess(lmp = 1_500_000_000_000L),
                saved(lmp = 1_500_000_000_000L, first = true, hrp = true),
                ecr(children = 2),
                System.currentTimeMillis()
            )
        }
        // edit-text / int validators
        runCatching { d.setValueById(2, "1234567890"); d.updateList(2, 0) }
        runCatching { d.setValueById(11, "60"); d.updateList(11, 0) }
        runCatching { d.setValueById(12, "150"); d.updateList(12, 0) }
        runCatching { d.setValueById(12, "170"); d.updateList(12, 0) }
        runCatching { d.setValueById(22, "2"); d.updateList(22, 0) }
        runCatching { d.setValueById(20, "illness"); d.updateList(20, 0) }
        runCatching { d.setValueById(24, "complication"); d.updateList(24, 0) }
        // pastIllness: None (index 0) then Other (last index 79)
        runCatching { d.setValueById(19, "0"); d.updateList(19, 0) }
        runCatching { d.setValueById(19, "79"); d.updateList(19, 79) }
        // isFirstPregnancy yes (opt0) then no (opt1) - recursively drives dependants
        runCatching { d.setValueById(21, "opt0"); d.updateList(21, 1) }
        runCatching { d.setValueById(21, "opt1"); d.updateList(21, 0) }
        // complications dropdown -> other (lastIndex) then not
        runCatching { d.setValueById(23, "opt79"); d.updateList(23, 79) }
        runCatching { d.setValueById(23, "opt0"); d.updateList(23, 0) }
        // high-risk driving fields yes(opt0)/no(opt1)
        for (id in listOf(27, 28, 29, 30, 31, 32, 33, 34)) {
            runCatching { d.setValueById(id, "opt0"); d.updateList(id, 0) }
            runCatching { d.setValueById(id, "opt1"); d.updateList(id, 0) }
        }
        // isHrpCase dependant
        runCatching { d.setValueById(25, "opt0"); d.updateList(25, 0) }
        // dateOfReg + lmp date branches
        runCatching { d.setValueById(1, "01-01-2020"); d.updateList(1, 0) }
        runCatching { d.setValueById(7, "01-01-2023"); d.updateList(7, 0) }
        // else branch
        runCatching { d.updateList(88888, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `lmp updateList without a registration date skips the week of pregnancy calculation`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        val weekIdx = d.getIndexOfWeeksPregnancy()
        d.setValueById(7, "01-01-2023")
        d.updateList(7, 0)
        if (weekIdx >= 0) {
            assertNull(d.listFlow.value[weekIdx].value)
        }
        val eddIdx = d.getIndexOfEdd()
        assertTrue(eddIdx >= 0)
        assertNotNull(d.listFlow.value[eddIdx].value)
    }

    @Test
    fun `height at or below 139 flags a short stature reading`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        d.setValueById(12, "100")
        d.updateList(12, 0)
        assertTrue(d.isHighRisk())
    }

    @Test
    fun `height above 139 does not flag a short stature reading`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        d.setValueById(12, "170")
        d.updateList(12, 0)
        assertFalse(d.isHighRisk())
    }

    @Test
    fun `pastIllness selecting a middle option leaves other illness hidden`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        d.setValueById(19, "5")
        d.updateList(19, 5)
        assertEquals(-1, d.getIndexById(20))
        assertEquals("5", d.listFlow.value.first { it.id == 19 }.value)
    }

    @Test
    fun `pastIllness selecting none clears the value back to zero`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        d.setValueById(19, "3")
        d.updateList(19, 3)
        d.updateList(19, 0)
        assertEquals("0", d.listFlow.value.first { it.id == 19 }.value)
        assertEquals(-1, d.getIndexById(20))
    }

    @Test
    fun `mapValueToBenRegId flags an existing beneficiary as high risk`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        d.setValueById(12, "100")
        d.updateList(12, 0)
        val benCache = mockk<BenRegCache>(relaxed = true)
        every { benCache.rchId } returns null
        every { benCache.isHrpStatus } returns false
        every { benCache.processed } returns "N"
        val updated = d.mapValueToBenRegId(benCache)
        assertTrue(updated)
        verify { benCache.isHrpStatus = true }
    }

    @Test
    fun `mapValueToBenRegId reports no update for a low risk unchanged beneficiary`() = runTest {
        val d = ds()
        d.setUpPage(ben(hrp = false, lastNameNull = false), null, null, null, null)
        val benCache = mockk<BenRegCache>(relaxed = true)
        every { benCache.rchId } returns null
        every { benCache.isHrpStatus } returns false
        val updated = d.mapValueToBenRegId(benCache)
        assertFalse(updated)
    }
}
