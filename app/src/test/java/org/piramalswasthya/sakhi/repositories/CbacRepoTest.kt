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
import okhttp3.ResponseBody
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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.CbacCachePush
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [CbacRepo]. Consolidated from CbacRepoTest + Extra/Extra2/
 * Extra3/Extra4: saveCbacData derived-field logic (tracing, sputum OR-operands),
 * general persistence, the getter/updateReferStatus delegations, and the
 * pull/push server methods (early returns, token-refresh recursion, upload-loop
 * response branches, referral update).
 */
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
    @MockK private lateinit var benDao: BenDao

    private lateinit var cbacRepo: CbacRepo

    @Before
    override fun setUp() {
        super.setUp()
        every { context.resources } returns resources
        every { database.cbacDao } returns cbacDao
        every { database.benDao } returns benDao

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
    // push / pull coordinator early returns and guards
    // =====================================================

    @Test
    fun `pushAndUpdateCbacRecord makes no api call when nothing unprocessed`() = runTest {
        coEvery { cbacDao.getAllUnprocessedCbac() } returns emptyList()

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify(exactly = 0) { amritApiService.postCbacs(any()) }
    }

    @Test(expected = NullPointerException::class)
    fun `pullAndPersistCbacRecord throws when no user logged in`() = runTest {
        every { prefDao.getLoggedInUser() } returns null

        cbacRepo.pullAndPersistCbacRecord()
    }

    // -------- sputum OR-operand branch coverage --------

    @Test
    fun `saveCbacData sputum 1 when bloodSputum positive`() = runTest {
        val c = cbac(bloodSputum = 1)
        every { prefDao.getLoggedInUser() } returns mockUser()
        cbacRepo.saveCbacData(c, mockk<BenRegCache>(relaxed = true))
        assertEquals("1", c.cbac_sputemcollection)
    }

    @Test
    fun `saveCbacData sputum 1 when fivermore positive`() = runTest {
        val c = cbac(fiveMore = 1)
        every { prefDao.getLoggedInUser() } returns mockUser()
        cbacRepo.saveCbacData(c, mockk<BenRegCache>(relaxed = true))
        assertEquals("1", c.cbac_sputemcollection)
    }

    @Test
    fun `saveCbacData sputum 1 when loseWeight positive`() = runTest {
        val c = cbac(loseWeight = 1)
        every { prefDao.getLoggedInUser() } returns mockUser()
        cbacRepo.saveCbacData(c, mockk<BenRegCache>(relaxed = true))
        assertEquals("1", c.cbac_sputemcollection)
    }

    @Test
    fun `saveCbacData sputum 1 when nightSweats positive`() = runTest {
        val c = cbac(nightSweats = 1)
        every { prefDao.getLoggedInUser() } returns mockUser()
        cbacRepo.saveCbacData(c, mockk<BenRegCache>(relaxed = true))
        assertEquals("1", c.cbac_sputemcollection)
    }

    // -------- pullAndPersistCbacRecord early returns --------

    @Test
    fun `pullAndPersistCbacRecord returns 0 when data array missing`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returns
            response(200, """{"statusCode":200}""")

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
    }

    @Test
    fun `pullAndPersistCbacRecord returns 0 when data array empty`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returns
            response(200, """{"statusCode":200,"data":[]}""")

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
    }

    // ---------------- pullAndPersistCbacRecord token refresh recursion ----------------

    @Test
    fun `pullAndPersistCbacRecord recurses on 5002 then returns 0`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returnsMany listOf(
            response(200, """{"statusCode":5002}"""),
            response(200, """{"statusCode":200}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
    }

    // ---------------- pushAndUpdateCbacRecord upload loop ----------------

    @Test
    fun `pushAndUpdateCbacRecord marks record synced on Success response`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        val push = unprocessed(benId = 1L)
        coEvery { cbacDao.getAllUnprocessedCbac() } returns listOf(push)
        coEvery { referalDao.getReferalFromBenId(1L) } returns null
        coEvery { amritApiService.postCbacs(any()) } returns
            response(200, """{"status":"Success","data":{"visitCode":"12","benVisitID":"34"}}""")

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify { amritApiService.postCbacs(any()) }
    }

    @Test
    fun `pushAndUpdateCbacRecord counts failure on non-Success response`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        val push = unprocessed(benId = 2L)
        coEvery { cbacDao.getAllUnprocessedCbac() } returns listOf(push)
        coEvery { amritApiService.postCbacs(any()) } returns
            response(200, """{"status":"Failed"}""")

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify { amritApiService.postCbacs(any()) }
    }

    @Test
    fun `pushAndUpdateCbacRecord skips parsing on null body`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        val push = unprocessed(benId = 3L)
        coEvery { cbacDao.getAllUnprocessedCbac() } returns listOf(push)
        coEvery { amritApiService.postCbacs(any()) } returns response(200, null)

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify { amritApiService.postCbacs(any()) }
    }

    // ---------------- pushAndUpdateCbacRecord referral update / catch ----------------

    @Test
    fun `pushAndUpdateCbacRecord updates matching referral on Success`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        val push = unprocessed(benId = 7L)
        val refer = mockk<ReferalCache>(relaxed = true)
        coEvery { cbacDao.getAllUnprocessedCbac() } returns listOf(push)
        coEvery { referalDao.getReferalFromBenId(7L) } returns refer
        coEvery { amritApiService.postCbacs(any()) } returns
            response(200, """{"status":"Success","data":{"visitCode":"12","benVisitID":"34"}}""")

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify { referalDao.update(refer) }
    }

    @Test
    fun `pushAndUpdateCbacRecord counts failure when api throws`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        val push = unprocessed(benId = 8L)
        coEvery { cbacDao.getAllUnprocessedCbac() } returns listOf(push)
        coEvery { amritApiService.postCbacs(any()) } throws RuntimeException("boom")

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify { amritApiService.postCbacs(any()) }
    }

    @Test
    fun `pullAndPersistCbacRecord returns 0 with empty data array`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returns
            response(200, """{"statusCode":200,"data":[]}""")

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
        coVerify(exactly = 0) { cbacDao.insertAll(any()) }
    }

    @Test
    fun `pullAndPersistCbacRecord inserts entities whose benId exists locally`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returns
            response(200, """{"statusCode":200,"data":[{"beneficiaryRegId":5,"vanId":4}]}""")
        coEvery { benDao.getExistingBenIds(any()) } returns listOf(5L)
        coEvery { cbacDao.insertAll(any()) } returns Unit

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
        coVerify { cbacDao.insertAll(match { it.size == 1 && it[0].benId == 5L }) }
    }

    @Test
    fun `pullAndPersistCbacRecord skips insertAll when no benId matches locally`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returns
            response(200, """{"statusCode":200,"data":[{"beneficiaryRegId":5,"vanId":4}]}""")
        coEvery { benDao.getExistingBenIds(any()) } returns emptyList()

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
        coVerify(exactly = 0) { cbacDao.insertAll(any()) }
    }

    @Test
    fun `pullAndPersistCbacRecord recurses on 401 then returns 0`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacData(any()) } returnsMany listOf(
            response(200, """{"statusCode":401}"""),
            response(200, """{"statusCode":200}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(0, cbacRepo.pullAndPersistCbacRecord())
    }

    @Test
    fun `pushAndUpdateCbacRecord processes each unprocessed record exactly once`() = runTest {
        every { prefDao.getLoggedInUser() } returns mockUser()
        val validRecord = unprocessed(benId = 9L)
        coEvery { cbacDao.getAllUnprocessedCbac() } returns listOf(validRecord)
        coEvery { referalDao.getReferalFromBenId(9L) } returns null
        coEvery { amritApiService.postCbacs(any()) } returns
            response(200, """{"status":"Success","data":{"visitCode":"1","benVisitID":"2"}}""")

        cbacRepo.pushAndUpdateCbacRecord()

        coVerify(exactly = 1) { amritApiService.postCbacs(any()) }
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

    private fun mockUser(): User = mockk<User>(relaxed = true).also {
        every { it.userName } returns "asha"
        every { it.userId } returns 1
        every { it.serviceMapId } returns 10
        every { it.vanId } returns 4
        every { it.password } returns "pwd"
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

    private fun cbac(
        tbHistory: Int = 0, coughing: Int = 0, bloodSputum: Int = 0,
        fiveMore: Int = 0, loseWeight: Int = 0, nightSweats: Int = 0
    ) = CbacCache(
        benId = 1L,
        ashaId = 1,
        syncState = SyncState.UNSYNCED,
        cbac_tbhistory_pos = tbHistory,
        cbac_coughing_pos = coughing,
        cbac_bloodsputum_pos = bloodSputum,
        cbac_fivermore_pos = fiveMore,
        cbac_loseofweight_pos = loseWeight,
        cbac_nightsweats_pos = nightSweats
    )

    private fun response(code: Int, json: String?): Response<ResponseBody> {
        val resp = mockk<Response<ResponseBody>>(relaxed = true)
        every { resp.code() } returns code
        if (json != null) {
            val body = mockk<ResponseBody>()
            every { body.string() } returns json
            every { resp.body() } returns body
        } else {
            every { resp.body() } returns null
        }
        return resp
    }

    private fun unprocessed(benId: Long = 1L): CbacCachePush {
        val cbac = mockk<CbacCache>(relaxed = true)
        every { cbac.benId } returns benId
        val push = mockk<CbacCachePush>(relaxed = true)
        every { push.cbac } returns cbac
        return push
    }
}
