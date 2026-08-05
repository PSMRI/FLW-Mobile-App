package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil
import retrofit2.Response

/**
 * Unit tests for [MalariaRepo]. Consolidated from the previous
 * MalariaRepoTest + ExtraTest + Extra2/3/4Test files into a single class:
 * getters/delegations, IRS round-limit logic, no-user guards, the server-pull
 * inner status-code branches, and the chunked push loops.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MalariaRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var malariaDao: MalariaDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var api: AmritApiService

    private lateinit var repo: MalariaRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = MalariaRepo(malariaDao, benDao, preferenceDao, userRepo, api)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
    }

    private fun resp200(json: String): Response<ResponseBody> {
        val body = mockk<ResponseBody>()
        every { body.string() } returns json
        val resp = mockk<Response<ResponseBody>>(relaxed = true)
        every { resp.code() } returns 200
        every { resp.body() } returns body
        return resp
    }

    private fun resp(code: Int, json: String? = null): Response<ResponseBody> {
        val resp = mockk<Response<ResponseBody>>(relaxed = true)
        every { resp.code() } returns code
        if (json != null) {
            val body = mockk<ResponseBody>(relaxed = true)
            every { body.string() } returns json
            every { resp.body() } returns body
        } else {
            every { resp.body() } returns null
        }
        return resp
    }

    private suspend fun assertNoUser(block: suspend () -> Unit) {
        try {
            block()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // ===================== construction / getters =====================

    @Test
    fun `repo initializes successfully`() {
        assertNotNull(repo)
    }

    @Test
    fun `getLatestVisitForBen returns cache when exists`() = runTest {
        val cache = mockk<MalariaScreeningCache>(relaxed = true)
        coEvery { malariaDao.getLatestVisitForBen(1L) } returns cache
        assertEquals(cache, repo.getLatestVisitForBen(1L))
    }

    @Test
    fun `getLatestVisitForBen returns null when not found`() = runTest {
        coEvery { malariaDao.getLatestVisitForBen(1L) } returns null
        assertNull(repo.getLatestVisitForBen(1L))
    }

    @Test
    fun `getlastvisitIdforBen returns id when exists`() = runTest {
        coEvery { malariaDao.getLastVisitIdForBen(1L) } returns 42L
        assertEquals(42L, repo.getlastvisitIdforBen(1L))
    }

    @Test
    fun `getlastvisitIdforBen returns null when not found`() = runTest {
        coEvery { malariaDao.getLastVisitIdForBen(1L) } returns null
        assertNull(repo.getlastvisitIdforBen(1L))
    }

    @Test
    fun `saveMalariaScreening delegates to dao`() = runTest {
        val cache = mockk<MalariaScreeningCache>(relaxed = true)
        coEvery { malariaDao.saveMalariaScreening(cache) } returns Unit
        repo.saveMalariaScreening(cache)
        coVerify { malariaDao.saveMalariaScreening(cache) }
    }

    @Test
    fun `getMalariaConfirmed returns cache when exists`() = runTest {
        val cache = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        coEvery { malariaDao.getMalariaConfirmed(1L) } returns cache
        assertEquals(cache, repo.getMalariaConfirmed(1L))
    }

    @Test
    fun `getMalariaConfirmed returns null when not found`() = runTest {
        coEvery { malariaDao.getMalariaConfirmed(1L) } returns null
        assertNull(repo.getMalariaConfirmed(1L))
    }

    @Test
    fun `saveMalariaConfirmed delegates to dao`() = runTest {
        val cache = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        coEvery { malariaDao.saveMalariaConfirmed(cache) } returns Unit
        repo.saveMalariaConfirmed(cache)
        coVerify { malariaDao.saveMalariaConfirmed(cache) }
    }

    @Test
    fun `getIRSScreening returns cache when exists`() = runTest {
        val cache = mockk<IRSRoundScreening>(relaxed = true)
        coEvery { malariaDao.getIRSScreening(1L) } returns cache
        assertEquals(cache, repo.getIRSScreening(1L))
    }

    @Test
    fun `getIRSScreening returns null when not found`() = runTest {
        coEvery { malariaDao.getIRSScreening(1L) } returns null
        assertNull(repo.getIRSScreening(1L))
    }

    @Test
    fun `saveIRSScreening delegates to dao`() = runTest {
        val cache = mockk<IRSRoundScreening>(relaxed = true)
        coEvery { malariaDao.saveIRSScreening(cache) } returns Unit
        repo.saveIRSScreening(cache)
        coVerify { malariaDao.saveIRSScreening(cache) }
    }

    @Test
    fun `getAllActiveIRSRecords returns empty list when none`() = runTest {
        coEvery { malariaDao.getAllActiveIRSRecords(1L) } returns emptyList()
        assertEquals(0, repo.getAllActiveIRSRecords(1L).size)
    }

    @Test
    fun `getAllActiveIRSRecords returns records when exist`() = runTest {
        val records = listOf(mockk<IRSRoundScreening>(relaxed = true))
        coEvery { malariaDao.getAllActiveIRSRecords(1L) } returns records
        assertEquals(1, repo.getAllActiveIRSRecords(1L).size)
    }

    // ===================== IRS save update-branch / update =====================

    @Test
    fun `saveIRSScreening updates when id is not zero`() = runTest {
        val irs = mockk<IRSRoundScreening>(relaxed = true)
        every { irs.id } returns 5
        repo.saveIRSScreening(irs)
        coVerify { malariaDao.update(irs) }
    }

    @Test
    fun `updateIRSRecord delegates to dao updateIRS`() = runTest {
        val arr = arrayOf(mockk<IRSRoundScreening>(relaxed = true))
        repo.updateIRSRecord(arr)
        coVerify { malariaDao.updateIRS(*anyVararg()) }
    }

    // ===================== push coordinator =====================

    @Test
    fun `pushUnSyncedRecords returns true when nothing to sync`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        assertTrue(repo.pushUnSyncedRecords())
    }

    // ===================== round-limit logic =====================

    @Test
    fun `canSubmit returns true when rounds under limit`() = runTest {
        mockkObject(HelperUtil)
        every { HelperUtil.getYearRange(any()) } returns Pair(0L, 100L)
        coEvery { malariaDao.countRoundsInYear(1L, any(), any()) } returns 2
        assertTrue(repo.canSubmit(1L))
    }

    @Test
    fun `canSubmit returns false when rounds at limit`() = runTest {
        mockkObject(HelperUtil)
        every { HelperUtil.getYearRange(any()) } returns Pair(0L, 100L)
        coEvery { malariaDao.countRoundsInYear(1L, any(), any()) } returns 4
        assertFalse(repo.canSubmit(1L))
    }

    @Test
    fun `getCount returns dao count`() = runTest {
        mockkObject(HelperUtil)
        every { HelperUtil.getYearRange(any()) } returns Pair(0L, 100L)
        coEvery { malariaDao.countRoundsInYear(1L, any(), any()) } returns 3
        assertEquals(3, repo.getCount(1L))
    }

    @Test
    fun `submitRound saves and returns true when under limit`() = runTest {
        mockkObject(HelperUtil)
        every { HelperUtil.getYearRange(any()) } returns Pair(0L, 100L)
        val round = mockk<IRSRoundScreening>(relaxed = true)
        every { round.householdId } returns 1L
        coEvery { malariaDao.countRoundsInYear(1L, any(), any()) } returns 1
        assertTrue(repo.submitRound(round))
        coVerify { malariaDao.saveIRSScreening(*anyVararg()) }
    }

    @Test
    fun `submitRound returns false when limit reached`() = runTest {
        mockkObject(HelperUtil)
        every { HelperUtil.getYearRange(any()) } returns Pair(0L, 100L)
        val round = mockk<IRSRoundScreening>(relaxed = true)
        every { round.householdId } returns 1L
        coEvery { malariaDao.countRoundsInYear(1L, any(), any()) } returns 4
        assertFalse(repo.submitRound(round))
    }

    // ===================== no-user guards =====================

    @Test
    fun `getIRSScreeningDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getIRSScreeningDetailsFromServer() }
    }

    @Test
    fun `getMalariaScreeningDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getMalariaScreeningDetailsFromServer() }
    }

    @Test
    fun `getMalariaConfiremedDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getMalariaConfiremedDetailsFromServer() }
    }

    // ===================== IRS pull inner status branches =====================

    @Test
    fun `IRS pull returns 1 on inner 200 with empty object data`() = runTest {
        loggedIn()
        coEvery { api.getScreeningData(any()) } returns
            resp200("""{"statusCode":200,"data":"{}","errorMessage":""}""")
        assertEquals(1, repo.getIRSScreeningDetailsFromServer())
    }

    @Test
    fun `IRS pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getScreeningData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getIRSScreeningDetailsFromServer())
    }

    @Test
    fun `IRS pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getIRSScreeningDetailsFromServer())
    }

    @Test
    fun `IRS pull returns -2 on 5002 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.getScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-2, repo.getIRSScreeningDetailsFromServer())
    }

    @Test
    fun `IRS pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getScreeningData(any()) } returns
            resp200("""{"statusCode":999,"errorMessage":""}""")
        assertEquals(-1, repo.getIRSScreeningDetailsFromServer())
    }

    // ===================== screening pull inner status branches =====================

    @Test
    fun `screening pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":200,"data":"[]"}""")
        assertEquals(1, repo.getMalariaScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found","data":"[]"}""")
        assertEquals(0, repo.getMalariaScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":5002,"data":"[]"}""")
        assertEquals(-1, repo.getMalariaScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns
            resp200("""{"statusCode":999,"data":"[]"}""")
        assertEquals(-1, repo.getMalariaScreeningDetailsFromServer())
    }

    // ===================== confirmed pull inner status branches =====================

    @Test
    fun `confirmed pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { api.getMalariaConfirmedData(any()) } returns
            resp200("""{"statusCode":200,"data":"[]"}""")
        assertEquals(1, repo.getMalariaConfiremedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { api.getMalariaConfirmedData(any()) } returns
            resp200("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getMalariaConfiremedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.getMalariaConfirmedData(any()) } returns
            resp200("""{"statusCode":5002}""")
        assertEquals(-1, repo.getMalariaConfiremedDetailsFromServer())
    }

    @Test
    fun `confirmed pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { api.getMalariaConfirmedData(any()) } returns
            resp200("""{"statusCode":999}""")
        assertEquals(-1, repo.getMalariaConfiremedDetailsFromServer())
    }

    // ===================== push: malaria screening chunks =====================

    @Test
    fun `pushUnSyncedRecords marks malaria screening synced on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<MalariaScreeningCache>(relaxed = true)
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(200, """{"statusCode":200}""")
        assertTrue(repo.pushUnSyncedRecords())
        coVerify { malariaDao.saveMalariaScreening(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles malaria screening 401 with token refresh`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaScreeningCache>(relaxed = true))
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(200, """{"statusCode":401}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria screening 5002 without refresh`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaScreeningCache>(relaxed = true))
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(200, """{"statusCode":5002}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria screening unknown status`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaScreeningCache>(relaxed = true))
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(200, """{"statusCode":999}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria screening http error`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaScreeningCache>(relaxed = true))
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(500)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria screening exception`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaScreeningCache>(relaxed = true))
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveMalariaScreeningData(any()) } throws RuntimeException("boom")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria screening 200 null body`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaScreeningCache>(relaxed = true))
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(200, null)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords processes multiple screening chunks`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns
            List(21) { mockk<MalariaScreeningCache>(relaxed = true) }
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns emptyList()
        coEvery { api.saveMalariaScreeningData(any()) } returns resp(200, """{"statusCode":200}""")
        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 21) { malariaDao.saveMalariaScreening(any()) }
    }

    // ===================== push: malaria confirmed chunks =====================

    @Test
    fun `pushUnSyncedRecords marks malaria confirmed synced on inner 200`() = runTest {
        loggedIn()
        val cache = mockk<MalariaConfirmedCasesCache>(relaxed = true)
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveMalariaConfirmedData(any()) } returns resp(200, """{"statusCode":200}""")
        assertTrue(repo.pushUnSyncedRecords())
        coVerify { malariaDao.saveMalariaConfirmed(cache) }
    }

    @Test
    fun `pushUnSyncedRecords handles malaria confirmed 5002 with refresh`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaConfirmedCasesCache>(relaxed = true))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { api.saveMalariaConfirmedData(any()) } returns resp(200, """{"statusCode":5002}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria confirmed http error`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaConfirmedCasesCache>(relaxed = true))
        coEvery { api.saveMalariaConfirmedData(any()) } returns resp(500)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria confirmed unknown status`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaConfirmedCasesCache>(relaxed = true))
        coEvery { api.saveMalariaConfirmedData(any()) } returns resp(200, """{"statusCode":999}""")
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria confirmed 200 null body`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaConfirmedCasesCache>(relaxed = true))
        coEvery { api.saveMalariaConfirmedData(any()) } returns resp(200, null)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords handles malaria confirmed exception`() = runTest {
        loggedIn()
        coEvery { malariaDao.getMalariaScreening(SyncState.UNSYNCED) } returns emptyList()
        coEvery { malariaDao.getMalariaConfirmed(SyncState.UNSYNCED) } returns
            listOf(mockk<MalariaConfirmedCasesCache>(relaxed = true))
        coEvery { api.saveMalariaConfirmedData(any()) } throws RuntimeException("boom")
        assertTrue(repo.pushUnSyncedRecords())
    }

    // ===================== pull: null body / non-200 fall-through =====================

    @Test
    fun `getMalariaScreeningDetailsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getMalariaScreeningDetailsFromServer())
    }

    @Test
    fun `getMalariaConfiremedDetailsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getMalariaConfirmedData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getMalariaConfiremedDetailsFromServer())
    }

    @Test
    fun `getIRSScreeningDetailsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { api.getScreeningData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getIRSScreeningDetailsFromServer())
    }

    @Test
    fun `getMalariaScreeningDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getMalariaScreeningData(any()) } returns resp(500)
        assertEquals(-1, repo.getMalariaScreeningDetailsFromServer())
    }

    @Test
    fun `getMalariaConfiremedDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getMalariaConfirmedData(any()) } returns resp(500)
        assertEquals(-1, repo.getMalariaConfiremedDetailsFromServer())
    }

    @Test
    fun `getIRSScreeningDetailsFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getScreeningData(any()) } returns resp(500)
        assertEquals(-1, repo.getIRSScreeningDetailsFromServer())
    }
}
