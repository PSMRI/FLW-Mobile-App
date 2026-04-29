package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

import android.content.Context
import android.content.res.Resources
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class NcdEligibleListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: NcdEligibleListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(R.string.all) } returns "ALL"
        every { mockResources.getString(R.string.screened) } returns "Screened"
        every { mockResources.getString(R.string.not_screened) } returns "Not Screened"
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 123
        every { preferenceDao.getLoggedInUser() } returns user
        every { recordsRepo.getNcdEligibleList } returns flowOf(emptyList())
        viewModel = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `clickedPosition is initially 0`() {
        assertEquals(0, viewModel.clickedPosition)
    }

    // =====================================================
    // filterText() Tests
    // =====================================================

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("test")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with empty string does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
    }

    // =====================================================
    // setSelectedCategory() Tests
    // =====================================================

    @Test
    fun `setSelectedCategory does not throw`() = runTest {
        viewModel.setSelectedCategory("Screened")
        advanceUntilIdle()
    }

    @Test
    fun `setSelectedCategory with ALL does not throw`() = runTest {
        viewModel.setSelectedCategory("ALL")
        advanceUntilIdle()
    }

    // =====================================================
    // setSelectedBenId() / getSelectedBenId() Tests
    // =====================================================

    @Test
    fun `getSelectedBenId returns 0 initially`() {
        assertEquals(0L, viewModel.getSelectedBenId())
    }

    @Test
    fun `setSelectedBenId updates selected ben id`() = runTest {
        viewModel.setSelectedBenId(42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.getSelectedBenId())
    }

    // =====================================================
    // getAshaId() Tests
    // =====================================================

    @Test
    fun `getAshaId returns user id from preferences`() = runTest {
        advanceUntilIdle()
        assertEquals(123, viewModel.getAshaId())
    }

    // =====================================================
    // categoryData() Tests
    // =====================================================

    @Test
    fun `categoryData returns list with ALL Screened Not Screened`() {
        val categories = viewModel.categoryData()
        assertEquals(3, categories.size)
        assertEquals("ALL", categories[0])
        assertEquals("Screened", categories[1])
        assertEquals("Not Screened", categories[2])
    }

    @Test
    fun `categoryData clears and rebuilds on each call`() {
        viewModel.categoryData()
        val categories = viewModel.categoryData()
        assertEquals(3, categories.size)
    }

    // =====================================================
    // yearsList() Tests
    // =====================================================

    @Test
    fun `yearsList returns 11 entries starting with Select Years`() {
        val years = viewModel.yearsList()
        assertEquals(11, years.size)
        assertEquals("Select Years", years[0])
        assertEquals("35 YEARS", years[1])
        assertEquals("80 YEARS", years[10])
    }

    @Test
    fun `yearsList clears and rebuilds on each call`() {
        viewModel.yearsList()
        val years = viewModel.yearsList()
        assertEquals(11, years.size)
    }
}
