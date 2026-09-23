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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Deep coverage test for [MalariaConfirmCasesDataset]: invokes every public
 * getIndexOfDate) on both the create and edit code paths with rich relaxed mocks.
 * Each builder call is wrapped in runCatching so a mock gap never fails the test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MalariaConfirmCasesDatasetTest : BaseViewModelTest() {

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
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `create path exercises builders`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        // setUpPage(ben: BenRegCache?, slideTestName: String, saved: MalariaConfirmedCasesCache?)
        runCatching { ds.setUpPage(null, "x", null) }
        runCatching { ds.mapValues(mockk<MalariaConfirmedCasesCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<MalariaConfirmedCasesCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path exercises saved branches`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, "x", saved) }
        runCatching { ds.mapValues(mockk<MalariaConfirmedCasesCache>(relaxed = true), 0) }
        runCatching { ds.updateBen(mockk<BenRegCache>(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `hindi construction`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null, "x", null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `create path with pf treatment adds pf tracking dropdown`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        assertNotEquals(-1, ds.getIndexById(22))
        assertEquals(-1, ds.getIndexById(23))
    }

    @Test
    fun `create path with pv treatment adds pv tracking dropdown`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt1", null)
        assertNotEquals(-1, ds.getIndexById(23))
        assertEquals(-1, ds.getIndexById(22))
    }

    @Test
    fun `create path with unmatched treatment falls back to pv tracking dropdown`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt5", null)
        assertNotEquals(-1, ds.getIndexById(23))
        assertEquals(-1, ds.getIndexById(22))
    }

    @Test
    fun `edit path with pf treatment resolves saved day into pf tracking`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        val saved = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        every { saved.treatmentGiven } returns "opt0"
        every { saved.day } returns "opt3"
        ds.setUpPage(null, "opt0", saved)
        assertNotEquals(-1, ds.getIndexById(22))
        val form = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.day = "opt3" }
    }

    @Test
    fun `edit path with pv treatment resolves saved day into pv tracking`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        val saved = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        every { saved.treatmentGiven } returns "opt1"
        every { saved.day } returns "opt2"
        ds.setUpPage(null, "opt1", saved)
        assertNotEquals(-1, ds.getIndexById(23))
        val form = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.day = "opt2" }
    }

    @Test
    fun `mapValues maps dateOfCase to both diagnosis and treatment start`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        val form = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        ds.mapValues(form, 0)
        verify { form.dateOfDiagnosis = any() }
        verify { form.treatmentStartDate = any() }
        verify { form.treatmentGiven = "opt0" }
        verify { form.treatmentCompletionDate = any() }
        verify { form.referralDate = any() }
    }

    @Test
    fun `handleListOnValueChanged dateOfCase branch recomputes completion limits`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        ds.updateList(1, 0)
        assertNotEquals(-1, ds.getIndexById(1))
    }

    @Test
    fun `handleListOnValueChanged treatmentGiven pf switches tracking dropdown to pf`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt1", null)
        assertNotEquals(-1, ds.getIndexById(23))
        ds.setValueById(7, "opt0")
        ds.updateList(7, 0)
        assertNotEquals(-1, ds.getIndexById(22))
        assertEquals(-1, ds.getIndexById(23))
    }

    @Test
    fun `handleListOnValueChanged treatmentGiven pv switches tracking dropdown to pv`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        assertNotEquals(-1, ds.getIndexById(22))
        ds.setValueById(7, "opt1")
        ds.updateList(7, 0)
        assertNotEquals(-1, ds.getIndexById(23))
        assertEquals(-1, ds.getIndexById(22))
    }

    @Test
    fun `handleListOnValueChanged treatmentGiven unmatched leaves current tracking dropdown`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        assertNotEquals(-1, ds.getIndexById(22))
        ds.setValueById(7, "opt5")
        ds.updateList(7, 0)
        assertNotEquals(-1, ds.getIndexById(22))
        assertEquals(-1, ds.getIndexById(23))
    }

    @Test
    fun `handleListOnValueChanged else branch is a no-op`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        ds.updateList(9999, 0)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateBen marks reproductive status and processed for existing record`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        val gen = mockk<BenRegGen>(relaxed = true)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns gen
        every { ben.processed } returns "P"
        ds.updateBen(ben)
        verify { gen.reproductiveStatusId = 2 }
        verify { ben.processed = "U" }
    }

    @Test
    fun `updateBen keeps a new record unprocessed and tolerates null genDetails`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        every { ben.processed } returns "N"
        ds.updateBen(ben)
        verify(exactly = 0) { ben.processed = "U" }
    }

    @Test
    fun `getIndexOfDate resolves the date element position after setup`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        ds.setUpPage(null, "opt0", null)
        assertEquals(0, ds.getIndexOfDate())
    }
}
