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
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class CDRFormDatasetTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao

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
        every { preferenceDao.getLoggedInUser() } returns null
    }

    @Test
    fun cdrFormDeep() = runTest {
        val ds = CDRFormDataset(context, Languages.ENGLISH, preferenceDao)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<CDRCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, null, null, null, null) }
        runCatching { ds.setUpPage(ben, "addr", "12", "mohalla", saved) }
        runCatching { ds.mapValues(mockk<CDRCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfCDR1() }
        runCatching { ds.getIndexOfCDR2() }
        runCatching { ds.getIndexOfIsDeathCertificate() }
        assertNotNull(ds.listFlow)
    }
}
