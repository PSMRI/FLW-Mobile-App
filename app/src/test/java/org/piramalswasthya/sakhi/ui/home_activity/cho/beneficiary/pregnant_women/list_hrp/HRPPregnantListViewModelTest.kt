package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.list_hrp

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.BenWithHRPTListDomain
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HRPPregnantListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var hrpRepo: HRPRepo

    private lateinit var viewModel: HRPPregnantListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.hrpTrackingPregList } returns flowOf(emptyList())
        coEvery { hrpRepo.getAllPregTrack() } returns emptyList()
        viewModel = HRPPregnantListViewModel(recordsRepo, hrpRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList is not null`() {
        assertNotNull(viewModel.benList)
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
    // getTrackDetails() Tests
    // =====================================================

    @Test
    fun `getTrackDetails returns list after init`() = runTest {
        advanceUntilIdle()
        val result = viewModel.getTrackDetails()
        assertNotNull(result)
    }

    // =====================================================
    // benList emission Tests
    // =====================================================

    @Test
    fun `benList emits unfiltered repository list by default`() = runTest {
        every { recordsRepo.hrpTrackingPregList } returns flowOf(
            listOf(mockk<BenWithHRPTListDomain>(relaxed = true))
        )
        val vm = HRPPregnantListViewModel(recordsRepo, hrpRepo)
        val list = vm.benList.first()
        assertEquals(1, list.size)
    }

    // =====================================================
    // updateBenWithForms() Tests
    // =====================================================

    @Test
    fun `updateBenWithForms enables form1 when last visit was in a different month`() = runTest {
        val ben = mockk<BenBasicDomainForForm>(relaxed = true)
        every { ben.benId } returns 1L
        val oldVisit = HRPPregnantTrackCache(
            benId = 1L,
            visitDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60)
        )
        coEvery { hrpRepo.getHrPregTrackList(1L) } returns listOf(oldVisit)
        viewModel.updateBenWithForms(ben)
        io.mockk.verify { ben.form1Enabled = true }
    }

    @Test
    fun `updateBenWithForms disables form1 when last visit was this month`() = runTest {
        val ben = mockk<BenBasicDomainForForm>(relaxed = true)
        every { ben.benId } returns 2L
        val currentVisit = HRPPregnantTrackCache(
            benId = 2L,
            visitDate = System.currentTimeMillis()
        )
        coEvery { hrpRepo.getHrPregTrackList(2L) } returns listOf(currentVisit)
        viewModel.updateBenWithForms(ben)
        io.mockk.verify { ben.form1Enabled = false }
    }

    @Test
    fun `updateBenWithForms does nothing when track list is empty`() = runTest {
        val ben = mockk<BenBasicDomainForForm>(relaxed = true)
        every { ben.benId } returns 3L
        coEvery { hrpRepo.getHrPregTrackList(3L) } returns emptyList()
        viewModel.updateBenWithForms(ben)
        io.mockk.verify(exactly = 0) { ben.form1Enabled = any() }
    }

    @Test
    fun `updateBenWithForms does nothing when track list is null`() = runTest {
        val ben = mockk<BenBasicDomainForForm>(relaxed = true)
        every { ben.benId } returns 4L
        coEvery { hrpRepo.getHrPregTrackList(4L) } returns null
        viewModel.updateBenWithForms(ben)
        io.mockk.verify(exactly = 0) { ben.form1Enabled = any() }
    }
}
