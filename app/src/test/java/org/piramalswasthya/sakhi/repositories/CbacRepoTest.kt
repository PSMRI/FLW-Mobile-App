package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.content.res.Resources
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class CbacRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var resources: Resources
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var prefDao: PreferenceDao
    @MockK private lateinit var referalDao: NcdReferalDao
    @MockK private lateinit var cbacDao: CbacDao

    private lateinit var cbacRepo: CbacRepo

    @Before
    override fun setUp() {
        super.setUp()
        every { context.resources } returns resources
        every { database.cbacDao } returns cbacDao

        cbacRepo = CbacRepo(context, database, userRepo, amritApiService, prefDao, referalDao)
    }

    // =====================================================
    // saveCbacData() — TB Tracing Derived Fields
    // =====================================================

    @Test
    fun `saveCbacData sets tracing to 1 when suffering from TB`() = runTest {
        val cbac = createCbacCache(sufferingTb = 1, antiTbDrugs = 0)
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("1", cbac.cbac_tracing_all_fm)
    }

    @Test
    fun `saveCbacData sets tracing to 1 when on anti-TB drugs`() = runTest {
        val cbac = createCbacCache(sufferingTb = 0, antiTbDrugs = 1)
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("1", cbac.cbac_tracing_all_fm)
    }

    @Test
    fun `saveCbacData sets tracing to 0 when neither TB condition`() = runTest {
        val cbac = createCbacCache(sufferingTb = 0, antiTbDrugs = 0)
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("0", cbac.cbac_tracing_all_fm)
    }

    // =====================================================
    // saveCbacData() — Sputum Collection Derived Fields
    // =====================================================

    @Test
    fun `saveCbacData sets sputum to 1 when any TB symptom positive`() = runTest {
        val cbac = createCbacCache(
            tbHistory = 1, coughing = 0, bloodSputum = 0,
            fiveMore = 0, loseWeight = 0, nightSweats = 0
        )
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("1", cbac.cbac_sputemcollection)
    }

    @Test
    fun `saveCbacData sets sputum to 1 when coughing positive`() = runTest {
        val cbac = createCbacCache(
            tbHistory = 0, coughing = 1, bloodSputum = 0,
            fiveMore = 0, loseWeight = 0, nightSweats = 0
        )
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("1", cbac.cbac_sputemcollection)
    }

    @Test
    fun `saveCbacData sets sputum to 0 when no TB symptoms`() = runTest {
        val cbac = createCbacCache(
            tbHistory = 0, coughing = 0, bloodSputum = 0,
            fiveMore = 0, loseWeight = 0, nightSweats = 0
        )
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("0", cbac.cbac_sputemcollection)
    }

    @Test
    fun `saveCbacData sets sputum to 1 when multiple symptoms positive`() = runTest {
        val cbac = createCbacCache(
            tbHistory = 1, coughing = 1, bloodSputum = 1,
            fiveMore = 1, loseWeight = 1, nightSweats = 1
        )
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("1", cbac.cbac_sputemcollection)
    }

    // =====================================================
    // saveCbacData() — General Persistence
    // =====================================================

    @Test
    fun `saveCbacData sets Processed to N and serverUpdatedStatus to 0`() = runTest {
        val cbac = createCbacCache()
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("N", cbac.Processed)
        assertEquals(0, cbac.serverUpdatedStatus)
    }

    @Test
    fun `saveCbacData sets createdBy from logged in user`() = runTest {
        val cbac = createCbacCache()
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser(userName = "asha_worker_1")
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        cbacRepo.saveCbacData(cbac, ben)

        assertEquals("asha_worker_1", cbac.createdBy)
    }

    @Test
    fun `saveCbacData returns true on success`() = runTest {
        val cbac = createCbacCache()
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } returns Unit
        coEvery { database.benDao.updateBen(any()) } returns Unit

        val result = cbacRepo.saveCbacData(cbac, ben)

        assertTrue(result)
        coVerify { cbacDao.upsert(cbac) }
        coVerify { database.benDao.updateBen(ben) }
    }

    @Test
    fun `saveCbacData returns false when exception occurs`() = runTest {
        val cbac = createCbacCache()
        val ben = mockk<BenRegCache>(relaxed = true)
        val user = createMockUser()
        every { prefDao.getLoggedInUser() } returns user
        coEvery { cbacDao.upsert(any()) } throws RuntimeException("DB error")

        val result = cbacRepo.saveCbacData(cbac, ben)

        assertFalse(result)
    }

    @Test
    fun `saveCbacData throws when no user logged in`() = runTest {
        val cbac = createCbacCache()
        val ben = mockk<BenRegCache>(relaxed = true)
        every { prefDao.getLoggedInUser() } returns null

        try {
            cbacRepo.saveCbacData(cbac, ben)
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    // =====================================================
    // getCbacCacheFromId() Tests
    // =====================================================

    @Test
    fun `getCbacCacheFromId returns cbac when found`() = runTest {
        val cbac = mockk<CbacCache>()
        coEvery { cbacDao.getCbacFromBenId(42) } returns cbac

        val result = cbacRepo.getCbacCacheFromId(42)

        assertNotNull(result)
    }

    @Test
    fun `getCbacCacheFromId throws when not found`() = runTest {
        coEvery { cbacDao.getCbacFromBenId(999) } returns null

        try {
            cbacRepo.getCbacCacheFromId(999)
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertEquals("No CBAC entry found!", e.message)
        }
    }

    // =====================================================
    // getLastFilledCbac() Tests
    // =====================================================

    @Test
    fun `getLastFilledCbac returns cbac when exists`() = runTest {
        val cbac = mockk<CbacCache>()
        coEvery { cbacDao.getLastFilledCbacFromBenId(100L) } returns cbac

        val result = cbacRepo.getLastFilledCbac(100L)

        assertNotNull(result)
    }

    @Test
    fun `getLastFilledCbac returns null when no cbac exists`() = runTest {
        coEvery { cbacDao.getLastFilledCbacFromBenId(100L) } returns null

        val result = cbacRepo.getLastFilledCbac(100L)

        assertEquals(null, result)
    }

    // =====================================================
    // updateReferStatus() Tests
    // =====================================================

    @Test
    fun `updateReferStatus calls dao with correct params`() = runTest {
        coEvery { cbacDao.updateReferralStatus(100L, true) } returns Unit

        cbacRepo.updateReferStatus(100L, true)

        coVerify { cbacDao.updateReferralStatus(100L, true) }
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun createMockUser(
        userName: String = "testuser",
        userId: Int = 1,
        serviceMapId: Int = 10,
        vanId: Int = 4
    ): User {
        return mockk<User>(relaxed = true).also {
            every { it.userName } returns userName
            every { it.userId } returns userId
            every { it.serviceMapId } returns serviceMapId
            every { it.vanId } returns vanId
            every { it.password } returns "password"
        }
    }

    private fun createCbacCache(
        benId: Long = 1L,
        ashaId: Int = 1,
        sufferingTb: Int = 0,
        antiTbDrugs: Int = 0,
        tbHistory: Int = 0,
        coughing: Int = 0,
        bloodSputum: Int = 0,
        fiveMore: Int = 0,
        loseWeight: Int = 0,
        nightSweats: Int = 0
    ): CbacCache {
        return CbacCache(
            benId = benId,
            ashaId = ashaId,
            syncState = SyncState.UNSYNCED,
            cbac_sufferingtb_pos = sufferingTb,
            cbac_antitbdrugs_pos = antiTbDrugs,
            cbac_tbhistory_pos = tbHistory,
            cbac_coughing_pos = coughing,
            cbac_bloodsputum_pos = bloodSputum,
            cbac_fivermore_pos = fiveMore,
            cbac_loseofweight_pos = loseWeight,
            cbac_nightsweats_pos = nightSweats
        )
    }
}
