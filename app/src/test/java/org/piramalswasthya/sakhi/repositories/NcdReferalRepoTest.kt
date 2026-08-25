package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class NcdReferalRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var referalDao: NcdReferalDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var benDao: BenDao

    private lateinit var repo: NcdReferalRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { database.benDao } returns benDao
        repo = NcdReferalRepo(referalDao, preferenceDao, userRepo, amritApiService, database)
    }

    // =====================================================
    // getReferedNCD() Tests
    // =====================================================

    @Test
    fun `getReferedNCD returns cache when exists`() = runTest {
        val cache = mockk<ReferalCache>(relaxed = true)
        coEvery { referalDao.getReferalFromBenId(1L) } returns cache
        val result = repo.getReferedNCD(1L)
        assertEquals(cache, result)
    }

    @Test
    fun `getReferedNCD returns null when not found`() = runTest {
        coEvery { referalDao.getReferalFromBenId(1L) } returns null
        val result = repo.getReferedNCD(1L)
        assertNull(result)
    }

    // =====================================================
    // saveReferedNCD() Tests
    // =====================================================

    @Test
    fun `saveReferedNCD delegates to dao`() = runTest {
        val cache = mockk<ReferalCache>(relaxed = true)
        coEvery { referalDao.upsert(cache) } returns Unit
        repo.saveReferedNCD(cache)
        coVerify { referalDao.upsert(cache) }
    }

    // =====================================================
    // pushAndUpdateNCDReferRecord() Tests
    // =====================================================

    @Test
    fun `pushAndUpdateNCDReferRecord returns when no unprocessed records`() = runTest {
        coEvery { referalDao.getAllUnprocessedReferals() } returns emptyList()
        repo.pushAndUpdateNCDReferRecord()
        coVerify { referalDao.getAllUnprocessedReferals() }
    }

    @Test
    fun `toApiDateFormat returns ISO-like timestamp string`() {
        val result = with(repo) { 0L.toApiDateFormat() }
        assertTrue(
            "Unexpected format: $result",
            result.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        )
    }

    @Test
    fun `saveReferedNCD delegates to dao upsert`() = runTest {
        val cache = mockk<ReferalCache>(relaxed = true)
        coEvery { referalDao.upsert(cache) } returns Unit

        repo.saveReferedNCD(cache)

        coVerify(exactly = 1) { referalDao.upsert(cache) }
    }

    @Test
    fun `pullAndPersistReferRecord throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.pullAndPersistReferRecord()
            assertTrue("Should have thrown", false)
        } catch (e: NullPointerException) {
            // expected: userName!! on a null user
        }
        coVerify(exactly = 0) { amritApiService.getCbacReferData(any()) }
    }

    // =====================================================
    // pushAndUpdateNCDReferRecord() additional Tests
    // =====================================================

    @Test
    fun `pushAndUpdateNCDReferRecord marks record synced on Success response`() = runTest {
        val cache = referalCache(benId = 1L)
        coEvery { referalDao.getAllUnprocessedReferals() } returns listOf(cache)
        coEvery { amritApiService.postRefer(any()) } returns
            response(200, """{"status":"Success"}""")
        coEvery { referalDao.upsert(any()) } returns Unit

        repo.pushAndUpdateNCDReferRecord()

        coVerify { referalDao.upsert(match { it.syncState == SyncState.SYNCED }) }
    }

    @Test
    fun `pushAndUpdateNCDReferRecord skips update on non-Success response`() = runTest {
        val cache = referalCache(benId = 2L)
        coEvery { referalDao.getAllUnprocessedReferals() } returns listOf(cache)
        coEvery { amritApiService.postRefer(any()) } returns
            response(200, """{"status":"Failed"}""")

        repo.pushAndUpdateNCDReferRecord()

        coVerify(exactly = 0) { referalDao.upsert(any()) }
    }

    @Test
    fun `pushAndUpdateNCDReferRecord skips processing when body is null`() = runTest {
        val cache = referalCache(benId = 3L)
        coEvery { referalDao.getAllUnprocessedReferals() } returns listOf(cache)
        coEvery { amritApiService.postRefer(any()) } returns response(200, null)

        repo.pushAndUpdateNCDReferRecord()

        coVerify(exactly = 0) { referalDao.upsert(any()) }
    }

    @Test
    fun `pushAndUpdateNCDReferRecord processes multiple unprocessed records`() = runTest {
        val cache1 = referalCache(benId = 4L)
        val cache2 = referalCache(benId = 5L)
        coEvery { referalDao.getAllUnprocessedReferals() } returns listOf(cache1, cache2)
        coEvery { amritApiService.postRefer(any()) } returns
            response(200, """{"status":"Success"}""")
        coEvery { referalDao.upsert(any()) } returns Unit

        repo.pushAndUpdateNCDReferRecord()

        coVerify(exactly = 2) { referalDao.upsert(any()) }
    }

    @Test
    fun `pushAndUpdateNCDReferRecord propagates exception when api throws`() = runTest {
        val cache = referalCache(benId = 6L)
        coEvery { referalDao.getAllUnprocessedReferals() } returns listOf(cache)
        coEvery { amritApiService.postRefer(any()) } throws RuntimeException("boom")

        try {
            repo.pushAndUpdateNCDReferRecord()
            assertTrue("Should have thrown", false)
        } catch (e: RuntimeException) {
            assertEquals("boom", e.message)
        }
        coVerify(exactly = 0) { referalDao.upsert(any()) }
    }

    // =====================================================
    // pullAndPersistReferRecord() additional Tests
    // =====================================================

    @Test
    fun `pullAndPersistReferRecord returns 0 when data array missing`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacReferData(any()) } returns
            response(200, """{"statusCode":200}""")

        assertEquals(0, repo.pullAndPersistReferRecord())
        coVerify(exactly = 0) { referalDao.insertAll(any()) }
    }

    @Test
    fun `pullAndPersistReferRecord returns 0 when data array empty`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacReferData(any()) } returns
            response(200, """{"statusCode":200,"data":[]}""")

        assertEquals(0, repo.pullAndPersistReferRecord())
        coVerify(exactly = 0) { referalDao.insertAll(any()) }
    }

    @Test
    fun `pullAndPersistReferRecord recurses on 5002 then returns 0`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacReferData(any()) } returnsMany listOf(
            response(200, """{"statusCode":5002}"""),
            response(200, """{"statusCode":200}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(0, repo.pullAndPersistReferRecord())

        coVerify(exactly = 2) { amritApiService.getCbacReferData(any()) }
        coVerify(exactly = 1) { userRepo.refreshTokenTmc("asha", "pwd") }
    }

    @Test
    fun `pullAndPersistReferRecord inserts entities whose benId exists`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacReferData(any()) } returns
            response(200, """{"statusCode":200,"data":[${ncdReferalJson(benId = 100L)}]}""")
        coEvery { benDao.getExistingBenIds(listOf(100L)) } returns listOf(100L)
        val slot = slot<List<ReferalCache>>()
        coEvery { referalDao.insertAll(capture(slot)) } returns Unit

        assertEquals(0, repo.pullAndPersistReferRecord())

        coVerify(exactly = 1) { referalDao.insertAll(any()) }
        assertEquals(1, slot.captured.size)
        assertEquals(100L, slot.captured[0].benId)
    }

    @Test
    fun `pullAndPersistReferRecord skips entities whose benId does not exist`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns mockUser()
        coEvery { amritApiService.getCbacReferData(any()) } returns
            response(200, """{"statusCode":200,"data":[${ncdReferalJson(benId = 200L)}]}""")
        coEvery { benDao.getExistingBenIds(listOf(200L)) } returns emptyList()

        assertEquals(0, repo.pullAndPersistReferRecord())

        coVerify(exactly = 0) { referalDao.insertAll(any()) }
    }

    @Test
    fun `pullAndPersistReferRecord inserts only entities whose benId exists in a mixed batch`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns mockUser()
        val dataJson = "${ncdReferalJson(benId = 300L)},${ncdReferalJson(benId = 301L)}"
        coEvery { amritApiService.getCbacReferData(any()) } returns
            response(200, """{"statusCode":200,"data":[$dataJson]}""")
        coEvery { benDao.getExistingBenIds(listOf(300L, 301L)) } returns listOf(300L)
        val slot = slot<List<ReferalCache>>()
        coEvery { referalDao.insertAll(capture(slot)) } returns Unit

        assertEquals(0, repo.pullAndPersistReferRecord())

        coVerify(exactly = 1) { referalDao.insertAll(any()) }
        assertEquals(1, slot.captured.size)
        assertEquals(300L, slot.captured[0].benId)
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun referalCache(
        benId: Long = 1L,
        syncState: SyncState = SyncState.UNSYNCED
    ) = ReferalCache(
        benId = benId,
        syncState = syncState
    )

    private fun mockUser(): User = mockk<User>(relaxed = true).also {
        every { it.userName } returns "asha"
        every { it.password } returns "pwd"
    }

    private fun ncdReferalJson(benId: Long): String {
        return """
            {
                "id": 0,
                "benId": $benId,
                "referredToInstituteID": 1,
                "refrredToAdditionalServiceList": ["service1"],
                "referredToInstituteName": "Hospital A",
                "referralReason": "reason",
                "revisitDate": "2024-01-01",
                "vanID": 1,
                "parkingPlaceID": 1,
                "beneficiaryRegID": $benId,
                "benVisitID": 1,
                "visitCode": 1,
                "providerServiceMapID": 1,
                "createdBy": "asha",
                "type": "ncd",
                "isSpecialist": false
            }
        """.trimIndent()
    }

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

}
