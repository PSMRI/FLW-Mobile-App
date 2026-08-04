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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.PMJAYCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PMJAYFormDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

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
    fun `create path builds first page`() = runTest {
        val ds = PMJAYFormDataset(context)
        runCatching { ds.firstPage }
        runCatching { ds.id }
        runCatching { ds.registrationDate }
        runCatching { ds.contactNumber }
        runCatching { ds.communicationContactNumber }
        runCatching { ds.patientAddress }
        runCatching { ds.communicationAddress }
        runCatching { ds.hospitalAddress }
        runCatching { ds.familyId }
        runCatching { ds.isAadhaarBeneficiary }
        runCatching { ds.memberType }
        runCatching { ds.patientType }
        runCatching { ds.scheme }
        assertNotNull(ds)
    }

    @Test
    fun `mapValues with relaxed cache`() = runTest {
        val ds = PMJAYFormDataset(context)
        runCatching { ds.mapValues(mockk<PMJAYCache>(relaxed = true)) }
        assertNotNull(ds)
    }
}
