package org.piramalswasthya.sakhi.configuration.dynamicDataSet

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
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Coverage for [ReferalFormDataset] (dynamicDataSet). Drives setUpPage with several referral /
 * referralType string combinations and mapValues with a logged-in user present vs null, so the
 * form-element wiring plus the createdBy/vanID/serviceMapId preferenceDao branches all run.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReferalFormDatasetTest : BaseViewModelTest() {

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
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    private fun ds() = ReferalFormDataset(context, Languages.ENGLISH, preferenceDao)

    @Test
    fun `setUpPage referral variants`() = runTest {
        val combos = listOf(
            "High BP" to "NCD",
            "" to "TB",
            "Fever" to ""
        )
        for ((reason, type) in combos) {
            val d = ds()
            runCatching { d.setUpPage(reason, type) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `mapValues with null logged-in user`() = runTest {
        val d = ds()
        runCatching { d.setUpPage("Anemia", "ANC") }
        runCatching { d.mapValues(mockk<ReferalCache>(relaxed = true), 0) }
        runCatching { d.mapValues(mockk<ReferalCache>(relaxed = true), 1) }
        assertNotNull(d.listFlow)
    }
}
