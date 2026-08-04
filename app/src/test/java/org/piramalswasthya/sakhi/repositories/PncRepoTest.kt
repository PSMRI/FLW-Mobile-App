package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PncRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var maternalHealthDao: MaternalHealthDao
    @MockK private lateinit var pncDao: PncDao
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var repo: PncRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = PncRepo(amritApiService, maternalHealthDao, pncDao, database, userRepo, benDao, preferenceDao)
        repo = PncRepo(
            amritApiService, maternalHealthDao, pncDao, database, userRepo, benDao, preferenceDao
        )
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
    }

    private fun response(code: Int, json: String? = null): Response<ResponseBody> {
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

    // =====================================================
    // getSavedPncRecord() Tests
    // =====================================================

    @Test
    fun `getSavedPncRecord returns record when exists`() = runTest {
        val pnc = mockk<PNCVisitCache>()
        coEvery { pncDao.getSavedRecord(100L, 1) } returns pnc

        val result = repo.getSavedPncRecord(100L, 1)

        assertNotNull(result)
        assertEquals(pnc, result)
    }

    @Test
    fun `getSavedPncRecord returns null when not exists`() = runTest {
        coEvery { pncDao.getSavedRecord(999L, 1) } returns null

        val result = repo.getSavedPncRecord(999L, 1)

        assertNull(result)
    }

    @Test
    fun `getSavedPncRecord passes correct visit number`() = runTest {
        coEvery { pncDao.getSavedRecord(any(), any()) } returns null

        repo.getSavedPncRecord(100L, 3)

        coVerify { pncDao.getSavedRecord(100L, 3) }
    }

    // =====================================================
    // getLastFilledPncRecord() Tests
    // =====================================================

    @Test
    fun `getLastFilledPncRecord returns record when exists`() = runTest {
        val pnc = mockk<PNCVisitCache>()
        coEvery { pncDao.getLastSavedRecord(100L) } returns pnc

        val result = repo.getLastFilledPncRecord(100L)

        assertNotNull(result)
        assertEquals(pnc, result)
    }

    @Test
    fun `getLastFilledPncRecord returns null when no records`() = runTest {
        coEvery { pncDao.getLastSavedRecord(999L) } returns null

        val result = repo.getLastFilledPncRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // persistPncRecord() Tests
    // =====================================================

    @Test
    fun `persistPncRecord calls dao insert`() = runTest {
        val pnc = mockk<PNCVisitCache>()
        coEvery { pncDao.insert(pnc) } returns Unit

        repo.persistPncRecord(pnc)

        coVerify(exactly = 1) { pncDao.insert(pnc) }
    }

    // =====================================================
    // processPncVisits() Tests
    // =====================================================

    @Test
    fun `processPncVisits throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processPncVisits()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processPncVisits returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns emptyList()

        val result = repo.processPncVisits()

        assertEquals(true, result)
    }

    @Test
    fun `getAllPncVisitsForBeneficiary delegates to dao`() = runTest {
        val list = listOf(mockk<PNCVisitCache>(relaxed = true))
        coEvery { pncDao.getPncVisitsByBenId(77L) } returns list

        val result = repo.getAllPncVisitsForBeneficiary(77L)

        assertEquals(list, result)
        coVerify { pncDao.getPncVisitsByBenId(77L) }
    }

    @Test
    fun `setToInactive updates each returned record`() = runTest {
        val record = mockk<PNCVisitCache>(relaxed = true)
        coEvery { pncDao.getAllPNCs(any()) } returns listOf(record)

        repo.setToInactive(setOf(1L, 2L))

        coVerify { pncDao.update(record) }
    }

    @Test
    fun `setToInactive does not update when no records match`() = runTest {
        coEvery { pncDao.getAllPNCs(any()) } returns emptyList()

        repo.setToInactive(setOf(9L))

        coVerify(exactly = 0) { pncDao.update(*anyVararg()) }
    }

    @Test
    fun `getPncVisitsFromServer throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getPncVisitsFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    @Test
    fun `getPncVisitsFromServer returns -2 on socket timeout`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPncVisitsData(any()) } throws SocketTimeoutException("t")
        assertEquals(-2, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPncVisitsData(any()) } returns response(500)
        assertEquals(-1, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns 0 on 5000 no record found`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"No record found","statusCode":5000}"""
        coEvery { amritApiService.getPncVisitsData(any()) } returns response(200, json)
        assertEquals(0, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns -1 on 5000 with other message`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"boom","statusCode":5000}"""
        coEvery { amritApiService.getPncVisitsData(any()) } returns response(200, json)
        assertEquals(-1, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns 1 on 200 with empty data`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"","statusCode":200,"data":"[]"}"""
        coEvery { amritApiService.getPncVisitsData(any()) } returns response(200, json)
        assertEquals(1, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns -1 on unknown status code`() = runTest {
        loggedIn()
        val json = """{"errorMessage":"","statusCode":9999}"""
        coEvery { amritApiService.getPncVisitsData(any()) } returns response(200, json)
        assertEquals(-1, repo.getPncVisitsFromServer())
    }

    // ---------------- processPncVisits upload loop ----------------

    @Test
    fun `processPncVisits marks record processed on 200 success`() = runTest {
        loggedIn()
        val record = mockk<PNCVisitCache>(relaxed = true)
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns listOf(record)
        coEvery { amritApiService.postPncForm(any()) } returns
            resp(200, """{"statusCode":200,"errorMessage":""}""")

        assertTrue(repo.processPncVisits())
        coVerify { amritApiService.postPncForm(any()) }
        coVerify(atLeast = 1) { pncDao.update(record) }
    }

    @Test
    fun `processPncVisits marks record unsynced on non-200 http`() = runTest {
        loggedIn()
        val record = mockk<PNCVisitCache>(relaxed = true)
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns listOf(record)
        coEvery { amritApiService.postPncForm(any()) } returns resp(500)

        assertTrue(repo.processPncVisits())
        coVerify(atLeast = 1) { pncDao.update(record) }
    }

    @Test
    fun `processPncVisits handles 401 without refresh`() = runTest {
        loggedIn()
        val record = mockk<PNCVisitCache>(relaxed = true)
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns listOf(record)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.postPncForm(any()) } returns
            resp(200, """{"statusCode":401,"errorMessage":""}""")

        assertTrue(repo.processPncVisits())
        coVerify(atLeast = 1) { pncDao.update(record) }
    }

    @Test
    fun `processPncVisits handles unknown status`() = runTest {
        loggedIn()
        val record = mockk<PNCVisitCache>(relaxed = true)
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns listOf(record)
        coEvery { amritApiService.postPncForm(any()) } returns
            resp(200, """{"statusCode":999,"errorMessage":""}""")

        assertTrue(repo.processPncVisits())
        coVerify(atLeast = 1) { pncDao.update(record) }
    }

    @Test
    fun `processPncVisits handles record that throws during mapping`() = runTest {
        loggedIn()
        val record = mockk<PNCVisitCache>(relaxed = true)
        every { record.asNetworkModel() } throws RuntimeException("map error")
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns listOf(record)

        assertTrue(repo.processPncVisits())
        coVerify(atLeast = 1) { pncDao.update(record) }
    }

    // ---------------- getPncVisitsFromServer refresh + null body ----------------

    @Test
    fun `getPncVisitsFromServer returns -2 on 5002 refresh success`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true
        coEvery { amritApiService.getPncVisitsData(any()) } returns
            resp(200, """{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-2, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns -1 on 5002 refresh fail`() = runTest {
        loggedIn()
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        coEvery { amritApiService.getPncVisitsData(any()) } returns
            resp(200, """{"statusCode":5002,"errorMessage":""}""")
        assertEquals(-1, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer returns -1 on null body`() = runTest {
        loggedIn()
        coEvery { amritApiService.getPncVisitsData(any()) } returns resp(200, null)
        assertEquals(-1, repo.getPncVisitsFromServer())
    }

    @Test
    fun `getPncVisitsFromServer skips insert when record already saved`() = runTest {
        loggedIn()
        // data holds one PNCNetwork with benId/pncPeriod; existing record -> no insert
        val json = """{"errorMessage":"","statusCode":200,"data":"[{\"benId\":5,\"pncPeriod\":1}]"}"""
        coEvery { amritApiService.getPncVisitsData(any()) } returns resp(200, json)
        coEvery { pncDao.getSavedRecord(5L, 1) } returns mockk<PNCVisitCache>(relaxed = true)

        assertEquals(1, repo.getPncVisitsFromServer())
        coVerify(exactly = 0) { pncDao.insert(any()) }
    }

    @Test
    fun `processPncVisits counts multiple successful uploads`() = runTest {
        loggedIn()
        val r1 = mockk<PNCVisitCache>(relaxed = true)
        val r2 = mockk<PNCVisitCache>(relaxed = true)
        coEvery { pncDao.getAllUnprocessedPncVisits() } returns listOf(r1, r2)
        coEvery { amritApiService.postPncForm(any()) } returns
            resp(200, """{"statusCode":200,"errorMessage":""}""")

        assertTrue(repo.processPncVisits())
        coVerify(atLeast = 1) { pncDao.update(r1) }
        coVerify(atLeast = 1) { pncDao.update(r2) }
    }

}
