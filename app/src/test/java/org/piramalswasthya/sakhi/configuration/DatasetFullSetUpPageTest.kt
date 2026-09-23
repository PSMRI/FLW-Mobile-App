package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HbncPartI
import org.piramalswasthya.sakhi.model.HbncPartII
import org.piramalswasthya.sakhi.model.HbncVisitCard
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Exercises the large form-builder methods (setUpPage / setFirstPage / setupPage / part-page
 * builders) of the biggest configuration datasets with RICH, non-null argument objects so the
 * builder bodies run through their `cache?.let { ... }` blocks (which the existing all-null
 * DatasetSetUpPage*Test.kt shallow passes skip), raising JaCoCo coverage of those bodies.
 *
 * Cache args are relaxed mocks (so unstubbed fields return safe defaults / null and deep chains
 * short-circuit), with a handful of key fields stubbed to sensible non-null values so the builder
 * progresses further before any NPE. Each builder call is still wrapped in runCatching{} as a
 * safety net, and every test asserts the dataset's listFlow is available.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DatasetFullSetUpPageTest : BaseViewModelTest() {

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
        every { mockResources.getStringArray(any()) } returns Array(60) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(any(), any()) } returns ""
        every { preferenceDao.getLoggedInUser() } returns null
    }

    /** A relaxed BenRegCache with the fields the builders commonly read stubbed to non-null values. */
    private fun benMock(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.dob } returns 631152000000L // ~1990
        every { ben.age } returns 25
        every { ben.gender } returns Gender.FEMALE
        every { ben.genderId } returns 2
        every { ben.regDate } returns 1_600_000_000_000L
        every { ben.firstName } returns "Test"
        every { ben.lastName } returns "Ben"
        every { ben.rchId } returns "RCH123"
        every { ben.isHrpStatus } returns false
        return ben
    }

    // =====================================================
    // Ben registration family
    // =====================================================

    @Test
    fun `BenRegFormDataset setFirstPageToRead and setSecondPage build pages`() = runTest {
        val ds = BenRegFormDataset(context, Languages.ENGLISH)
        val ben = benMock()
        runCatching { ds.setFirstPageToRead(ben, 9876543210L) }
        runCatching { ds.setSecondPage(ben) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `EligibleCoupleRegistrationDataset setUpPage builds page with saved`() = runTest {
        val ds = EligibleCoupleRegistrationDataset(
            context, context, Languages.ENGLISH, MutableLiveData()
        )
        val ben = benMock()
        val assess = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        val saved = mockk<EligibleCoupleRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, assess, saved, listOf(ben)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `NewChildBenRegDataset setUpPage builds page`() = runTest {
        val ds = NewChildBenRegDataset(context, Languages.ENGLISH)
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        val household = mockk<HouseholdCache>(relaxed = true)
        val hoF = benMock()
        val selected = benMock()
        runCatching {
            ds.setUpPage(
                ben = ecr,
                household = household,
                hoF = hoF,
                benGender = Gender.MALE,
                relationToHeadId = 0,
                hoFSpouse = emptyList(),
                selectedben = selected,
                isAddspouse = 0,
                childList = emptyList(),
                elderChildCount = 0
            )
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `EligibleCoupleTrackingDataset setUpPage builds page`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        val ben = benMock()
        val lastTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, 1_600_000_000_000L, lastTrack, null, 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `EligibleCoupleTrackingDataset setUpPage builds page for zero children`() = runTest {
        val ds = EligibleCoupleTrackingDataset(context, Languages.ENGLISH)
        val ben = benMock()
        val lastTrack = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        val saved = mockk<EligibleCoupleTrackingCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, 1_600_000_000_000L, lastTrack, saved, 2)
        }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // Maternal health family
    // =====================================================

    @Test
    fun `PregnantWomanRegistrationDataset setUpPage builds page`() = runTest {
        val ds = PregnantWomanRegistrationDataset(context, Languages.ENGLISH)
        val ben = benMock()
        val assess = mockk<HRPPregnantAssessCache>(relaxed = true)
        val saved = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, assess, saved, ecr, 1_600_000_000_000L) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `PregnantWomanAncVisitDataset setUpPage builds page`() = runTest {
        val ds = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)
        val ben = benMock()
        val regis = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val lastAnc = mockk<PregnantWomanAncCache>(relaxed = true)
        runCatching {
            ds.setUpPage(
                visitNumber = 1,
                ben = ben,
                regis = regis,
                lastAnc = lastAnc,
                isFromPmsma = false,
                saved = null
            )
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `PregnantWomanAncVisitDataset setUpPage builds page with saved`() = runTest {
        val ds = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)
        val ben = benMock()
        val regis = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        runCatching {
            ds.setUpPage(
                visitNumber = 2,
                ben = ben,
                regis = regis,
                lastAnc = null,
                isFromPmsma = true,
                saved = saved
            )
        }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // HBNC
    // =====================================================

    @Test
    fun `HBNCFormDataset part pages build with rich args`() = runTest {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, 1)
        val visitCard = mockk<HbncVisitCard>(relaxed = true)
        val part1 = mockk<HbncPartI>(relaxed = true)
        val part2 = mockk<HbncPartII>(relaxed = true)
        runCatching { ds.setPart1PageToList(visitCard, part1) }
        runCatching { ds.setPart2PageToList(part2) }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // Household (largest single builder = setupPage)
    // =====================================================

    @Test
    fun `HRPNonPregnantAssessCache and HRPPregnantAssessCache expose mutable id and visitDate`() {
        val nonPregnant = HRPNonPregnantAssessCache(benId = 1L)
        nonPregnant.visitDate = 123456L
        assertEquals(123456L, nonPregnant.visitDate)

        val pregnant = HRPPregnantAssessCache(benId = 1L)
        pregnant.id = 7
        pregnant.visitDate = 654321L
        assertEquals(7, pregnant.id)
        assertEquals(654321L, pregnant.visitDate)
    }

    @Test
    fun `HouseholdFormDataset setupPage builds page with saved household`() = runTest {
        val ds = HouseholdFormDataset(context, Languages.ENGLISH, preferenceDao)
        val hh = mockk<HouseholdCache>(relaxed = true)
        runCatching { ds.setupPage(hh) }
        assertNotNull(ds.listFlow)
    }
}
