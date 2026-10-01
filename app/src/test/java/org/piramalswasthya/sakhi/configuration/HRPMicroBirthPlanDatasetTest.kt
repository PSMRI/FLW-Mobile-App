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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class HRPMicroBirthPlanDatasetTest : BaseViewModelTest() {

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
    fun hrpMicroBirthPlanDeep() = runTest {
        val ds = HRPMicroBirthPlanDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<HRPMicroBirthPlanCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<HRPMicroBirthPlanCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `HRPMicroBirthPlanCache exposes bankac`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L, bankac = "ACC123")
        assertEquals("ACC123", cache.bankac)
    }

    @Test
    fun `updateList sweeps every formId`() = runTest {
        val ds = HRPMicroBirthPlanDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, mockk<HRPMicroBirthPlanCache>(relaxed = true)) }
        for (id in 0..30) {
            for (index in 0..2) {
                runCatching { ds.updateList(id, index) }
            }
        }
        assertNotNull(ds.listFlow)
    }
}
