package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.list

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.network.BenHealthDetails
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class BenListCHOViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: BenListCHOViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.getBenListCHO() } returns flowOf(emptyList())
        viewModel = BenListCHOViewModel(recordsRepo)
        viewModel.benRepo = benRepo
    }

    private fun locationRecord(): LocationRecord {
        val entity = LocationEntity(id = 1, name = "test")
        return LocationRecord(country = entity, state = entity, district = entity, block = entity, village = entity)
    }

    private fun benRegCache() = BenRegCache(
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
        processed = "N",
        isDeactivate = false
    )

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
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
    // resetBenRegId() Tests
    // =====================================================

    @Test
    fun `resetBenRegId sets benRegId to null`() {
        viewModel.resetBenRegId()
        assertNull(viewModel.benRegId.value)
    }

    // =====================================================
    // fetchAbha() Tests
    // =====================================================

    @Test
    fun `fetchAbha sets benId and does nothing when ben not found`() = runTest {
        coEvery { benRepo.getBenFromId(42L) } returns null
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.benId.value)
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `fetchAbha sets abha and updates record when health id found`() = runTest {
        val cache = benRegCache()
        coEvery { benRepo.getBenFromId(50L) } returns cache
        coEvery { benRepo.getBeneficiaryWithId(cache.benRegId) } returns BenHealthDetails(
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
    }

    // =====================================================
    // benList emission Tests
    // =====================================================

    @Test
    fun `benList emits unfiltered repository list by default`() = runTest {
        val ben = io.mockk.mockk<org.piramalswasthya.sakhi.model.BenBasicDomain>(relaxed = true)
        every { recordsRepo.getBenListCHO() } returns flowOf(listOf(ben))
        val vm = BenListCHOViewModel(recordsRepo)
        val list = vm.benList.first()
        assertEquals(1, list.size)
    }
}
