package org.piramalswasthya.sakhi.ui.home_activity.all_household

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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import io.mockk.mockk
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class AllHouseholdViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var householdRepo: HouseholdRepo
    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: AllHouseholdViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.hhList } returns flowOf(emptyList())
        viewModel = AllHouseholdViewModel(householdRepo, recordsRepo, benRepo, preferenceDao)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
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
    fun `navigateToNewHouseholdRegistration with delete false does not delete`() = runTest {
        viewModel.navigateToNewHouseholdRegistration(false)
        advanceUntilIdle()
        coVerify(exactly = 0) { householdRepo.deleteHouseholdDraft() }
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
    fun `setSelectedHouseholdId updates selectedHouseholdId`() {
        coEvery { householdRepo.getRecord(42L) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(42L) } returns emptyList()
        viewModel.setSelectedHouseholdId(42L)
        // Verify the synchronous assignment happened immediately
        assertEquals(42L, viewModel.selectedHouseholdId)
    }

    @Test
    fun `resetSelectedHouseholdId resets to 0 and clears ben list`() {
        viewModel.resetSelectedHouseholdId()
        assertEquals(0L, viewModel.selectedHouseholdId)
        assertEquals(0, viewModel.householdBenList.size)
    }

    // =====================================================
    // Extended checkDraft() Tests
    // =====================================================

    @Test
    fun `checkDraft called twice does not throw`() = runTest {
        coEvery { householdRepo.getDraftRecord() } returns null
        viewModel.checkDraft()
        viewModel.checkDraft()
        advanceUntilIdle()
        assertFalse(viewModel.hasDraft.value!!)
    }

    @Test
    fun `checkDraft toggles from true to false when draft removed`() = runTest {
        coEvery { householdRepo.getDraftRecord() } returns mockk(relaxed = true)
        viewModel.checkDraft()
        advanceUntilIdle()
        assertEquals(true, viewModel.hasDraft.value)

        coEvery { householdRepo.getDraftRecord() } returns null
        viewModel.checkDraft()
        advanceUntilIdle()
        assertFalse(viewModel.hasDraft.value!!)
    }

    // =====================================================
    // Extended filterText() Tests
    // =====================================================

    @Test
    fun `filterText with special characters does not throw`() = runTest {
        viewModel.filterText("!@#\$%")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with unicode does not throw`() = runTest {
        viewModel.filterText("कुमार")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with long string does not throw`() = runTest {
        viewModel.filterText("a".repeat(200))
        advanceUntilIdle()
    }

    @Test
    fun `filterText called multiple times does not throw`() = runTest {
        viewModel.filterText("first")
        viewModel.filterText("second")
        viewModel.filterText("third")
        advanceUntilIdle()
    }

    // =====================================================
    // Extended setSelectedHouseholdId() Tests
    // =====================================================

    @Test
    fun `setSelectedHouseholdId with 0 works`() {
        coEvery { householdRepo.getRecord(0L) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(0L) } returns emptyList()
        viewModel.setSelectedHouseholdId(0L)
        assertEquals(0L, viewModel.selectedHouseholdId)
    }

    @Test
    fun `setSelectedHouseholdId with large id works`() {
        coEvery { householdRepo.getRecord(999999L) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(999999L) } returns emptyList()
        viewModel.setSelectedHouseholdId(999999L)
        assertEquals(999999L, viewModel.selectedHouseholdId)
    }

    @Test
    fun `setSelectedHouseholdId called twice updates id`() {
        coEvery { householdRepo.getRecord(any()) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(any()) } returns emptyList()
        viewModel.setSelectedHouseholdId(10L)
        assertEquals(10L, viewModel.selectedHouseholdId)
        viewModel.setSelectedHouseholdId(20L)
        assertEquals(20L, viewModel.selectedHouseholdId)
    }

    @Test
    fun `setSelectedHouseholdId then resetSelectedHouseholdId`() {
        coEvery { householdRepo.getRecord(42L) } returns null
        coEvery { householdRepo.getAllBenOfHousehold(42L) } returns emptyList()
        viewModel.setSelectedHouseholdId(42L)
        assertEquals(42L, viewModel.selectedHouseholdId)
        viewModel.resetSelectedHouseholdId()
        assertEquals(0L, viewModel.selectedHouseholdId)
    }

    // =====================================================
    // Extended navigateToNewHouseholdRegistration() Tests
    // =====================================================

    @Test
    fun `navigateToNewHouseholdRegistration called twice`() = runTest {
        viewModel.navigateToNewHouseholdRegistration(false)
        advanceUntilIdle()
        assertEquals(true, viewModel.navigateToNewHouseholdRegistration.value)
        viewModel.navigateToNewHouseholdRegistrationCompleted()
        assertFalse(viewModel.navigateToNewHouseholdRegistration.value!!)
        viewModel.navigateToNewHouseholdRegistration(false)
        advanceUntilIdle()
        assertEquals(true, viewModel.navigateToNewHouseholdRegistration.value)
    }

    // =====================================================
    // Multiple Instance Tests
    // =====================================================

    @Test
    fun `multiple instances are independent`() {
        val vm1 = AllHouseholdViewModel(householdRepo, recordsRepo, benRepo, preferenceDao)
        val vm2 = AllHouseholdViewModel(householdRepo, recordsRepo, benRepo, preferenceDao)
        assertNotNull(vm1)
        assertNotNull(vm2)
    }

    @Test
    fun `householdList flow is consistent`() {
        val h1 = viewModel.householdList
        val h2 = viewModel.householdList
        assertEquals(h1, h2)
    }
}
