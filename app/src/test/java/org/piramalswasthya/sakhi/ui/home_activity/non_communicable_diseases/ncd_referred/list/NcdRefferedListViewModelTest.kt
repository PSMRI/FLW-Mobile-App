package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class NcdRefferedListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context

    private lateinit var viewModel: NcdRefferedListViewModel

    @Before
    override fun setUp() {
        super.setUp()
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
}
