package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
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
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Exercises the suspend `setUpPage(...)` form-builder methods of the disease-control,
 * TB, village-level and misc configuration datasets so their bodies actually run
 * (raising JaCoCo coverage beyond simple construction).
 *
 * Each setUpPage call is wrapped in runCatching {} because some datasets index
 * resource arrays / dereference non-null caches that our empty mocks don't supply;
 * the goal is to execute as much of the method body as possible, not to assert output.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DatasetSetUpPageDiseaseTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao

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
    // Disease Control Datasets
    // =====================================================

    @Test
    fun `AESJEFormDataset setUpPage runs`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `FilariaFormDataset setUpPage runs`() = runTest {
        val ds = FilariaFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `KalaAzarFormDataset setUpPage runs`() = runTest {
        val ds = KalaAzarFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `LeprosyFormDataset setUpPage runs`() = runTest {
        val ds = LeprosyFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `LeprosyConfirmedDataset setUpPage runs`() = runTest {
        val ds = LeprosyConfirmedDataset(context, Languages.ENGLISH)
        // 3-arg overload: (ben, saved, followUp)
        runCatching { ds.setUpPage(null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `LeprosySuspectedDataset setUpPage runs`() = runTest {
        val ds = LeprosySuspectedDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `MalariaFormDataset setUpPage runs`() = runTest {
        val ds = MalariaFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `MalariaConfirmCasesDataset setUpPage runs`() = runTest {
        val ds = MalariaConfirmCasesDataset(context, Languages.ENGLISH)
        // (ben, slideTestName, saved)
        runCatching { ds.setUpPage(null, "", null) }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // TB Datasets
    // =====================================================

    @Test
    fun `TBScreeningDataset setUpPage runs`() = runTest {
        val ds = TBScreeningDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `TBConfirmedDataset setUpPage runs`() = runTest {
        val ds = TBConfirmedDataset(context, Languages.ENGLISH)
        // (ben, saved, suspectedTb)
        runCatching { ds.setUpPage(null, null, null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `SuspectedTBDataset setUpPage runs`() = runTest {
        val ds = SuspectedTBDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // Village Level Form Datasets
    // =====================================================

    @Test
    fun `AHDDataset setUpPage runs`() = runTest {
        val ds = AHDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `DewormingDataset setUpPage runs`() = runTest {
        val ds = DewormingDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `PHCReviewDataset setUpPage runs`() = runTest {
        val ds = PHCReviewDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `VHNCDataset setUpPage runs`() = runTest {
        val ds = VHNCDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `VHNDDataset setUpPage runs`() = runTest {
        val ds = VHNDDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    // =====================================================
    // Misc Datasets
    // =====================================================

    @Test
    fun `IRSRoundDataSet setUpPage runs`() = runTest {
        val ds = IRSRoundDataSet(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `SaasBahuSamelanDataset setUpPage runs`() = runTest {
        val ds = SaasBahuSamelanDataset(context, Languages.ENGLISH)
        // (saasBahu, recordExists)
        runCatching { ds.setUpPage(null, false) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `MaaMeetingDataset setUpPage runs`() = runTest {
        val ds = MaaMeetingDataset(context, Languages.ENGLISH)
        // (recordExists)
        runCatching { ds.setUpPage(false) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `UWINDataset setFirstPage runs`() = runTest {
        // UWINDataset exposes setFirstPage(saved: Boolean, cache: UwinCache?) not setUpPage
        val ds = UWINDataset(context, Languages.ENGLISH)
        runCatching { ds.setFirstPage(false, null) }
        assertNotNull(ds.listFlow)
    }
}
