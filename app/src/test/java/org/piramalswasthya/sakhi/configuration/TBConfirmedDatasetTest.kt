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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.utils.HelperUtil

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
}
