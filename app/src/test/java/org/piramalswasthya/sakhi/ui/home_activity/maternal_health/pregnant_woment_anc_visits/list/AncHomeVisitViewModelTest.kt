package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao

@OptIn(ExperimentalCoroutinesApi::class)
class AncHomeVisitViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var formResponseDao: FormResponseANCJsonDao

    private lateinit var viewModel: AncHomeVisitViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AncHomeVisitViewModel(formResponseDao)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `homeVisits is not null`() {
        assertNotNull(viewModel.homeVisits)
    }

    // =====================================================
    // loadHomeVisits() Tests
    // =====================================================

    @Test
    fun `loadHomeVisits does not throw`() = runTest {
        coEvery { formResponseDao.getSyncedVisitsByRchId(any()) } returns emptyList()
        viewModel.loadHomeVisits(1L)
        advanceUntilIdle()
    }

    // =====================================================
    // getNextVisitNumber() Tests
    // =====================================================

    @Test
    fun `getNextVisitNumber returns value for empty list`() = runTest {
        coEvery { formResponseDao.getSyncedVisitsByRchId(any()) } returns emptyList()
        val result = viewModel.getNextVisitNumber(1L)
        assertNotNull(result)
    }
}
