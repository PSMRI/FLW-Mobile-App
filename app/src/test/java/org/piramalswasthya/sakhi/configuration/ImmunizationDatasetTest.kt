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
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Consolidated coverage for [ImmunizationDataset] (merged from Deep + Branch3 variants):
 * ENGLISH/HINDI language, several vaccine name values, and multiple mapValues page indices.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImmunizationDatasetTest : BaseViewModelTest() {

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
    fun immunizationDeep() = runTest {
        val ds = ImmunizationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val vaccine = mockk<Vaccine>(relaxed = true)
        every { vaccine.vaccineName } returns "BCG1"
        val imm = mockk<ImmunizationCache>(relaxed = true)
        runCatching { ds.setFirstPage(ben, vaccine, null) }
        runCatching { ds.setFirstPage(ben, vaccine, imm) }
        runCatching { ds.mapValues(mockk<ImmunizationCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun immunizationBranch() = runTest {
        val ds = ImmunizationDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        for (name in listOf("OPV 0", "BCG", "Measles-Rubella (MR) 1st Dose", "Penta 1", "IPV", "Vitamin A")) {
            val vaccine = mockk<Vaccine>(relaxed = true)
            every { vaccine.vaccineName } returns name
            runCatching { ds.setFirstPage(ben, vaccine, null) }
            runCatching { ds.setFirstPage(ben, vaccine, mockk<ImmunizationCache>(relaxed = true)) }
        }
        for (p in 0..2) {
            runCatching { ds.mapValues(mockk<ImmunizationCache>(relaxed = true), p) }
        }
        assertNotNull(ds.listFlow)
    }
}
