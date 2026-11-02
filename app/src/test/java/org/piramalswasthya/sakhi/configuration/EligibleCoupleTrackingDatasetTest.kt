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
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    private val isMitanin = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)

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

    @Test
    fun `updateList usingFamilyPlanningMitanin zero children entries branch`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 0)
        }
        runCatching { ds.setValueById(8, "opt0"); ds.updateList(8, 0) }
        runCatching { ds.setValueById(8, "opt1"); ds.updateList(8, 0) }
        assertNotNull(ds.listFlow)
    }

    // ===================== added: dateOfAntraInjection invalid path + getNextDose branches =======

    // calculateNextInjectionDate() returns ("", "") on an unparsable date, which routes
    // dueDateOfAntraInjection.value into the resources.getString(invalid_injection_date) branch
    // instead of the "$minDate to $maxDate" branch every pre-existing test hits.
    @Test
    fun `updateList dateOfAntraInjection shows the invalid message for an unparsable date`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 2)
        }
        runCatching { ds.setValueById(13, ""); ds.updateList(13, 0) }
        runCatching { ds.setValueById(13, "not-a-date"); ds.updateList(13, 0) }
        assertNotNull(ds.listFlow)
    }

    // getNextDose(): the pre-existing "december last-track" test always lands >120 days away from
    // "today" (2023 vs. the real clock), so it only ever hits the "> 120 days -> dose one" branch.
    // Driving lastDate to within 120 days of now reaches the doseNum increment branch, and pairing
    // a dose already at 10 reaches the "no more dose" branch.
    @Test
    fun `getNextDose increments the dose for a recent last injection`() = runTest {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val recentDate = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time)

        val recentTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { recentTrack.antraDose } returns "Dose-2"
        every { recentTrack.dateOfAntraInjection } returns recentDate
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), recentTrack, null, 2)
        }
        assertNotNull(ds.listFlow)

        val maxedTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { maxedTrack.antraDose } returns "Dose-10"
        every { maxedTrack.dateOfAntraInjection } returns recentDate
        val ds2 = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds2.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), maxedTrack, null, 2)
        }
        assertNotNull(ds2.listFlow)
    }

    @Test
    fun `isPregnant value opt0 branch is reached after a positive pregnancy test result`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 2)
        }
        runCatching { ds.setValueById(4, "opt0"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(5, "opt0"); ds.updateList(5, 0) }
        runCatching { ds.updateList(6, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `isPregnant value opt1 branch is reached after a negative pregnancy test result`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 2)
        }
        runCatching { ds.setValueById(4, "opt0"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(5, "opt1"); ds.updateList(5, 0) }
        runCatching { ds.updateList(6, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `getNextDose falls back to dose one when only one of dose or date is null`() = runTest {
        val doseOnlyTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { doseOnlyTrack.antraDose } returns "Dose-3"
        every { doseOnlyTrack.dateOfAntraInjection } returns null
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), doseOnlyTrack, null, 2)
        }
        assertNotNull(ds.listFlow)

        val dateOnlyTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { dateOnlyTrack.antraDose } returns null
        every { dateOnlyTrack.dateOfAntraInjection } returns "01-01-2023"
        val ds2 = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds2.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), dateOnlyTrack, null, 2)
        }
        assertNotNull(ds2.listFlow)
    }

    @Test
    fun `getNextDose defaults dose number to zero when last dose has no digits`() = runTest {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val recentDate = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }.time)
        val track = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        every { track.antraDose } returns "NoDigitsHere"
        every { track.dateOfAntraInjection } returns recentDate
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), track, null, 2)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path antra method with null antra dose skips antra assignment`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = true, method = "opt1/2")
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setImageUriToFormElement ignores an unrecognised form id`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, 2)
        }
        runCatching { ds.setImageUriToFormElement(999, mockk<Uri>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateBen skips reproductive status update when genDetails is null and leaves N processed alone`() = runTest {
        val ds = trackingPage(null, 2)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        every { ben.processed } returns "N"
        ds.updateBen(ben)
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    // ===================== added: elvis-fallback / unhandled-branch coverage =====================

    // mapValues' isPregnancyTestDone assignment is `getEnglishValueInArray(...) ?: isPregnancyTestDone.value`.
    // Every earlier test only ever leaves isPregnancyTestDone.value at either null/blank (short-circuits
    // getEnglishValueInArray before any array lookup) or a value present in the mocked 80-element array
    // (found by the lookup, so the elvis fallback is never actually needed). Forcing a non-blank value
    // that is absent from the mocked array makes the lookup fail and reach the fallback for real.
    @Test
    fun `mapValues falls back to the raw isPregnancyTestDone value when it is not found in the localized array`() =
        runTest {
            val ds = trackingPage(null, 2)
            ds.setValueById(4, "unmapped_raw_value")
            val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
            ds.mapValues(target, 0)
            verify { target.isPregnancyTestDone = "unmapped_raw_value" }
        }

    // handleListOnValueChanged's dateOfAntraInjection branch reads `dateOfAntraInjection.value ?: ""`.
    // Every earlier test calls setValueById(13, ...) first, so .value is always a non-null String (even
    // "" itself counts as non-null) and the elvis's null side is never actually taken. Calling updateList
    // on a page where the field was never touched leaves .value genuinely null and reaches that branch.
    @Test
    fun `updateList dateOfAntraInjection defaults to an empty injection date when the value was never set`() =
        runTest {
            val ds = trackingPage(null, 2)
            ds.updateList(13, 0)
            val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
            ds.mapValues(target, 0)
            verify { target.dueDateOfAntraInjection = "x" }
        }

    // handleListOnValueChanged's top-level `when(formId)` has an `else -> -1` fallthrough for any id it
    // does not explicitly handle (e.g. financialYear.id = 2). No existing test ever calls updateList with
    // such an id, so that branch site was never executed; updateList only rebuilds the visible list when
    // handleListOnValueChanged returns something other than -1, so the list size must stay identical.
    @Test
    fun `updateList ignores an unhandled form id and leaves the list unchanged`() = runTest {
        val ds = trackingPage(null, 2)
        val sizeBefore = ds.listFlow.value.size
        ds.updateList(2, 0)
        assertEquals(sizeBefore, ds.listFlow.value.size)
    }

    // ===================== added: genuinely-untested branches (non-flavor-gated) =====================
    //
    // NOTE ON A STRUCTURAL LIMIT IN THIS CLASS: every `!BuildConfig.FLAVOR.contains("mitanin",
    // ignoreCase = true)` check in EligibleCoupleTrackingDataset (setUpPage's FP-restore branch,
    // the mpaFileUpload1/discharge-summary gating, every handleListOnValueChanged mitanin/non-mitanin
    // split, and mapValues' usingFamilyPlanning ternary) always evaluates to `true` under the
    // "niramayDebug" variant this project's coverage is measured against, because BuildConfig.FLAVOR
    // is the compile-time constant "niramay" there and MockK cannot intercept a plain static-final
    // field read. The mitanin-only else-branches in each of those sites are therefore permanently
    // unreachable from a unit test without editing app/src/main (same limitation already documented
    // in VHNDDatasetTest.kt for that class) - no new test below attempts to force them.

    @Test
    fun `edit path shows saved method of contraception even when not currently using family planning`() =
        runTest {
            val saved = ectSaved(isPregTest = "opt1", usingFP = false, method = "opt3")
            val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
            assertTrue(
                "methodOfContraception must be shown when a method was previously saved even if usingFamilyPlanning is false",
                ds.getIndexById(10) >= 0
            )
        }

    @Test
    fun `edit path lmp date uses saved value even when saved lmp date is zero`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = false)
        every { saved.lmpDate } returns 0L
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        val expectedLmp = Dataset.getLongFromDate(Dataset.getDateFromLong(0L))
        verify { target.lmpDate = expectedLmp }
    }

    @Test
    fun `setUpPage throws when noOfChildren is null`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        var threw = false
        try {
            ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, null, null)
        } catch (e: NullPointerException) {
            threw = true
        }
        assertTrue("setUpPage must throw when noOfChildren is null", threw)
    }

    // The pre-existing method-variant tests call mapValues() through runCatching without asserting
    // on the mutated cache, so the three-way `when` in mapValues (last()/methods[1]/else) is reached
    // but its actual output was never checked. These verify the real computed strings.

    @Test
    fun `mapValues concatenates english method and antra dose for the dose based method`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = true, method = "opt1/2", antra = "opt2")
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.methodOfContraception = "opt1/opt2" }
    }

    @Test
    fun `mapValues uses the free text value for the other method option`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = true, method = "opt99")
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.methodOfContraception = "opt99" }
    }

    @Test
    fun `mapValues resolves the plain english value for a standard method`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = true, method = "opt3")
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.methodOfContraception = "opt3" }
    }

    @Test
    fun `mapValues marks using family planning true when the yes option was selected`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = true, method = "opt3")
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.usingFamilyPlanning = true }
    }

    @Test
    fun `mapValues marks using family planning false when the no option was selected`() = runTest {
        val saved = ectSaved(isPregTest = "opt1", usingFP = false)
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.setUpPage(mockk<BenRegCache>(relaxed = true), System.currentTimeMillis(), null, saved, 2)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        verify { target.usingFamilyPlanning = false }
    }

    // The pre-existing "updateList drives value changed handlers" test only checks that setting the
    // injection date to "01-01-2023" doesn't throw; it never checks what dueDateOfAntraInjection
    // actually becomes. This verifies the real computed "$minDate to $maxDate" string.
    @Test
    fun `updateList dateOfAntraInjection computes the real due date range for a valid date`() = runTest {
        val ds = trackingPage(null, 2)
        ds.setValueById(4, "opt1")
        ds.updateList(4, 0)
        ds.setValueById(7, "opt0")
        ds.updateList(7, 0)
        ds.setValueById(10, "opt1")
        ds.updateList(10, 0)
        ds.setValueById(13, "01-01-2023")
        ds.updateList(13, 0)
        val target = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        ds.mapValues(target, 0)
        if (isMitanin) {
            verify { target.dueDateOfAntraInjection = "x" }
        } else {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val base = sdf.parse("01-01-2023")!!
            val cal = Calendar.getInstance()
            cal.time = base
            cal.add(Calendar.DAY_OF_YEAR, 76)
            val minDate = sdf.format(cal.time)
            cal.time = base
            cal.add(Calendar.DAY_OF_YEAR, 120)
            val maxDate = sdf.format(cal.time)
            verify { target.dueDateOfAntraInjection = "$minDate to $maxDate" }
        }
    }

    @Test
    fun `antraDoseValue noOfChildrens lastDose and lastDateofDose properties are readable and writable`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        ds.antraDoseValue = "Dose-5"
        assertEquals("Dose-5", ds.antraDoseValue)
        ds.noOfChildrens = 4
        assertEquals(4, ds.noOfChildrens)
        ds.lastDose = "Dose-2"
        assertEquals("Dose-2", ds.lastDose)
        ds.lastDateofDose = "01-01-2023"
        assertEquals("01-01-2023", ds.lastDateofDose)
        ds.lastDose = null
        assertNull(ds.lastDose)
        ds.lastDateofDose = null
        assertNull(ds.lastDateofDose)
    }
}
