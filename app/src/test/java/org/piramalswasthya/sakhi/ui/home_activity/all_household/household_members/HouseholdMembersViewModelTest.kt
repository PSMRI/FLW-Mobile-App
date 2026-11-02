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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdMembersViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: HouseholdMembersViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "hhId" to 1L,
        "fromDisease" to 0,
        "diseaseType" to "No"
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

    @Test
    fun `fetchAbha sets abha and updates record when health id found`() = runTest {
        val cache = benRegCache()
        coEvery { benRepo.getBenFromId(50L) } returns cache
        coEvery { benRepo.getBeneficiaryWithId(cache.benRegId) } returns
            org.piramalswasthya.sakhi.network.BenHealthDetails(
                benHealthID = 1,
                healthIdNumber = "12-3456-7890-1234",
                beneficiaryRegID = cache.benRegId,
                healthId = "test@sbx",
                isNewAbha = true
            )
        coEvery { benRepo.updateRecord(any()) } returns Unit

        viewModel.fetchAbha(50L)
        advanceUntilIdle()

        assertEquals("12-3456-7890-1234", viewModel.abha.value)
        assertTrue(cache.isNewAbha)
        coVerify { benRepo.updateRecord(cache) }
    }

    @Test
    fun `fetchAbha sets benRegId when health id not found`() = runTest {
        val cache = benRegCache()
        coEvery { benRepo.getBenFromId(51L) } returns cache
        coEvery { benRepo.getBeneficiaryWithId(cache.benRegId) } returns null

        viewModel.fetchAbha(51L)
        advanceUntilIdle()

        assertEquals(cache.benRegId, viewModel.benRegId.value)
        assertNull(viewModel.abha.value)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
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

    // =====================================================
    // deActivateBeneficiary() Tests
    // =====================================================

    private fun locationRecord(): LocationRecord {
        val entity = LocationEntity(id = 1, name = "test")
        return LocationRecord(
            country = entity, state = entity, district = entity, block = entity, village = entity
        )
    }

    private fun benBasicDomain(benId: Long = 1L, hhId: Long = 1L, isDeactivate: Boolean = false) =
        BenBasicDomain(
            benId = benId,
            hhId = hhId,
            reproductiveStatusId = 0,
            regDate = "01-01-2026",
            benName = "Test",
            gender = "Female",
            dob = System.currentTimeMillis() - 1000L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false,
            isDeactivate = isDeactivate
        )

    private fun benRegCache(processed: String? = "N", isDeactivate: Boolean = false) = BenRegCache(
        householdId = 1L,
        beneficiaryId = 1L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 1,
        isKid = false,
        isAdult = true,
        locationRecord = locationRecord(),
        syncState = SyncState.SYNCED,
        isDraft = false,
        processed = processed,
        isDeactivate = isDeactivate
    )

    @Test
    fun `deActivateBeneficiary does nothing to repo when benRegCache not found`() = runTest {
        val domain = benBasicDomain(benId = 10L, isDeactivate = false)
        coEvery { benRepo.getBenFromId(10L) } returns null

        viewModel.deActivateBeneficiary(domain)
        advanceUntilIdle()

        assertTrue(domain.isDeactivate)
        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
        coVerify(exactly = 0) { benRepo.deactivateBeneficiary(any()) }
    }

    @Test
    fun `deActivateBeneficiary updates record without touching sync state when processed is N`() = runTest {
        val domain = benBasicDomain(benId = 11L, isDeactivate = false)
        val cache = benRegCache(processed = "N", isDeactivate = false)
        coEvery { benRepo.getBenFromId(11L) } returns cache
        coEvery { benRepo.deactivateBeneficiary(any()) } returns true

        viewModel.deActivateBeneficiary(domain)
        advanceUntilIdle()

        assertTrue(domain.isDeactivate)
        assertTrue(cache.isDeactivate)
        assertEquals("N", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertEquals(0, cache.serverUpdatedStatus)
        coVerify(exactly = 1) { benRepo.updateRecord(cache) }
        coVerify(exactly = 1) { benRepo.deactivateBeneficiary(listOf(cache)) }
    }

    @Test
    fun `deActivateBeneficiary marks record unsynced and bumps status when processed is not N`() = runTest {
        val domain = benBasicDomain(benId = 12L, isDeactivate = false)
        val cache = benRegCache(processed = "P", isDeactivate = false)
        coEvery { benRepo.getBenFromId(12L) } returns cache
        coEvery { benRepo.deactivateBeneficiary(any()) } returns true

        viewModel.deActivateBeneficiary(domain)
        advanceUntilIdle()

        assertTrue(domain.isDeactivate)
        assertTrue(cache.isDeactivate)
        assertEquals("U", cache.processed)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
        assertEquals(2, cache.serverUpdatedStatus)
        coVerify(exactly = 1) { benRepo.updateRecord(cache) }
        coVerify(exactly = 1) { benRepo.deactivateBeneficiary(listOf(cache)) }
    }

    @Test
    fun `deActivateBeneficiary toggles an already-deactivated beneficiary back to active`() = runTest {
        val domain = benBasicDomain(benId = 13L, isDeactivate = true)
        val cache = benRegCache(processed = null, isDeactivate = true)
        coEvery { benRepo.getBenFromId(13L) } returns cache
        coEvery { benRepo.deactivateBeneficiary(any()) } returns false

        viewModel.deActivateBeneficiary(domain)
        advanceUntilIdle()

        assertFalse(domain.isDeactivate)
        assertFalse(cache.isDeactivate)
        assertEquals("U", cache.processed)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
        assertEquals(2, cache.serverUpdatedStatus)
        coVerify(exactly = 1) { benRepo.updateRecord(cache) }
        coVerify(exactly = 1) { benRepo.deactivateBeneficiary(listOf(cache)) }
    }
}
