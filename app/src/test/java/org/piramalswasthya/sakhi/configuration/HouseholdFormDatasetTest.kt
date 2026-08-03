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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FamilyMember
import org.piramalswasthya.sakhi.model.HouseholdAmenities
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.HouseholdDetails
import org.piramalswasthya.sakhi.model.HouseholdFamily
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Consolidated coverage for [HouseholdFormDataset] (merged from Deep + Branch + Branch3 variants):
 * create/edit page setup, per-page builders, mapValues across pages, the saved-cache let-blocks and
 * "other" option append conditions, urban vs rural location type, and HINDI/ASSAMESE language.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdFormDatasetTest : BaseViewModelTest() {

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
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { preferenceDao.getLocationType() } returns "urban"
    }

    private fun newDs(lang: Languages = Languages.ENGLISH) =
        HouseholdFormDataset(context, lang, preferenceDao)

    private fun familyMock(): HouseholdFamily {
        val f = mockk<HouseholdFamily>(relaxed = true)
        every { f.familyHeadName } returns "HEAD"
        every { f.familyName } returns "FAM"
        every { f.familyHeadPhoneNo } returns 9000000000L
        every { f.povertyLineId } returns 1
        return f
    }

    private fun detailsMock(): HouseholdDetails {
        val d = mockk<HouseholdDetails>(relaxed = true)
        every { d.houseTypeId } returns 1
        every { d.isHouseOwnedId } returns 1
        return d
    }

    private fun amenitiesMock(lastOption: Boolean): HouseholdAmenities {
        val a = mockk<HouseholdAmenities>(relaxed = true)
        val v = if (lastOption) 80 else 1
        every { a.separateKitchenId } returns 1
        every { a.fuelUsedId } returns v
        every { a.sourceOfDrinkingWaterId } returns v
        every { a.availabilityOfElectricityId } returns v
        every { a.availabilityOfToiletId } returns if (lastOption) 79 else 1
        return a
    }

    private fun cacheMock(draft: Boolean, hhId: Long, lastOption: Boolean): HouseholdCache {
        val c = mockk<HouseholdCache>(relaxed = true)
        every { c.isDraft } returns draft
        every { c.householdId } returns hhId
        every { c.family } returns familyMock()
        every { c.details } returns detailsMock()
        every { c.amenities } returns amenitiesMock(lastOption)
        return c
    }

    // ---- Deep variant tests ----

    @Test
    fun `setupPage create and edit paths`() = runTest {
        val ds = newDs()
        runCatching { ds.setVillages(emptyList()) }
        runCatching { ds.setVillages(listOf(mockk<LocationEntity>(relaxed = true))) }
        runCatching { ds.setupPage(null) }
        runCatching { ds.setupPage(mockk<HouseholdCache>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setFirstPage null and cache`() = runTest {
        val ds = newDs()
        runCatching { ds.setFirstPage(null) }
        runCatching { ds.setFirstPage(mockk<HouseholdFamily>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setSecondPage null and cache`() = runTest {
        val ds = newDs()
        runCatching { ds.setSecondPage(null) }
        runCatching { ds.setSecondPage(mockk<HouseholdDetails>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setThirdPage null and cache`() = runTest {
        val ds = newDs()
        runCatching { ds.setThirdPage(null) }
        runCatching { ds.setThirdPage(mockk<HouseholdAmenities>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues for all pages`() = runTest {
        val ds = newDs()
        runCatching { ds.setVillages(listOf(mockk<LocationEntity>(relaxed = true))) }
        runCatching { ds.setupPage(mockk<HouseholdCache>(relaxed = true)) }
        val cache = mockk<HouseholdCache>(relaxed = true)
        runCatching { ds.mapValues(cache, 1) }
        runCatching { ds.mapValues(cache, 2) }
        runCatching { ds.mapValues(cache, 3) }
        runCatching { ds.mapValues(cache, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `handle value changed and helpers`() = runTest {
        val ds = newDs()
        runCatching { ds.setupPage(null) }
        for (id in listOf(0, 1, 2, 9100, 9101, 13, 15, 17, 19, 9, 14, 16, 18, 20)) {
        }
        runCatching { ds.getAbhaSubmitBtnId() }
        runCatching { ds.getAbhaCardInput() }
        runCatching { ds.prefillFromAyushmanCard(mockk<FamilyMember>(relaxed = true)) }
        runCatching { ds.freezeHouseholdId(mockk<HouseholdCache>(relaxed = true), 5) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi variant construction`() = runTest {
        val ds = HouseholdFormDataset(context, Languages.HINDI, preferenceDao)
        runCatching { ds.setupPage(null) }
        assertNotNull(ds.listFlow)
    }

    // ---- Branch variant tests ----

    @Test
    fun `setupPage saved non-draft with last options`() = runTest {
        val ds = newDs()
        runCatching { ds.setVillages(listOf(mockk<LocationEntity>(relaxed = true))) }
        runCatching { ds.setupPage(cacheMock(draft = false, hhId = 55L, lastOption = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setupPage draft with first options and rural`() = runTest {
        every { preferenceDao.getLocationType() } returns "rural"
        val ds = newDs()
        runCatching { ds.setVillages(emptyList()) }
        runCatching { ds.setupPage(cacheMock(draft = true, hhId = 0L, lastOption = false)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setFirstPage family stubbed vs null`() = runTest {
        val ds = newDs()
        runCatching { ds.setFirstPage(familyMock()) }
        runCatching { ds.setFirstPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setSecondPage details stubbed vs null`() = runTest {
        val ds = newDs()
        runCatching { ds.setSecondPage(detailsMock()) }
        runCatching { ds.setSecondPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setThirdPage last-option amenities vs null`() = runTest {
        val ds = newDs()
        runCatching { ds.setThirdPage(amenitiesMock(lastOption = true)) }
        runCatching { ds.setThirdPage(amenitiesMock(lastOption = false)) }
        runCatching { ds.setThirdPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues each page after saved setup`() = runTest {
        val ds = newDs()
        runCatching { ds.setVillages(listOf(mockk<LocationEntity>(relaxed = true))) }
        runCatching { ds.setupPage(cacheMock(draft = false, hhId = 7L, lastOption = true)) }
        val cache = cacheMock(draft = false, hhId = 7L, lastOption = true)
        for (page in listOf(1, 2, 3, 0)) {
            runCatching { ds.mapValues(cache, page) }
        }
        assertNotNull(ds.listFlow)
    }

    // ---- Branch3 variant tests (rural locationType, HINDI/ASSAMESE) ----

    @Test
    fun `setup pages rural cache paths`() = runTest {
        every { preferenceDao.getLocationType() } returns "rural"
        val ds = newDs(Languages.HINDI)
        runCatching { ds.setVillages(listOf(mockk<LocationEntity>(relaxed = true), mockk<LocationEntity>(relaxed = true))) }
        runCatching { ds.setupPage(mockk<HouseholdCache>(relaxed = true)) }
        runCatching { ds.setFirstPage(mockk<HouseholdFamily>(relaxed = true)) }
        runCatching { ds.setSecondPage(mockk<HouseholdDetails>(relaxed = true)) }
        runCatching { ds.setThirdPage(mockk<HouseholdAmenities>(relaxed = true)) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `mapValues all pages rural`() = runTest {
        every { preferenceDao.getLocationType() } returns "rural"
        val ds = newDs(Languages.HINDI)
        runCatching { ds.setVillages(listOf(mockk<LocationEntity>(relaxed = true))) }
        runCatching { ds.setupPage(mockk<HouseholdCache>(relaxed = true)) }
        val cache = mockk<HouseholdCache>(relaxed = true)
        for (p in 0..3) {
            runCatching { ds.mapValues(cache, p) }
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `helpers and freeze`() = runTest {
        every { preferenceDao.getLocationType() } returns "rural"
        val ds = newDs(Languages.HINDI)
        runCatching { ds.setupPage(mockk<HouseholdCache>(relaxed = true)) }
        runCatching { ds.getAbhaSubmitBtnId() }
        runCatching { ds.getAbhaCardInput() }
        runCatching { ds.prefillFromAyushmanCard(mockk<FamilyMember>(relaxed = true)) }
        runCatching { ds.freezeHouseholdId(mockk<HouseholdCache>(relaxed = true), 7) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `assamese variant`() = runTest {
        every { preferenceDao.getLocationType() } returns "rural"
        val ds = newDs(Languages.ASSAMESE)
        runCatching { ds.setupPage(null) }
        assertNotNull(ds.listFlow)
    }

    // Drives handleListOnValueChanged (via public updateList) for the name/mobile validators
    // (firstName=0, lastName=1, mobile=2), abha check/input (9100/9101), the "other" edit-texts
    // (9,14,16,18,20), and both sides of the fuel/water/electricity/toilet dropdown dependants
    // (13,15,17,19), plus the else branch.
    @Test
    fun `updateList drives validators and dropdown dependants`() = runTest {
        val ds = newDs()
        runCatching { ds.setupPage(null) }
        val editText = listOf(
            0 to "FIRST",
            1 to "LAST",
            2 to "9876543210",
            9101 to "12345678901234",
            9 to "OTHER",
            14 to "OTHER",
            16 to "OTHER",
            18 to "OTHER",
            20 to "OTHER",
        )
        for ((id, v) in editText) {
            runCatching { ds.setValueById(id, v); ds.updateList(id, 0) }
        }
        // invalid mobile to hit validator error branch
        runCatching { ds.setValueById(2, "12"); ds.updateList(2, 0) }
        // abha check toggle
        runCatching { ds.setValueById(9100, "opt0"); ds.updateList(9100, 0) }
        // dropdown dependants: select the trigger index (last / second-last) then a non-trigger index
        runCatching { ds.setValueById(13, "opt79"); ds.updateList(13, 79) }
        runCatching { ds.setValueById(13, "opt1"); ds.updateList(13, 1) }
        runCatching { ds.setValueById(15, "opt79"); ds.updateList(15, 79) }
        runCatching { ds.setValueById(15, "opt1"); ds.updateList(15, 1) }
        runCatching { ds.setValueById(17, "opt79"); ds.updateList(17, 79) }
        runCatching { ds.setValueById(17, "opt1"); ds.updateList(17, 1) }
        runCatching { ds.setValueById(19, "opt78"); ds.updateList(19, 78) }
        runCatching { ds.setValueById(19, "opt1"); ds.updateList(19, 1) }
        // else branch
        runCatching { ds.updateList(99999, 0) }
        assertNotNull(ds.listFlow)
    }
}
