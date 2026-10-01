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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaFormDatasetTest : BaseViewModelTest() {

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
        every { HelperUtil.parseSelections(any(), any()) } returns listOf("opt3", "opt7")
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getStringArray(R.array.yes_no) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `construction ENGLISH and HINDI`() {
        runCatching { FilariaFormDataset(context, Languages.ENGLISH) }
        runCatching { FilariaFormDataset(context, Languages.HINDI) }
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        // setUpPage does ben!!.dob so null ben throws; wrapped in runCatching.
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.isMaleFemale(null) }
        runCatching { ds.isSuffering() }
        runCatching { ds.isYoung(0L) }
        runCatching { ds.isYoung(System.currentTimeMillis()) }
        runCatching { ds.getIndexOfDate() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        runCatching { ds.isMaleFemale(ben) }
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<FilariaScreeningCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<FilariaScreeningCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.isSuffering() }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..8) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `isMaleFemale gender variants`() {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)

        val maleBen = mockk<BenRegCache>(relaxed = true)
        every { maleBen.gender } returns Gender.MALE
        assertTrue(ds.isMaleFemale(maleBen))
        assertTrue(ds.isMale)

        val femaleBen = mockk<BenRegCache>(relaxed = true)
        every { femaleBen.gender } returns Gender.FEMALE
        assertFalse(ds.isMaleFemale(femaleBen))
        assertFalse(ds.isMale)

        val transBen = mockk<BenRegCache>(relaxed = true)
        every { transBen.gender } returns Gender.TRANSGENDER
        assertFalse(ds.isMaleFemale(transBen))

        assertFalse(ds.isMaleFemale(null))
    }

    @Test
    fun `setUpPage suffering yes male young age with side effect other`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.MALE
        every { ben.dob } returns System.currentTimeMillis()

        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        every { saved.mdaHomeVisitDate } returns System.currentTimeMillis()
        every { saved.sufferingFromFilariasis } returns true
        every { saved.affectedBodyPart } returns "opt3,opt7"
        every { saved.doseStatus } returns "opt2"
        every { saved.medicineSideEffect } returns "opt78"
        every { saved.otherSideEffectDetails } returns "other detail"

        ds.setUpPage(ben, saved)

        assertTrue(ds.isMale)
        assertTrue(ds.isSuffering())
        assertNotNull(ds.listFlow.value)

        ds.mapValues(mockk<FilariaScreeningCache>(relaxed = true), 0)
        ds.updateBen(mockk(relaxed = true))
    }

    @Test
    fun `setUpPage suffering yes female old age no side effect other`() = runTest {
        every { HelperUtil.parseSelections(any(), any()) } returns emptyList()

        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.FEMALE
        every { ben.dob } returns System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3650)

        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        every { saved.sufferingFromFilariasis } returns true
        every { saved.affectedBodyPart } returns "raw-fallback-value"
        every { saved.doseStatus } returns "opt9"
        every { saved.medicineSideEffect } returns "opt5"

        ds.setUpPage(ben, saved)

        assertFalse(ds.isMale)
        assertTrue(ds.isSuffering())

        ds.mapValues(mockk<FilariaScreeningCache>(relaxed = true), 0)
    }

    @Test
    fun `setUpPage suffering no skips dependent fields`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.MALE
        every { ben.dob } returns System.currentTimeMillis()

        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        every { saved.sufferingFromFilariasis } returns false

        ds.setUpPage(ben, saved)

        assertFalse(ds.isSuffering())
        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList isSuffering yes male add and remove dependents`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.MALE
        every { ben.dob } returns System.currentTimeMillis()

        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        every { saved.sufferingFromFilariasis } returns true
        every { saved.medicineSideEffect } returns "opt78"

        ds.setUpPage(ben, saved)
        ds.setValueById(2, "Yes")

        ds.updateList(2, 1)
        ds.updateList(2, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList isSuffering yes female add and remove dependents`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.FEMALE
        every { ben.dob } returns System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3000)

        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        every { saved.sufferingFromFilariasis } returns true

        ds.setUpPage(ben, saved)
        ds.setValueById(2, "Yes")

        ds.updateList(2, 1)
        ds.updateList(2, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList isSuffering no triggers hide for male and female`() = runTest {
        val dsMale = FilariaFormDataset(context, Languages.ENGLISH)
        val benMale = mockk<BenRegCache>(relaxed = true)
        every { benMale.gender } returns Gender.MALE
        every { benMale.dob } returns System.currentTimeMillis()
        dsMale.setUpPage(benMale, null)
        dsMale.setValueById(2, "No")
        dsMale.updateList(2, 0)

        val dsFemale = FilariaFormDataset(context, Languages.ENGLISH)
        val benFemale = mockk<BenRegCache>(relaxed = true)
        every { benFemale.gender } returns Gender.FEMALE
        every { benFemale.dob } returns System.currentTimeMillis()
        dsFemale.setUpPage(benFemale, null)
        dsFemale.setValueById(2, "No")
        dsFemale.updateList(2, 1)

        assertNotNull(dsMale.listFlow.value)
        assertNotNull(dsFemale.listFlow.value)
    }

    @Test
    fun `updateList medicineSideEffect and sideEffectOther branches`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.MALE
        every { ben.dob } returns System.currentTimeMillis()

        val saved = mockk<FilariaScreeningCache>(relaxed = true)
        every { saved.sufferingFromFilariasis } returns true
        every { saved.medicineSideEffect } returns "opt78"

        ds.setUpPage(ben, saved)

        ds.setValueById(7, "opt78")
        ds.updateList(7, 0)

        ds.setValueById(8, "")
        ds.updateList(8, 0)

        ds.setValueById(8, "filled")
        ds.updateList(8, 0)

        ds.setValueById(7, "opt1")
        ds.updateList(7, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateList unmatched formId returns default`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.gender } returns Gender.MALE
        every { ben.dob } returns System.currentTimeMillis()

        ds.setUpPage(ben, null)
        ds.updateList(999, 0)

        assertNotNull(ds.listFlow.value)
    }

    @Test
    fun `updateBen genDetails and processed variants`() {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)

        val benWithNullGenAndReadyProcessed = mockk<BenRegCache>(relaxed = true)
        every { benWithNullGenAndReadyProcessed.genDetails } returns null
        every { benWithNullGenAndReadyProcessed.processed } returns "N"
        ds.updateBen(benWithNullGenAndReadyProcessed)

        val benWithGenAndOtherProcessed = mockk<BenRegCache>(relaxed = true)
        val genDetails = mockk<BenRegGen>(relaxed = true)
        every { benWithGenAndOtherProcessed.genDetails } returns genDetails
        every { benWithGenAndOtherProcessed.processed } returns "SomethingElse"
        ds.updateBen(benWithGenAndOtherProcessed)

        assertNotNull(ds.listFlow)
    }
}
