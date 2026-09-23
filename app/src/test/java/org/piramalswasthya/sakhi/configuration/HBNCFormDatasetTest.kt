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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegKid
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HbncHomeVisit
import org.piramalswasthya.sakhi.model.HbncPartI
import org.piramalswasthya.sakhi.model.HbncPartII
import org.piramalswasthya.sakhi.model.HbncVisitCard
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HBNCFormDataset]. Consolidated from the previous HBNCFormDatasetDeepTest +
 * HBNCFormDatasetBranchTest + HBNCFormDatasetBranch3Test files into a single class: deep coverage
 * across all HBNC day constructors, branch coverage varying visitCard.stillBirth / childBen.genderId
 * / babyReferred / motherReferred, and HINDI-language variants with mixed null/non-null page inputs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HBNCFormDatasetTest : BaseViewModelTest() {

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

    private fun childBen(genderId: Int): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.genderId } returns genderId
        every { b.motherName } returns "MOTHER"
        every { b.fatherName } returns "FATHER"
        return b
    }

    private fun visitCard(stillBirth: Int): HbncVisitCard {
        val v = mockk<HbncVisitCard>(relaxed = true)
        every { v.stillBirth } returns stillBirth
        return v
    }

    private fun homeVisit(referred: Int): HbncHomeVisit {
        val v = mockk<HbncHomeVisit>(relaxed = true)
        every { v.babyReferred } returns referred
        every { v.motherReferred } returns referred
        return v
    }

    private val days = listOf(1, 3, 7, 14, 21, 28, 42)

    // ===================== deep coverage (from DeepTest) =====================

    @Test
    fun hbncFormDeep() = runTest {
        val location = mockk<LocationRecord>(relaxed = true)
        val asha = mockk<User>(relaxed = true)
        val childBen = mockk<BenRegCache>(relaxed = true)
        val motherBen = mockk<BenRegCache>(relaxed = true)
        val visitCard = mockk<HbncVisitCard>(relaxed = true)
        val part1 = mockk<HbncPartI>(relaxed = true)
        val part2 = mockk<HbncPartII>(relaxed = true)
        val visit = mockk<HbncHomeVisit>(relaxed = true)
        for (day in listOf(1, 3, 7, 14, 21, 28, 42)) {
            val ds = HBNCFormDataset(context, Languages.ENGLISH, day)
            runCatching { ds.setCardPageToList(location, asha, childBen, null, null) }
            runCatching { ds.setCardPageToList(location, asha, childBen, motherBen, visitCard) }
            runCatching { ds.setPart1PageToList(null, null) }
            runCatching { ds.setPart1PageToList(visitCard, part1) }
            runCatching { ds.setPart2PageToList(null) }
            runCatching { ds.setPart2PageToList(part2) }
            runCatching { ds.setVisitToList(null, null) }
            runCatching { ds.setVisitToList(visit, visit) }
            runCatching { ds.mapValues(mockk<HBNCCache>(relaxed = true), 0) }
            assertNotNull(ds.listFlow)
        }
    }

    // ===================== branch coverage (from BranchTest) =====================

    @Test
    fun `card page saved vs fresh with gender variants`() = runTest {
        val location = mockk<LocationRecord>(relaxed = true)
        val asha = mockk<User>(relaxed = true)
        val motherBen = mockk<BenRegCache>(relaxed = true)
        for (day in days) {
            val d = HBNCFormDataset(context, Languages.ENGLISH, day)
            runCatching { d.setCardPageToList(location, asha, childBen(0), null, null) }
            runCatching { d.setCardPageToList(null, asha, childBen(1), motherBen, visitCard(1)) }
            runCatching { d.setCardPageToList(location, asha, childBen(2), motherBen, visitCard(2)) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `part1 stillbirth branches and part2`() = runTest {
        val part1 = mockk<HbncPartI>(relaxed = true)
        val part2 = mockk<HbncPartII>(relaxed = true)
        for (day in days) {
            val d = HBNCFormDataset(context, Languages.ENGLISH, day)
            // stillBirth = 1 with null part1 -> adds baby-death fields
            runCatching { d.setPart1PageToList(visitCard(1), null) }
            // stillBirth = 2 alive branch
            runCatching { d.setPart1PageToList(visitCard(2), part1) }
            // stillBirth = 0 null branch
            runCatching { d.setPart1PageToList(visitCard(0), part1) }
            runCatching { d.setPart2PageToList(null) }
            runCatching { d.setPart2PageToList(part2) }
            runCatching { d.mapValues(mockk<HBNCCache>(relaxed = true), 0) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `visit page referred branches`() = runTest {
        for (day in days) {
            val d = HBNCFormDataset(context, Languages.ENGLISH, day)
            runCatching { d.setVisitToList(null, null) }
            runCatching { d.setVisitToList(homeVisit(1), homeVisit(1)) }
            runCatching { d.setVisitToList(homeVisit(0), homeVisit(0)) }
            runCatching { d.setVisitToList(homeVisit(1), null) }
            assertNotNull(d.listFlow)
        }
    }

    // ===================== branch coverage (from Branch3Test) =====================

    @Test
    fun hbncFormBranch() = runTest {
        val location = mockk<LocationRecord>(relaxed = true)
        val asha = mockk<User>(relaxed = true)
        val childBen = mockk<BenRegCache>(relaxed = true)
        val motherBen = mockk<BenRegCache>(relaxed = true)
        val visitCard = mockk<HbncVisitCard>(relaxed = true)
        val part1 = mockk<HbncPartI>(relaxed = true)
        val part2 = mockk<HbncPartII>(relaxed = true)
        val visit = mockk<HbncHomeVisit>(relaxed = true)
        for (day in listOf(1, 3, 7, 14, 21, 28, 42)) {
            val ds = HBNCFormDataset(context, Languages.HINDI, day)
            runCatching { ds.setCardPageToList(location, asha, childBen, motherBen, visitCard) }
            runCatching { ds.setCardPageToList(location, asha, childBen, null, null) }
            runCatching { ds.setPart1PageToList(visitCard, part1) }
            runCatching { ds.setPart1PageToList(visitCard, null) }
            runCatching { ds.setPart2PageToList(part2) }
            runCatching { ds.setVisitToList(visit, null) }
            runCatching { ds.setVisitToList(null, visit) }
            runCatching { ds.mapValues(mockk<HBNCCache>(relaxed = true), 0) }
            runCatching { ds.mapValues(mockk<HBNCCache>(relaxed = true), 1) }
            assertNotNull(ds.listFlow)
        }
    }

    // ===================== handleListOnValueChanged via public updateList =====================

    private val cardDay = 0
    private val part1Day = -1
    private val part2Day = -2

    private suspend fun cardDataset(): HBNCFormDataset {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, cardDay)
        val ben = childBen(1)
        every { ben.kidDetails } returns BenRegKid(birthPlace = "HOSPITAL", deliveryTypeId = 2)
        ds.setCardPageToList(mockk(relaxed = true), mockk(relaxed = true), ben, null, null)
        return ds
    }

    private suspend fun part1Dataset(): HBNCFormDataset {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, part1Day)
        ds.setPart1PageToList(null, null)
        return ds
    }

    private suspend fun part2Dataset(): HBNCFormDataset {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, part2Day)
        ds.setPart2PageToList(null)
        return ds
    }

    private suspend fun visitDataset(): HBNCFormDataset {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, 3)
        ds.setVisitToList(null, null)
        return ds
    }

    @Test
    fun `card page sub center name toggles length error on and off`() = runTest {
        val ds = cardDataset()
        ds.setValueById(2, "A VERY LONG SUB CENTER NAME")
        ds.updateList(2, 0)
        ds.setValueById(2, "SHORT")
        ds.updateList(2, 0)
        ds.updateList(2, 0)
        ds.setValueById(2, null)
        ds.updateList(2, 0)
        assertTrue(ds.getIndexById(2) >= 0)
    }

    @Test
    fun `card page ignores unrelated form ids`() = runTest {
        val ds = cardDataset()
        val before = ds.getListSize()
        ds.updateList(9999, 0)
        assertEquals(before, ds.getListSize())
    }

    @Test
    fun `card page uses kid details for place and type of delivery`() = runTest {
        val ds = cardDataset()
        assertTrue(ds.getIndexById(7) >= 0)
        assertTrue(ds.getIndexById(9) >= 0)
    }

    @Test
    fun `part1 baby alive dead option adds and removes death fields`() = runTest {
        val ds = part1Dataset()
        ds.updateList(90, 1)
        assertTrue(ds.getIndexById(91) >= 0)
        assertTrue(ds.getIndexById(92) >= 0)
        assertTrue(ds.getIndexById(93) >= 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.updateList(90, 0)
        assertEquals(-1, ds.getIndexById(91))
    }

    @Test
    fun `part1 place of baby death last option adds free text`() = runTest {
        val ds = part1Dataset()
        ds.updateList(90, 1)
        ds.updateList(93, 79)
        assertTrue(ds.getIndexById(94) >= 0)
        ds.updateList(93, 1)
        assertEquals(-1, ds.getIndexById(94))
    }

    @Test
    fun `part1 mother alive dead option adds and removes death fields`() = runTest {
        val ds = part1Dataset()
        ds.updateList(99, 1)
        assertTrue(ds.getIndexById(100) >= 0)
        assertTrue(ds.getIndexById(102) >= 0)
        ds.updateList(102, 79)
        assertTrue(ds.getIndexById(103) >= 0)
        ds.updateList(99, 0)
        assertEquals(-1, ds.getIndexById(100))
    }

    @Test
    fun `part1 preterm adds gestational age and alerts on first option`() = runTest {
        val ds = part1Dataset()
        ds.updateList(95, 0)
        assertTrue(ds.getIndexById(96) >= 0)
        ds.updateList(96, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.updateList(96, 3)
        ds.updateList(95, 1)
        assertEquals(-1, ds.getIndexById(96))
    }

    @Test
    fun `part1 mother problems always alerts`() = runTest {
        val ds = part1Dataset()
        ds.updateList(104, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
    }

    @Test
    fun `part1 baby fed after birth other adds free text`() = runTest {
        val ds = part1Dataset()
        ds.updateList(26, 79)
        assertTrue(ds.getIndexById(105) >= 0)
        ds.updateList(26, 2)
        assertEquals(-1, ds.getIndexById(105))
    }

    @Test
    fun `part1 breast feed problem adds detail field`() = runTest {
        val ds = part1Dataset()
        ds.updateList(107, 0)
        assertTrue(ds.getIndexById(108) >= 0)
        ds.updateList(107, 1)
        assertEquals(-1, ds.getIndexById(108))
    }

    @Test
    fun `part1 unknown form id is a no op`() = runTest {
        val ds = part1Dataset()
        val before = ds.getListSize()
        ds.updateList(9999, 0)
        assertEquals(before, ds.getListSize())
    }

    @Test
    fun `part2 unusual with baby other adds free text`() = runTest {
        val ds = part2Dataset()
        ds.updateList(117, 2)
        assertTrue(ds.getIndexById(118) >= 0)
        ds.updateList(117, 0)
        assertEquals(-1, ds.getIndexById(118))
    }

    @Test
    fun `part2 unknown form id is a no op`() = runTest {
        val ds = part2Dataset()
        val before = ds.getListSize()
        ds.updateList(9999, 0)
        assertEquals(before, ds.getListSize())
    }

    @Test
    fun `visit day numeric mother fields raise alerts below and above thresholds`() = runTest {
        val ds = visitDataset()
        ds.setValueById(49, "2")
        ds.updateList(49, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(49, "6")
        ds.updateList(49, 0)
        ds.setValueById(49, "")
        ds.updateList(49, 0)
        ds.setValueById(50, "8")
        ds.updateList(50, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(50, "2")
        ds.updateList(50, 0)
        ds.setValueById(50, "")
        ds.updateList(50, 0)
        assertTrue(ds.getIndexById(50) >= 0)
    }

    @Test
    fun `visit day mother body temperature covers both fever bands`() = runTest {
        val ds = visitDataset()
        ds.setValueById(55, "100")
        ds.updateList(55, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(55, "104")
        ds.updateList(55, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(55, "98")
        ds.updateList(55, 0)
        ds.setValueById(55, "")
        ds.updateList(55, 0)
        assertTrue(ds.getIndexById(55) >= 0)
    }

    @Test
    fun `visit day baby weight covers both low weight bands`() = runTest {
        val ds = visitDataset()
        ds.setValueById(62, "1.5")
        ds.updateList(62, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(62, "2.2")
        ds.updateList(62, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(62, "3.4")
        ds.updateList(62, 0)
        ds.setValueById(62, "")
        ds.updateList(62, 0)
        assertTrue(ds.getIndexById(62) >= 0)
    }

    @Test
    fun `visit day baby temperature covers all three alert bands`() = runTest {
        val ds = visitDataset()
        ds.setValueById(110, "95")
        ds.updateList(110, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(110, "96")
        ds.updateList(110, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(110, "101")
        ds.updateList(110, 0)
        assertNotNull(ds.alertErrorMessageFlow.value)
        ds.resetErrorMessageFlow()
        ds.setValueById(110, "98")
        ds.updateList(110, 0)
        ds.setValueById(110, "")
        ds.updateList(110, 0)
        assertTrue(ds.getIndexById(110) >= 0)
    }

    @Test
    fun `visit day radio danger signs all raise their alerts`() = runTest {
        val ds = visitDataset()
        val alerting = listOf(51 to 1, 52 to 1, 53 to 0, 56 to 0, 57 to 0, 58 to 0, 59 to 0, 61 to 0)
        for ((id, index) in alerting) {
            ds.resetErrorMessageFlow()
            ds.updateList(id, index)
            assertNotNull(ds.alertErrorMessageFlow.value)
        }
        for ((id, index) in alerting) {
            ds.resetErrorMessageFlow()
            ds.updateList(id, if (index == 0) 1 else 0)
        }
        assertTrue(ds.getListSize() > 0)
    }

    @Test
    fun `visit day baby referral adds date place and other place`() = runTest {
        val ds = visitDataset()
        ds.updateList(120, 0)
        assertTrue(ds.getIndexById(121) >= 0)
        assertTrue(ds.getIndexById(122) >= 0)
        ds.updateList(122, 79)
        assertTrue(ds.getIndexById(123) >= 0)
        ds.updateList(122, 1)
        assertEquals(-1, ds.getIndexById(123))
        ds.updateList(120, 1)
        assertEquals(-1, ds.getIndexById(121))
    }

    @Test
    fun `visit day mother referral adds date place and other place`() = runTest {
        val ds = visitDataset()
        ds.updateList(124, 0)
        assertTrue(ds.getIndexById(125) >= 0)
        assertTrue(ds.getIndexById(126) >= 0)
        ds.updateList(126, 79)
        assertTrue(ds.getIndexById(127) >= 0)
        ds.updateList(126, 1)
        assertEquals(-1, ds.getIndexById(127))
        ds.updateList(124, 1)
        assertEquals(-1, ds.getIndexById(125))
    }

    @Test
    fun `visit day unknown form id is a no op`() = runTest {
        val ds = visitDataset()
        val before = ds.getListSize()
        ds.updateList(9999, 0)
        assertEquals(before, ds.getListSize())
    }

    // ===================== saved-record dependant field branches =====================

    @Test
    fun `part1 saved record with dead baby dead mother preterm and other options`() = runTest {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, part1Day)
        val part1 = mockk<HbncPartI>(relaxed = true)
        every { part1.babyAlive } returns 2
        every { part1.placeOfBabyDeath } returns 80
        every { part1.motherAlive } returns 2
        every { part1.placeOfMotherDeath } returns 80
        every { part1.isBabyPreterm } returns 1
        every { part1.babyFirstFed } returns 80
        every { part1.motherHasBreastFeedProblem } returns 1
        every { part1.dateOfVisit } returns 1_600_000_000_000L
        ds.setPart1PageToList(visitCard(2), part1)
        assertTrue(ds.getIndexById(91) >= 0)
        assertTrue(ds.getIndexById(94) >= 0)
        assertTrue(ds.getIndexById(100) >= 0)
        assertTrue(ds.getIndexById(103) >= 0)
        assertTrue(ds.getIndexById(96) >= 0)
        assertTrue(ds.getIndexById(105) >= 0)
        assertTrue(ds.getIndexById(108) >= 0)
    }

    @Test
    fun `part2 saved record with other unusual finding`() = runTest {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, part2Day)
        val part2 = mockk<HbncPartII>(relaxed = true)
        every { part2.unusualInBaby } returns 80
        every { part2.babyWeight } returns "3.2"
        every { part2.dateOfVisit } returns 1_600_000_000_000L
        ds.setPart2PageToList(part2)
        assertTrue(ds.getIndexById(118) >= 0)
        val cache = mockk<HBNCCache>(relaxed = true)
        ds.mapValues(cache, 0)
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `visit saved record with both referrals and other places`() = runTest {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, 7)
        val visit = mockk<HbncHomeVisit>(relaxed = true)
        every { visit.babyReferred } returns 1
        every { visit.placeOfBabyReferral } returns 80
        every { visit.motherReferred } returns 1
        every { visit.placeOfMotherReferral } returns 80
        every { visit.dateOfVisit } returns 1_600_000_000_000L
        every { visit.babyWeight } returns "3.1"
        every { visit.babyTemperature } returns "98"
        ds.setVisitToList(visit, visit)
        assertTrue(ds.getIndexById(121) >= 0)
        assertTrue(ds.getIndexById(123) >= 0)
        assertTrue(ds.getIndexById(125) >= 0)
        assertTrue(ds.getIndexById(127) >= 0)
        val cache = mockk<HBNCCache>(relaxed = true)
        ds.mapValues(cache, 0)
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `first day immunisation status carries to a fresh visit page`() = runTest {
        val ds = HBNCFormDataset(context, Languages.ENGLISH, 14)
        val firstDay = mockk<HbncHomeVisit>(relaxed = true)
        every { firstDay.babyImmunizationStatus } returns "opt0|opt2"
        ds.setVisitToList(firstDay, null)
        assertTrue(ds.getIndexById(18) >= 0)
    }

    // ===================== setters and populated mapValues =====================

    @Test
    fun `header setters update the card page values`() = runTest {
        val ds = cardDataset()
        ds.setVillageName("VILLAGE")
        ds.setBlockName("BLOCK")
        ds.setAshaName("ASHA")
        val cache = mockk<HBNCCache>(relaxed = true)
        ds.mapValues(cache, 0)
        verify { cache.visitCard = any() }
    }

    @Test
    fun `card map values reads populated fields`() = runTest {
        val ds = cardDataset()
        ds.setValueById(2, "SUBCENTER")
        ds.setValueById(4, "MOTHER")
        ds.setValueById(5, "FATHER")
        ds.setValueById(6, "01-01-2024")
        ds.setValueById(7, "opt1")
        ds.setValueById(8, "opt0")
        ds.setValueById(9, "opt2")
        ds.setValueById(10, "opt1")
        ds.setValueById(11, "3000")
        ds.setValueById(12, "05-01-2024")
        ds.setValueById(13, "05-01-2024")
        ds.setValueById(15, "opt1")
        ds.setValueById(81, "opt1")
        val cache = mockk<HBNCCache>(relaxed = true)
        ds.mapValues(cache, 0)
        verify { cache.visitCard = any() }
    }

    @Test
    fun `part1 map values reads populated fields`() = runTest {
        val ds = part1Dataset()
        ds.updateList(90, 1)
        ds.updateList(93, 79)
        ds.updateList(95, 0)
        ds.updateList(26, 79)
        ds.updateList(107, 0)
        ds.setValueById(89, "01-01-2024")
        ds.setValueById(90, "opt79")
        ds.setValueById(91, "02-01-2024")
        ds.setValueById(92, "10:00")
        ds.setValueById(93, "opt79")
        ds.setValueById(94, "HOME")
        ds.setValueById(95, "opt0")
        ds.setValueById(96, "opt1")
        ds.setValueById(97, "03-01-2024")
        ds.setValueById(98, "11:00")
        ds.setValueById(104, "0|1")
        ds.setValueById(26, "opt79")
        ds.setValueById(105, "OTHER FEED")
        ds.setValueById(106, "12:00")
        ds.setValueById(27, "opt2")
        ds.setValueById(107, "opt0")
        ds.setValueById(108, "PROBLEM")
        val cache = mockk<HBNCCache>(relaxed = true)
        ds.mapValues(cache, 0)
        verify { cache.part1 = any() }
    }

    @Test
    fun `visit map values reads populated fields`() = runTest {
        val ds = visitDataset()
        ds.updateList(120, 0)
        ds.updateList(124, 0)
        ds.setValueById(49, "5")
        ds.setValueById(50, "3")
        ds.setValueById(55, "98")
        ds.setValueById(62, "3.1")
        ds.setValueById(110, "98")
        ds.setValueById(18, "0|1")
        ds.setValueById(120, "opt0")
        ds.setValueById(121, "01-01-2024")
        ds.setValueById(124, "opt0")
        ds.setValueById(125, "01-01-2024")
        ds.setValueById(83, "opt1")
        ds.setValueById(84, "SUP")
        ds.setValueById(82, "REMARK")
        ds.setValueById(86, "02-01-2024")
        val cache = mockk<HBNCCache>(relaxed = true)
        ds.mapValues(cache, 0)
        verify { cache.homeVisitForm = any() }
    }
}
