package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var repository: CUFYFormRepository
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: CUFYListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getString(any()) } returns "Check SAM"
        every { pref.getCurrentLanguage() } returns Languages.ENGLISH
        every { recordsRepo.childFilteredList } returns flowOf(emptyList())
        viewModel = CUFYListViewModel(recordsRepo, repository, pref, context)
    }

    private fun ben(benId: Long): BenBasicDomain {
        val b = mockk<BenBasicDomain>(relaxed = true)
        every { b.benId } returns benId
        return b
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `benListWithSamStatus is not null`() {
        assertNotNull(viewModel.benListWithSamStatus)
    }

    @Test
    fun `childOptionsList is not null`() {
        assertNotNull(viewModel.childOptionsList)
    }

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("abc")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // getSavedVisits() Tests
    // =====================================================

    @Test
    fun `getSavedVisits returns repository result`() = runTest {
        coEvery { repository.getSavedDataByFormId("cufy", 1L) } returns emptyList()
        val result = viewModel.getSavedVisits("cufy", 1L)
        assertTrue(result.isEmpty())
    }

    // =====================================================
    // getSamStatusForBeneficiary() Tests
    // =====================================================

    @Test
    fun `getSamStatusForBeneficiary returns status from repository`() = runTest {
        coEvery { repository.getCurrentSamStatus(1L) } returns "Normal"
        assertEquals("Normal", viewModel.getSamStatusForBeneficiary(1L))
    }

    @Test
    fun `getSamStatusForBeneficiary falls back to localized string on error`() = runTest {
        coEvery { repository.getCurrentSamStatus(2L) } throws RuntimeException("network error")
        assertEquals("Check SAM", viewModel.getSamStatusForBeneficiary(2L))
    }

    // =====================================================
    // updateSamStatusesOnce() / startSamStatusUpdates() Tests
    // =====================================================

    @Test
    fun `updateSamStatusesOnce populates benListWithSamStatus`() = runTest {
        every { recordsRepo.childFilteredList } returns flowOf(listOf(ben(1L)))
        coEvery { repository.getCurrentSamStatus(1L) } returns "Normal"
        val vm = CUFYListViewModel(recordsRepo, repository, pref, context)
        advanceUntilIdle()
        vm.updateSamStatusesOnce()
        advanceUntilIdle()
        assertEquals(1, vm.benListWithSamStatus.value.size)
        assertEquals("Normal", vm.benListWithSamStatus.value[0].samStatus)
    }

    @Test
    fun `updateSamStatusesOnce falls back to localized string when repo throws`() = runTest {
        every { recordsRepo.childFilteredList } returns flowOf(listOf(ben(1L)))
        coEvery { repository.getCurrentSamStatus(1L) } throws RuntimeException("boom")
        val vm = CUFYListViewModel(recordsRepo, repository, pref, context)
        advanceUntilIdle()
        vm.updateSamStatusesOnce()
        advanceUntilIdle()
        assertEquals("Check SAM", vm.benListWithSamStatus.value[0].samStatus)
    }

    @Test
    fun `startSamStatusUpdates collects benList and populates sam status`() = runTest {
        every { recordsRepo.childFilteredList } returns flowOf(listOf(ben(5L)))
        coEvery { repository.getCurrentSamStatus(5L) } returns "MAM"
        val vm = CUFYListViewModel(recordsRepo, repository, pref, context)
        vm.startSamStatusUpdates()
        advanceUntilIdle()
        assertEquals(1, vm.benListWithSamStatus.value.size)
        assertEquals("MAM", vm.benListWithSamStatus.value[0].samStatus)
    }
}
