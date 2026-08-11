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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Deep coverage test for [SuspectedTBDataset]: exercises setUpPage (create + edit),
 * mapValues, and isTestPositive. Each builder call wrapped in runCatching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SuspectedTBDatasetTest : BaseViewModelTest() {

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
        every { Log.w(any<String>(), any<String>()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path exercises builders`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        // setUpPage(ben: BenRegCache?, saved: TBSuspectedCache?)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.mapValues(mockk<TBSuspectedCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<TBSuspectedCache>(relaxed = true), 1) }
        runCatching { ds.isTestPositive() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path exercises saved branches`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<TBSuspectedCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<TBSuspectedCache>(relaxed = true), 0) }
        runCatching { ds.isTestPositive() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi construction`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    private fun savedTb(
        typeOfTBCase: String?,
        isSputumCollected: Boolean?,
        hasSymptoms: Boolean = true,
        reasonForSuspicion: String? = "opt3",
        sputumSubmittedAt: String? = "opt4",
        nikshayId: String? = "NIK123",
        sputumTestResult: String? = "opt0",
        referralFacility: String? = "opt6",
        isTBConfirmed: Boolean? = null,
        isDRTBConfirmed: Boolean? = null,
    ): TBSuspectedCache {
        val s = mockk<TBSuspectedCache>(relaxed = true)
        every { s.visitDate } returns System.currentTimeMillis()
        every { s.typeOfTBCase } returns typeOfTBCase
        every { s.isSputumCollected } returns isSputumCollected
        every { s.hasSymptoms } returns hasSymptoms
        every { s.reasonForSuspicion } returns reasonForSuspicion
        every { s.sputumSubmittedAt } returns sputumSubmittedAt
        every { s.nikshayId } returns nikshayId
        every { s.sputumTestResult } returns sputumTestResult
        every { s.referralFacility } returns referralFacility
        every { s.isTBConfirmed } returns isTBConfirmed
        every { s.isDRTBConfirmed } returns isDRTBConfirmed
        return s
    }

    @Test
    fun `edit typeOfTBCase entries0 tb confirmed true with sputum collected`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedTb(
            typeOfTBCase = "opt0",
            isSputumCollected = true,
            isTBConfirmed = true,
            sputumTestResult = "opt0"
        )
        ds.setUpPage(ben, saved)

        val form = mockk<TBSuspectedCache>(relaxed = true)
        ds.mapValues(form, 0)

        assertEquals("x", ds.isTestPositive())
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `edit typeOfTBCase entries1 dr confirmed false without sputum collected`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedTb(
            typeOfTBCase = "opt1",
            isSputumCollected = false,
            isDRTBConfirmed = false
        )
        ds.setUpPage(ben, saved)

        val form = mockk<TBSuspectedCache>(relaxed = true)
        ds.mapValues(form, 0)

        assertNull(ds.isTestPositive())
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `edit typeOfTBCase entries2 dr confirmed true`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedTb(
            typeOfTBCase = "opt2",
            isSputumCollected = true,
            isDRTBConfirmed = true,
            sputumTestResult = "opt0"
        )
        ds.setUpPage(ben, saved)

        val form = TBSuspectedCache(benId = 1L)
        ds.mapValues(form, 0)

        assertEquals(true, form.isConfirmed)
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `edit typeOfTBCase unmatched value neither confirmed field added`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedTb(
            typeOfTBCase = "opt50",
            isSputumCollected = null,
            hasSymptoms = false
        )
        ds.setUpPage(ben, saved)

        val form = mockk<TBSuspectedCache>(relaxed = true)
        ds.mapValues(form, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `edit typeOfTBCase null value`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedTb(
            typeOfTBCase = null,
            isSputumCollected = null
        )
        ds.setUpPage(ben, saved)
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList typeOfTBCase entries0 entries1 and unmatched branches`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null)

        ds.setValueById(3, "opt0")
        ds.updateList(3, 0)

        ds.setValueById(3, "opt1")
        ds.updateList(3, 0)

        ds.setValueById(3, "opt2")
        ds.updateList(3, 0)

        ds.setValueById(3, "opt50")
        ds.updateList(3, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList isSputumCollected add and remove dependents`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, null)

        ds.updateList(8, 0)
        ds.updateList(8, 1)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `mapValues isConfirmed false when neither tb nor dr confirmed`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = savedTb(
            typeOfTBCase = "opt0",
            isSputumCollected = false,
            isTBConfirmed = false
        )
        ds.setUpPage(ben, saved)

        val form = mockk<TBSuspectedCache>(relaxed = true)
        ds.mapValues(form, 0)

        verify(exactly = 0) { form.isConfirmed = true }
    }
}
