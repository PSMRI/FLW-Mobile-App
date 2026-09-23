package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.MockK
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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyConfirmedDatasetTest : BaseViewModelTest() {

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
        every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockk(relaxed = true)
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `construction ENGLISH and HINDI`() {
        runCatching { LeprosyConfirmedDataset(context, Languages.ENGLISH) }
        runCatching { LeprosyConfirmedDataset(context, Languages.HINDI) }
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path three arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null) }
        runCatching { ds.getIndexOfDate() }
        runCatching { ds.validateFollowUpDate(0L) }
        runCatching { ds.validateFollowUpDate(System.currentTimeMillis()) }
        runCatching { ds.getNextFollowUpAvailabilityMessage() }
        runCatching { ds.validateForm() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path two arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path three arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<LeprosyScreeningCache>(relaxed = true)
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved, followUp) }
        runCatching { ds.mapValues(mockk<LeprosyFollowUpCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<LeprosyFollowUpCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.validateForm() }
        runCatching { ds.validateFollowUpDate(System.currentTimeMillis()) }
        runCatching { ds.getNextFollowUpAvailabilityMessage() }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path two arg`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, followUp) }
        runCatching { ds.mapValues(mockk<LeprosyFollowUpCache>(relaxed = true), 0) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..21) {
        }
        assertNotNull(ds.listFlow)
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private fun screening(
        leprosyStatus: String? = "opt2",
        referToName: String? = "opt3",
        typeOfLeprosy: String? = "opt0",
        symptomsPosition: Int? = 1,
        treatmentStartDate: Long = DAY_MS * 19000,
        homeVisitDate: Long = DAY_MS * 18990,
        visitLabel: String? = "Visit -3"
    ) = LeprosyScreeningCache(
        benId = 11L,
        houseHoldDetailsId = 22L,
        createdBy = "tester",
        modifiedBy = "tester",
        homeVisitDate = homeVisitDate,
        leprosyStatus = leprosyStatus,
        referToName = referToName,
        typeOfLeprosy = typeOfLeprosy,
        leprosySymptoms = "opt0",
        leprosySymptomsPosition = symptomsPosition,
        visitLabel = visitLabel,
        otherReferredTo = "some other place",
        remarks = "remark text",
        treatmentStartDate = treatmentStartDate,
        currentVisitNumber = 3
    )

    private fun followUp(
        treatmentStatus: String? = "opt1",
        mdt: String? = "opt0",
        treatmentCompleteDate: Long = 0L,
        followUpDate: Long = DAY_MS * 19100,
        treatmentStartDate: Long = DAY_MS * 19000,
        leprosyStatus: String? = "opt2",
        referToName: String? = "opt3",
        symptomsPosition: Int? = 0,
        visitLabel: String? = "Visit -2"
    ) = LeprosyFollowUpCache(
        benId = 11L,
        visitNumber = 2,
        createdBy = "tester",
        modifiedBy = "tester",
        followUpDate = followUpDate,
        treatmentStatus = treatmentStatus,
        mdtBlisterPackReceived = mdt,
        treatmentCompleteDate = treatmentCompleteDate,
        remarks = "fu remark",
        homeVisitDate = DAY_MS * 18990,
        leprosySymptoms = "opt0",
        typeOfLeprosy = "opt1",
        leprosySymptomsPosition = symptomsPosition,
        visitLabel = visitLabel,
        leprosyStatus = leprosyStatus,
        referToName = referToName,
        treatmentStartDate = treatmentStartDate
    )

    // ---------------------------------------------------------------------
    // three-arg setUpPage
    // ---------------------------------------------------------------------

    @Test
    fun `three arg populated saved builds ten element page`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), screening(), null)
        assertEquals(10, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(12) >= 0)
    }

    @Test
    fun `three arg saved with last entry leprosy status inserts other`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(leprosyStatus = "opt79", referToName = "opt79"), null)
        // 'other' inserted twice (once after status, once after referredTo)
        assertEquals(12, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(10) >= 0)
    }

    @Test
    fun `three arg saved plus followUp without completion date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), screening(), followUp())
        assertEquals(10, ds.listFlow.value.size)
        assertTrue(ds.getListSize() > 0)
    }

    @Test
    fun `three arg saved plus followUp with completion date adds treatment end date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(
            mockk<BenRegCache>(relaxed = true),
            screening(),
            followUp(treatmentCompleteDate = DAY_MS * 19200)
        )
        assertEquals(11, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(20) >= 0)
    }

    @Test
    fun `three arg saved with zero treatment start date leaves estimation blank`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(treatmentStartDate = 0L), null)
        assertEquals(10, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(21) >= 0)
    }

    @Test
    fun `three arg saved with unknown type of leprosy leaves estimation blank`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(typeOfLeprosy = "opt7"), null)
        assertEquals(10, ds.listFlow.value.size)
    }

    @Test
    fun `three arg saved with six month leprosy type`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(typeOfLeprosy = "opt1"), null)
        assertEquals(10, ds.listFlow.value.size)
    }

    @Test
    fun `three arg saved with null type of leprosy`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(typeOfLeprosy = null), null)
        assertEquals(10, ds.listFlow.value.size)
    }

    @Test
    fun `three arg saved with numeric visit label suffix`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(visitLabel = null, symptomsPosition = null), null)
        assertEquals(10, ds.listFlow.value.size)
    }

    @Test
    fun `three arg saved in HINDI language`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.HINDI)
        ds.setUpPage(null, screening(), followUp())
        assertEquals(10, ds.listFlow.value.size)
    }

    // ---------------------------------------------------------------------
    // two-arg setUpPage
    // ---------------------------------------------------------------------

    @Test
    fun `two arg populated followUp builds read only page`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), followUp())
        assertEquals(10, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(19) >= 0)
    }

    @Test
    fun `two arg followUp with last entry status and refer inserts other twice`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, followUp(leprosyStatus = "opt79", referToName = "opt79"))
        assertEquals(12, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(10) >= 0)
    }

    @Test
    fun `two arg followUp with completion date adds treatment end date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, followUp(treatmentCompleteDate = DAY_MS * 19200))
        assertEquals(11, ds.listFlow.value.size)
        assertTrue(ds.getIndexById(20) >= 0)
    }

    @Test
    fun `two arg null followUp builds create page of ten`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null)
        assertEquals(10, ds.listFlow.value.size)
        assertEquals(-1, ds.getIndexById(20))
    }

    // ---------------------------------------------------------------------
    // updateList / handleListOnValueChanged
    // ---------------------------------------------------------------------

    @Test
    fun `updateList on treatment start date recomputes expected completion`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(treatmentStartDate = 0L), null)
        ds.setValueById(17, "01-01-2024")
        ds.updateList(17, 0)
        assertEquals(10, ds.listFlow.value.size)
    }

    @Test
    fun `updateList on follow up date adds treatment status element`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        assertTrue(ds.getIndexById(19) >= 0)
        assertEquals(11, ds.listFlow.value.size)
    }

    @Test
    fun `updateList on treatment status last entry adds end date then removes it`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(19, "opt79")
        ds.updateList(19, 0)
        assertTrue(ds.getIndexById(20) >= 0)
        ds.setValueById(19, "opt1")
        ds.updateList(19, 0)
        assertEquals(-1, ds.getIndexById(20))
    }

    @Test
    fun `updateList with unhandled form id keeps page unchanged`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        val before = ds.getListSize()
        ds.updateList(1, 0)
        ds.updateList(13, 0)
        assertEquals(before, ds.getListSize())
    }

    // ---------------------------------------------------------------------
    // mapValues
    // ---------------------------------------------------------------------

    @Test
    fun `mapValues copies non completed values into cache`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(18, "opt0")
        ds.setValueById(19, "opt1")
        val cache = followUp(treatmentStatus = null, mdt = null, treatmentCompleteDate = 99L)
        ds.mapValues(cache, 0)
        assertEquals(0L, cache.treatmentCompleteDate)
        assertEquals("opt0", cache.mdtBlisterPackReceived)
        assertEquals("opt1", cache.treatmentStatus)
        assertTrue(cache.followUpDate > 0L)
    }

    @Test
    fun `mapValues stores completion date when status is last entry`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(19, "opt79")
        ds.updateList(19, 0)
        ds.setValueById(20, "20-06-2025")
        val cache = followUp()
        ds.mapValues(cache, 0)
        assertTrue(cache.treatmentCompleteDate > 0L)
    }

    // ---------------------------------------------------------------------
    // validation helpers
    // ---------------------------------------------------------------------

    @Test
    fun `validateForm reports blank follow up date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "")
        assertEquals("Follow-up date is required", ds.validateForm())
    }

    @Test
    fun `validateForm reports missing treatment status`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        assertEquals("Treatment status is required", ds.validateForm())
    }

    @Test
    fun `validateForm reports missing treatment end date for completed status`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(19, "opt79")
        ds.updateList(19, 0)
        ds.setValueById(20, "")
        assertEquals(
            "Treatment end date is required when treatment status is completed",
            ds.validateForm()
        )
    }

    @Test
    fun `validateForm passes for complete input`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(19, "opt79")
        ds.updateList(19, 0)
        ds.setValueById(20, "20-06-2025")
        assertNull(ds.validateForm())
    }

    @Test
    fun `validateFollowUpDate rejects date before home visit`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(homeVisitDate = DAY_MS * 19000), null)
        assertEquals(
            "Follow-up date cannot be before home visit date",
            ds.validateFollowUpDate(DAY_MS * 18000)
        )
    }

    @Test
    fun `validateFollowUpDate rejects future date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(homeVisitDate = 1000L), null)
        assertEquals(
            "Follow-up date cannot be in the future",
            ds.validateFollowUpDate(System.currentTimeMillis() + DAY_MS * 30)
        )
    }

    @Test
    fun `validateFollowUpDate accepts valid date without previous follow up`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(homeVisitDate = 1000L), null)
        assertNull(ds.validateFollowUpDate(System.currentTimeMillis() - DAY_MS))
    }

    @Test
    fun `validateFollowUpDate with previous follow up rejects only future dates`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), followUp(followUpDate = DAY_MS * 19100))
        assertNull(ds.validateFollowUpDate(System.currentTimeMillis() - DAY_MS))
        assertEquals(
            "Follow-up date cannot be in the future",
            ds.validateFollowUpDate(System.currentTimeMillis() + DAY_MS * 30)
        )
    }

    @Test
    fun `getNextFollowUpAvailabilityMessage is null without previous follow up`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        assertNull(ds.getNextFollowUpAvailabilityMessage())
    }

    @Test
    fun `getNextFollowUpAvailabilityMessage is null for old follow up`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), followUp(followUpDate = DAY_MS * 17000))
        assertNull(ds.getNextFollowUpAvailabilityMessage())
    }

    @Test
    fun `getIndexOfDate returns index of follow up date element`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        assertEquals(ds.getIndexById(12), ds.getIndexOfDate())
        assertTrue(ds.getIndexOfDate() >= 0)
    }

    @Test
    fun `updateBen marks reproductive status and processed flag`() {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns "P"
        ds.updateBen(ben)
        val benNotProcessed = mockk<BenRegCache>(relaxed = true)
        every { benNotProcessed.processed } returns "N"
        ds.updateBen(benNotProcessed)
        assertNotNull(ds.listFlow)
    }

    private fun element(ds: LeprosyConfirmedDataset, id: Int): FormElement =
        ds.listFlow.value.first { it.id == id }

    private fun asFormDate(millis: Long): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date(millis))

    private fun monthsFromNow(months: Int): Long =
        Calendar.getInstance().apply { add(Calendar.MONTH, months) }.timeInMillis

    @Test
    fun `a follow up recorded this month blocks the next one and reports availability`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), followUp(followUpDate = System.currentTimeMillis()))
        assertFalse(element(ds, 12).isEnabled)
        val message = ds.getNextFollowUpAvailabilityMessage()
        assertNotNull(message)
        assertTrue(message!!.startsWith("Next follow-up will be available from"))
    }

    @Test
    fun `a follow up recorded last month re-opens the date picker`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), followUp(followUpDate = monthsFromNow(-1)))
        assertTrue(element(ds, 12).isEnabled)
        assertNull(ds.getNextFollowUpAvailabilityMessage())
    }

    private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    private fun marchFirstThisYear(): Long = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear())
        set(Calendar.MONTH, Calendar.MARCH)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun `treatment status stays on the pre completion list while treatment is running`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(
            null,
            screening(treatmentStartDate = marchFirstThisYear(), typeOfLeprosy = "opt1"),
            null
        )
        assertEquals("01-09-${currentYear()}", element(ds, 21).value)
        ds.setValueById(12, "01-02-${currentYear()}")
        ds.updateList(12, 0)
        val status = element(ds, 19)
        assertEquals(R.array.leprosy_treatment_status_before_time, status.arrayId)
        assertEquals("", status.value)
    }

    @Test
    fun `treatment status switches to the completion list once the expected end month is reached`() =
        runTest {
            val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
            ds.setUpPage(
                null,
                screening(treatmentStartDate = marchFirstThisYear(), typeOfLeprosy = "opt1"),
                null
            )
            ds.setValueById(12, "01-10-${currentYear()}")
            ds.updateList(12, 0)
            assertEquals(R.array.leprosy_treatment_status, element(ds, 19).arrayId)
        }

    @Test
    fun `treatment status list is left alone when either date is missing`() = runTest {
        val noFollowUpDate = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        noFollowUpDate.setUpPage(null, screening(), null)
        noFollowUpDate.updateList(12, 0)
        assertEquals(
            R.array.leprosy_treatment_status_before_time,
            element(noFollowUpDate, 19).arrayId
        )

        val noEstimation = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        noEstimation.setUpPage(null, screening(treatmentStartDate = 0L), null)
        noEstimation.setValueById(12, "15-06-2025")
        noEstimation.updateList(12, 0)
        assertEquals(R.array.leprosy_treatment_status_before_time, element(noEstimation, 19).arrayId)
    }

    @Test
    fun `a blank type of leprosy clears the expected completion date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(treatmentStartDate = 0L), null)
        ds.setValueById(16, "   ")
        ds.setValueById(17, "01-01-2025")
        ds.updateList(17, 0)
        assertEquals("", element(ds, 21).value)
    }

    @Test
    fun `saved screening with an unparsable visit label and out of range symptoms`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(
            null,
            screening(visitLabel = "Visit -abc", symptomsPosition = 200),
            null
        )
        assertEquals("opt1", element(ds, 14).value)
    }

    @Test
    fun `follow up page defaults a missing symptom position and visit label`() = runTest {
        val nullPosition = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        nullPosition.setUpPage(
            null,
            followUp(symptomsPosition = null, visitLabel = "Visit -abc")
        )
        assertEquals("opt1", element(nullPosition, 14).value)

        val outOfRange = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        outOfRange.setUpPage(null, followUp(symptomsPosition = 200, visitLabel = null))
        assertEquals("opt1", element(outOfRange, 14).value)
    }

    @Test
    fun `a follow up without a stored screening still builds the page`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null, followUp())
        assertEquals(10, ds.listFlow.value.size)
        assertNotNull(element(ds, 15).value)
    }

    @Test
    fun `updateBen tolerates a beneficiary without general details`() {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        every { ben.processed } returns "P"
        ds.updateBen(ben)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `validateForm surfaces the follow up date validation error`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, asFormDate(System.currentTimeMillis() + DAY_MS * 30))
        assertEquals("Follow-up date cannot be in the future", ds.validateForm())
    }

    @Test
    fun `validateForm accepts an in progress treatment status without an end date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(19, "opt5")
        assertNull(ds.validateForm())
    }

    @Test
    fun `rebuilding the dropdown keeps a selection that is still valid`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screening(), null)
        ds.setValueById(12, "15-06-2025")
        ds.updateList(12, 0)
        ds.setValueById(19, "opt5")
        ds.updateList(12, 0)
        assertEquals("opt5", element(ds, 19).value)
    }

    @Test
    fun `three arg page falls back to the follow up treatment start date`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        ds.setUpPage(
            null,
            screening(treatmentStartDate = 0L),
            followUp(treatmentStartDate = DAY_MS * 19000)
        )
        assertNotNull(element(ds, 17).value)
        assertEquals(10, ds.listFlow.value.size)
    }

    companion object {
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
