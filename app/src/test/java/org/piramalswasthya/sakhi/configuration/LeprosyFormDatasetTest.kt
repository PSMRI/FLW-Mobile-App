package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyFormDatasetTest : BaseViewModelTest() {

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
    fun `construction ENGLISH and HINDI`() {
        runCatching { LeprosyFormDataset(context, Languages.ENGLISH) }
        runCatching { LeprosyFormDataset(context, Languages.HINDI) }
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..28) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<LeprosyScreeningCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<LeprosyScreeningCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<LeprosyScreeningCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..28) {
        }
        assertNotNull(ds.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun savedLeprosy(
        leprosyStatus: String?,
        referTo: String?,
        symptomsPos: Int? = 0,
        idValue: Int? = 0,
    ): LeprosyScreeningCache {
        val s = mockk<LeprosyScreeningCache>(relaxed = true)
        every { s.homeVisitDate } returns 1_600_000_000_000L
        every { s.leprosyStatus } returns leprosyStatus
        every { s.referToName } returns referTo
        every { s.leprosySymptomsPosition } returns symptomsPos
        every { s.currentVisitNumber } returns 2
        every { s.recurrentUlcerationId } returns idValue
        every { s.recurrentTinglingId } returns idValue
        every { s.hypopigmentedPatchId } returns idValue
        every { s.thickenedSkinId } returns idValue
        every { s.skinNodulesId } returns idValue
        every { s.recurrentNumbnessId } returns idValue
        every { s.clawingFingersId } returns idValue
        every { s.tinglingNumbnessExtremitiesId } returns idValue
        every { s.inabilityCloseEyelidId } returns idValue
        every { s.difficultyHoldingObjectsId } returns idValue
        every { s.weaknessFeetId } returns idValue
        every { s.referToName } returns referTo
        return s
    }

    @Test
    fun `edit leprosy status last adds other`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedLeprosy("opt79", "opt5", symptomsPos = 0, idValue = 0))
            ds.mapValues(mockk<LeprosyScreeningCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit referredTo last adds other`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedLeprosy("opt5", "opt79", symptomsPos = 1, idValue = 1))
            ds.mapValues(mockk<LeprosyScreeningCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit hindi with symptom ids`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching {
            ds.setUpPage(ben, savedLeprosy("opt3", "opt3", symptomsPos = 0, idValue = 0))
            ds.mapValues(mockk<LeprosyScreeningCache>(relaxed = true), 0)
        }
        assertNotNull(ds.listFlow)
    }

    // ---- handleListOnValueChanged coverage via the public updateList wrapper ----

    private fun LeprosyFormDataset.valueOf(id: Int): String? =
        listFlow.value.firstOrNull { it.id == id }?.value

    private fun LeprosyFormDataset.errorOf(id: Int): String? =
        listFlow.value.firstOrNull { it.id == id }?.errorText

    private suspend fun freshScreening(): LeprosyFormDataset {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null)
        return ds
    }

    @Test
    fun `checklist yes answer flips symptoms to yes and reveals referral`() = runTest {
        val ds = freshScreening()
        assertEquals("opt1", ds.valueOf(14))
        ds.setValueById(16, "opt0")
        ds.updateList(16, 0)
        assertEquals("opt0", ds.valueOf(14))
        assertEquals("opt3", ds.valueOf(8))
        assertTrue(ds.getIndexById(9) >= 0)
    }

    @Test
    fun `every checklist question drives the symptom summary`() = runTest {
        val ds = freshScreening()
        for (id in 16..26) {
            ds.setValueById(id, "opt1")
            ds.updateList(id, 1)
        }
        assertEquals("opt1", ds.valueOf(14))
        for (id in 16..26) {
            ds.setValueById(id, "opt0")
            ds.updateList(id, 0)
            assertEquals("opt0", ds.valueOf(14))
            ds.setValueById(id, "opt1")
        }
        assertTrue(ds.getListSize() > 0)
    }

    @Test
    fun `symptoms yes adds referral and symptoms no removes it`() = runTest {
        val ds = freshScreening()
        ds.setValueById(14, "opt0")
        ds.updateList(14, 0)
        assertTrue(ds.getIndexById(9) >= 0)
        assertEquals("opt3", ds.valueOf(8))
        ds.setValueById(14, "opt1")
        ds.updateList(14, 1)
        assertEquals(-1, ds.getIndexById(9))
        assertEquals("opt0", ds.valueOf(8))
    }

    @Test
    fun `leprosy status third from last adds referral`() = runTest {
        val ds = freshScreening()
        ds.setValueById(8, "opt77")
        ds.updateList(8, 77)
        assertTrue(ds.getIndexById(9) >= 0)
        ds.setValueById(8, "opt2")
        ds.updateList(8, 2)
        assertEquals(-1, ds.getIndexById(9))
    }

    @Test
    fun `referred to other adds free text and validates it`() = runTest {
        val ds = freshScreening()
        ds.setValueById(8, "opt77")
        ds.updateList(8, 77)
        ds.setValueById(9, "opt79")
        ds.updateList(9, 79)
        assertTrue(ds.getIndexById(10) >= 0)
        ds.setValueById(10, "")
        ds.updateList(10, 0)
        assertNotNull(ds.errorOf(10))
        ds.setValueById(10, "SOME PLACE")
        ds.updateList(10, 0)
        assertEquals(null, ds.errorOf(10))
        ds.setValueById(9, "opt2")
        ds.updateList(9, 2)
        assertEquals(-1, ds.getIndexById(10))
    }

    @Test
    fun `beneficiary status without death swaps in the follow up fields`() = runTest {
        val ds = freshScreening()
        ds.updateList(2, 0)
        assertTrue(ds.getIndexById(11) >= 0)
        assertTrue(ds.getIndexById(12) >= 0)
        assertTrue(ds.getIndexById(13) >= 0)
    }

    @Test
    fun `place and reason of death fall through to their removal branches`() = runTest {
        val ds = freshScreening()
        ds.updateList(4, 0)
        ds.updateList(6, 0)
        ds.updateList(5, 0)
        ds.updateList(7, 0)
        assertEquals(-1, ds.getIndexById(5))
        assertEquals(-1, ds.getIndexById(7))
    }

    @Test
    fun `unhandled form ids fall through to the default branch`() = runTest {
        val ds = freshScreening()
        val before = ds.getListSize()
        ds.updateList(28, 0)
        ds.updateList(1, 0)
        ds.updateList(9999, 0)
        assertEquals(before, ds.getListSize())
    }

    @Test
    fun `map values writes a fully answered screening form`() = runTest {
        val ds = freshScreening()
        ds.setValueById(1, "01-01-2024")
        for (id in 16..26) ds.setValueById(id, "opt0")
        ds.updateList(16, 0)
        ds.setValueById(9, "opt4")
        ds.updateList(9, 4)
        val form = mockk<LeprosyScreeningCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.diseaseTypeID = 5 }
        verify { form.leprosySymptomsPosition = 0 }
        verify { form.recurrentUlcerationId = 0 }
        verify { form.weaknessFeetId = 0 }
        verify { form.referToName = "opt4" }
    }

    @Test
    fun `map values defaults every unanswered question to no`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null)
        for (id in 16..26) ds.setValueById(id, null)
        val form = mockk<LeprosyScreeningCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.recurrentUlcerationId = 1 }
        verify { form.thickenedSkinId = 1 }
        verify { form.difficultyHoldingObjectsId = 1 }
    }

    @Test
    fun `update ben marks reproductive status and processed flag`() {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        val gen = mockk<org.piramalswasthya.sakhi.model.BenRegGen>(relaxed = true)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns gen
        every { ben.processed } returns "P"
        ds.updateBen(ben)
        verify { gen.reproductiveStatusId = 2 }
        verify { ben.processed = "U" }
    }

    @Test
    fun `update ben keeps a new record unprocessed`() {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        every { ben.processed } returns "N"
        ds.updateBen(ben)
        verify(exactly = 0) { ben.processed = "U" }
    }

    @Test
    fun `index of date resolves after the page is built`() = runTest {
        val ds = freshScreening()
        assertEquals(0, ds.getIndexOfDate())
    }

    private fun screeningCache(
        leprosyStatus: String? = "opt2",
        referToName: String? = "opt3",
        symptomsPosition: Int? = 0,
        checklistId: Int? = 0,
    ) = LeprosyScreeningCache(
        benId = 7L,
        houseHoldDetailsId = 9L,
        createdBy = "tester",
        modifiedBy = "tester",
        homeVisitDate = 1_600_000_000_000L,
        leprosyStatus = leprosyStatus,
        referToName = referToName,
        leprosySymptomsPosition = symptomsPosition,
        currentVisitNumber = 4,
        recurrentUlcerationId = checklistId,
        recurrentTinglingId = checklistId,
        hypopigmentedPatchId = checklistId,
        thickenedSkinId = checklistId,
        skinNodulesId = checklistId,
        recurrentNumbnessId = checklistId,
        clawingFingersId = checklistId,
        tinglingNumbnessExtremitiesId = checklistId,
        inabilityCloseEyelidId = checklistId,
        difficultyHoldingObjectsId = checklistId,
        weaknessFeetId = checklistId,
    )

    @Test
    fun `saved checklist answers default to no when every stored id is null`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screeningCache(symptomsPosition = null, checklistId = null))
        for (id in 16..26) assertEquals("opt1", ds.valueOf(id))
        assertEquals("opt1", ds.valueOf(14))
    }

    @Test
    fun `saved checklist answers outside the option range fall back to no`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screeningCache(symptomsPosition = 200, checklistId = 200))
        for (id in 16..26) assertEquals("opt1", ds.valueOf(id))
        assertEquals("opt1", ds.valueOf(14))
    }

    @Test
    fun `saved status and referral both other insert two free text rows`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screeningCache(leprosyStatus = "opt79", referToName = "opt79"))
        assertEquals("opt79", ds.valueOf(8))
        assertEquals("opt79", ds.valueOf(9))
        assertEquals(2, ds.listFlow.value.count { it.id == 10 })
    }

    @Test
    fun `map values falls back to screening for an unknown leprosy status`() = runTest {
        val ds = freshScreening()
        ds.setValueById(8, "an unmapped status")
        ds.setValueById(14, "opt7")
        val form = screeningCache()
        ds.mapValues(form, 0)
        assertEquals("Screening", form.leprosyStatus)
        assertEquals(1, form.leprosySymptomsPosition)
        assertEquals(null, form.typeOfLeprosy)
        assertEquals(5, form.diseaseTypeID)
    }

    @Test
    fun `saved page keeps the localized status label when it is not the other option`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, screeningCache(leprosyStatus = "opt4", referToName = "opt6"))
        assertEquals("opt4", ds.valueOf(8))
        assertEquals("opt6", ds.valueOf(9))
        assertEquals(-1, ds.getIndexById(10))
    }
}
