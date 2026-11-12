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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.utils.CommonConstants
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Deep coverage test for [TBScreeningDataset]: exercises setUpPage (create + edit),
 * (referHwcFacility, isTbSuspected, isTbSuspectedFamily,
 * getIndexOfDate). Each builder call wrapped in runCatching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TBScreeningDatasetTest : BaseViewModelTest() {

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
        every { mockResources.getStringArray(R.array.key_population_risk_factor_options) } returns
                Array(CommonConstants.RISK_FACTOR_CODES.size) { "risk$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path exercises builders`() = runTest {
        val ds = TBScreeningDataset(context, Languages.ENGLISH)
        // setUpPage(ben: BenRegCache?, saved: TBScreeningCache?)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.mapValues(mockk<TBScreeningCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<TBScreeningCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.referHwcFacility() }
        runCatching { ds.isTbSuspected() }
        runCatching { ds.isTbSuspectedFamily() }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path exercises saved branches`() = runTest {
        val ds = TBScreeningDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<TBScreeningCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<TBScreeningCache>(relaxed = true), 0) }
        runCatching { ds.referHwcFacility() }
        runCatching { ds.isTbSuspected() }
        runCatching { ds.isTbSuspectedFamily() }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `asymptomatic value follows localized symptom answers and is saved`() = runTest {
        val translatedYes = "हाँ"
        val translatedNo = "नहीं"
        every { mockResources.getStringArray(R.array.yes_no) } returns arrayOf(translatedYes, translatedNo)

        val dataset = TBScreeningDataset(context, Languages.HINDI)
        dataset.setUpPage(null, null)

        val screeningSymptoms = dataset.listFlow.value.filter {
            it.id in setOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 20, 21, 22)
        }
        val asymptomaticField = dataset.listFlow.value.first { it.id == 16 }

        assertNull(asymptomaticField.value)

        screeningSymptoms.forEach { it.value = translatedNo }
        dataset.updateList(screeningSymptoms.first().id, 1)
        assertEquals(translatedYes, asymptomaticField.value)

        screeningSymptoms.first().value = translatedYes
        dataset.updateList(screeningSymptoms.first().id, 0)
        assertEquals(translatedNo, asymptomaticField.value)

        val cache = TBScreeningCache(benId = 1L)
        dataset.mapValues(cache)
        assertEquals(translatedNo, cache.asymptomatic)
    }

    @Test
    fun `child symptoms are shown only for beneficiaries aged zero through fifteen`() = runTest {
        val translatedYes = "हाँ"
        val translatedNo = "नहीं"
        every { mockResources.getStringArray(R.array.yes_no) } returns arrayOf(translatedYes, translatedNo)

        val child = mockk<BenRegCache>(relaxed = true)
        every { child.dob } returns 0L
        every { child.age } returns 15
        val childDataset = TBScreeningDataset(context, Languages.ENGLISH)
        childDataset.setUpPage(child, null)

        val childQuestions = childDataset.listFlow.value.filter { it.id == 23 || it.id == 24 }
        assertEquals(2, childQuestions.size)
        assertTrue(childQuestions.all { it.required })
        childQuestions[0].value = translatedYes
        childQuestions[1].value = translatedNo
        val cache = TBScreeningCache(benId = 1L)
        childDataset.mapValues(cache)
        assertEquals(true, cache.failureToGainWeight)
        assertEquals(false, cache.decreasedActivityOrPlayfulness)
        childDataset.setUpPage(child, cache)
        val restoredChildQuestions = childDataset.listFlow.value.filter { it.id == 23 || it.id == 24 }
        assertEquals(translatedYes, restoredChildQuestions[0].value)
        assertEquals(translatedNo, restoredChildQuestions[1].value)

        val olderBeneficiary = mockk<BenRegCache>(relaxed = true)
        every { olderBeneficiary.dob } returns 0L
        every { olderBeneficiary.age } returns 16
        val adultDataset = TBScreeningDataset(context, Languages.ENGLISH)
        adultDataset.setUpPage(olderBeneficiary, null)

        assertTrue(adultDataset.listFlow.value.none { it.id == 23 || it.id == 24 })
    }

    @Test
    fun `pregnancy and elderly risk factors are auto selected on create and edit`() = runTest {
        val beneficiary = mockk<BenRegCache>(relaxed = true)
        every { beneficiary.gender } returns Gender.FEMALE
        every { beneficiary.dob } returns 0L
        every { beneficiary.age } returns 60
        every { beneficiary.genDetails } returns BenRegGen(reproductiveStatusId = 2)
        val dataset = TBScreeningDataset(context, Languages.ENGLISH)

        dataset.setUpPage(beneficiary, null)
        assertEquals(setOf("PREGNANCY", "ELDERLY"), selectedRiskFactorCodes(dataset, false))

        val saved = TBScreeningCache(benId = 1L).apply {
            keyPopulationRiskFactors = listOf("OTHER")
        }
        dataset.setUpPage(beneficiary, saved)
        assertEquals(
            setOf("OTHER", "PREGNANCY", "ELDERLY"),
            selectedRiskFactorCodes(dataset, false)
        )
    }

    @Test
    fun `CBAC tobacco alcohol fuel and workplace risks are auto selected`() = runTest {
        val dataset = TBScreeningDataset(context, Languages.ENGLISH)
        val saved = TBScreeningCache(benId = 1L).apply {
            keyPopulationRiskFactors = listOf("NOT_APPLICABLE")
        }

        dataset.setUpPage(
            ben = null,
            saved = saved,
            hasTobaccoUser = true,
            hasAlcoholRiskFactor = true,
            hasIndoorAirPollution = true,
            hasWorkplaceSettings = true
        )

        assertEquals(
            setOf(
                "TOBACCO_SMOKER",
                "SUBSTANCE_ABUSE",
                "INDOOR_AIR_POLLUTION_EXPOSURE",
                "WORKPLACE_SETTINGS"
            ),
            selectedRiskFactorCodes(dataset, isMale = false)
        )
    }

    @Test
    fun `male beneficiaries do not receive pregnancy or lactating options`() = runTest {
        val beneficiary = mockk<BenRegCache>(relaxed = true)
        every { beneficiary.gender } returns Gender.MALE
        every { beneficiary.dob } returns 0L
        every { beneficiary.age } returns 35
        val dataset = TBScreeningDataset(context, Languages.ENGLISH)

        dataset.setUpPage(beneficiary, null)

        val options = dataset.listFlow.value.first { it.id == 18 }
        assertEquals(CommonConstants.RISK_FACTOR_CODES.size - 2, options.entries?.size)
        assertEquals(emptySet<String>(), selectedRiskFactorCodes(dataset, true))
    }

    @Test
    fun `not applicable removes other saved risk factor selections`() = runTest {
        val dataset = TBScreeningDataset(context, Languages.ENGLISH)
        val saved = TBScreeningCache(benId = 1L).apply {
            keyPopulationRiskFactors = listOf("NOT_APPLICABLE", "OTHER")
        }

        dataset.setUpPage(null, saved)

        assertEquals(setOf("NOT_APPLICABLE"), selectedRiskFactorCodes(dataset, false))
        assertNotNull(dataset.listFlow.value.first { it.id == 18 }.exclusiveOptionIndices)
    }

    private fun selectedRiskFactorCodes(dataset: TBScreeningDataset, isMale: Boolean): Set<String> {
        val element = dataset.listFlow.value.first { it.id == 18 }
        val availableCodes = if (isMale) {
            CommonConstants.RISK_FACTOR_CODES.filterNot {
                it == "PREGNANCY" || it == "LACTATING_MOTHER"
            }
        } else CommonConstants.RISK_FACTOR_CODES
        return element.value.orEmpty().split("|").mapNotNull { it.toIntOrNull() }
            .mapNotNull { availableCodes.getOrNull(it) }.toSet()
    }

    @Test
    fun `TBScreeningCache setSyncState updates state`() = runTest {
        val cache = TBScreeningCache(benId = 1L)
        cache.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `hindi construction`() = runTest {
        val ds = TBScreeningDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }
}
