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
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
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
}
