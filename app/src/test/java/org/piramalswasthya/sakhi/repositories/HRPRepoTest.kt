package org.piramalswasthya.sakhi.repositories

import android.app.Application
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.PwrPost
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [HRPRepo]. Consolidated from the previously separate
 * HRPRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HRPRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var context: Application
    @MockK private lateinit var hrpDao: HrpDao
    private val successData = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[]}"}"""
    private val noRecord = """{"statusCode":5000,"errorMessage":"No record found"}"""
    private val tokenRefresh = """{"statusCode":5002,"errorMessage":""}"""
    private val unexpected = """{"statusCode":9999,"errorMessage":""}"""

    private lateinit var repo: HRPRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.hrpDao } returns hrpDao
        loggedInUser()
        repo = HRPRepo(database, userRepo, maternalHealthRepo, ecrRepo, preferenceDao, tmcNetworkApiService, context)
    }

    private suspend fun assertNoUser(block: suspend () -> Unit) {
        try {
            block()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    private fun loggedInUser() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha_user"
        every { user.password } returns "pw"
        every { preferenceDao.getLoggedInUser() } returns user
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    @Test
    fun `getPregnantAssess returns record when exists`() = runTest {
        val assess = mockk<HRPPregnantAssessCache>()
        coEvery { hrpDao.getPregnantAssess(100L) } returns assess

        val result = repo.getPregnantAssess(100L)

        assertNotNull(result)
        assertEquals(assess, result)
    }

    @Test
    fun `getPregnantAssess returns null when not exists`() = runTest {
        coEvery { hrpDao.getPregnantAssess(999L) } returns null

        val result = repo.getPregnantAssess(999L)

        assertNull(result)
    }

    @Test
    fun `getNonPregnantAssess returns record when exists`() = runTest {
        val assess = mockk<HRPNonPregnantAssessCache>()
        coEvery { hrpDao.getNonPregnantAssess(100L) } returns assess

        val result = repo.getNonPregnantAssess(100L)

        assertNotNull(result)
        assertEquals(assess, result)
    }

    @Test
    fun `getNonPregnantAssess returns null when not exists`() = runTest {
        coEvery { hrpDao.getNonPregnantAssess(999L) } returns null

        val result = repo.getNonPregnantAssess(999L)

        assertNull(result)
    }

    @Test
    fun `getMicroBirthPlan returns record when exists`() = runTest {
        val plan = mockk<HRPMicroBirthPlanCache>()
        coEvery { hrpDao.getMicroBirthPlan(100L) } returns plan

        val result = repo.getMicroBirthPlan(100L)

        assertNotNull(result)
        assertEquals(plan, result)
    }

    @Test
    fun `getMicroBirthPlan returns null when not exists`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(999L) } returns null

        val result = repo.getMicroBirthPlan(999L)

        assertNull(result)
    }

    @Test
    fun `saveRecord pregnant assess calls dao`() = runTest {
        val assess = mockk<HRPPregnantAssessCache>()
        coEvery { hrpDao.saveRecord(assess) } returns Unit

        repo.saveRecord(assess)

        coVerify(exactly = 1) { hrpDao.saveRecord(assess) }
    }

    @Test
    fun `saveRecord non pregnant assess calls dao`() = runTest {
        val assess = mockk<HRPNonPregnantAssessCache>()
        coEvery { hrpDao.saveRecord(assess) } returns Unit

        repo.saveRecord(assess)

        coVerify(exactly = 1) { hrpDao.saveRecord(assess) }
    }

    @Test
    fun `getNonPregnantTrackList delegates to dao`() = runTest {
        val list = listOf(mockk<HRPNonPregnantTrackCache>(relaxed = true))
        coEvery { hrpDao.getNonPregnantTrackList(5L) } returns list
        assertEquals(list, repo.getNonPregnantTrackList(5L))
    }

    @Test
    fun `getPregnantTrackList delegates to dao`() = runTest {
        val list = listOf(mockk<HRPPregnantTrackCache>(relaxed = true))
        coEvery { hrpDao.getPregnantTrackList(5L) } returns list
        assertEquals(list, repo.getPregnantTrackList(5L))
    }

    @Test
    fun `getHRPTrack delegates to dao`() = runTest {
        val track = mockk<HRPPregnantTrackCache>()
        coEvery { hrpDao.getHRPTrack(9L) } returns track
        assertEquals(track, repo.getHRPTrack(9L))
    }

    @Test
    fun `getHRPNonTrack delegates to dao`() = runTest {
        val track = mockk<HRPNonPregnantTrackCache>()
        coEvery { hrpDao.getHRPNonTrack(9L) } returns track
        assertEquals(track, repo.getHRPNonTrack(9L))
    }

    @Test
    fun `getAllPregTrack delegates to dao`() = runTest {
        val list = listOf(mockk<HRPPregnantTrackCache>(relaxed = true))
        coEvery { hrpDao.getAllPregTrack() } returns list
        assertEquals(list, repo.getAllPregTrack())
    }

    @Test
    fun `getHrPregTrackList delegates to dao getAllPregTrackforBen`() = runTest {
        val list = listOf(mockk<HRPPregnantTrackCache>(relaxed = true))
        coEvery { hrpDao.getAllPregTrackforBen(5L) } returns list
        assertEquals(list, repo.getHrPregTrackList(5L))
    }

    @Test
    fun `getAllNonPregTrack delegates to dao`() = runTest {
        val list = listOf(mockk<HRPNonPregnantTrackCache>(relaxed = true))
        coEvery { hrpDao.getAllNonPregTrack() } returns list
        assertEquals(list, repo.getAllNonPregTrack())
    }

    @Test
    fun `getHrNonPregTrackList delegates to dao getAllNonPregTrackforBen`() = runTest {
        val list = listOf(mockk<HRPNonPregnantTrackCache>(relaxed = true))
        coEvery { hrpDao.getAllNonPregTrackforBen(5L) } returns list
        assertEquals(list, repo.getHrNonPregTrackList(5L))
    }

    @Test
    fun `getMaxLmp delegates to dao`() = runTest {
        coEvery { hrpDao.getMaxLmp(5L) } returns 123L
        assertEquals(123L, repo.getMaxLmp(5L))
    }

    @Test
    fun `getMaxDoVNonHrp delegates to dao getMaxDoV`() = runTest {
        coEvery { hrpDao.getMaxDoV(5L) } returns 456L
        assertEquals(456L, repo.getMaxDoVNonHrp(5L))
    }

    @Test
    fun `getMaxDoVHrp delegates to dao getMaxDoVhrp`() = runTest {
        coEvery { hrpDao.getMaxDoVhrp(5L) } returns 789L
        assertEquals(789L, repo.getMaxDoVHrp(5L))
    }

    @Test
    fun `saveRecord non pregnant track calls dao`() = runTest {
        val track = mockk<HRPNonPregnantTrackCache>(relaxed = true)
        repo.saveRecord(track)
        coVerify(exactly = 1) { hrpDao.saveRecord(track) }
    }

    @Test
    fun `saveRecord pregnant track calls dao`() = runTest {
        val track = mockk<HRPPregnantTrackCache>(relaxed = true)
        repo.saveRecord(track)
        coVerify(exactly = 1) { hrpDao.saveRecord(track) }
    }

    @Test
    fun `saveRecord micro birth plan calls dao`() = runTest {
        val plan = mockk<HRPMicroBirthPlanCache>(relaxed = true)
        repo.saveRecord(plan)
        coVerify(exactly = 1) { hrpDao.saveRecord(plan) }
    }

    @Test
    fun `pushUnSyncedRecords returns true when nothing to sync`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pushUnSyncedRecords()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getHighRiskAssessDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHighRiskAssessDetailsFromServer() }
    }

    @Test
    fun `getHRPAssessDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRPAssessDetailsFromServer() }
    }

    @Test
    fun `getHRPTrackDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRPTrackDetailsFromServer() }
    }

    @Test
    fun `getHRNonPAssessDetailsFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRNonPAssessDetailsFromServer() }
    }

    @Test
    fun `getHighRiskAssess returns 1 on success`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(successData)
        assertEquals(1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess returns 0 on no record`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(noRecord)
        assertEquals(0, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess returns -1 on token refresh success`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns 1 on success`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(successData)
        assertEquals(1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns 0 on no record`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(noRecord)
        assertEquals(0, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns -1 on token refresh failure`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0) }
    }

    @Test
    fun `getHRPAssess returns 1 on success`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(successData)
        assertEquals(1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPAssess returns 0 on no record`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(noRecord)
        assertEquals(0, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns 1 on success`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(successData)
        assertEquals(1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns 0 on no record`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(noRecord)
        assertEquals(0, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns -1 on unexpected status`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(unexpected)
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns 1 on success`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(successData)
        assertEquals(1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns 0 on no record`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(noRecord)
        assertEquals(0, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns 1 on success`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(successData)
        assertEquals(1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns 0 on no record`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(noRecord)
        assertEquals(0, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRNonPTrackDetailsFromServer(0) }
    }

    @Test
    fun `getHighRiskAssess returns -1 on non-200 http`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess returns -1 on unexpected status`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(unexpected)
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPAssess returns -1 on token refresh success`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPAssess returns -1 on non-200 http`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns -1 on non-200 http`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns -1 on unexpected status`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(unexpected)
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns -1 on token refresh success`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns -1 on token refresh failure`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns -1 on unexpected status`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(unexpected)
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns -1 on token refresh success`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns -1 on non-200 http`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHighRiskAssessDetailsFromServer(0) }
    }

    @Test
    fun `getHRPAssess throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRPAssessDetailsFromServer(0) }
    }

    @Test
    fun `getHRPTrack throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRPTrackDetailsFromServer(0) }
    }

    @Test
    fun `getHRNonPAssess throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getHRNonPAssessDetailsFromServer(0) }
    }

    @Test
    fun `getHRPAssess returns -1 on unexpected status`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(unexpected)
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns -1 on non-200 http`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns -1 on non-200 http`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns -1 on unexpected status`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(unexpected)
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    // ---------------- opposite token-refresh branches ----------------

    @Test
    fun `getHighRiskAssess returns -1 on token refresh failure`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPAssess returns -1 on token refresh failure`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns -1 on token refresh failure`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns -1 on token refresh success`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns -1 on token refresh success`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns -1 on token refresh failure`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    // ---------------- parser bodies: responses that actually carry entries ----------------
    // The shared `successData` fixture has an empty entries array, so every save* parser body
    // is dead behind it. These build a payload WITH entries so the loops run.
    // HRPRepo.getLongFromDate parses only "MMM d, yyyy h:mm:ss a".

    private val serverDate = "Jul 22, 2023 8:17:23 AM"

    private fun dataWithEntries(entriesJson: String): String {
        val flattened = entriesJson.replace(Regex("\\s+"), " ")
        val inner = """{"entries":$flattened}"""
        val escaped = inner.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"statusCode":200,"errorMessage":"","data":"$escaped"}"""
    }

    @Test
    fun `getHRPAssess parses entries and saves new records`() = runTest {
        val entries = """[
            {"benId":1001,"visitDate":"$serverDate"},
            {"benId":1002,"visitDate":"$serverDate"}
        ]"""
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getPregnantAssess(any<Long>()) } returns null

        assertEquals(1, repo.getHRPAssessDetailsFromServer(0))
        coVerify(atLeast = 1) { hrpDao.getPregnantAssess(any<Long>()) }
    }

    @Test
    fun `getHRPAssess skips invalid benId and existing records`() = runTest {
        val entries = """[
            {"benId":0,"visitDate":"$serverDate"},
            {"benId":-5,"visitDate":"$serverDate"},
            {"benId":2001,"visitDate":"$serverDate"},
            {"benId":2002}
        ]"""
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getPregnantAssess(2001L) } returns mockk<HRPPregnantAssessCache>(relaxed = true)

        assertEquals(1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack parses entries and looks up existing track`() = runTest {
        val entries = """[
            {"benId":3001,"visit":"1","visitDate":"$serverDate"},
            {"benId":3002,"visit":"2","visitDate":"$serverDate"},
            {"benId":3003,"visit":"3"}
        ]"""
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getHRPTrack(any(), any(), any(), any()) } returns null

        assertEquals(1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack skips entries whose track already exists`() = runTest {
        val entries = """[{"benId":3101,"visit":"1","visitDate":"$serverDate"}]"""
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getHRPTrack(any(), any(), any(), any()) } returns
                mockk<HRPPregnantTrackCache>(relaxed = true)

        assertEquals(1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess parses entries`() = runTest {
        val entries = """[
            {"benId":4001,"visitDate":"$serverDate"},
            {"benId":4002}
        ]"""
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getNonPregnantAssess(any<Long>()) } returns null

        assertEquals(1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess skips already saved records`() = runTest {
        val entries = """[{"benId":4101,"visitDate":"$serverDate"}]"""
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getNonPregnantAssess(any<Long>()) } returns
                mockk<HRPNonPregnantAssessCache>(relaxed = true)

        assertEquals(1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack parses entries`() = runTest {
        val entries = """[
            {"benId":5001,"visitDate":"$serverDate"},
            {"benId":5002}
        ]"""
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getHRPNonTrack(any(), any(), any()) } returns null

        assertEquals(1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack skips already saved tracks`() = runTest {
        val entries = """[{"benId":5101,"visitDate":"$serverDate"}]"""
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getHRPNonTrack(any(), any(), any()) } returns
                mockk<HRPNonPregnantTrackCache>(relaxed = true)

        assertEquals(1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess parses entries`() = runTest {
        val entries = """[
            {"benId":6001,"visitDate":"$serverDate"},
            {"benId":6002}
        ]"""
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getPregnantAssess(any<Long>()) } returns null

        assertEquals(1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan parses entries`() = runTest {
        val entries = """[
            {"benId":7001,"visitDate":"$serverDate"},
            {"benId":7002}
        ]"""
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getMicroBirthPlan(any<Long>()) } returns null

        assertEquals(1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    // ---------------- pushUnSyncedRecords: the private chunk loops ----------------
    // pushUnSyncedRecords() always returns true (worker must succeed); the value of these
    // tests is that they drive the five private chunk loops end to end.

    private val pushOk = """{"statusCode":200,"errorMessage":""}"""
    private val pushTokenExpired = """{"statusCode":5002,"errorMessage":""}"""
    private val pushFailed = """{"statusCode":9999,"errorMessage":""}"""

    @Test
    fun `pushUnSyncedRecords drives micro birth plan and track chunk loops`() = runTest {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 8001L))
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns
                listOf(HRPPregnantTrackCache(benId = 8002L))
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns
                listOf(HRPNonPregnantTrackCache(benId = 8003L))

        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns jsonResponse(pushOk)
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } returns jsonResponse(pushOk)
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } returns jsonResponse(pushOk)
        coEvery { hrpDao.getSavedRecord(any()) } returns null

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 1) { tmcNetworkApiService.saveHRPTrackData(any()) }
    }

    @Test
    fun `pushUnSyncedRecords handles token expiry on every chunk`() = runTest {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 8101L))
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns
                listOf(HRPPregnantTrackCache(benId = 8102L))
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns
                listOf(HRPNonPregnantTrackCache(benId = 8103L))

        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { hrpDao.getSavedRecord(any()) } returns null

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 1) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `pushUnSyncedRecords handles server rejection and http errors`() = runTest {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 8201L))
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns
                listOf(HRPPregnantTrackCache(benId = 8202L))
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns
                listOf(HRPNonPregnantTrackCache(benId = 8203L))

        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns jsonResponse(pushFailed)
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } returns jsonResponse("{}", code = 500)
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } throws RuntimeException("boom")
        coEvery { hrpDao.getSavedRecord(any()) } returns null

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushUnSyncedRecords drives the non pregnant assess loop through ecr mapping`() = runTest {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns
                listOf(HRPNonPregnantAssessCache(benId = 8301L))

        // A relaxed EcrPost keeps asPostModel (Base64 / file IO) out of the test.
        coEvery { ecrRepo.getSavedRecord(any()) } returns mockk(relaxed = true)
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())
    }

    // ---------------- pushUnSyncedRecordsHRPAssess (private, driven via pushUnSyncedRecords) ----------------

    private fun isolateOtherHrpLoops() {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns emptyList()
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess returns early and skips push when no unsynced records`() = runTest {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        isolateOtherHrpLoops()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tmcNetworkApiService.saveHighRiskAssessData(any()) }
        coVerify(exactly = 0) { maternalHealthRepo.postPwrToAmritServer(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess syncs single record on success when no existing pwr registration`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9001L, multiplePregnancy = "No", lmpDate = 1000L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(9001L) } returns null
        coEvery { maternalHealthRepo.postPwrToAmritServer(any()) } returns true
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
        coVerify(exactly = 0) { maternalHealthRepo.persistRegisterRecord(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess syncs single record and persists pwr when registration exists`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9002L, multiplePregnancy = "Yes", lmpDate = 2000L))
        val pwrCache = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { pwrCache.asPwrPost() } returns PwrPost(benId = 9002L, isActive = true, createdBy = "test", updatedBy = "test")
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(9002L) } returns pwrCache
        coEvery { maternalHealthRepo.postPwrToAmritServer(any()) } returns true
        coEvery { maternalHealthRepo.persistRegisterRecord(any()) } returns Unit
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { maternalHealthRepo.persistRegisterRecord(pwrCache) }
        coVerify(exactly = 1) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess skips pwr persist when pwr push to server fails`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9003L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(9003L) } returns
                mockk<PregnantWomanRegistrationCache>(relaxed = true)
        coEvery { maternalHealthRepo.postPwrToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { maternalHealthRepo.persistRegisterRecord(any()) }
        coVerify(exactly = 1) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess refreshes token when chunk reports token expiry`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9004L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess leaves record unsynced when token refresh fails`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9005L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess marks chunk failed on unexpected server status code`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9006L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushFailed)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess marks chunk failed on non-200 http response`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9007L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess catches exception thrown mid loop and continues`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9008L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess skips processing when response body string is null`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9009L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        val nullBodyResponse = mockk<Response<ResponseBody>>(relaxed = true)
        every { nullBodyResponse.code() } returns 200
        every { nullBodyResponse.body() } returns null
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns nullBodyResponse

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess pushes across multiple chunks when more than 20 records unsynced`() = runTest {
        val entities = (1..25).map { HRPPregnantAssessCache(benId = it.toLong()) }
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { maternalHealthRepo.postPwrToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 2) { tmcNetworkApiService.saveHighRiskAssessData(any()) }
        coVerify(exactly = 25) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    // ---------------- pushUnSyncedRecordsHRPMicroBirthPlan branch coverage ----------------

    private fun isolateAllExceptMBP() {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns emptyList()
    }

    @Test
    fun `pushUnSyncedRecordsHRPMicroBirthPlan syncs record on success`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 9101L))
        isolateAllExceptMBP()
        val savedRecord = HRPMicroBirthPlanCache(benId = 9101L)
        coEvery { hrpDao.getSavedRecord(9101L) } returns savedRecord
        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { hrpDao.saveRecord(savedRecord) }
        assertEquals(SyncState.SYNCED, savedRecord.syncState)
    }

    @Test
    fun `pushUnSyncedRecordsHRPMicroBirthPlan leaves record unsynced when token refresh fails`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 9102L))
        isolateAllExceptMBP()
        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPMicroBirthPlanCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPMicroBirthPlan marks chunk failed on unexpected status`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 9103L))
        isolateAllExceptMBP()
        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns jsonResponse(pushFailed)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPMicroBirthPlanCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPMicroBirthPlan catches exception mid loop`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 9104L))
        isolateAllExceptMBP()
        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPMicroBirthPlanCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPMicroBirthPlan skips update when response body is null`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns
                listOf(HRPMicroBirthPlanCache(benId = 9105L))
        isolateAllExceptMBP()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) } returns response

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(any<HRPMicroBirthPlanCache>()) }
    }

    // ---------------- pushUnSyncedRecordsHRPTrack branch coverage ----------------

    private fun isolateAllExceptTrack() {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns emptyList()
    }

    @Test
    fun `pushUnSyncedRecordsHRPTrack syncs record on success`() = runTest {
        val cache = HRPPregnantTrackCache(benId = 9201L)
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptTrack()
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { hrpDao.saveRecord(cache) }
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `pushUnSyncedRecordsHRPTrack leaves record unsynced when token refresh fails`() = runTest {
        val cache = HRPPregnantTrackCache(benId = 9202L)
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptTrack()
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPTrack marks chunk failed on http error`() = runTest {
        val cache = HRPPregnantTrackCache(benId = 9203L)
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptTrack()
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPTrack catches exception mid loop`() = runTest {
        val cache = HRPPregnantTrackCache(benId = 9204L)
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptTrack()
        coEvery { tmcNetworkApiService.saveHRPTrackData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    // ---------------- pushUnSyncedRecordsHRNonPAssess branch coverage ----------------

    private fun isolateAllExceptNonPAssess() {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns emptyList()
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPAssess syncs record and skips ecr persist when upload fails`() = runTest {
        val cache = HRPNonPregnantAssessCache(benId = 9301L)
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPAssess()
        coEvery { ecrRepo.postECRDataToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { ecrRepo.persistRecord(any()) }
        coVerify(exactly = 1) { hrpDao.saveRecord(cache) }
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPAssess persists ecr record when upload succeeds`() = runTest {
        val cache = HRPNonPregnantAssessCache(benId = 9302L)
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPAssess()
        coEvery { ecrRepo.postECRDataToAmritServer(any()) } returns true
        val ecrCache = mockk<EligibleCoupleRegCache>(relaxed = true)
        coEvery { ecrRepo.getSavedRecord(any()) } returns ecrCache
        coEvery { ecrRepo.persistRecord(any()) } returns Unit
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { ecrRepo.persistRecord(ecrCache) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPAssess leaves record unsynced when token refresh fails`() = runTest {
        val cache = HRPNonPregnantAssessCache(benId = 9303L)
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPAssess()
        coEvery { ecrRepo.postECRDataToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPAssess catches exception mid loop`() = runTest {
        val cache = HRPNonPregnantAssessCache(benId = 9304L)
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPAssess()
        coEvery { ecrRepo.postECRDataToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    // ---------------- pushUnSyncedRecordsHRNonPTrack branch coverage ----------------

    private fun isolateAllExceptNonPTrack() {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPTrack syncs record on success`() = runTest {
        val cache = HRPNonPregnantTrackCache(benId = 9401L)
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPTrack()
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { hrpDao.saveRecord(cache) }
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPTrack leaves record unsynced when token refresh fails`() = runTest {
        val cache = HRPNonPregnantTrackCache(benId = 9402L)
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPTrack()
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } returns jsonResponse(pushTokenExpired)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPTrack marks chunk failed on unexpected status`() = runTest {
        val cache = HRPNonPregnantTrackCache(benId = 9403L)
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPTrack()
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } returns jsonResponse(pushFailed)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPTrack catches exception mid loop`() = runTest {
        val cache = HRPNonPregnantTrackCache(benId = 9404L)
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPTrack()
        coEvery { tmcNetworkApiService.saveHRNonPTrackData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { hrpDao.saveRecord(cache) }
    }

    private fun newUser(): User {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha_user"
        every { user.password } returns "pw"
        return user
    }

    private fun nullBodyResponse(code: Int = 200): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns null
        return response
    }

    private val noRecordOtherMsg = """{"statusCode":5000,"errorMessage":"Some other error"}"""

    @Test
    fun `getHighRiskAssess returns -1 when response body is null`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns -1 when response body is null`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getHRPAssess returns -1 when response body is null`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns -1 when response body is null`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns -1 when response body is null`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns -1 when response body is null`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess returns -1 on 5000 with other error message`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(noRecordOtherMsg)
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(0))
    }

    @Test
    fun `getMicroBirthPlan returns -1 on 5000 with other error message`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(noRecordOtherMsg)
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))
    }

    @Test
    fun `getHRPAssess returns -1 on 5000 with other error message`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(noRecordOtherMsg)
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRPTrack returns -1 on 5000 with other error message`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(noRecordOtherMsg)
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPAssess returns -1 on 5000 with other error message`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(noRecordOtherMsg)
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(0))
    }

    @Test
    fun `getHRNonPTrack returns -1 on 5000 with other error message`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(noRecordOtherMsg)
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(0))
    }

    @Test
    fun `getHighRiskAssess retries on token refresh then exhausts retries`() = runTest {
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHighRiskAssessDetailsFromServer(1))
        coVerify(atLeast = 2) { tmcNetworkApiService.getHighRiskAssessData(any()) }
    }

    @Test
    fun `getMicroBirthPlan retries on token refresh then exhausts retries`() = runTest {
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(1))
        coVerify(atLeast = 2) { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) }
    }

    @Test
    fun `getHRPAssess retries on token refresh then exhausts retries`() = runTest {
        coEvery { tmcNetworkApiService.getHRPAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRPAssessDetailsFromServer(1))
        coVerify(atLeast = 2) { tmcNetworkApiService.getHRPAssessData(any()) }
    }

    @Test
    fun `getHRPTrack retries on token refresh then exhausts retries`() = runTest {
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRPTrackDetailsFromServer(1))
        coVerify(atLeast = 2) { tmcNetworkApiService.getHRPTrackData(any()) }
    }

    @Test
    fun `getHRNonPAssess retries on token refresh then exhausts retries`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPAssessData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRNonPAssessDetailsFromServer(1))
        coVerify(atLeast = 2) { tmcNetworkApiService.getHRNonPAssessData(any()) }
    }

    @Test
    fun `getHRNonPTrack retries on token refresh then exhausts retries`() = runTest {
        coEvery { tmcNetworkApiService.getHRNonPTrackData(any()) } returns jsonResponse(tokenRefresh)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-1, repo.getHRNonPTrackDetailsFromServer(1))
        coVerify(atLeast = 2) { tmcNetworkApiService.getHRNonPTrackData(any()) }
    }

    @Test
    fun `getHighRiskAssess saveHighRiskAssess creates new pregnant and non pregnant records when none exist`() = runTest {
        val entries = """[{"benId":10001,"createdDate":"$serverDate"}]"""
        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getPregnantAssess(10001L) } returns null
        coEvery { hrpDao.getNonPregnantAssess(10001L) } returns null

        assertEquals(1, repo.getHighRiskAssessDetailsFromServer(0))

        coVerify(exactly = 1) { hrpDao.saveRecord(match<HRPPregnantAssessCache> { it.benId == 10001L }) }
        coVerify(exactly = 1) { hrpDao.saveRecord(match<HRPNonPregnantAssessCache> { it.benId == 10001L }) }
    }

    @Test
    fun `getHighRiskAssess saveHighRiskAssess fills null fields, preserves existing values, and computes isHighRisk`() = runTest {
        val entries = """[
            {"benId":10002,"noOfDeliveries":"Yes","timeLessThan18m":"Yes","heightShort":"Yes","age":"Yes","createdDate":"$serverDate"},
            {"benId":10003,"noOfDeliveries":"Yes","timeLessThan18m":"Yes","heightShort":"Yes","age":"Yes","createdDate":"$serverDate"}
        ]"""
        val preg2 = HRPPregnantAssessCache(benId = 10002L)
        val nonPreg2 = HRPNonPregnantAssessCache(benId = 10002L)
        val preg3 = HRPPregnantAssessCache(
            benId = 10003L, noOfDeliveries = "No", timeLessThan18m = "No", heightShort = "No", age = "No"
        )
        val nonPreg3 = HRPNonPregnantAssessCache(
            benId = 10003L, noOfDeliveries = "No", timeLessThan18m = "No", heightShort = "No", age = "No"
        )

        coEvery { tmcNetworkApiService.getHighRiskAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getPregnantAssess(10002L) } returns preg2
        coEvery { hrpDao.getNonPregnantAssess(10002L) } returns nonPreg2
        coEvery { hrpDao.getPregnantAssess(10003L) } returns preg3
        coEvery { hrpDao.getNonPregnantAssess(10003L) } returns nonPreg3

        assertEquals(1, repo.getHighRiskAssessDetailsFromServer(0))

        assertEquals("Yes", preg2.noOfDeliveries)
        assertTrue(preg2.isHighRisk)
        assertEquals("Yes", nonPreg2.noOfDeliveries)
        assertTrue(nonPreg2.isHighRisk)

        assertEquals("No", preg3.noOfDeliveries)
        assertFalse(preg3.isHighRisk)
        assertEquals("No", nonPreg3.noOfDeliveries)
        assertFalse(nonPreg3.isHighRisk)

        coVerify(exactly = 1) { hrpDao.saveRecord(preg2) }
        coVerify(exactly = 1) { hrpDao.saveRecord(nonPreg2) }
        coVerify(exactly = 1) { hrpDao.saveRecord(preg3) }
        coVerify(exactly = 1) { hrpDao.saveRecord(nonPreg3) }
    }

    @Test
    fun `getMicroBirthPlan saveHighRiskAssessMicroBirthPlan creates new record when none exists`() = runTest {
        val entries = """[{"benId":20001,"nearestSc":"SC1"}]"""
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getMicroBirthPlan(20001L) } returns null

        assertEquals(1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))

        coVerify(exactly = 1) { hrpDao.saveRecord(match<HRPMicroBirthPlanCache> { it.benId == 20001L }) }
    }

    @Test
    fun `getMicroBirthPlan saveHighRiskAssessMicroBirthPlan fills only null fields on existing record`() = runTest {
        val entries = """[{
            "benId":20002,"nearestSc":"NewSC","bloodGroup":"NewBG","contactNumber1":"New1","contactNumber2":"New2",
            "scHosp":"NewSH","usg":"NewUSG","block":"NewBlock","nearestPhc":"NewPHC","nearestFru":"NewFRU",
            "bloodDonors1":"NewBD1","bloodDonors2":"NewBD2","birthCompanion":"NewBC","careTaker":"NewCT",
            "communityMember":"NewCM","communityMemberContact":"NewCMC","modeOfTransportation":"NewMOT"
        }]"""
        val existing = HRPMicroBirthPlanCache(
            benId = 20002L,
            nearestSc = "OldSC",
            bloodGroup = null,
            contactNumber1 = "Old1",
            contactNumber2 = null,
            scHosp = "OldSH",
            usg = null,
            block = "OldBlock",
            nearestPhc = null,
            nearestFru = "OldFRU",
            bloodDonors1 = null,
            bloodDonors2 = "OldBD2",
            birthCompanion = null,
            careTaker = "OldCT",
            communityMember = null,
            communityMemberContact = "OldCMC",
            modeOfTransportation = null
        )
        coEvery { tmcNetworkApiService.getMicroBirthPlanAssessData(any()) } returns
                jsonResponse(dataWithEntries(entries))
        coEvery { hrpDao.getMicroBirthPlan(20002L) } returns existing

        assertEquals(1, repo.getHighRiskAssessMicroBirthPlanDetailsFromServer(0))

        assertEquals("OldSC", existing.nearestSc)
        assertEquals("NewBG", existing.bloodGroup)
        assertEquals("Old1", existing.contactNumber1)
        assertEquals("New2", existing.contactNumber2)
        assertEquals("OldSH", existing.scHosp)
        assertEquals("NewUSG", existing.usg)
        assertEquals("OldBlock", existing.block)
        assertEquals("NewPHC", existing.nearestPhc)
        assertEquals("OldFRU", existing.nearestFru)
        assertEquals("NewBD1", existing.bloodDonors1)
        assertEquals("OldBD2", existing.bloodDonors2)
        assertEquals("NewBC", existing.birthCompanion)
        assertEquals("OldCT", existing.careTaker)
        assertEquals("NewCM", existing.communityMember)
        assertEquals("OldCMC", existing.communityMemberContact)
        assertEquals("NewMOT", existing.modeOfTransportation)

        coVerify(exactly = 1) { hrpDao.saveRecord(existing) }
    }

    @Test
    fun `getHRPTrack treats missing visit as null and saves new track directly`() = runTest {
        val entries = """[{"benId":3201,"visitDate":"$serverDate"}]"""
        coEvery { tmcNetworkApiService.getHRPTrackData(any()) } returns
                jsonResponse(dataWithEntries(entries))

        assertEquals(1, repo.getHRPTrackDetailsFromServer(0))

        coVerify(exactly = 0) { hrpDao.getHRPTrack(any(), any(), any(), any()) }
        coVerify(atLeast = 1) { hrpDao.saveRecord(match<HRPPregnantTrackCache> { it.benId == 3201L }) }
    }

    @Test
    fun `pushUnSyncedRecords throws when user missing during HRPAssess phase`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        every { preferenceDao.getLoggedInUser() } returnsMany listOf(newUser(), null)

        try {
            repo.pushUnSyncedRecords()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pushUnSyncedRecords throws when user missing during HRPTrack phase`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        every { preferenceDao.getLoggedInUser() } returnsMany listOf(newUser(), newUser(), null)

        try {
            repo.pushUnSyncedRecords()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pushUnSyncedRecords throws when user missing during HRNonPAssess phase`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        every { preferenceDao.getLoggedInUser() } returnsMany listOf(newUser(), newUser(), newUser(), null)

        try {
            repo.pushUnSyncedRecords()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pushUnSyncedRecords throws when user missing during HRNonPTrack phase`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns emptyList()
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns emptyList()
        every { preferenceDao.getLoggedInUser() } returnsMany listOf(newUser(), newUser(), newUser(), newUser(), null)

        try {
            repo.pushUnSyncedRecords()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess returns early when dao returns null instead of empty list`() = runTest {
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns null
        isolateOtherHrpLoops()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tmcNetworkApiService.saveHighRiskAssessData(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPMicroBirthPlan returns early when dao returns null instead of empty list`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(any<SyncState>()) } returns null
        isolateAllExceptMBP()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tmcNetworkApiService.saveMicroBirthPlanAssessData(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPTrack returns early when dao returns null instead of empty list`() = runTest {
        coEvery { hrpDao.getHRPTrack(any<SyncState>()) } returns null
        isolateAllExceptTrack()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tmcNetworkApiService.saveHRPTrackData(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPAssess returns early when dao returns null instead of empty list`() = runTest {
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns null
        isolateAllExceptNonPAssess()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { ecrRepo.postECRDataToAmritServer(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPTrack returns early when dao returns null instead of empty list`() = runTest {
        coEvery { hrpDao.getHRNonPTrack(any<SyncState>()) } returns null
        isolateAllExceptNonPTrack()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { tmcNetworkApiService.saveHRNonPTrackData(any()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRPAssess maps pwr with null multiplePregnancy when no existing registration`() = runTest {
        val entities = listOf(HRPPregnantAssessCache(benId = 9010L, multiplePregnancy = null, lmpDate = 5000L))
        coEvery { hrpDao.getHRPAssess(any<SyncState>()) } returns entities
        isolateOtherHrpLoops()
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(9010L) } returns null
        coEvery { maternalHealthRepo.postPwrToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { hrpDao.saveRecord(any<HRPPregnantAssessCache>()) }
    }

    @Test
    fun `pushUnSyncedRecordsHRNonPAssess maps ecr as new post when no existing ecr record`() = runTest {
        val cache = HRPNonPregnantAssessCache(
            benId = 9305L,
            misCarriage = "Yes",
            homeDelivery = "No",
            medicalIssues = "No",
            pastCSection = "No",
            isHighRisk = true
        )
        coEvery { hrpDao.getNonPregnantAssess(any<SyncState>()) } returns listOf(cache)
        isolateAllExceptNonPAssess()
        coEvery { ecrRepo.getSavedRecord(9305L) } returns null
        coEvery { ecrRepo.postECRDataToAmritServer(any()) } returns false
        coEvery { tmcNetworkApiService.saveHighRiskAssessData(any()) } returns jsonResponse(pushOk)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 1) { hrpDao.saveRecord(cache) }
    }
}
