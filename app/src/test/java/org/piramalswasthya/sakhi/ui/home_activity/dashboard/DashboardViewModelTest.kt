package org.piramalswasthya.sakhi.ui.home_activity.dashboard

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var database: InAppDb
    private lateinit var pref: PreferenceDao

    @Before
    override fun setUp() {
        super.setUp()

        val benDao = mockk<BenDao>(relaxed = true) {
            every { getAllBenCount(any()) } returns flowOf(0)
            every { getAllPregnancyWomenListCount(any(), any()) } returns flowOf(0)
            every { getHighRiskWomenCount(any()) } returns flowOf(0)
            every { getAllDeliveredWomenListCount(any()) } returns flowOf(0)
            every { getAllBenGenderCount(any(), any()) } returns flowOf(0)
            every { getUnProcessedRecordCount() } returns flowOf(0)
        }
        val householdDao = mockk<HouseholdDao>(relaxed = true) {
            every { getAllHouseholdsCount(any()) } returns flowOf(0)
        }

        database = mockk(relaxed = true) {
            every { this@mockk.benDao } returns benDao
            every { this@mockk.householdDao } returns householdDao
        }
        pref = mockk(relaxed = true) {
            every { getLocationRecord() } returns null
            every { getLoggedInUser() } returns null
        }

        viewModel = DashboardViewModel(database, pref)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }
}
