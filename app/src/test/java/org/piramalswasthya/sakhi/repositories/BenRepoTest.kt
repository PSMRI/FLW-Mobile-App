package org.piramalswasthya.sakhi.repositories

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.GeneralOpdDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class BenRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Application
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var householdDao: HouseholdDao
    @MockK private lateinit var benIdGenDao: BeneficiaryIdsAvailDao
    @MockK private lateinit var infantRegRepo: InfantRegRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var generalOpdDao: GeneralOpdDao
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var formResponseJsonDao: FormResponseJsonDao
    @MockK private lateinit var cufyFormResponseJsonDao: CUFYFormResponseJsonDao

    private lateinit var repo: BenRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = BenRepo(
            context, benDao, householdDao, benIdGenDao, infantRegRepo,
            preferenceDao, userRepo, generalOpdDao, tmcNetworkApiService,
            formResponseJsonDao, cufyFormResponseJsonDao
        )
    }

    // =====================================================
    // updateBenToSync() Tests
    // =====================================================

    @Test
    fun `updateBenToSync calls dao with correct params`() = runTest {
        coEvery { benDao.updateBenToSync(any(), any(), any(), any()) } returns Unit

        repo.updateBenToSync(100L, SyncState.UNSYNCED)

        coVerify { benDao.updateBenToSync(100L, SyncState.UNSYNCED, "U", 2) }
    }

    // =====================================================
    // updateHousehold() Tests
    // =====================================================

    @Test
    fun `updateHousehold calls dao with correct params`() = runTest {
        coEvery { benDao.updateHofSpouseAdded(any(), any(), any(), any()) } returns Unit

        repo.updateHousehold(200L, SyncState.UNSYNCED)

        coVerify { benDao.updateHofSpouseAdded(200L, SyncState.UNSYNCED, "U", 2) }
    }

    // =====================================================
    // getHousehold() Tests
    // =====================================================

    @Test
    fun `getHousehold returns record when exists`() = runTest {
        val household = mockk<HouseholdCache>()
        coEvery { householdDao.getHousehold(100L) } returns household

        val result = repo.getHousehold(100L)

        assertNotNull(result)
        assertEquals(household, result)
    }

    @Test
    fun `getHousehold returns null when not exists`() = runTest {
        coEvery { householdDao.getHousehold(999L) } returns null

        val result = repo.getHousehold(999L)

        assertNull(result)
    }

    // =====================================================
    // getBenFromId() Tests
    // =====================================================

    @Test
    fun `getBenFromId returns ben when exists`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L) } returns ben

        val result = repo.getBenFromId(100L)

        assertNotNull(result)
        assertEquals(ben, result)
    }

    @Test
    fun `getBenFromId returns null when not exists`() = runTest {
        coEvery { benDao.getBen(999L) } returns null

        val result = repo.getBenFromId(999L)

        assertNull(result)
    }

    // =====================================================
    // getBeneficiaryRecord() Tests
    // =====================================================

    @Test
    fun `getBeneficiaryRecord returns record when exists`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L, 200L) } returns ben

        val result = repo.getBeneficiaryRecord(100L, 200L)

        assertNotNull(result)
    }

    @Test
    fun `getBeneficiaryRecord returns null when not exists`() = runTest {
        coEvery { benDao.getBen(999L, 999L) } returns null

        val result = repo.getBeneficiaryRecord(999L, 999L)

        assertNull(result)
    }

    // =====================================================
    // Companion Object Tests
    // =====================================================

    @Test
    fun `getCurrentDate returns formatted date string`() {
        val result = BenRepo.getCurrentDate(1577817001000L)

        assertTrue(result.contains("T"))
        assertTrue(result.endsWith(".000Z"))
    }

    @Test
    fun `getLongFromDateStr parses valid date`() {
        val dateStr = "2026-01-15T10:30:00.000Z"
        val result = BenRepo.getLongFromDateStr(dateStr)

        assertTrue(result > 0)
    }

    @Test
    fun `getLongFromDateStr throws for invalid date`() {
        try {
            BenRepo.getLongFromDateStr("not-a-date")
            assert(false) { "Should have thrown" }
        } catch (e: Exception) {
            // Expected
        }
    }

    // =====================================================
    // processNewBen() Tests
    // =====================================================

    @Test
    fun `processNewBen throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewBen()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }
}
