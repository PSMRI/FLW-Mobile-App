package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

import android.content.Context
import android.content.res.Resources
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithCbacCache
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.database.room.SyncState
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
        every { context.getString(R.string.select_years) } returns "Select Years"
        every { context.getString(R.string.years_35) } returns "35 YEARS"
        every { context.getString(R.string.years_40) } returns "40 YEARS"
        every { context.getString(R.string.years_45) } returns "45 YEARS"
        every { context.getString(R.string.years_50) } returns "50 YEARS"
        every { context.getString(R.string.years_55) } returns "55 YEARS"
        every { context.getString(R.string.years_60) } returns "60 YEARS"
        every { context.getString(R.string.years_65) } returns "65 YEARS"
        every { context.getString(R.string.years_70) } returns "70 YEARS"
        every { context.getString(R.string.years_75) } returns "75 YEARS"
        every { context.getString(R.string.years_80) } returns "80 YEARS"
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
        val years = viewModel.yearsList(
            context = context
        )
        assertEquals(11, years.size)
        assertEquals("Select Years", years[0])
        assertEquals("35 YEARS", years[1])
        assertEquals("80 YEARS", years[10])
    }

    @Test
    fun `yearsList clears and rebuilds on each call`() {
        viewModel.yearsList(    context = context)
        val years = viewModel.yearsList(    context = context)
        assertEquals(11, years.size)
    }

    private fun benCache(benId: Long, ageInt: Int, screened: Boolean): BenWithCbacCache {
        val domain = mockk<BenBasicDomain>(relaxed = true)
        every { domain.benId } returns benId
        every { domain.ageInt } returns ageInt
        val ben = mockk<BenBasicCache>(relaxed = true)
        every { ben.benId } returns benId
        every { ben.asBasicDomainModel() } returns domain
        val cbacRecords = if (screened) listOf(CbacCache(benId = benId, ashaId = 1, syncState = SyncState.SYNCED)) else emptyList()
        return BenWithCbacCache(ben = ben, savedCbacRecords = cbacRecords)
    }

    @Test
    fun `benList returns all matching beneficiaries when category is ALL`() = runTest {
        every { recordsRepo.getNcdEligibleList } returns flowOf(
            listOf(benCache(1L, 40, screened = true), benCache(2L, 50, screened = false))
        )
        val vm = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)

        val list = vm.benList.first()

        assertEquals(2, list.size)
    }

    @Test
    fun `benList filters to only screened beneficiaries`() = runTest {
        every { recordsRepo.getNcdEligibleList } returns flowOf(
            listOf(benCache(1L, 40, screened = true), benCache(2L, 50, screened = false))
        )
        val vm = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)
        vm.setSelectedCategory("Screened")
        advanceUntilIdle()

        val list = vm.benList.first()

        assertEquals(1, list.size)
        assertEquals(1L, list[0].ben.benId)
    }

    @Test
    fun `benList filters to only not-screened beneficiaries`() = runTest {
        every { recordsRepo.getNcdEligibleList } returns flowOf(
            listOf(benCache(1L, 40, screened = true), benCache(2L, 50, screened = false))
        )
        val vm = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)
        vm.setSelectedCategory("Not Screened")
        advanceUntilIdle()

        val list = vm.benList.first()

        assertEquals(1, list.size)
        assertEquals(2L, list[0].ben.benId)
    }

    @Test
    fun `benList applies the selected age band`() = runTest {
        every { recordsRepo.getNcdEligibleList } returns flowOf(
            listOf(benCache(1L, 40, screened = true), benCache(2L, 60, screened = true))
        )
        val vm = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)
        vm.setSelectedYear(vm.yearForPosition(2))
        advanceUntilIdle()

        val list = vm.benList.first()

        assertEquals(1, list.size)
        assertEquals(1L, list[0].ben.benId)
    }

    @Test
    fun `yearForPosition returns null for position zero`() {
        assertEquals(null, viewModel.yearForPosition(0))
    }

    @Test
    fun `yearForPosition returns the expected age band start`() {
        assertEquals(35, viewModel.yearForPosition(1))
        assertEquals(80, viewModel.yearForPosition(10))
    }

    // =====================================================
    // ncdDetails Tests
    // =====================================================

    @Test
    fun `ncdDetails emits reversed cbac records for selected ben`() = runTest {
        every { recordsRepo.getNcdEligibleList } returns flowOf(
            listOf(benCache(1L, 40, screened = true))
        )
        val vm = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)
        vm.setSelectedBenId(1L)
        advanceUntilIdle()

        val details = vm.ncdDetails.first()

        assertTrue(details.isNotEmpty())
    }

    @Test
    fun `ncdDetails does not emit when no ben selected`() = runTest {
        every { recordsRepo.getNcdEligibleList } returns flowOf(
            listOf(benCache(1L, 40, screened = true))
        )
        val vm = NcdEligibleListViewModel(recordsRepo, preferenceDao, context)

        var emitted = false
        val job = launch(Dispatchers.Unconfined) {
            vm.ncdDetails.collect { emitted = true }
        }
        advanceUntilIdle()
        job.cancel()

        assertTrue(!emitted)
    }
}
