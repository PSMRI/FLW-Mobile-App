package org.piramalswasthya.sakhi.ui.home_activity.general_op_care

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.GeneralOpdDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class GeneralOpCareViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var generalOpdDao: GeneralOpdDao

    private lateinit var viewModel: GeneralOpCareViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { generalOpdDao.getAll() } returns flowOf(emptyList())
        viewModel = GeneralOpCareViewModel(benRepo, generalOpdDao)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `allBenList flow is not null`() {
        assertNotNull(viewModel.allBenList)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `abha is initially null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `benId is initially null`() {
        assertNull(viewModel.benId.value)
    }

    @Test
    fun `benRegId is initially null`() {
        assertNull(viewModel.benRegId.value)
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
    // getBenFromId() Tests
    // =====================================================

    @Test
    fun `getBenFromId returns benRegId when ben exists`() = runTest {
        val ben = mockk<BenRegCache>()
        every { ben.benRegId } returns 999L
        coEvery { benRepo.getBenFromId(100L) } returns ben

        val result = viewModel.getBenFromId(100L)

        assertEquals(999L, result)
    }

    @Test
    fun `getBenFromId returns 0 when ben not found`() = runTest {
        coEvery { benRepo.getBenFromId(100L) } returns null

        val result = viewModel.getBenFromId(100L)

        assertEquals(0L, result)
    }
}
