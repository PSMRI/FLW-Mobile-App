package org.piramalswasthya.sakhi.ui.home_activity.disease_control

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import io.mockk.mockk
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class AllHouseHoldDiseaseControlViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var householdRepo: HouseholdRepo
    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: AllHouseHoldDiseaseControlViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "diseaseType" to "AES"
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.hhList } returns flowOf(emptyList())
        viewModel = AllHouseHoldDiseaseControlViewModel(savedStateHandle, householdRepo, recordsRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `diseaseType is set from SavedStateHandle`() {
        assertEquals("AES", viewModel.diseaseType)
    }

    @Test
    fun `initial hasDraft is false`() {
        assertFalse(viewModel.hasDraft.value!!)
    }

    @Test
    fun `initial navigateToNewHouseholdRegistration is false`() {
        assertFalse(viewModel.navigateToNewHouseholdRegistration.value!!)
    }

    @Test
    fun `householdList is not null`() {
        assertNotNull(viewModel.householdList)
    }

    @Test
    fun `initial selectedHouseholdId is 0`() {
        assertEquals(0L, viewModel.selectedHouseholdId)
    }

    @Test
    fun `initial householdBenList is empty`() {
        assertEquals(0, viewModel.householdBenList.size)
    }

    @Test
    fun `initial selectedHousehold is null`() {
        assertNull(viewModel.selectedHousehold)
    }

    // =====================================================
    // checkDraft() Tests
    // =====================================================

    @Test
    fun `checkDraft sets hasDraft true when draft exists`() = runTest {
        coEvery { householdRepo.getDraftRecord() } returns mockk(relaxed = true)
        viewModel.checkDraft()
        advanceUntilIdle()
        assertEquals(true, viewModel.hasDraft.value)
    }

    @Test
    fun `checkDraft sets hasDraft false when no draft`() = runTest {
        coEvery { householdRepo.getDraftRecord() } returns null
        viewModel.checkDraft()
        advanceUntilIdle()
        assertFalse(viewModel.hasDraft.value!!)
    }

    // =====================================================
    // navigateToNewHouseholdRegistration() Tests
    // =====================================================

    @Test
    fun `navigateToNewHouseholdRegistration with delete true calls deleteHouseholdDraft`() = runTest {
        coEvery { householdRepo.deleteHouseholdDraft() } returns Unit
        viewModel.navigateToNewHouseholdRegistration(true)
        advanceUntilIdle()
        coVerify { householdRepo.deleteHouseholdDraft() }
        assertEquals(true, viewModel.navigateToNewHouseholdRegistration.value)
    }

    @Test
    fun `navigateToNewHouseholdRegistrationCompleted resets flag`() {
        viewModel.navigateToNewHouseholdRegistrationCompleted()
        assertFalse(viewModel.navigateToNewHouseholdRegistration.value!!)
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
    // setSelectedHouseholdId() / resetSelectedHouseholdId() Tests
    // =====================================================

    @Test
    fun `setSelectedHouseholdId updates selectedHouseholdId`() = runTest {
        coEvery { householdRepo.getRecord(42L) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(42L) } returns emptyList()
        viewModel.setSelectedHouseholdId(42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.selectedHouseholdId)
    }

    @Test
    fun `resetSelectedHouseholdId resets to 0 and clears ben list`() {
        viewModel.resetSelectedHouseholdId()
        assertEquals(0L, viewModel.selectedHouseholdId)
        assertEquals(0, viewModel.householdBenList.size)
    }
}
