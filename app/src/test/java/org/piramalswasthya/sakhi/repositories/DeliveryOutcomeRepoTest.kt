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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * Unit tests for [DeliveryOutcomeRepo]. Consolidated from DeliveryOutcomeRepoTest
 * + Extra/Extra3: getter/save delegations, processNewDeliveryOutcome guards and
 * missing-beneficiary branch, getExpiredRecords / setToInactive loops, and the
 * getDeliveryOutcomesFromServer when-branches / early returns plus the companion
 * date formatter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryOutcomeRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Application
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var deliveryOutcomeDao: DeliveryOutcomeDao

    private lateinit var repo: DeliveryOutcomeRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = DeliveryOutcomeRepo(context, preferenceDao, amritApiService, userRepo, benDao, deliveryOutcomeDao)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
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

    private fun nullBodyResponse(): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        return response
    }

    // =====================================================
    // getDeliveryOutcome() Tests
    // =====================================================

    @Test
    fun `getDeliveryOutcome returns record when exists`() = runTest {
        val outcome = mockk<DeliveryOutcomeCache>()
        coEvery { deliveryOutcomeDao.getDeliveryOutcome(100L) } returns outcome

        val result = repo.getDeliveryOutcome(100L)

        assertNotNull(result)
        assertEquals(outcome, result)
    }

    @Test
    fun `getDeliveryOutcome returns null when not exists`() = runTest {
        coEvery { deliveryOutcomeDao.getDeliveryOutcome(999L) } returns null

        val result = repo.getDeliveryOutcome(999L)

        assertNull(result)
    }

    // =====================================================
    // saveDeliveryOutcome() Tests
    // =====================================================

    @Test
    fun `saveDeliveryOutcome calls dao save`() = runTest {
        val outcome = mockk<DeliveryOutcomeCache>()
        coEvery { deliveryOutcomeDao.saveDeliveryOutcome(outcome) } returns Unit

        repo.saveDeliveryOutcome(outcome)

        coVerify(exactly = 1) { deliveryOutcomeDao.saveDeliveryOutcome(outcome) }
    }

    // =====================================================
    // processNewDeliveryOutcome() Tests
    // =====================================================

    @Test
    fun `processNewDeliveryOutcome throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewDeliveryOutcome()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewDeliveryOutcome returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { deliveryOutcomeDao.getAllUnprocessedDeliveryOutcomes() } returns emptyList()

        val result = repo.processNewDeliveryOutcome()

        assertEquals(true, result)
    }

    @Test
    fun `processNewDeliveryOutcome marks unsynced when beneficiary missing`() = runTest {
        loggedIn()
        val record = mockk<DeliveryOutcomeCache>(relaxed = true)
        every { record.benId } returns 55L
        coEvery { deliveryOutcomeDao.getAllUnprocessedDeliveryOutcomes() } returns listOf(record)
        coEvery { benDao.getBen(55L) } returns null
        coEvery { deliveryOutcomeDao.updateDeliveryOutcome(record) } returns Unit

        val result = repo.processNewDeliveryOutcome()

        assertTrue(result)
        coVerify(atLeast = 1) { deliveryOutcomeDao.updateDeliveryOutcome(record) }
    }

    // =====================================================
    // getExpiredRecords() / setToInactive() Tests
    // =====================================================

    @Test
    fun `getExpiredRecords returns empty when no records`() = runTest {
        coEvery { deliveryOutcomeDao.getAllBenIdAndDeliverDate() } returns emptyMap()
        assertTrue(repo.getExpiredRecords().isEmpty())
    }

    @Test
    fun `getExpiredRecords includes records past the pnc gap`() = runTest {
        // deliver date of 0 (epoch) is well beyond the gap from today
        coEvery { deliveryOutcomeDao.getAllBenIdAndDeliverDate() } returns mapOf(1L to 0L)
        assertTrue(repo.getExpiredRecords().contains(1L))
    }

    @Test
    fun `setToInactive updates each returned record`() = runTest {
        val record = mockk<DeliveryOutcomeCache>(relaxed = true)
        coEvery { deliveryOutcomeDao.getAllDeliveryOutcomes(any()) } returns listOf(record)

        repo.setToInactive(setOf(1L))

        coVerify { deliveryOutcomeDao.updateDeliveryOutcome(record) }
    }

    @Test
    fun `setToInactive does not update when nothing matches`() = runTest {
        coEvery { deliveryOutcomeDao.getAllDeliveryOutcomes(any()) } returns emptyList()

        repo.setToInactive(setOf(9L))

        coVerify(exactly = 0) { deliveryOutcomeDao.updateDeliveryOutcome(any()) }
    }

    // =====================================================
    // getDeliveryOutcomesFromServer() guards / when-branches
    // =====================================================

    @Test
    fun `getDeliveryOutcomesFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getDeliveryOutcomesFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns 1 on 200 with empty data`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -1 on 5000 with other message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Other"}"""
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":401,"errorMessage":""}"""
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -1 on unexpected status code`() = runTest {
        loggedIn()
        val json = """{"statusCode":7777,"errorMessage":""}"""
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns nullBodyResponse()

        assertEquals(-1, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } returns response

        assertEquals(-1, repo.getDeliveryOutcomesFromServer())
    }

    @Test
    fun `getDeliveryOutcomesFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { amritApiService.getDeliveryOutcomeData(any()) } throws SocketTimeoutException("timeout")

        assertEquals(-2, repo.getDeliveryOutcomesFromServer())
    }

    // ---------------- companion getCurrentDate ----------------

    @Test
    fun `getCurrentDate formats millis as iso like string`() {
        val result = DeliveryOutcomeRepo.getCurrentDate(0L)
        assertTrue(result.contains("T"))
        assertTrue(result.endsWith(".000Z"))
    }
}
