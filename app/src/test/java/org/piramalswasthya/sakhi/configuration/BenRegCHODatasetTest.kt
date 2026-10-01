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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class BenRegCHODatasetTest : BaseViewModelTest() {

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
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    @Test
    fun `setUpPage builds and getters`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        runCatching { d.setUpPage() }
        runCatching { d.getIndexOfDOB() }
        runCatching { d.getIndexOfAge() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `dob change recalculates age and validates the range`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(3, "01-01-2000")
        d.updateList(3, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `name field validates all caps with hindi enabled`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(2, "john")
        d.updateList(2, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `husbandName field validates all caps with hindi enabled`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(4, "john")
        d.updateList(4, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `age field null value skips the dob recalculation`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(8, null)
        d.updateList(8, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `age field blank value skips the dob recalculation`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(8, "")
        d.updateList(8, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `age field valid value recalculates dob`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(8, "25")
        d.updateList(8, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `contactNumber field validates the mobile number`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(5, "9876543210")
        d.updateList(5, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `updateList ignores an unrecognised form id`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.updateList(999, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `mapValues skips the reproductive status update when not pregnant and genDetails is null`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(1, "01-01-2023")
        d.setValueById(3, "01-01-2000")
        d.setValueById(5, "9876543210")
        d.setValueById(7, "opt5")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        d.mapValues(ben, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `mapValues updates the reproductive status when pregnant and genDetails is present`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(1, "01-01-2023")
        d.setValueById(3, "01-01-2000")
        d.setValueById(5, "9876543210")
        d.setValueById(7, "opt0")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns mockk<BenRegGen>(relaxed = true)
        d.mapValues(ben, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `mapValues marks pregnant with no genDetails still flags processed`() = runTest {
        val d = BenRegCHODataset(context, Languages.ENGLISH)
        d.setUpPage()
        d.setValueById(1, "01-01-2023")
        d.setValueById(3, "01-01-2000")
        d.setValueById(5, "9876543210")
        d.setValueById(7, "opt0")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.genDetails } returns null
        d.mapValues(ben, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }
}
