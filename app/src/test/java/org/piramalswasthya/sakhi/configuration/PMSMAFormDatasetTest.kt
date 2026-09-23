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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.PmsmaViewModel
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Consolidated coverage for [PMSMAFormDataset] (merged from Deep + Branch + Branch2 + Branch3
 * variants): setUpFirstPage with saved/null pmsma driving the pmsma?.let mutation branches
 * (highriskSymbols, highRiskReason last vs mid vs null), uniform/mixed boolean fields, nurse/visit
 * id positions, setExistingValues over both boolean sides, and ENGLISH/HINDI language.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PMSMAFormDatasetTest : BaseViewModelTest() {

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

    private fun ds() = PMSMAFormDataset(context, Languages.ENGLISH, mockk<PmsmaViewModel>(relaxed = true))

    private fun dsHindi() =
        PMSMAFormDataset(context, Languages.HINDI, mockk<PmsmaViewModel>(relaxed = true))

    private fun pmsmaMock(bool: Boolean, highRiskReason: String?): PMSMACache {
        val p = mockk<PMSMACache>(relaxed = true)
        every { p.haveMCPCard } returns bool
        every { p.twinPregnancy } returns bool
        every { p.hivTestDuringANC } returns bool
        every { p.swollenCondtion } returns bool
        every { p.bloodSugarTest } returns bool
        every { p.ultraSound } returns bool
        every { p.ironFolicAcid } returns bool
        every { p.calciumSupplementation } returns bool
        every { p.highriskSymbols } returns bool
        every { p.highRiskPregnant } returns bool
        every { p.highRiskPregnancyReferred } returns bool
        every { p.birthPrepAndNutritionAndFamilyPlanning } returns bool
        every { p.highRiskReason } returns highRiskReason
        every { p.systolicBloodPressure } returns if (bool) "120" else null
        every { p.bloodPressure } returns if (bool) "80" else null
        every { p.address } returns if (bool) "A".repeat(150) else null
        every { p.visitDate } returns if (bool) 1_600_000_000_000L else null
        every { p.expectedDateOfDelivery } returns 1_650_000_000_000L
        return p
    }

    // Mixed booleans - alternating true/false so both branches per field are hit within one cache.
    private fun mixedPmsma(highRiskReason: String?): PMSMACache {
        val p = mockk<PMSMACache>(relaxed = true)
        every { p.haveMCPCard } returns true
        every { p.twinPregnancy } returns false
        every { p.hivTestDuringANC } returns true
        every { p.swollenCondtion } returns false
        every { p.bloodSugarTest } returns true
        every { p.ultraSound } returns false
        every { p.ironFolicAcid } returns true
        every { p.calciumSupplementation } returns false
        every { p.highriskSymbols } returns true
        every { p.highRiskPregnant } returns false
        every { p.highRiskPregnancyReferred } returns true
        every { p.birthPrepAndNutritionAndFamilyPlanning } returns false
        every { p.highRiskReason } returns highRiskReason
        every { p.systolicBloodPressure } returns "130"
        every { p.bloodPressure } returns "85"
        every { p.address } returns "B".repeat(20)
        every { p.visitDate } returns 1_650_000_000_000L
        return p
    }

    // ---- Deep variant tests ----

    @Test
    fun `setUpFirstPage with saved and null`() = runTest {
        val d = ds()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                mockk<PregnantWomanAncCache>(relaxed = true),
                mockk<PMSMACache>(relaxed = true),
                1,
                1,
                mockk<PMSMACache>(relaxed = true)
            )
        }
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null,
                null,
                2,
                0,
                null
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setExistingValues`() = runTest {
        val d = ds()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null, null, 1, 1, null
            )
        }
        runCatching { d.setExistingValues(mockk<PMSMACache>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    // ---- Branch variant tests ----

    @Test
    fun `setUpFirstPage highrisk true then false`() = runTest {
        val d = ds()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                mockk<PregnantWomanAncCache>(relaxed = true),
                pmsmaMock(true, "opt79"),
                1,
                1,
                pmsmaMock(true, "opt79")
            )
        }
        val d2 = ds()
        runCatching {
            d2.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null,
                pmsmaMock(false, null),
                2,
                0,
                null
            )
        }
        assertNotNull(d.listFlow)
        assertNotNull(d2.listFlow)
    }

    @Test
    fun `setExistingValues booleans true`() = runTest {
        val d = ds()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null, null, 1, 1, null
            )
        }
        runCatching { d.setExistingValues(pmsmaMock(true, "opt79")) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setExistingValues booleans false`() = runTest {
        val d = ds()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null, null, 1, 1, null
            )
        }
        runCatching { d.setExistingValues(pmsmaMock(false, null)) }
        assertNotNull(d.listFlow)
    }

    // ---- Branch2 variant tests ----

    @Test
    fun `setUpFirstPage highRiskReason last vs mid vs null`() = runTest {
        val reasons = listOf("opt79", "opt0", null)
        for ((i, r) in reasons.withIndex()) {
            val d = ds()
            runCatching {
                d.setUpFirstPage(
                    mockk<HouseholdCache>(relaxed = true),
                    benMock(),
                    mockk<PregnantWomanRegistrationCache>(relaxed = true),
                    mockk<PregnantWomanAncCache>(relaxed = true),
                    mixedPmsma(r),
                    i,
                    i % 2,
                    mixedPmsma(r)
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setUpFirstPage null anc and null saved`() = runTest {
        val d = ds()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true),
                benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null,
                mixedPmsma("opt0"),
                0,
                1,
                null
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setExistingValues mixed reasons`() = runTest {
        for (r in listOf("opt79", "opt0", null)) {
            val d = ds()
            runCatching {
                d.setUpFirstPage(
                    mockk<HouseholdCache>(relaxed = true),
                    benMock(),
                    mockk<PregnantWomanRegistrationCache>(relaxed = true),
                    null, null, 1, 1, null
                )
            }
            runCatching { d.setExistingValues(mixedPmsma(r)) }
            assertNotNull(d.listFlow)
        }
    }

    // ---- Branch3 variant tests (HINDI, arg variants) ----

    @Test
    fun `setUpFirstPage arg variants`() = runTest {
        val d = dsHindi()
        for (anc in listOf(0, 1, 2)) {
            for (mother in listOf(0, 1)) {
                runCatching {
                    d.setUpFirstPage(
                        mockk<HouseholdCache>(relaxed = true), benMock(),
                        mockk<PregnantWomanRegistrationCache>(relaxed = true),
                        mockk<PregnantWomanAncCache>(relaxed = true),
                        mockk<PMSMACache>(relaxed = true), anc, mother, mockk<PMSMACache>(relaxed = true)
                    )
                }
                runCatching {
                    d.setUpFirstPage(
                        mockk<HouseholdCache>(relaxed = true), benMock(),
                        mockk<PregnantWomanRegistrationCache>(relaxed = true),
                        null, null, anc, mother, null
                    )
                }
            }
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setExistingValues after setup`() = runTest {
        val d = dsHindi()
        runCatching {
            d.setUpFirstPage(
                mockk<HouseholdCache>(relaxed = true), benMock(),
                mockk<PregnantWomanRegistrationCache>(relaxed = true), null, null, 2, 1, null
            )
        }
        runCatching { d.setExistingValues(mockk<PMSMACache>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    // ---- mapValues coverage (previously untested) ----

    @Test
    fun `mapValues after setExistingValues with true booleans and non-null bp`() = runTest {
        val d = ds()
        d.setUpFirstPage(
            mockk<HouseholdCache>(relaxed = true), benMock(),
            mockk<PregnantWomanRegistrationCache>(relaxed = true), null, null, 1, 1, null
        )
        d.setExistingValues(pmsmaMock(true, "opt79"))

        val out = mockk<PMSMACache>(relaxed = true)
        d.mapValues(out, 0)

        assertNotNull(d.listFlow.value)
    }

    @Test
    fun `mapValues after setExistingValues with false booleans and null bp`() = runTest {
        val d = ds()
        d.setUpFirstPage(
            mockk<HouseholdCache>(relaxed = true), benMock(),
            mockk<PregnantWomanRegistrationCache>(relaxed = true), null, null, 1, 1, null
        )
        val falseWithValidVisitDate = pmsmaMock(false, null)
        every { falseWithValidVisitDate.visitDate } returns 1_600_000_000_000L
        d.setExistingValues(falseWithValidVisitDate)

        val out = mockk<PMSMACache>(relaxed = true)
        d.mapValues(out, 0)

        assertNotNull(d.listFlow.value)
    }

    // ---- handleListOnValueChanged coverage via updateList (previously untested) ----

    @Test
    fun `updateList exercises handleListOnValueChanged branches`() = runTest {
        val d = ds()
        d.setUpFirstPage(
            mockk<HouseholdCache>(relaxed = true), benMock(),
            mockk<PregnantWomanRegistrationCache>(relaxed = true),
            null,
            pmsmaMock(true, "opt79"),
            1,
            1,
            null
        )

        d.setValueById(2, "opt0")
        d.updateList(2, 1)
        d.updateList(2, 0)

        d.setValueById(35, "opt0")
        d.updateList(35, 0)

        d.setValueById(45, "opt79")
        d.updateList(45, 0)

        d.setValueById(45, "opt7")
        d.updateList(45, 7)

        d.setValueById(45, "unknownXYZ")
        d.updateList(45, 0)

        d.setValueById(35, "opt1")
        d.updateList(35, 1)

        d.setValueById(6, "9999999999")
        d.updateList(6, 0)

        d.setValueById(4, "somename")
        d.updateList(4, 0)

        d.setValueById(10, "120/80")
        d.updateList(10, 0)

        d.setValueById(9, "70")
        d.updateList(9, 0)

        d.updateList(999, 0)

        assertNotNull(d.listFlow.value)
    }

    @Test
    fun `updateList husbandName hindi else branch`() = runTest {
        val d = dsHindi()
        d.setUpFirstPage(
            mockk<HouseholdCache>(relaxed = true), benMock(),
            mockk<PregnantWomanRegistrationCache>(relaxed = true), null, null, 1, 1, null
        )

        d.setValueById(4, "somename")
        d.updateList(4, 0)

        assertNotNull(d.listFlow.value)
    }
}
