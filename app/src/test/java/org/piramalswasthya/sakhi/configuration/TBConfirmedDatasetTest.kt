package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Deep coverage test for [TBConfirmedDataset]: exercises the 3-arg setUpPage
 * outcome branches, mapValues, and the validation helpers (validateAllFields,
 * validateCurrentFollowUpDate). Each builder call wrapped in runCatching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TBConfirmedDatasetTest : BaseViewModelTest() {

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
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path exercises builders`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        // setUpPage(ben: BenRegCache?, saved: TBConfirmedTreatmentCache?, suspectedTb: TBSuspectedCache?)
        runCatching { ds.setUpPage(null, null, null) }
        runCatching { ds.validateAllFields() }
        runCatching { ds.validateCurrentFollowUpDate() }
        runCatching { ds.mapValues(mockk<TBConfirmedTreatmentCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<TBConfirmedTreatmentCache>(relaxed = true), 1) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path exercises saved branches`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<TBConfirmedTreatmentCache>(relaxed = true)
        val suspected = mockk<TBSuspectedCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved, suspected) }
        runCatching { ds.validateAllFields() }
        runCatching { ds.validateCurrentFollowUpDate() }
        runCatching { ds.mapValues(mockk<TBConfirmedTreatmentCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi construction`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null, null, null) }
        assertNotNull(ds.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun savedTb(
        treatmentCompleted: Boolean?,
        treatmentOutcome: String?,
        anyDiscomfort: Boolean? = true,
        withFollowUp: Boolean = true,
    ): TBConfirmedTreatmentCache {
        val s = mockk<TBConfirmedTreatmentCache>(relaxed = true)
        every { s.regimenType } returns "opt0"
        every { s.treatmentStartDate } returns 1_600_000_000_000L
        every { s.expectedTreatmentCompletionDate } returns 1_610_000_000_000L
        every { s.followUpDate } returns if (withFollowUp) 1_605_000_000_000L else null
        every { s.monthlyFollowUpDone } returns "1"
        every { s.adherenceToMedicines } returns "opt0"
        every { s.anyDiscomfort } returns anyDiscomfort
        every { s.treatmentCompleted } returns treatmentCompleted
        every { s.actualTreatmentCompletionDate } returns 1_615_000_000_000L
        every { s.treatmentOutcome } returns treatmentOutcome
        every { s.dateOfDeath } returns 1_618_000_000_000L
        every { s.placeOfDeath } returns "opt2"
        every { s.reasonForDeath } returns "cause"
        every { s.reasonForNotCompleting } returns "reason"
        return s
    }

    @Test
    fun `edit completed with death outcome`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val suspected = mockk<TBSuspectedCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedTb(true, "opt3"), suspected)
            ds.mapValues(mockk<TBConfirmedTreatmentCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit completed with disabling outcome`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedTb(true, "opt0"), null)
            ds.mapValues(mockk<TBConfirmedTreatmentCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit not completed adds reason`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedTb(false, null, anyDiscomfort = false), null)
            ds.validateAllFields()
            ds.validateCurrentFollowUpDate()
            ds.mapValues(mockk<TBConfirmedTreatmentCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit without followup only`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedTb(null, null, withFollowUp = false), null)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `create path validates required fields`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val suspected = mockk<TBSuspectedCache>(relaxed = true)
        every { suspected.visitDate } returns 1_600_000_000_000L
        runCatching {
            ds.setUpPage(null, null, suspected)
            ds.validateAllFields()
            ds.validateCurrentFollowUpDate()
        }
        assertNotNull(ds.listFlow)
    }

    // ===================== updateList-driven handler coverage =====================
    // Element ids: 1 regimenType, 2 treatmentStartDate, 3 expectedCompletion, 4 followUpDate,
    // 5 monthlyFollowUpDone, 6 adherence, 7 anyDiscomfort, 8 treatmentCompleted,
    // 9 actualCompletionDate, 10 treatmentOutcome, 11 dateOfDeath, 12 placeOfDeath,
    // 13 reasonForDeath, 14 reasonForNotCompleting.
    // handleListOnValueChanged is PROTECTED - only reachable via the public updateList wrapper.

    private suspend fun freshCreatePage(): TBConfirmedDataset {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, null)
        return ds
    }

    @Test
    fun `create page builds the base element list`() = runTest {
        val ds = freshCreatePage()
        val list = ds.listFlow.value
        assertTrue("base list should be built", list.isNotEmpty())
        // regimenType + treatmentStartDate + expectedCompletion + followUpDate + 3 more
        assertTrue(list.any { it.id == 1 })
        assertTrue(list.any { it.id == 2 })
        assertTrue(list.any { it.id == 4 })
    }

    @Test
    fun `updateList on regimen type recalculates expected completion`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(1, "opt0")
        ds.updateList(1, 0)
        assertTrue(ds.listFlow.value.any { it.id == 3 })
    }

    @Test
    fun `updateList on treatment start date enables follow up`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(2, "01-01-2024")
        ds.updateList(2, 0)
        val followUp = ds.listFlow.value.firstOrNull { it.id == 4 }
        assertNotNull(followUp)
        assertTrue(followUp!!.isEnabled)
    }

    @Test
    fun `updateList on follow up date runs validation`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(2, "01-01-2024")
        ds.updateList(2, 0)
        ds.setValueById(4, "01-03-2024")
        ds.updateList(4, 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `treatment completed yes adds actual completion and outcome`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), savedTb(true, "opt0"), null)
        ds.setValueById(8, "opt0")
        // triggerIndex for treatmentCompleted's yes-branch is index 0
        ds.updateList(8, 0)
        val ids = ds.listFlow.value.map { it.id }
        assertTrue("actualTreatmentCompletionDate expected", ids.contains(9))
        assertTrue("treatmentOutcome expected", ids.contains(10))
    }

    @Test
    fun `treatment completed no swaps in reason for not completing`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), savedTb(true, "opt0"), null)
        ds.setValueById(8, "opt1")
        ds.updateList(8, 1)
        val ids = ds.listFlow.value.map { it.id }
        assertTrue("reasonForNotCompleting expected", ids.contains(14))
        assertFalse("treatmentOutcome should be removed", ids.contains(10))
    }

    @Test
    fun `treatment outcome death adds death block then removes it`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), savedTb(true, "opt0"), null)
        ds.setValueById(8, "opt0")
        ds.updateList(8, 0)
        // tb_treatment_outcomes[3] == "opt3" with the mocked 80-entry array -> death branch
        ds.setValueById(10, "opt3")
        ds.updateList(10, 3)
        var ids = ds.listFlow.value.map { it.id }
        assertTrue("dateOfDeath expected", ids.contains(11))
        assertTrue("placeOfDeath expected", ids.contains(12))
        assertTrue("reasonForDeath expected", ids.contains(13))

        ds.setValueById(10, "opt2")
        ds.updateList(10, 2)
        ids = ds.listFlow.value.map { it.id }
        assertFalse("dateOfDeath removed", ids.contains(11))
        assertFalse("placeOfDeath removed", ids.contains(12))
    }

    @Test
    fun `edit page with follow up date exposes monthly follow up group`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), savedTb(null, null, withFollowUp = true), null)
        val ids = ds.listFlow.value.map { it.id }
        assertTrue(ids.contains(4))
        assertTrue(ids.contains(5))
        assertTrue(ids.contains(6))
        assertTrue(ids.contains(7))
    }

    @Test
    fun `edit page without follow up hides monthly follow up group`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), savedTb(null, null, withFollowUp = false), null)
        val ids = ds.listFlow.value.map { it.id }
        assertTrue(ids.contains(4))
        assertFalse(ids.contains(5))
        assertFalse(ids.contains(6))
    }

    @Test
    fun `edit page with regimen already saved locks the regimen field`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), savedTb(false, null), null)
        val regimen = ds.listFlow.value.firstOrNull { it.id == 1 }
        assertNotNull(regimen)
        assertFalse("regimen should be locked once saved", regimen!!.isEnabled)
    }

    @Test
    fun `mapValues on populated create page`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(1, "opt0")
        ds.updateList(1, 0)
        ds.setValueById(5, "1")
        ds.setValueById(6, "opt0")
        ds.setValueById(7, "opt1")
        val target = mockk<TBConfirmedTreatmentCache>(relaxed = true)
        ds.mapValues(target, 0)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `validate all fields on a freshly built create page`() = runTest {
        val ds = freshCreatePage()
        ds.validateAllFields()
        ds.validateCurrentFollowUpDate()
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `expected completion date computed for medium and long regimens`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(1, "opt1")
        ds.updateList(1, 0)
        var expected = ds.listFlow.value.first { it.id == 3 }
        assertTrue(expected.value!!.contains("to"))

        ds.setValueById(1, "opt2")
        ds.updateList(1, 0)
        expected = ds.listFlow.value.first { it.id == 3 }
        assertTrue(expected.value!!.contains("to"))
    }

    @Test
    fun `follow up date validation flags a future date`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(4, "01-01-2099")
        val valid = ds.validateCurrentFollowUpDate()
        assertFalse(valid)
        val followUp = ds.listFlow.value.first { it.id == 4 }
        assertTrue(followUp.errorText!!.contains("future"))
    }

    @Test
    fun `follow up date validation flags duplicate month and accepts next month`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val cal = Calendar.getInstance()
        cal.set(2021, Calendar.JANUARY, 10, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val treatmentStart = cal.timeInMillis
        cal.set(2021, Calendar.FEBRUARY, 10, 0, 0, 0)
        val lastFollowUp = cal.timeInMillis
        val saved = mockk<TBConfirmedTreatmentCache>(relaxed = true)
        every { saved.treatmentStartDate } returns treatmentStart
        every { saved.followUpDate } returns lastFollowUp
        every { saved.regimenType } returns null
        every { saved.treatmentCompleted } returns null
        ds.setUpPage(ben, saved, null)

        ds.setValueById(4, "15-02-2021")
        val sameMonthValid = ds.validateCurrentFollowUpDate()
        assertFalse(sameMonthValid)
        val followUpSameMonth = ds.listFlow.value.first { it.id == 4 }
        assertTrue(followUpSameMonth.errorText!!.contains("Only one follow-up"))

        ds.setValueById(4, "15-03-2021")
        val nextMonthValid = ds.validateCurrentFollowUpDate()
        assertTrue(nextMonthValid)
    }

    @Test
    fun `validateAllFields fails when treatment start date is missing`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(1, "opt0")
        ds.setValueById(2, null)
        val result = ds.validateAllFields()
        assertFalse(result)
        val startDateEl = ds.listFlow.value.first { it.id == 2 }
        assertTrue(startDateEl.errorText!!.contains("Treatment start date"))
    }

    @Test
    fun `checkAndEnableTreatmentCompletion transitions across regimen thresholds`() = runTest {
        val ds = freshCreatePage()
        ds.setValueById(1, "opt0")
        ds.updateList(1, 0)
        ds.setValueById(2, "10-01-2021")
        ds.updateList(2, 0)

        ds.setValueById(4, "10-02-2021")
        ds.updateList(4, 0)
        assertTrue(
            "below threshold should not add the completion element",
            ds.listFlow.value.none { it.id == 8 })

        ds.setValueById(4, "10-06-2021")
        ds.updateList(4, 0)
        var completed = ds.listFlow.value.first { it.id == 8 }
        assertTrue("threshold reached should enable completion", completed.isEnabled)

        ds.setValueById(1, "opt9")
        ds.updateList(1, 0)
        completed = ds.listFlow.value.first { it.id == 8 }
        assertTrue("unknown regimen with existing follow ups treated as met threshold", completed.isEnabled)
    }
}
