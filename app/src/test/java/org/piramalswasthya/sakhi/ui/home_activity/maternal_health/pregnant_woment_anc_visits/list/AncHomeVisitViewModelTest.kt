package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.model.HomeVisitDomain
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity

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
        every { formResponseDao.getVisitsByBenFlow(any()) } returns flowOf(emptyList())
        viewModel.loadHomeVisits(1L)
        advanceUntilIdle()
    }

    @Test
    fun `loadHomeVisits publishes sorted home visits from the dao flow`() = runTest {
        every { formResponseDao.getVisitsByBenFlow(4L) } returns flowOf(
            listOf(
                ancEntity(id = 2, visitDate = "19-03-2026"),
                ancEntity(id = 1, visitDate = "17-03-2026")
            )
        )

        viewModel.loadHomeVisits(4L)
        advanceUntilIdle()

        val visits = viewModel.homeVisits.value
        assertEquals(2, visits?.size)
        assertEquals(1, visits?.first()?.id)
        assertEquals(2, visits?.last()?.id)
    }

    @Test
    fun `loadHomeVisits emits again when the dao flow reports a sync state change`() = runTest {
        val unsynced = ancEntity(id = 1, visitDate = "17-03-2026", isSynced = false)
        val synced = unsynced.copy(isSynced = true)
        every { formResponseDao.getVisitsByBenFlow(6L) } returns flowOf(
            listOf(unsynced),
            listOf(synced)
        )

        viewModel.loadHomeVisits(6L)
        advanceUntilIdle()

        assertEquals(true, viewModel.homeVisits.value?.first()?.isSynced)
    }

    @Test
    fun `loadHomeVisits publishes empty list when the dao flow fails`() = runTest {
        every { formResponseDao.getVisitsByBenFlow(8L) } returns
                flow { throw IllegalStateException("db down") }

        viewModel.loadHomeVisits(8L)
        advanceUntilIdle()

        assertEquals(emptyList<HomeVisitDomain>(), viewModel.homeVisits.value)
    }

    private fun ancEntity(
        id: Int,
        visitDate: String,
        isSynced: Boolean = true
    ) = ANCFormResponseJsonEntity(
        id = id,
        benId = 1L,
        visitDay = "",
        visitDate = visitDate,
        formId = "anc_form_001",
        version = 1,
        formDataJson = """{"visitNumber": 1}""",
        isSynced = isSynced
    )

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
