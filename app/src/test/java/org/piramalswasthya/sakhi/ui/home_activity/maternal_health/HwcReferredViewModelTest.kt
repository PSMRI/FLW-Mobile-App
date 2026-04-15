package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

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
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.hwc.list.HwcReferredViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class HwcReferredViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context

    private lateinit var viewModel: HwcReferredViewModel

    @Before
    override fun setUp() {
        super.setUp()
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 789
        every { user.name } returns "ASHA Worker"
        every { preferenceDao.getLoggedInUser() } returns user
        every { recordsRepo.getHwcRefferedList } returns flowOf(emptyList())
        viewModel = HwcReferredViewModel(recordsRepo, preferenceDao, context)
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
        assertEquals("ASHA Worker", viewModel.userName)
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
    // getAshaId() Tests
    // =====================================================

    @Test
    fun `getAshaId returns user id`() = runTest {
        advanceUntilIdle()
        assertEquals(789, viewModel.getAshaId())
    }
}
