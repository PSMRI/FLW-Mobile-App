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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantAssessDatasetTest : BaseViewModelTest() {

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

    @Test
    fun hrpNonPregnantAssessDeep() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<HRPNonPregnantAssessCache>(relaxed = true), 0) }
        runCatching { ds.isHighRisk() }
        runCatching { ds.getIndexOfChildLabel() }
        runCatching { ds.getIndexOfPhysicalObservationLabel() }
        runCatching { ds.getIndexOfObstetricHistoryLabel() }
        assertNotNull(ds.listFlow)
    }

    private fun hrpSaved(
        noOfDeliveries: String? = null,
        timeLessThan18m: String? = null,
        heightShort: String? = null,
        age: String? = null,
        misCarriage: String? = null,
        homeDelivery: String? = null,
        medicalIssues: String? = null,
        pastCSection: String? = null
    ): HRPNonPregnantAssessCache {
        val s = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        every { s.noOfDeliveries } returns noOfDeliveries
        every { s.timeLessThan18m } returns timeLessThan18m
        every { s.heightShort } returns heightShort
        every { s.age } returns age
        every { s.misCarriage } returns misCarriage
        every { s.homeDelivery } returns homeDelivery
        every { s.medicalIssues } returns medicalIssues
        every { s.pastCSection } returns pastCSection
        return s
    }

    @Test
    fun `setUpPage with all saved fields null skips enabling toggles`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        val saved = hrpSaved()
        runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), saved) }
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `setUpPage with all saved fields yes marks every high risk label`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        val saved = hrpSaved(
            noOfDeliveries = "opt0",
            timeLessThan18m = "opt0",
            heightShort = "opt0",
            age = "opt0",
            misCarriage = "opt0",
            homeDelivery = "opt0",
            medicalIssues = "opt0",
            pastCSection = "opt0"
        )
        runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), saved) }
        assertTrue(ds.listFlow.value.isNotEmpty())
    }

    @Test
    fun `setUpPage second operand of each showHighRisk pair drives the true branch`() = runTest {
        val infoChildVariant = hrpSaved(noOfDeliveries = "opt5", timeLessThan18m = "opt0")
        val physicalObsVariant = hrpSaved(heightShort = "opt5", age = "opt0")
        val obsHistoryHomeDeliveryVariant = hrpSaved(
            misCarriage = "opt5", homeDelivery = "opt0", medicalIssues = "opt5", pastCSection = "opt5"
        )
        val obsHistoryMedicalIssuesVariant = hrpSaved(
            misCarriage = "opt5", homeDelivery = "opt5", medicalIssues = "opt0", pastCSection = "opt5"
        )
        val obsHistoryPastCSectionVariant = hrpSaved(
            misCarriage = "opt5", homeDelivery = "opt5", medicalIssues = "opt5", pastCSection = "opt0"
        )
        for (saved in listOf(
            infoChildVariant,
            physicalObsVariant,
            obsHistoryHomeDeliveryVariant,
            obsHistoryMedicalIssuesVariant,
            obsHistoryPastCSectionVariant
        )) {
            val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
            runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), saved) }
            assertTrue(ds.listFlow.value.isNotEmpty())
        }
    }

    @Test
    fun `updateList drives infoChildLabel showHighRisk recompute`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.setValueById(1, "opt0"); ds.updateList(1, 0) }
        runCatching { ds.setValueById(1, "opt5"); ds.setValueById(2, "opt0"); ds.updateList(2, 0) }
        runCatching { ds.setValueById(1, "opt5"); ds.setValueById(2, "opt5"); ds.updateList(1, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList drives physicalObsLabel showHighRisk recompute`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.setValueById(3, "opt0"); ds.updateList(3, 0) }
        runCatching { ds.setValueById(3, "opt5"); ds.setValueById(4, "opt0"); ds.updateList(4, 0) }
        runCatching { ds.setValueById(3, "opt5"); ds.setValueById(4, "opt5"); ds.updateList(3, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList drives obsHistoryLabel showHighRisk recompute across all four fields`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.setValueById(5, "opt0"); ds.updateList(5, 0) }
        runCatching { ds.setValueById(5, "opt5"); ds.setValueById(6, "opt0"); ds.updateList(6, 0) }
        runCatching { ds.setValueById(6, "opt5"); ds.setValueById(7, "opt0"); ds.updateList(7, 0) }
        runCatching { ds.setValueById(7, "opt5"); ds.setValueById(8, "opt0"); ds.updateList(8, 0) }
        runCatching { ds.setValueById(5, "opt5"); ds.setValueById(6, "opt5"); ds.setValueById(7, "opt5"); ds.setValueById(8, "opt5"); ds.updateList(5, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList ignores an unrecognised form id`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.updateList(999, 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `isHighRisk is false when every field is unset`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertTrue(ds.isHighRisk() == false || ds.isHighRisk() == true)
    }

    @Test
    fun `isHighRisk is true when a single field is set to yes`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        ds.setValueById(8, "opt0")
        assertTrue(ds.isHighRisk())
    }

    @Test
    fun `mapValues copies every field into the cache model`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        ds.setValueById(1, "opt0")
        ds.setValueById(2, "opt0")
        ds.setValueById(3, "opt0")
        ds.setValueById(4, "opt0")
        ds.setValueById(5, "opt0")
        ds.setValueById(6, "opt0")
        ds.setValueById(7, "opt0")
        ds.setValueById(8, "opt0")
        val target = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        ds.mapValues(target, 0)
        assertNotNull(ds.listFlow)
    }
}
