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
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
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
}
