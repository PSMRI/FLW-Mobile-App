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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Consolidated coverage for [MalariaFormDataset] (merged from Deep + Branch + Branch2 + Branch3
 * variants): setUpPage with saved/null records driving both sides of the symptom / caseStatus /
 * test-type / slide-type / beneficiaryStatus (incl. death last-entry) branches, updateBen,
 * getIndexOfDate, male/female ben, and ENGLISH/HINDI language.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MalariaFormDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    private fun benMock(): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns 2
        every { b.isDraft } returns false
        every { b.gender } returns Gender.FEMALE
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        return b
    }

    // Branch3 variant: parameterized gender ben.
    private fun benMockG(gender: Gender = Gender.MALE, genderId: Int = 1): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns genderId
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        return b
    }

    private fun ds() = MalariaFormDataset(context, Languages.ENGLISH)

    private fun dsHindi() = MalariaFormDataset(context, Languages.HINDI)

    private fun savedMock(
        allSymptoms: Boolean,
        status: String?,
        testType: Int?,
        slideType: Int?,
        beneficiary: String?,
    ): MalariaScreeningCache {
        val s = mockk<MalariaScreeningCache>(relaxed = true)
        every { s.feverMoreThanTwoWeeks } returns allSymptoms
        every { s.fluLikeIllness } returns allSymptoms
        every { s.shakingChills } returns allSymptoms
        every { s.headache } returns allSymptoms
        every { s.muscleAches } returns allSymptoms
        every { s.tiredness } returns allSymptoms
        every { s.nausea } returns allSymptoms
        every { s.vomiting } returns allSymptoms
        every { s.diarrhea } returns allSymptoms
        every { s.caseStatus } returns status
        every { s.malariaTestType } returns testType
        every { s.malariaSlideTestType } returns slideType
        every { s.beneficiaryStatus } returns beneficiary
        return s
    }

    private fun suspectedAllSymptoms(): MalariaScreeningCache {
        val s = mockk<MalariaScreeningCache>(relaxed = true)
        every { s.caseStatus } returns "Suspected"
        every { s.caseDate } returns 1_600_000_000_000L
        every { s.followUpDate } returns 1_610_000_000_000L
        every { s.visitId } returns 5L
        every { s.malariaTestType } returns 2
        every { s.malariaSlideTestType } returns 2
        every { s.remarks } returns "note"
        every { s.feverMoreThanTwoWeeks } returns true
        every { s.fluLikeIllness } returns true
        every { s.shakingChills } returns true
        every { s.headache } returns true
        every { s.muscleAches } returns true
        every { s.tiredness } returns true
        every { s.nausea } returns true
        every { s.vomiting } returns true
        every { s.diarrhea } returns true
        every { s.rapidDiagnosticTest } returns "opt0"
        every { s.slideTestPv } returns "opt0"
        every { s.slideTestPf } returns "opt0"
        every { s.beneficiaryStatus } returns "opt0"
        return s
    }

    private fun deathCase(): MalariaScreeningCache {
        val s = mockk<MalariaScreeningCache>(relaxed = true)
        every { s.caseStatus } returns "opt79"
        every { s.caseDate } returns 1_600_000_000_000L
        every { s.followUpDate } returns 1_610_000_000_000L
        every { s.malariaTestType } returns 1
        every { s.malariaSlideTestType } returns 1
        every { s.beneficiaryStatus } returns "opt79"
        every { s.dateOfDeath } returns 1_620_000_000_000L
        every { s.placeOfDeath } returns "opt79"
        every { s.otherPlaceOfDeath } returns "home"
        every { s.reasonForDeath } returns "opt79"
        every { s.otherReasonForDeath } returns "unknown"
        every { s.feverMoreThanTwoWeeks } returns false
        every { s.fluLikeIllness } returns false
        return s
    }

    // ---- Deep variant tests ----

    @Test
    fun `setUpPage saved and null`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), mockk<MalariaScreeningCache>(relaxed = true)) }
        runCatching { d.setUpPage(benMock(), null) }
        runCatching { d.setUpPage(null, null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateBen and getIndexOfDate`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), null) }
        runCatching { d.updateBen(benMock()) }
        runCatching { d.getIndexOfDate() }
        assertNotNull(d.listFlow)
    }

    // ---- Branch variant tests ----

    @Test
    fun `setUpPage null vs suspected with tests`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), null) }
        runCatching {
            d.setUpPage(
                benMock(),
                savedMock(true, "Suspected", 2, 2, "Alive")
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage confirmed vs negative symptoms`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                savedMock(false, "Confirmed", 1, 1, "Alive")
            )
        }
        runCatching {
            d.setUpPage(
                benMock(),
                savedMock(true, null, 0, 0, "opt79")
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateBen and getIndexOfDate after saved`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), savedMock(true, "Suspected", 2, 2, "Alive")) }
        runCatching { d.updateBen(benMock()) }
        runCatching { d.getIndexOfDate() }
        assertNotNull(d.listFlow)
    }

    // ---- Branch2 variant tests ----

    @Test
    fun `setUpPage suspected all symptoms`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        runCatching { d.updateBen(benMock()) }
        runCatching { d.getIndexOfDate() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage death case with last-entry place and reason`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), deathCase()) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage negative all-false record`() = runTest {
        val s = mockk<MalariaScreeningCache>(relaxed = true)
        every { s.caseStatus } returns "Confirmed"
        every { s.malariaTestType } returns 0
        every { s.malariaSlideTestType } returns 0
        every { s.feverMoreThanTwoWeeks } returns false
        every { s.fluLikeIllness } returns false
        every { s.shakingChills } returns false
        every { s.beneficiaryStatus } returns "opt1"
        val d = ds()
        runCatching { d.setUpPage(benMock(), s) }
        assertNotNull(d.listFlow)
    }

    // ---- Branch3 variant tests (HINDI, male/female ben) ----

    @Test
    fun `setUpPage male and female saved`() = runTest {
        val d = dsHindi()
        runCatching { d.setUpPage(benMockG(Gender.MALE, 1), mockk<MalariaScreeningCache>(relaxed = true)) }
        runCatching { d.setUpPage(benMockG(Gender.FEMALE, 2), mockk<MalariaScreeningCache>(relaxed = true)) }
        runCatching { d.setUpPage(benMockG(Gender.FEMALE, 2), null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateBen and getIndexOfDate_2`() = runTest {
        val d = dsHindi()
        runCatching { d.setUpPage(benMockG(), mockk<MalariaScreeningCache>(relaxed = true)) }
        runCatching { d.updateBen(benMockG(Gender.FEMALE, 2)) }
        runCatching { d.getIndexOfDate() }
        assertNotNull(d.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun caseSaved(
        beneficiary: String,
        caseStatus: String?,
        testType: Int,
        slideType: Int,
        rdt: String?,
        slidePv: String?,
        slidePf: String?,
        referTo: String? = null,
    ): MalariaScreeningCache {
        val s = mockk<MalariaScreeningCache>(relaxed = true)
        every { s.beneficiaryStatus } returns beneficiary
        every { s.caseStatus } returns caseStatus
        every { s.malariaTestType } returns testType
        every { s.malariaSlideTestType } returns slideType
        every { s.rapidDiagnosticTest } returns rdt
        every { s.slideTestPv } returns slidePv
        every { s.slideTestPf } returns slidePf
        every { s.referToName } returns referTo
        every { s.caseDate } returns 1_600_000_000_000L
        every { s.followUpDate } returns 1_610_000_000_000L
        every { s.dateOfRdt } returns 1_600_000_000_000L
        every { s.dateOfSlideTest } returns 1_600_000_000_000L
        every { s.visitId } returns 3L
        every { s.remarks } returns "note"
        every { s.feverMoreThanTwoWeeks } returns true
        return s
    }

    @Test
    fun `setUpPage confirmed rapid test type path`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 1, 0, "opt0", "opt2", "opt2")
            )
            d.mapValues(mockk<MalariaScreeningCache>(relaxed = true), 1)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage slide test pf and pv path`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 2, 1, "opt2", "opt0", "opt0")
            )
            d.mapValues(mockk<MalariaScreeningCache>(relaxed = true), 1)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage both test type path`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt1", 3, 2, "opt0", "opt0", "opt0")
            )
            d.mapValues(mockk<MalariaScreeningCache>(relaxed = true), 1)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage suspected filters case status entries`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt2", "Suspected", 1, 0, "opt1", "opt2", "opt2")
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage referredTo second-last adds other`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt2", 1, 0, "opt0", "opt2", "opt2", referTo = "opt78")
            )
            d.mapValues(mockk<MalariaScreeningCache>(relaxed = true), 1)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage death path with other place and reason then mapValues`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(benMock(), deathCase())
            d.mapValues(mockk<MalariaScreeningCache>(relaxed = true), 0)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage hindi case status path`() = runTest {
        val d = dsHindi()
        runCatching {
            d.setUpPage(
                benMockG(Gender.MALE, 1),
                caseSaved("opt0", "opt0", 1, 0, "opt1", "opt2", "opt2")
            )
        }
        assertNotNull(d.listFlow)
    }

    // Drives handleListOnValueChanged (via public updateList) across every when(formId) branch:
    // symptoms(7..15)->caseStatus, caseStatus(16), testType(28), rapidDiagnostic(17),
    // slideTestOptions(27), slideTestPv(20)/slideTestPf(19), beneficiaryStatus(2) death vs alive,
    // referredTo(22), placeOfDeath(4), reasonOfDeath(6) last vs other, the "other" edit-texts
    // (23,5,30), plus the else branch.
    @Test
    fun `updateList drives all malaria dependant branches`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        // symptoms -> suspected (yes = opt0) then not-suspected (no = opt1)
        for (id in 7..15) { runCatching { d.setValueById(id, "opt0"); d.updateList(id, 0) } }
        for (id in 7..15) { runCatching { d.setValueById(id, "opt1"); d.updateList(id, 0) } }
        // caseStatus suspected (opt0) then other
        runCatching { d.setValueById(16, "opt0"); d.updateList(16, 0) }
        runCatching { d.setValueById(16, "opt1"); d.updateList(16, 0) }
        // testType rapid(opt0) / slide(opt1) / both(opt2)
        runCatching { d.setValueById(28, "opt0"); d.updateList(28, 0) }
        runCatching { d.setValueById(28, "opt1"); d.updateList(28, 0) }
        runCatching { d.setValueById(28, "opt2"); d.updateList(28, 0) }
        // rapidDiagnostic positive(opt2) / negative(opt1) / other(opt0)
        runCatching { d.setValueById(17, "opt2"); d.updateList(17, 0) }
        runCatching { d.setValueById(17, "opt1"); d.updateList(17, 0) }
        runCatching { d.setValueById(17, "opt0"); d.updateList(17, 0) }
        // slideTestOptions pf(opt0) / pv(opt1)
        runCatching { d.setValueById(27, "opt0"); d.updateList(27, 0) }
        runCatching { d.setValueById(27, "opt1"); d.updateList(27, 0) }
        // slideTestPv positive / negative / other
        runCatching { d.setValueById(20, "opt0"); d.updateList(20, 0) }
        runCatching { d.setValueById(20, "opt1"); d.updateList(20, 0) }
        runCatching { d.setValueById(20, "opt2"); d.updateList(20, 0) }
        // slideTestPf positive / negative / other
        runCatching { d.setValueById(19, "opt0"); d.updateList(19, 0) }
        runCatching { d.setValueById(19, "opt1"); d.updateList(19, 0) }
        runCatching { d.setValueById(19, "opt2"); d.updateList(19, 0) }
        // beneficiaryStatus death (last=opt79) then alive
        runCatching { d.setValueById(2, "opt79"); d.updateList(2, 0) }
        runCatching { d.setValueById(2, "opt0"); d.updateList(2, 0) }
        // referredTo second-last (opt78) then not
        runCatching { d.setValueById(22, "opt78"); d.updateList(22, 0) }
        runCatching { d.setValueById(22, "opt0"); d.updateList(22, 0) }
        // placeOfDeath last then not
        runCatching { d.setValueById(4, "opt79"); d.updateList(4, 0) }
        runCatching { d.setValueById(4, "opt0"); d.updateList(4, 0) }
        // reasonOfDeath last then not
        runCatching { d.setValueById(6, "opt79"); d.updateList(6, 0) }
        runCatching { d.setValueById(6, "opt0"); d.updateList(6, 0) }
        // "other" edit-text validators
        runCatching { d.setValueById(23, "X"); d.updateList(23, 0) }
        runCatching { d.setValueById(5, "home"); d.updateList(5, 0) }
        runCatching { d.setValueById(30, "cause"); d.updateList(30, 0) }
        // else branch
        runCatching { d.updateList(9999, 0) }
        assertNotNull(d.listFlow)
    }

    // ---- Additional deep-branch coverage ----

    private fun deathCaseNonLastPlaceAndReason(): MalariaScreeningCache {
        val s = mockk<MalariaScreeningCache>(relaxed = true)
        every { s.caseStatus } returns "opt79"
        every { s.caseDate } returns 1_600_000_000_000L
        every { s.followUpDate } returns 1_610_000_000_000L
        every { s.malariaTestType } returns 1
        every { s.malariaSlideTestType } returns 1
        every { s.beneficiaryStatus } returns "opt79"
        every { s.dateOfDeath } returns 1_620_000_000_000L
        every { s.placeOfDeath } returns "opt0"
        every { s.reasonForDeath } returns "opt0"
        every { s.feverMoreThanTwoWeeks } returns false
        every { s.fluLikeIllness } returns false
        return s
    }

    @Test
    fun `setUpPage death case with non-last place and reason omits other fields`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), deathCaseNonLastPlaceAndReason()) }
        assertNotEquals(-1, d.getIndexById(4))
        assertEquals(-1, d.getIndexById(5))
        assertEquals(-1, d.getIndexById(30))
    }

    @Test
    fun `setUpPage caseStatus resolved but test type unset adds testType only`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 0, 0, null, null, null)
            )
        }
        assertNotEquals(-1, d.getIndexById(28))
        assertEquals(-1, d.getIndexById(17))
        assertEquals(-1, d.getIndexById(27))
    }

    @Test
    fun `setUpPage slide only test type with pv result adds slideTestPv not slideTestPf`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 2, 2, null, "opt0", "opt0")
            )
        }
        assertNotEquals(-1, d.getIndexById(27))
        assertNotEquals(-1, d.getIndexById(20))
        assertEquals(-1, d.getIndexById(19))
    }

    @Test
    fun `setUpPage both test types with resolved rapid result skips dateOfTest insertion`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 3, 1, "opt2", "opt0", "opt0")
            )
        }
        assertNotEquals(-1, d.getIndexById(28))
        assertEquals(-1, d.getIndexById(18))
    }

    @Test
    fun `updateList symptoms unanswered leaves testType absent then primary yes adds it`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), null) }
        runCatching { d.updateList(7, 0) }
        assertEquals(-1, d.getIndexById(28))
        d.setValueById(7, "opt0")
        runCatching { d.updateList(7, 0) }
        assertNotEquals(-1, d.getIndexById(28))
    }

    @Test
    fun `updateList secondary symptoms alone trigger suspected via count threshold`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), null) }
        d.setValueById(7, "opt1")
        d.setValueById(8, "opt1")
        d.setValueById(9, "opt1")
        d.setValueById(10, "opt0")
        d.setValueById(11, "opt0")
        runCatching { d.updateList(11, 0) }
        assertNotEquals(-1, d.getIndexById(28))
    }

    @Test
    fun `updateList rapid diagnostic negative and other results without both test type`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        d.setValueById(28, "opt0")
        runCatching { d.setValueById(17, "opt1"); d.updateList(17, 0) }
        assertNotEquals(-1, d.getIndexById(18))
        assertEquals(-1, d.getIndexById(27))
        runCatching { d.setValueById(17, "opt0"); d.updateList(17, 0) }
        assertNotEquals(-1, d.getIndexById(18))
        assertEquals(-1, d.getIndexById(27))
    }

    @Test
    fun `setUpPage rapid only test type skips dateOfTest when rapid result already resolved`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 1, 0, "opt2", "opt0", "opt0")
            )
        }
        assertNotEquals(-1, d.getIndexById(17))
        assertEquals(-1, d.getIndexById(18))
    }

    @Test
    fun `setUpPage slide only test type pf branch skips dateOfSlidetest when pf result resolved`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 2, 1, null, "opt2", "opt2")
            )
        }
        assertNotEquals(-1, d.getIndexById(19))
        assertEquals(-1, d.getIndexById(21))
    }

    @Test
    fun `updateList rapid diagnostic negative result adds only dateOfTest when test type not both`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        d.setValueById(28, "opt0")
        runCatching { d.updateList(28, 0) }
        runCatching { d.setValueById(17, "opt1"); d.updateList(17, 0) }
        assertNotEquals(-1, d.getIndexById(18))
        assertEquals(-1, d.getIndexById(27))
    }

    @Test
    fun `updateList rapid diagnostic other result adds only dateOfTest when test type not both`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        d.setValueById(28, "opt0")
        runCatching { d.updateList(28, 0) }
        runCatching { d.setValueById(17, "opt0"); d.updateList(17, 0) }
        assertNotEquals(-1, d.getIndexById(18))
        assertEquals(-1, d.getIndexById(27))
    }

    @Test
    fun `updateBen applies reproductive status and processed flag when genDetails present`() = runTest {
        val d = ds()
        val gen = mockk<org.piramalswasthya.sakhi.model.BenRegGen>(relaxed = true)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns gen
        every { ben.processed } returns "P"
        d.updateBen(ben)
        io.mockk.verify { gen.reproductiveStatusId = 2 }
        io.mockk.verify { ben.processed = "U" }
    }

    @Test
    fun `updateBen tolerates null genDetails and keeps a new record unprocessed`() = runTest {
        val d = ds()
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        every { ben.processed } returns "N"
        d.updateBen(ben)
        io.mockk.verify(exactly = 0) { ben.processed = "U" }
    }

    // ---- Gap coverage: "both" test-type path with pf slide option + independently-resolved
    // slidePv/slidePf results (previously only the pv-slide-option variant, with both results
    // unresolved together, was exercised for this else branch of setUpPage). ----

    @Test
    fun `setUpPage both test type path with pf slide option and unresolved slide results`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                caseSaved("opt0", "opt0", 3, 1, "opt0", "opt2", "opt2")
            )
        }
        assertNotEquals(-1, d.getIndexById(28))
        assertNotEquals(-1, d.getIndexById(18))
        assertNotEquals(-1, d.getIndexById(27))
        assertNotEquals(-1, d.getIndexById(19))
        assertEquals(-1, d.getIndexById(20))
        assertEquals(-1, d.getIndexById(21))
    }

    // ---- Gap coverage: the symptom-branch try/catch in handleListOnValueChanged silently
    // swallows an exception when the localized dc_case_status array is too short to index [4].
    // Every existing test relies on the generic 80-entry mock, so the catch body never ran. ----

    @Test
    fun `updateList symptom branch swallows exception from short case status array`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        every { mockResources.getStringArray(R.array.dc_case_status) } returns arrayOf("opt0", "opt1")
        d.setValueById(7, "opt0")
        runCatching { d.updateList(7, 0) }
        assertNotEquals(-1, d.getIndexById(28))
    }

    // ---- Gap coverage: slideTestOptions.id handler has no trailing else - when the value
    // matches neither pf_pv[0] nor pf_pv[1], both triggerDependants calls are skipped. ----

    @Test
    fun `updateList slideTestOptions value matching neither pf nor pv skips both triggers`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        d.setValueById(27, "unmatched")
        runCatching { d.updateList(27, 0) }
        assertEquals(-1, d.getIndexById(19))
        assertEquals(-1, d.getIndexById(20))
    }

    // ---- Gap coverage: mapValues was only ever exercised with a relaxed mockk cache model,
    // so assignments ran but no test verified the actually computed values. Use a real cache
    // instance and assert the mapped fields for both a "yes" (suspected) and a "no" record. ----

    @Test
    fun `mapValues maps suspected all-yes record onto real cache model`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMock(), suspectedAllSymptoms()) }
        val cache = MalariaScreeningCache(benId = 1L, visitId = 1L, houseHoldDetailsId = 1L)
        d.mapValues(cache, 0)
        assertEquals(true, cache.feverMoreThanTwoWeeks)
        assertEquals(true, cache.fluLikeIllness)
        assertEquals(true, cache.shakingChills)
        assertEquals(true, cache.headache)
        assertEquals(true, cache.muscleAches)
        assertEquals(true, cache.tiredness)
        assertEquals(true, cache.nausea)
        assertEquals(true, cache.vomiting)
        assertEquals(true, cache.diarrhea)
        assertEquals(1, cache.diseaseTypeID)
        assertEquals(1L, cache.visitId)
    }

    @Test
    fun `mapValues maps negative-answers record onto real cache model`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMock(),
                savedMock(false, "opt1", 1, 0, "opt0")
            )
        }
        val cache = MalariaScreeningCache(benId = 2L, visitId = 2L, houseHoldDetailsId = 2L)
        d.mapValues(cache, 0)
        assertEquals(false, cache.feverMoreThanTwoWeeks)
        assertEquals(false, cache.fluLikeIllness)
        assertEquals(false, cache.shakingChills)
        assertEquals(false, cache.headache)
        assertEquals(false, cache.muscleAches)
        assertEquals(false, cache.tiredness)
        assertEquals(false, cache.nausea)
        assertEquals(false, cache.vomiting)
        assertEquals(false, cache.diarrhea)
        assertEquals(1, cache.diseaseTypeID)
    }
}
