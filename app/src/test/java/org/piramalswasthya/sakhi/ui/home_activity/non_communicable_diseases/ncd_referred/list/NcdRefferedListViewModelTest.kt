package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithCbacAndReferalCache
import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class NcdRefferedListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: NcdRefferedListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(R.string.all) } returns "ALL"
        every { mockResources.getString(R.string.cat_ncd) } returns "NCD"
        every { mockResources.getString(R.string.cat_tb) } returns "TB"
        every { mockResources.getString(R.string.cat_leprosy) } returns "LEPROSY"
        every { mockResources.getString(R.string.cat_geriatric) } returns "GERIATRIC"
        every { mockResources.getString(R.string.cat_hrp) } returns "HRP"
        every { mockResources.getString(R.string.cat_maternal) } returns "MATERNAL"
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 456
        every { user.name } returns "TestUser"
        every { preferenceDao.getLoggedInUser() } returns user
        every { recordsRepo.getNcdrefferedList } returns flowOf(emptyList())
        viewModel = NcdRefferedListViewModel(recordsRepo, preferenceDao, context)
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
    fun `userName is set from preferences`() {
        assertEquals("TestUser", viewModel.userName)
    }

    @Test
    fun `selectedPosition is initially 0`() {
        assertEquals(0, viewModel.selectedPosition)
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
    // setSelectedBenId() / getSelectedBenId() Tests
    // =====================================================

    @Test
    fun `getSelectedBenId returns 0 initially`() {
        assertEquals(0L, viewModel.getSelectedBenId())
    }

    @Test
    fun `setSelectedBenId updates value`() = runTest {
        viewModel.setSelectedBenId(100L)
        advanceUntilIdle()
        assertEquals(100L, viewModel.getSelectedBenId())
    }

    // =====================================================
    // getAshaId() Tests
    // =====================================================

    @Test
    fun `getAshaId returns user id from preferences`() = runTest {
        advanceUntilIdle()
        assertEquals(456, viewModel.getAshaId())
    }

    // =====================================================
    // setSelectedFilter() Tests
    // =====================================================

    @Test
    fun `setSelectedFilter does not throw`() = runTest {
        viewModel.setSelectedFilter("NCD")
        advanceUntilIdle()
    }

    // =====================================================
    // updateBottomSheetData() Tests
    // =====================================================

    @Test
    fun `updateBottomSheetData does not throw`() = runTest {
        viewModel.updateBottomSheetData(42L)
        advanceUntilIdle()
    }

    // =====================================================
    // categoryData() Tests
    // =====================================================

    @Test
    fun `categoryData returns 7 categories`() {
        val categories = viewModel.categoryData()
        assertEquals(7, categories.size)
        assertEquals("ALL", categories[0])
        assertEquals("NCD", categories[1])
        assertEquals("TB", categories[2])
        assertEquals("LEPROSY", categories[3])
        assertEquals("GERIATRIC", categories[4])
        assertEquals("HRP", categories[5])
        assertEquals("MATERNAL", categories[6])
    }

    @Test
    fun `categoryData clears and rebuilds on each call`() {
        viewModel.categoryData()
        val categories = viewModel.categoryData()
        assertEquals(7, categories.size)
    }

    @Test
    fun `categoryData rebuilds to seven items after mutating and re-calling`() {
        val first = viewModel.categoryData()
        first.add("EXTRA")
        val second = viewModel.categoryData()
        assertEquals(7, second.size)
        assertEquals("ALL", second[0])
        assertEquals("MATERNAL", second[6])
    }

    @Test
    fun `setSelectedFilter then filter combine does not throw`() = runTest {
        viewModel.setSelectedFilter("NCD")
        viewModel.filterText("ram")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `setSelectedFilter ALL keeps full list path`() = runTest {
        viewModel.setSelectedFilter("ALL")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `setSelectedBenId updates getSelectedBenId`() = runTest {
        assertEquals(0L, viewModel.getSelectedBenId())
        viewModel.setSelectedBenId(321L)
        advanceUntilIdle()
        assertEquals(321L, viewModel.getSelectedBenId())
    }

    @Test
    fun `getAshaId returns configured user id`() = runTest {
        advanceUntilIdle()
        assertEquals(456, viewModel.getAshaId())
    }

    @Test
    fun `updateBottomSheetData does not throw_2`() = runTest {
        viewModel.updateBottomSheetData(99L)
        advanceUntilIdle()
        assertNotNull(viewModel.selectedFilter)
    }

    @Test
    fun `userName resolved from preferences`() {
        assertEquals("TestUser", viewModel.userName)
    }

    private fun benCache(id: Long, type: String?, name: String): BenWithCbacAndReferalCache {
        val domain = BenBasicDomain(
            benId = id,
            hhId = id,
            reproductiveStatusId = 0,
            regDate = "01-01-2024",
            benName = name,
            gender = "Male",
            dob = 0L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = name,
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false
        )
        val referral = ReferalCache(benId = id, type = type, syncState = SyncState.SYNCED)
        val cache = mockk<BenWithCbacAndReferalCache>(relaxed = true)
        every { cache.referral } returns referral
        every { cache.asDomainModel() } returns BenWithCbacReferDomain(domain, emptyList(), referral)
        return cache
    }

    @Test
    fun `benList filters by selected category type`() = runTest {
        val ncd = benCache(1L, "NCD", "Alice")
        val tb = benCache(2L, "TB", "Bob")
        every { recordsRepo.getNcdrefferedList } returns flowOf(listOf(ncd, tb))
        val vm = NcdRefferedListViewModel(recordsRepo, preferenceDao, context)

        vm.setSelectedFilter("NCD")
        advanceUntilIdle()
        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `benList with ALL filter returns full list`() = runTest {
        val ncd = benCache(1L, "NCD", "Alice")
        val tb = benCache(2L, "TB", "Bob")
        every { recordsRepo.getNcdrefferedList } returns flowOf(listOf(ncd, tb))
        val vm = NcdRefferedListViewModel(recordsRepo, preferenceDao, context)

        advanceUntilIdle()
        val result = vm.benList.first()

        assertEquals(2, result.size)
    }

    @Test
    fun `benList search text filters by name`() = runTest {
        val ncd = benCache(1L, "NCD", "Alice")
        val tb = benCache(2L, "TB", "Bob")
        every { recordsRepo.getNcdrefferedList } returns flowOf(listOf(ncd, tb))
        val vm = NcdRefferedListViewModel(recordsRepo, preferenceDao, context)

        vm.filterText("alice")
        advanceUntilIdle()
        val result = vm.benList.first()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].ben.benId)
    }

    @Test
    fun `toEnglishCategory falls back to raw localized type when unmatched`() = runTest {
        val ncd = benCache(1L, "NCD", "Alice")
        every { recordsRepo.getNcdrefferedList } returns flowOf(listOf(ncd))
        val vm = NcdRefferedListViewModel(recordsRepo, preferenceDao, context)

        vm.setSelectedFilter("UNKNOWN_TYPE")
        advanceUntilIdle()
        val result = vm.benList.first()

        assertTrue(result.isEmpty())
    }
}
