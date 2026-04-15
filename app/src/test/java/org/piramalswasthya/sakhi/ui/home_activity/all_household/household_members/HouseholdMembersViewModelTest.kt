package org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdMembersViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: HouseholdMembersViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "hhId" to 1L
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { benRepo.getBenBasicListFromHousehold(any()) } returns flowOf(emptyList())
        viewModel = HouseholdMembersViewModel(savedStateHandle, benRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `hhId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.hhId)
    }

    @Test
    fun `isFromDisease is 0`() {
        assertEquals(0, viewModel.isFromDisease)
    }

    @Test
    fun `diseaseType is No`() {
        assertEquals("No", viewModel.diseaseType)
    }

    @Test
    fun `benList is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `benListWithChildren is not null`() {
        assertNotNull(viewModel.benListWithChildren)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `initial benId is null`() {
        assertNull(viewModel.benId.value)
    }

    @Test
    fun `initial benRegId is null`() {
        assertNull(viewModel.benRegId.value)
    }

    // =====================================================
    // fetchAbha() Tests
    // =====================================================

    @Test
    fun `fetchAbha sets benId`() = runTest {
        coEvery { benRepo.getBenFromId(42L) } returns null
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.benId.value)
    }

    @Test
    fun `fetchAbha resets abha to null`() = runTest {
        coEvery { benRepo.getBenFromId(42L) } returns null
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        assertNull(viewModel.abha.value)
    }

    // =====================================================
    // resetBenRegId() Tests
    // =====================================================

    @Test
    fun `resetBenRegId sets benRegId to null`() {
        viewModel.resetBenRegId()
        assertNull(viewModel.benRegId.value)
    }

    // =====================================================
    // isHOF() Tests
    // =====================================================

    @Test
    fun `isHOF returns true when ben is head of family`() = runTest {
        val benDomain = mockk<org.piramalswasthya.sakhi.model.BenBasicDomain>(relaxed = true)
        every { benDomain.hhId } returns 1L
        every { benDomain.benId } returns 100L

        val hofBen = mockk<BenRegCache>(relaxed = true)
        every { hofBen.familyHeadRelationPosition } returns 19
        every { hofBen.beneficiaryId } returns 100L

        coEvery { benRepo.getBenListFromHousehold(1L) } returns listOf(hofBen)

        val result = viewModel.isHOF(benDomain)
        assertTrue(result)
    }

    @Test
    fun `isHOF returns false when ben is not head of family`() = runTest {
        val benDomain = mockk<org.piramalswasthya.sakhi.model.BenBasicDomain>(relaxed = true)
        every { benDomain.hhId } returns 1L
        every { benDomain.benId } returns 200L

        val hofBen = mockk<BenRegCache>(relaxed = true)
        every { hofBen.familyHeadRelationPosition } returns 19
        every { hofBen.beneficiaryId } returns 100L

        coEvery { benRepo.getBenListFromHousehold(1L) } returns listOf(hofBen)

        val result = viewModel.isHOF(benDomain)
        assertFalse(result)
    }

    // =====================================================
    // canDeleteHoF() Tests
    // =====================================================

    @Test
    fun `canDeleteHoF returns true when HoF is only member`() = runTest {
        val hofBen = mockk<BenRegCache>(relaxed = true)
        every { hofBen.familyHeadRelationPosition } returns 19
        every { hofBen.beneficiaryId } returns 100L

        coEvery { benRepo.getBenListFromHousehold(1L) } returns listOf(hofBen)

        val result = viewModel.canDeleteHoF(1L)
        assertTrue(result)
    }

    @Test
    fun `canDeleteHoF returns false when other members exist`() = runTest {
        val hofBen = mockk<BenRegCache>(relaxed = true)
        every { hofBen.familyHeadRelationPosition } returns 19
        every { hofBen.beneficiaryId } returns 100L

        val otherBen = mockk<BenRegCache>(relaxed = true)
        every { otherBen.familyHeadRelationPosition } returns 1
        every { otherBen.beneficiaryId } returns 200L

        coEvery { benRepo.getBenListFromHousehold(1L) } returns listOf(hofBen, otherBen)

        val result = viewModel.canDeleteHoF(1L)
        assertFalse(result)
    }
}
