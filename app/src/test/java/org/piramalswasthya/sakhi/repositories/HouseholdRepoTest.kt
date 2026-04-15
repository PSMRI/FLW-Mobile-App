package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var householdDao: HouseholdDao
    @MockK private lateinit var benDao: BenDao

    private lateinit var repo: HouseholdRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = HouseholdRepo(householdDao, benDao)
    }

    // =====================================================
    // getDraftRecord() Tests
    // =====================================================

    @Test
    fun `getDraftRecord returns draft when exists`() = runTest {
        val draft = mockk<HouseholdCache>()
        coEvery { householdDao.getDraftHousehold() } returns draft

        val result = repo.getDraftRecord()

        assertNotNull(result)
        assertEquals(draft, result)
    }

    @Test
    fun `getDraftRecord returns null when no draft`() = runTest {
        coEvery { householdDao.getDraftHousehold() } returns null

        val result = repo.getDraftRecord()

        assertNull(result)
    }

    // =====================================================
    // getRecord() Tests
    // =====================================================

    @Test
    fun `getRecord returns household by id`() = runTest {
        val household = mockk<HouseholdCache>()
        coEvery { householdDao.getHousehold(100L) } returns household

        val result = repo.getRecord(100L)

        assertNotNull(result)
        coVerify { householdDao.getHousehold(100L) }
    }

    @Test
    fun `getRecord returns null for non existent id`() = runTest {
        coEvery { householdDao.getHousehold(999L) } returns null

        val result = repo.getRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // persistRecord() Tests
    // =====================================================

    @Test
    fun `persistRecord inserts when household does not exist`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        coEvery { household.householdId } returns 100L
        coEvery { householdDao.getHousehold(100L) } returns null
        coEvery { householdDao.upsert(household) } returns Unit

        repo.persistRecord(household)

        coVerify { householdDao.upsert(household) }
        coVerify(exactly = 0) { householdDao.update(household) }
    }

    @Test
    fun `persistRecord updates when household already exists`() = runTest {
        val existing = mockk<HouseholdCache>(relaxed = true)
        val household = mockk<HouseholdCache>(relaxed = true)
        coEvery { household.householdId } returns 100L
        coEvery { householdDao.getHousehold(100L) } returns existing
        coEvery { householdDao.update(household) } returns Unit

        repo.persistRecord(household)

        coVerify { householdDao.update(household) }
        coVerify(exactly = 0) { householdDao.upsert(household) }
    }

    @Test
    fun `persistRecord with null does nothing`() = runTest {
        repo.persistRecord(null)

        coVerify(exactly = 0) { householdDao.upsert(any()) }
        coVerify(exactly = 0) { householdDao.update(any<HouseholdCache>()) }
    }

    // =====================================================
    // getAllBenOfHousehold() Tests
    // =====================================================

    @Test
    fun `getAllBenOfHousehold returns beneficiaries`() = runTest {
        val benList = listOf(mockk<BenRegCache>(), mockk<BenRegCache>(), mockk<BenRegCache>())
        coEvery { benDao.getAllBenForHousehold(100L) } returns benList

        val result = repo.getAllBenOfHousehold(100L)

        assertEquals(3, result.size)
    }

    @Test
    fun `getAllBenOfHousehold returns empty for non existent household`() = runTest {
        coEvery { benDao.getAllBenForHousehold(999L) } returns emptyList()

        val result = repo.getAllBenOfHousehold(999L)

        assertEquals(0, result.size)
    }

    // =====================================================
    // deleteHouseholdDraft() Tests
    // =====================================================

    @Test
    fun `deleteHouseholdDraft calls dao delete`() = runTest {
        coEvery { householdDao.deleteDraftHousehold() } returns Unit

        repo.deleteHouseholdDraft()

        coVerify(exactly = 1) { householdDao.deleteDraftHousehold() }
    }

    // =====================================================
    // updateHousehold() Tests
    // =====================================================

    @Test
    fun `updateHousehold calls dao update`() = runTest {
        val household = mockk<HouseholdCache>()
        coEvery { householdDao.update(household) } returns Unit

        repo.updateHousehold(household)

        coVerify(exactly = 1) { householdDao.update(household) }
    }

    // =====================================================
    // updateHouseholdToSync() Tests
    // =====================================================

    @Test
    fun `updateHouseholdToSync passes correct params`() = runTest {
        coEvery { householdDao.updateHouseholdToSync(100L, "U", 2) } returns Unit

        repo.updateHouseholdToSync(100L)

        coVerify { householdDao.updateHouseholdToSync(100L, "U", 2) }
    }
}
