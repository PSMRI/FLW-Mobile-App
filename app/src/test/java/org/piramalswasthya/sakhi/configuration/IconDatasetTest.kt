package org.piramalswasthya.sakhi.configuration

import android.content.res.Resources
import android.util.Log
import android.view.animation.RotateAnimation
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.AdolescentHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

/**
 * Deep coverage test for [IconDataset]. IconDataset is not a Dataset (no listFlow); its many
 * getXIconDataset(resources) builder methods construct the large icon lists. Each is exercised
 * with a relaxed Resources mock and wrapped in runCatching so navigation/direction lookups that
 * need Android runtime cannot fail the test.
 *
 * getLeprosyDataset / getDiseaseControlDataset / getMalariaDataset all call
 * [org.piramalswasthya.sakhi.configuration.IconDataset.Disease.getTitleRes], a top-level extension
 * declared in ui/BindingUtils.kt. That file's facade class has a file-level
 * `private val rotate = RotateAnimation(...)` that runs unconditionally in its `<clinit>`, and a
 * real RotateAnimation() construction throws under the plain (non-Robolectric) android.jar stub
 * used for these unit tests. Without mocking its constructor first, the very first reference to the
 * facade class throws ExceptionInInitializerError, which - being a JVM class-initialization failure,
 * not an ordinary exception - permanently poisons that class for the rest of the test fork (every
 * later reference throws NoClassDefFoundError instead), exactly per BindingUtilsTest.kt's own
 * documented finding. Mocking the constructor here, mirroring BindingUtilsTest.kt's own setup,
 * guarantees the facade class initializes successfully the first time it is touched regardless of
 * class/fork ordering - both unlocking these three builders' coverage and defusing that latent
 * cross-file poisoning risk.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IconDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var recordsRepo: RecordsRepo

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var adolescentHealthRepo: AdolescentHealthRepo

    @MockK
    private lateinit var resources: Resources

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { resources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { resources.getString(any()) } returns "x"
        every { resources.getString(any(), any()) } returns "x"
        mockkConstructor(RotateAnimation::class, recordPrivateCalls = false)
        every { anyConstructed<RotateAnimation>().duration = any() } just Runs
        every { anyConstructed<RotateAnimation>().interpolator = any() } just Runs
        every { anyConstructed<RotateAnimation>().repeatCount = any() } just Runs
    }

    private fun ds() = IconDataset(recordsRepo, preferenceDao, adolescentHealthRepo)

    @Test
    fun `getImmunizationDataset and getVillageLevelFormsDataset apply colorPrimary on single item lists`() {
        val d = ds()
        val immunization = d.getImmunizationDataset(resources)
        val vlf = d.getVillageLevelFormsDataset(resources)
        assertEquals(1, immunization.size)
        assertTrue(immunization[0].colorPrimary)
        assertEquals(1, vlf.size)
        assertTrue(vlf[0].colorPrimary)
    }

    @Test
    fun `all icon builder methods execute`() {
        val d = ds()
        runCatching { d.getHomeIconDataset(resources) }
        runCatching { d.getHrpIconsDataset(resources) }
        runCatching { d.getCHOIconDataset(resources) }
        runCatching { d.getSupervisorIconsDataset(resources) }
        runCatching { d.getHRPPregnantWomenDataset(resources) }
        runCatching { d.getVLFDataset(resources) }
        runCatching { d.getHRPNonPregnantWomenDataset(resources) }
        runCatching { d.getChildCareDataset(resources) }
        runCatching { d.getLmsDataset(resources) }
        runCatching { d.getEligibleCoupleDataset(resources) }
        runCatching { d.getLeprosyDataset(resources) }
        runCatching { d.getDiseaseControlDataset(resources) }
        runCatching { d.getDeathReportDataset(resources) }
        runCatching { d.getMotherCareDataset(resources) }
        runCatching { d.getNCDDataset(resources) }
        runCatching { d.getImmunizationDataset(resources) }
        runCatching { d.getVillageLevelFormsDataset(resources) }
        runCatching { d.getCDDataset(resources) }
        runCatching { d.getMalariaDataset(resources) }
        assertNotNull(d)
    }

    @Test
    fun `getLeprosyDataset builds all three leprosy stage icons`() {
        val d = ds()
        val list = d.getLeprosyDataset(resources)
        assertEquals(3, list.size)
        assertTrue(list[0].colorPrimary)
        assertTrue(list[1].colorPrimary)
        assertTrue(list[2].colorPrimary)
    }

    @Test
    fun `getDiseaseControlDataset builds every disease icon including the title-res lookups`() {
        val d = ds()
        val list = d.getDiseaseControlDataset(resources)
        assertEquals(7, list.size)
        assertTrue(list[0].colorPrimary)
        assertTrue(!list[1].colorPrimary)
    }

    @Test
    fun `getMalariaDataset builds the malaria and confirmed-malaria icons`() {
        val d = ds()
        val list = d.getMalariaDataset(resources)
        assertEquals(2, list.size)
        assertTrue(list[0].colorPrimary)
        assertTrue(!list[1].colorPrimary)
    }
}
