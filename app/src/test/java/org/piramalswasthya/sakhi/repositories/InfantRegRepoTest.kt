package org.piramalswasthya.sakhi.repositories

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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [InfantRegRepo]. Consolidated from the previously separate
 * InfantRegRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InfantRegRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var infantRegDao: InfantRegDao

    private lateinit var repo: InfantRegRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = InfantRegRepo(preferenceDao, amritApiService, userRepo, benDao, infantRegDao)
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

    private fun nullBodyResponse(code: Int = 200): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns null
        return response
    }

    @Test
    fun `getInfantReg returns record when exists`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.getInfantReg(100L, 1) } returns infant

        val result = repo.getInfantReg(100L, 1)

        assertNotNull(result)
        assertEquals(infant, result)
    }

    @Test
    fun `getInfantReg returns null when not exists`() = runTest {
        coEvery { infantRegDao.getInfantReg(999L, 1) } returns null

        val result = repo.getInfantReg(999L, 1)

        assertNull(result)
    }

    @Test
    fun `getInfantReg passes correct baby index`() = runTest {
        coEvery { infantRegDao.getInfantReg(any(), any()) } returns null

        repo.getInfantReg(100L, 3)

        coVerify { infantRegDao.getInfantReg(100L, 3) }
    }

    @Test
    fun `getInfantRegFromChildBenId returns record when exists`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.getInfantRegFromChildBenId(200L) } returns infant

        val result = repo.getInfantRegFromChildBenId(200L)

        assertNotNull(result)
        assertEquals(infant, result)
    }

    @Test
    fun `getInfantRegFromChildBenId returns null when not exists`() = runTest {
        coEvery { infantRegDao.getInfantRegFromChildBenId(999L) } returns null

        val result = repo.getInfantRegFromChildBenId(999L)

        assertNull(result)
    }

    @Test
    fun `saveInfantReg calls dao save`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.saveInfantReg(infant) } returns Unit

        repo.saveInfantReg(infant)

        coVerify(exactly = 1) { infantRegDao.saveInfantReg(infant) }
    }

    @Test
    fun `processNewInfantRegister throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewInfantRegister()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewInfantRegister returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { infantRegDao.getAllUnprocessedInfantReg() } returns emptyList()

        val result = repo.processNewInfantRegister()

        assertEquals(true, result)
    }

    @Test
    fun `getNumBabyRegistered delegates to dao`() = runTest {
        coEvery { infantRegDao.getNumBabiesRegistered(5L) } returns 2
        assertEquals(2, repo.getNumBabyRegistered(5L))
    }

    @Test
    fun `update delegates to dao updateInfantReg`() = runTest {
        val cache = mockk<InfantRegCache>(relaxed = true)
        repo.update(cache)
        coVerify { infantRegDao.updateInfantReg(cache) }
    }

    @Test
    fun `setToInactive updates each returned record`() = runTest {
        val record = mockk<InfantRegCache>(relaxed = true)
        coEvery { infantRegDao.getAllInfantRegs(any()) } returns listOf(record)

        repo.setToInactive(setOf(1L))

        coVerify { infantRegDao.updateInfantReg(record) }
    }

    @Test
    fun `setToInactive does not update when nothing matches`() = runTest {
        coEvery { infantRegDao.getAllInfantRegs(any()) } returns emptyList()

        repo.setToInactive(setOf(9L))

        coVerify(exactly = 0) { infantRegDao.updateInfantReg(any()) }
    }

    @Test
    fun `getInfantRegFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getInfantRegFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `pull returns 1 on inner 200 with empty array`() = runTest {
        loggedIn()
        coEvery { amritApiService.getInfantRegData(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        assertEquals(1, repo.getInfantRegFromServer())
    }

    @Test
    fun `pull returns 0 on inner 5000 no record`() = runTest {
        loggedIn()
        coEvery { amritApiService.getInfantRegData(any()) } returns
            jsonResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")
        assertEquals(0, repo.getInfantRegFromServer())
    }

    @Test
    fun `pull returns -2 on inner 5002 when refresh succeeds`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.getInfantRegData(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-2, repo.getInfantRegFromServer())
    }

    @Test
    fun `pull returns -1 on inner 5002 when refresh fails`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.getInfantRegData(any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getInfantRegFromServer())
    }

    @Test
    fun `pull returns -1 on unknown inner status`() = runTest {
        loggedIn()
        coEvery { amritApiService.getInfantRegData(any()) } returns
            jsonResponse("""{"statusCode":8888,"errorMessage":""}""")
        assertEquals(-1, repo.getInfantRegFromServer())
    }

    @Test
    fun `pull returns -1 on 200 http with null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getInfantRegData(any()) } returns nullBodyResponse()
        assertEquals(-1, repo.getInfantRegFromServer())
    }

    @Test
    fun `pull returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { amritApiService.getInfantRegData(any()) } throws SocketTimeoutException("timeout")
        assertEquals(-2, repo.getInfantRegFromServer())
    }

    @Test
    fun `getCurrentDate formats millis as iso like string`() {
        val result = InfantRegRepo.getCurrentDate(0L)
        assertTrue(result.contains("T"))
        assertTrue(result.endsWith(".000Z"))
    }

    @Test
    fun `processNewInfantRegister returns true and marks synced on server accept`() = runTest {
        loggedIn()
        val record = mockk<InfantRegCache>(relaxed = true)
        coEvery { infantRegDao.getAllUnprocessedInfantReg() } returns listOf(record)
        coEvery { benDao.getBen(any()) } returns mockk(relaxed = true)
        coEvery { infantRegDao.updateInfantReg(any()) } returns Unit
        coEvery { amritApiService.postInfantRegForm(any()) } returns
            jsonResponse("""{"statusCode":200,"errorMessage":""}""")
        assertTrue(repo.processNewInfantRegister())
    }

    @Test
    fun `processNewInfantRegister returns true and marks unsynced on server reject`() = runTest {
        loggedIn()
        val record = mockk<InfantRegCache>(relaxed = true)
        coEvery { infantRegDao.getAllUnprocessedInfantReg() } returns listOf(record)
        coEvery { benDao.getBen(any()) } returns mockk(relaxed = true)
        coEvery { infantRegDao.updateInfantReg(any()) } returns Unit
        coEvery { amritApiService.postInfantRegForm(any()) } returns
            jsonResponse("""{"statusCode":7777,"errorMessage":""}""")
        assertTrue(repo.processNewInfantRegister())
    }
}
