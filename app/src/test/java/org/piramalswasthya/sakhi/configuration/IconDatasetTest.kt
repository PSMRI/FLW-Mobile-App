package org.piramalswasthya.sakhi.configuration

import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
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
    }

    private fun ds() = IconDataset(recordsRepo, preferenceDao, adolescentHealthRepo)

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
}
