package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import java.net.SocketTimeoutException
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
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenWithLeprosyScreeningCache
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.json.JSONObject
import retrofit2.Response

/**
 * Unit tests for [LeprosyRepo]. Consolidated from the previously separate
 * LeprosyRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var leprosyDao: LeprosyDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var context: Context
    @MockK private lateinit var resources: Resources

    private lateinit var repo: LeprosyRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.w(any(), any<String>()) } returns 0
        repo = LeprosyRepo(leprosyDao, benDao, preferenceDao, userRepo, tmcNetworkApiService, context)
    }

    private suspend fun assertNoUser(block: suspend () -> Unit) {
        try {
            block()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
    }

    private fun response(code: Int, json: String? = null): Response<ResponseBody> {
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

    private fun unsyncedFollowUp(): LeprosyFollowUpCache {
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        every { followUp.syncState } returns SyncState.UNSYNCED
        return followUp
    }

    private fun screeningEntryJson(benId: Long, homeVisitDate: String? = "2024-01-15"): String {
        val dateField = homeVisitDate?.let { "\"homeVisitDate\":\"$it\"," } ?: ""
        return """{"benId":$benId,$dateField"leprosyStatusDate":"2024-01-15","dateOfDeath":"2024-01-15","houseHoldDetailsId":1,"createdBy":"asha","createdDate":"2024-01-15","modifiedBy":"asha","lastModDate":"2024-01-15","treatmentStartDate":"2024-01-15","treatmentEndDate":"2024-01-15"}"""
    }

    private fun dataStringResponse(statusCode: Int, dataString: String): String {
        val obj = JSONObject()
        obj.put("statusCode", statusCode)
        obj.put("data", dataString)
        return obj.toString()
    }

    @Test
    fun `repo initializes successfully`() {
        assertNotNull(repo)
    }

    @Test
    fun `getLeprosyScreening returns cache when exists`() = runTest {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.getLeprosyScreening(1L) } returns cache
        val result = repo.getLeprosyScreening(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getLeprosyScreening returns null when not found`() = runTest {
        coEvery { leprosyDao.getLeprosyScreening(1L) } returns null
        val result = repo.getLeprosyScreening(1L)
        assertNull(result)
    }

    @Test
    fun `getLeprosyScreening with zero benId`() = runTest {
        coEvery { leprosyDao.getLeprosyScreening(0L) } returns null
        val result = repo.getLeprosyScreening(0L)
        assertNull(result)
    }

    @Test
    fun `saveLeprosyScreening delegates to dao`() = runTest {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.saveLeprosyScreening(cache) } returns Unit
        repo.saveLeprosyScreening(cache)
        coVerify { leprosyDao.saveLeprosyScreening(cache) }
    }

    @Test
    fun `updateLeprosyScreening delegates to dao`() = runTest {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.updateLeprosyScreening(cache) } returns Unit
        repo.updateLeprosyScreening(cache)
        coVerify { leprosyDao.updateLeprosyScreening(cache) }
    }

    @Test(expected = IllegalStateException::class)
    fun `getLeprosyScreeningDetailsFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getLeprosyScreeningDetailsFromServer()
    }

    @Test
    fun `getAllFollowUpsForBeneficiary delegates to dao`() = runTest {
        val list = listOf(mockk<LeprosyFollowUpCache>(relaxed = true))
        coEvery { leprosyDao.getAllFollowUpsForBeneficiary(5L) } returns list
        assertEquals(list, repo.getAllFollowUpsForBeneficiary(5L))
    }

    @Test
    fun `getFollowUpsForCurrentVisit delegates to dao getFollowUpsByVisit`() = runTest {
        val list = listOf(mockk<LeprosyFollowUpCache>(relaxed = true))
        coEvery { leprosyDao.getFollowUpsByVisit(5L, 2) } returns list
        assertEquals(list, repo.getFollowUpsForCurrentVisit(5L, 2))
    }

    @Test
    fun `getFollowUpsForVisit delegates to dao`() = runTest {
        val list = listOf(mockk<LeprosyFollowUpCache>(relaxed = true))
        coEvery { leprosyDao.getFollowUpsForVisit(5L, 3) } returns list
        assertEquals(list, repo.getFollowUpsForVisit(5L, 3))
    }

    @Test
    fun `saveFollowUp delegates to dao insertFollowUp`() = runTest {
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        repo.saveFollowUp(followUp)
        coVerify { leprosyDao.insertFollowUp(followUp) }
    }

    @Test
    fun `updateFollowUp delegates to dao`() = runTest {
        val followUp = mockk<LeprosyFollowUpCache>(relaxed = true)
        repo.updateFollowUp(followUp)
        coVerify { leprosyDao.updateFollowUp(followUp) }
    }

    @Test
    fun `getBenWithLeprosyData delegates to benDao`() = runTest {
        coEvery { benDao.getBenWithLeprosyScreeningAndFollowUps(5L) } returns null
        assertNull(repo.getBenWithLeprosyData(5L))
    }

    @Test
    fun `completeVisitAndStartNext returns false when no screening exists`() = runTest {
        coEvery { leprosyDao.getLeprosyScreening(5L) } returns null
        assertFalse(repo.completeVisitAndStartNext(5L))
    }

    @Test
    fun `pushUnSyncedRecords returns true when nothing to sync`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `pushAllUnSyncedRecords returns true when nothing to sync`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        assertTrue(repo.pushAllUnSyncedRecords())
    }

    @Test
    fun `getAllLeprosyDataFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getAllLeprosyDataFromServer() }
    }

    @Test
    fun `getAllLeprosyFollowUpDataFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getAllLeprosyFollowUpDataFromServer() }
    }

    @Test
    fun `pushUnSyncedLeprosyFollowUpData throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.pushUnSyncedLeprosyFollowUpData() }
    }

    @Test
    fun `screening pull returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } throws SocketTimeoutException("t")
        assertEquals(-2, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(500)
        assertEquals(-1, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 when statusCode missing`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, "{}")
        assertEquals(-1, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns 0 on 5000`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns
            response(200, """{"statusCode":5000}""")
        assertEquals(0, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns 1 on 200 with empty data`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns
            response(200, """{"statusCode":200,"data":"[]"}""")
        assertEquals(1, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 on unknown status code`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns
            response(200, """{"statusCode":9999}""")
        assertEquals(-1, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `all leprosy pull returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } throws SocketTimeoutException("t")
        assertEquals(-2, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns -1 when statusCode missing`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, "{}")
        assertEquals(-1, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns 0 on 5000`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns
            response(200, """{"statusCode":5000}""")
        assertEquals(0, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns 1 on 200 with empty data array`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns
            response(200, """{"statusCode":200,"data":[]}""")
        assertEquals(1, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all followup pull returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } throws SocketTimeoutException("t")
        assertEquals(-2, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `all followup pull returns -1 when statusCode missing`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns response(200, "{}")
        assertEquals(-1, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `all followup pull returns 0 on 5000`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":5000,"data":"none"}""")
        assertEquals(0, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `followup push returns 1 when nothing unsynced`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns emptyList()
        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
    }

    @Test
    fun `completeVisitAndStartNext returns true and updates screening`() = runTest {
        val screening = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.getLeprosyScreening(5L) } returns screening
        every { context.resources } returns resources
        every { resources.getStringArray(any()) } returns arrayOf("Suspected", "Confirmed")

        assertTrue(repo.completeVisitAndStartNext(5L))
        coVerify { leprosyDao.updateLeprosyScreening(screening) }
    }

    @Test
    fun `screening pull returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns
            response(200, """{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-2, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `screening pull returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns
            response(200, """{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `all leprosy pull returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns
            response(200, """{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-2, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns
            response(200, """{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns -1 on unknown status code`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns
            response(200, """{"statusCode":9999}""")
        assertEquals(-1, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(500)
        assertEquals(-1, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all followup pull returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":5002,"data":"x"}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        assertEquals(-2, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `all followup pull returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":5002,"data":"x"}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `all followup pull returns -1 on unknown status code`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":9999,"data":"x"}""")
        assertEquals(-1, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `all followup pull returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns response(500)
        assertEquals(-1, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `screening push marks records synced on 200 success`() = runTest {
        loggedIn()
        val record = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns listOf(record)
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } returns
            response(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { leprosyDao.saveLeprosyScreening(record) }
    }

    @Test
    fun `followup push marks records synced on 200 success`() = runTest {
        loggedIn()
        val followUp = unsyncedFollowUp()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns listOf(followUp)
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":200}""")

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
        coVerify { leprosyDao.updateFollowUp(followUp) }
    }

    @Test
    fun `followup push returns 1 and refreshes token on 401 or 5002 chunk`() = runTest {
        loggedIn()
        val followUp = unsyncedFollowUp()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns listOf(followUp)
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
        coVerify { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `followup push returns 1 on unknown status chunk`() = runTest {
        loggedIn()
        val followUp = unsyncedFollowUp()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns listOf(followUp)
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":9999}""")

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
    }

    @Test
    fun `followup push returns 1 on non-200 http chunk`() = runTest {
        loggedIn()
        val followUp = unsyncedFollowUp()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns listOf(followUp)
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } returns response(500)

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
    }

    @Test
    fun `screening push refreshes token on 401 or 5002 chunk`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<LeprosyScreeningCache>(relaxed = true))
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } returns
            response(200, """{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())
        coVerify { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `screening push returns true on unknown status chunk`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<LeprosyScreeningCache>(relaxed = true))
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } returns
            response(200, """{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `screening push returns true on non-200 http chunk`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<LeprosyScreeningCache>(relaxed = true))
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } returns response(500)

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `screening push returns true on 200 null body chunk`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<LeprosyScreeningCache>(relaxed = true))
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } returns response(200, null)

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `screening push returns true when chunk push throws`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns
            listOf(mockk<LeprosyScreeningCache>(relaxed = true))
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } throws RuntimeException("boom")

        assertTrue(repo.pushUnSyncedRecords())
    }

    @Test
    fun `screening push processes multiple chunks and marks synced`() = runTest {
        loggedIn()
        val records = List(21) { mockk<LeprosyScreeningCache>(relaxed = true) }
        coEvery { leprosyDao.getLeprosyScreening(SyncState.UNSYNCED) } returns records
        coEvery { tmcNetworkApiService.saveLeprosyScreeningData(any()) } returns
            response(200, """{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())
        coVerify(atLeast = 21) { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `followup push returns 1 when chunk push throws`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns listOf(unsyncedFollowUp())
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } throws RuntimeException("boom")

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
    }

    @Test
    fun `screening pull saves new record when array format and ben exists`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 501L)
        val responseJson = dataStringResponse(200, "[$entry]")
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(501L, any(), any()) } returns null
        coEvery { benDao.getBen(501L) } returns mockk(relaxed = true)
        coEvery { leprosyDao.saveLeprosyScreening(any()) } returns Unit

        assertEquals(1, repo.getLeprosyScreeningDetailsFromServer())
        coVerify { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `screening pull skips saving when cache already exists`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 502L)
        val responseJson = dataStringResponse(200, "[$entry]")
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(502L, any(), any()) } returns mockk(relaxed = true)

        assertEquals(1, repo.getLeprosyScreeningDetailsFromServer())
        coVerify(exactly = 0) { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `screening pull skips saving when ben does not exist locally`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 503L)
        val responseJson = dataStringResponse(200, "[$entry]")
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(503L, any(), any()) } returns null
        coEvery { benDao.getBen(503L) } returns null

        assertEquals(1, repo.getLeprosyScreeningDetailsFromServer())
        coVerify(exactly = 0) { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `screening pull parses requestDTO object format with leprosyLists`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 504L)
        val requestObj = """{"userId":42,"leprosyLists":[$entry]}"""
        val responseJson = dataStringResponse(200, requestObj)
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(504L, any(), any()) } returns null
        coEvery { benDao.getBen(504L) } returns mockk(relaxed = true)

        assertEquals(1, repo.getLeprosyScreeningDetailsFromServer())
        coVerify { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `screening pull skips entry with missing homeVisitDate`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 505L, homeVisitDate = null)
        val responseJson = dataStringResponse(200, "[$entry]")
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, responseJson)

        assertEquals(1, repo.getLeprosyScreeningDetailsFromServer())
        coVerify(exactly = 0) { leprosyDao.saveLeprosyScreening(any()) }
        coVerify(exactly = 0) { benDao.getBen(any()) }
    }

    @Test
    fun `screening pull returns -1 on 200 with null body`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, null)
        assertEquals(-1, repo.getLeprosyScreeningDetailsFromServer())
    }

    @Test
    fun `all leprosy pull saves new screening when ben exists`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 601L)
        val responseJson = """{"statusCode":200,"data":[$entry]}"""
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(601L, any(), any()) } returns null
        coEvery { benDao.getBen(601L) } returns mockk(relaxed = true)

        assertEquals(1, repo.getAllLeprosyDataFromServer())
        coVerify { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `all leprosy pull skips saving when ben does not exist`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 602L)
        val responseJson = """{"statusCode":200,"data":[$entry]}"""
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(602L, any(), any()) } returns null
        coEvery { benDao.getBen(602L) } returns null

        assertEquals(1, repo.getAllLeprosyDataFromServer())
        coVerify(exactly = 0) { leprosyDao.saveLeprosyScreening(any()) }
    }

    @Test
    fun `all leprosy pull updates existing screening`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 603L)
        val responseJson = """{"statusCode":200,"data":[$entry]}"""
        val existing = mockk<LeprosyScreeningCache>(relaxed = true)
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(603L, any(), any()) } returns existing

        assertEquals(1, repo.getAllLeprosyDataFromServer())
        coVerify { leprosyDao.updateLeprosyScreening(any()) }
    }

    @Test
    fun `all leprosy pull skips entry with missing homeVisitDate`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 604L, homeVisitDate = null)
        val responseJson = """{"statusCode":200,"data":[$entry]}"""
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, responseJson)

        assertEquals(1, repo.getAllLeprosyDataFromServer())
        coVerify(exactly = 0) { leprosyDao.saveLeprosyScreening(any()) }
        coVerify(exactly = 0) { leprosyDao.updateLeprosyScreening(any()) }
    }

    @Test
    fun `all leprosy pull returns 0 when save throws exception`() = runTest {
        loggedIn()
        val entry = screeningEntryJson(benId = 605L)
        val responseJson = """{"statusCode":200,"data":[$entry]}"""
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.getLeprosyScreening(605L, any(), any()) } throws RuntimeException("boom")

        assertEquals(0, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all leprosy pull returns -1 on 200 with null body`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyData(any()) } returns response(200, null)
        assertEquals(-1, repo.getAllLeprosyDataFromServer())
    }

    @Test
    fun `all followup pull returns -1 on 200 with null body`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns response(200, null)
        assertEquals(-1, repo.getAllLeprosyFollowUpDataFromServer())
    }

    @Test
    fun `followup push returns 1 on 200 null body chunk`() = runTest {
        loggedIn()
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns listOf(unsyncedFollowUp())
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } returns response(200, null)

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
    }

    @Test
    fun `followup push processes multiple chunks and marks synced`() = runTest {
        loggedIn()
        val followUps = List(21) { unsyncedFollowUp() }
        coEvery { leprosyDao.getAllFollowUpsByBenId() } returns followUps
        coEvery { tmcNetworkApiService.saveLeprosyFollowUpData(any()) } returns
            response(200, """{"statusCode":200}""")

        assertEquals(1, repo.pushUnSyncedLeprosyFollowUpData())
        coVerify(atLeast = 21) { leprosyDao.updateFollowUp(any()) }
    }

    @Test
    fun `getBenWithLeprosyData returns data when exists`() = runTest {
        val data = mockk<BenWithLeprosyScreeningCache>(relaxed = true)
        coEvery { benDao.getBenWithLeprosyScreeningAndFollowUps(5L) } returns data
        assertEquals(data, repo.getBenWithLeprosyData(5L))
    }

    @Test
    fun `screening pull returns 0 when data payload is malformed`() = runTest {
        loggedIn()
        val responseJson = dataStringResponse(200, "not a valid json")
        coEvery { tmcNetworkApiService.getMalariaScreeningData(any()) } returns response(200, responseJson)

        assertEquals(0, repo.getLeprosyScreeningDetailsFromServer())
    }

    private fun followUpEntryJson(benId: Long, visitNumber: Int = 1): String {
        return """{"benId":$benId,"visitNumber":$visitNumber,"followUpDate":"2024-01-15","createdBy":"asha","createdDate":"2024-01-15","modifiedBy":"asha","lastModDate":"2024-01-15"}"""
    }

    @Test
    fun `all followup pull returns -1 when data is a json array because the debug log reads it as a string first`() = runTest {
        // Real (pre-existing) production quirk: getAllLeprosyFollowUpDataFromServer's debug
        // Timber.d line calls jsonObj.getString("data") unconditionally before branching on
        // statusCode, but the 200 success branch expects "data" to be a JSON array. org.json's
        // getString() throws JSONException for a non-string value, so a well-formed array payload
        // is caught by the outer catch(JSONException) and returns -1 before insertFollowUp is
        // ever reached.
        loggedIn()
        val entry = followUpEntryJson(benId = 701L)
        val responseJson = """{"statusCode":200,"data":[$entry]}"""
        coEvery { tmcNetworkApiService.getAllLeprosyFollowUpData(any()) } returns response(200, responseJson)
        coEvery { leprosyDao.insertFollowUp(any()) } returns Unit

        assertEquals(-1, repo.getAllLeprosyFollowUpDataFromServer())
        coVerify(exactly = 0) { leprosyDao.insertFollowUp(any()) }
    }
}
