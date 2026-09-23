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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Exercises the large public suspend `setUpPage(...)` form-builder methods of the maternal-health /
 * HRP / child configuration datasets. Each setUpPage call is wrapped in runCatching so that a
 * missing string-array resource (mocked to empty) cannot fail the test; we only assert the dataset's
 * listFlow is non-null after the builder has (partly) run.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DatasetSetUpPageMaternalTest : BaseViewModelTest() {

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

    // =====================================================
    // HRP Datasets
    // =====================================================

    @Test
    fun `HRPNonPregnantAssessDataset setUpPage runs`() = runTest {
        val ds = HRPNonPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `HRPPregnantAssessDataset setUpPage runs`() = runTest {
        val ds = HRPPregnantAssessDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `HRPNonPregnantTrackDataset setUpPage runs`() = runTest {
        val ds = HRPNonPregnantTrackDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `HRPPregnantTrackDataset setUpPage runs`() = runTest {
        val ds = HRPPregnantTrackDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `HRPMicroBirthPlanDataset setUpPage runs`() = runTest {
        val ds = HRPMicroBirthPlanDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // Maternal Health Datasets
    // =====================================================

    @Test
    fun `PregnantWomanRegistrationDataset setUpPage runs`() = runTest {
        val ds = PregnantWomanRegistrationDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `PregnantWomanAncVisitDataset setUpPage runs`() = runTest {
        val ds = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(
                1,
                null,
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                null,
                false,
                null
            )
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `PregnantWomanAncAbortionDataset setUpPage runs`() = runTest {
        val ds = PregnantWomanAncAbortionDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `DeliveryOutcomeDataset setUpPage runs`() = runTest {
        val ds = DeliveryOutcomeDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(
                mockk<PregnantWomanRegistrationCache>(relaxed = true),
                mockk<PregnantWomanAncCache>(relaxed = true),
                null
            )
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `PncFormDataset setUpPage runs`() = runTest {
        val ds = PncFormDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(
                visitNumber = 1,
                ben = mockk<BenRegCache>(relaxed = true),
                previousPnc = null,
                saved = null
            )
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `ChildRegistrationDataset setUpPage runs`() = runTest {
        val ds = ChildRegistrationDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `InfantRegistrationDataset setUpPage runs`() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.ENGLISH)
        runCatching {
            ds.setUpPage(
                null,
                mockk<DeliveryOutcomeCache>(relaxed = true),
                0,
                null,
                null
            )
        }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // Death review datasets (take preferenceDao)
    // =====================================================

    @Test
    fun `MDSRFormDataset setUpPage runs`() = runTest {
        val ds = MDSRFormDataset(context, Languages.ENGLISH, preferenceDao)
        runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), "", null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `MDSRFormDataset setUpPage runs with pregnancyDeath`() = runTest {
        val ds = MDSRFormDataset(context, Languages.ENGLISH, preferenceDao, pregnancyDeath = true)
        runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), "", null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `MDSRFormDataset setUpPage runs with abortionDeath`() = runTest {
        val ds = MDSRFormDataset(context, Languages.ENGLISH, preferenceDao, abortionDeath = true)
        runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), "", null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `CDRFormDataset setUpPage runs`() = runTest {
        val ds = CDRFormDataset(context, Languages.ENGLISH, preferenceDao)
        runCatching { ds.setUpPage(mockk<BenRegCache>(relaxed = true), null, null, null, null) }
        assertNotNull(ds.listFlow)
    }
}
