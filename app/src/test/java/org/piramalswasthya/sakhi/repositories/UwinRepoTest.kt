package org.piramalswasthya.sakhi.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.model.UwinNetwork
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class UwinRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var appContext: Context
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var syncDao: SyncDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var uwinDao: UwinDao
    @MockK private lateinit var moshi: Moshi

    private lateinit var repo: UwinRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = UwinRepo(appContext, amritApiService, preferenceDao, syncDao, userRepo, uwinDao, moshi)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
    }

    @Test
    fun `getAllLocalRecords delegates to dao`() {
        val live = mockk<LiveData<List<UwinCache>>>()
        every { uwinDao.getAllUwinRecords() } returns live

        assertEquals(live, repo.getAllLocalRecords())
    }

    @Test
    fun `insertLocalRecord delegates to dao`() = runTest {
        val record = mockk<UwinCache>(relaxed = true)

        repo.insertLocalRecord(record)

        coVerify { uwinDao.insert(record) }
    }

    @Test
    fun `updateLocalRecord delegates to dao`() = runTest {
        val record = mockk<UwinCache>(relaxed = true)

        repo.updateLocalRecord(record)

        coVerify { uwinDao.update(record) }
    }

    @Test
    fun `getUwinById returns dao result`() = runTest {
        val cache = mockk<UwinCache>(relaxed = true)
        coEvery { uwinDao.getUwinById(3) } returns cache

        assertEquals(cache, repo.getUwinById(3))
    }

    @Test
    fun `tryUpsync returns true when nothing to sync`() = runTest {
        coEvery { uwinDao.getUnsyncedSessions(any()) } returns emptyList()

        assertTrue(repo.tryUpsync())
    }

    @Test
    fun `postUwinSession returns false when max retries exceeded`() = runTest {
        val network = mockk<UwinNetwork>(relaxed = true)

        assertFalse(repo.postUwinSession(network, retryCount = 3))
    }

    @Test
    fun `postUwinSession returns false when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val network = mockk<UwinNetwork>(relaxed = true)

        assertFalse(repo.postUwinSession(network))
    }

    @Test
    fun `downSyncAndPersist does nothing when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

    @Test
    fun `tryUpsync returns false when dao throws`() = runTest {
        coEvery { uwinDao.getUnsyncedSessions(any()) } throws RuntimeException("db down")

        assertFalse(repo.tryUpsync())
    }

    @Test
    fun `downSyncAndPersist returns without persisting when response unsuccessful`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery { amritApiService.getAllUwinSessions(any()) } returns response

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

    @Test
    fun `downSyncAndPersist returns without persisting when body is null`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { amritApiService.getAllUwinSessions(any()) } returns response

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

    private fun successfulResponse(json: String): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns json
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns responseBody
        return response
    }

    private fun stubParsedResponse(parsed: UwinRepo.UwinGetAllResponse?) {
        val adapter = mockk<JsonAdapter<UwinRepo.UwinGetAllResponse>>(relaxed = true)
        every { moshi.adapter(UwinRepo.UwinGetAllResponse::class.java) } returns adapter
        every { adapter.fromJson(any<String>()) } returns parsed
    }

    @Test
    fun `downSyncAndPersist returns without persisting when parsed body is null`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllUwinSessions(any()) } returns successfulResponse("not json")
        stubParsedResponse(null)

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

    @Test
    fun `downSyncAndPersist persists empty list when entries are null`() = runTest {
        loggedIn()
        coEvery { amritApiService.getAllUwinSessions(any()) } returns successfulResponse("{}")
        stubParsedResponse(
            UwinRepo.UwinGetAllResponse(data = null, statusCode = 200, status = "OK")
        )
        val slot = slot<List<UwinCache>>()
        coEvery { uwinDao.replaceAll(capture(slot)) } just Runs

        repo.downSyncAndPersist()

        assertTrue(slot.captured.isEmpty())
    }

    @Test
    fun `downSyncAndPersist persists parsed session with valid meeting date`() = runTest {
        loggedIn()
        val item = UwinRepo.UwinServerItem(
            id = 5,
            ashaId = 42,
            meetingDate = "2026-01-15",
            place = "Village Hall",
            participants = 10,
            meetingImages = null
        )
        coEvery { amritApiService.getAllUwinSessions(any()) } returns successfulResponse("{}")
        stubParsedResponse(
            UwinRepo.UwinGetAllResponse(
                data = UwinRepo.UwinData(entries = listOf(item)),
                statusCode = 200,
                status = "OK"
            )
        )
        val slot = slot<List<UwinCache>>()
        coEvery { uwinDao.replaceAll(capture(slot)) } just Runs

        repo.downSyncAndPersist()

        val expectedMillis = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2026-01-15")!!.time
        val persisted = slot.captured.single()
        assertEquals(5, persisted.id)
        assertEquals("Village Hall", persisted.place)
        assertEquals(10, persisted.participantsCount)
        assertEquals(expectedMillis, persisted.sessionDate)
        assertEquals("asha", persisted.createdBy)
        assertEquals("asha", persisted.updatedBy)
        assertEquals(SyncState.SYNCED, persisted.syncState)
        assertNull(persisted.uploadedFiles1)
        assertNull(persisted.uploadedFiles2)
    }

    @Test
    fun `downSyncAndPersist sets sessionDate to zero when meeting date is unparseable`() = runTest {
        loggedIn()
        val item = UwinRepo.UwinServerItem(
            id = 6,
            ashaId = 42,
            meetingDate = "not-a-date",
            place = "Field",
            participants = 3,
            meetingImages = null
        )
        coEvery { amritApiService.getAllUwinSessions(any()) } returns successfulResponse("{}")
        stubParsedResponse(
            UwinRepo.UwinGetAllResponse(
                data = UwinRepo.UwinData(entries = listOf(item)),
                statusCode = 200,
                status = "OK"
            )
        )
        val slot = slot<List<UwinCache>>()
        coEvery { uwinDao.replaceAll(capture(slot)) } just Runs

        repo.downSyncAndPersist()

        assertEquals(0L, slot.captured.single().sessionDate)
    }

    @Test
    fun `downSyncAndPersist sets sessionDate to zero and defaults null fields when meeting date is null`() =
        runTest {
            loggedIn()
            val item = UwinRepo.UwinServerItem(
                id = null,
                ashaId = 42,
                meetingDate = null,
                place = null,
                participants = null,
                meetingImages = null
            )
            coEvery { amritApiService.getAllUwinSessions(any()) } returns successfulResponse("{}")
            stubParsedResponse(
                UwinRepo.UwinGetAllResponse(
                    data = UwinRepo.UwinData(entries = listOf(item)),
                    statusCode = 200,
                    status = "OK"
                )
            )
            val slot = slot<List<UwinCache>>()
            coEvery { uwinDao.replaceAll(capture(slot)) } just Runs

            repo.downSyncAndPersist()

            val persisted = slot.captured.single()
            assertEquals(0L, persisted.sessionDate)
            assertEquals(0, persisted.id)
            assertEquals(0, persisted.participantsCount)
            assertNull(persisted.place)
        }

    // =====================================================
    // postUwinSession branch coverage (via successfulResponse / direct stubs)
    // =====================================================

    @Test
    fun `postUwinSession marks session synced on statusCode 200`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        every { network.id } returns 11
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("""{"statusCode":200}""")

        assertTrue(repo.postUwinSession(network))

        coVerify { uwinDao.updateSyncState(11, SyncState.SYNCED) }
    }

    @Test
    fun `postUwinSession marks session synced when body has id but no statusCode`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        every { network.id } returns 12
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("""{"id":999}""")

        assertTrue(repo.postUwinSession(network))

        coVerify { uwinDao.updateSyncState(12, SyncState.SYNCED) }
    }

    @Test
    fun `postUwinSession retries after token refresh and succeeds`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        every { network.id } returns 13
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returnsMany listOf(
            successfulResponse("""{"statusCode":5002}"""),
            successfulResponse("""{"statusCode":200}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.postUwinSession(network))

        coVerify(exactly = 2) {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        }
        coVerify { uwinDao.updateSyncState(13, SyncState.SYNCED) }
    }

    @Test
    fun `postUwinSession returns false when token refresh fails`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.postUwinSession(network))

        coVerify(exactly = 0) { uwinDao.updateSyncState(any(), any()) }
    }

    @Test
    fun `postUwinSession returns false on unexpected statusCode`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("""{"statusCode":9999}""")

        assertFalse(repo.postUwinSession(network))
    }

    @Test
    fun `postUwinSession returns false on unsuccessful http response`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns response

        assertFalse(repo.postUwinSession(network))
    }

    @Test
    fun `postUwinSession returns false when response body is malformed json`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("not-json")

        assertFalse(repo.postUwinSession(network))
    }

    @Test
    fun `postUwinSession returns false when the api call throws`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } throws RuntimeException("network down")

        assertFalse(repo.postUwinSession(network))
    }

    @Test
    fun `postUwinSession retries on SocketTimeoutException and eventually succeeds`() = runTest {
        loggedIn()
        val network = mockk<UwinNetwork>(relaxed = true)
        every { network.id } returns 14
        var callCount = 0
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } answers {
            callCount++
            if (callCount == 1) throw java.net.SocketTimeoutException("timeout")
            else successfulResponse("""{"statusCode":200}""")
        }

        assertTrue(repo.postUwinSession(network))

        coVerify(exactly = 2) {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        }
        coVerify { uwinDao.updateSyncState(14, SyncState.SYNCED) }
    }

    // =====================================================
    // tryUpsync: non-empty unsynced session loop
    // =====================================================

    @Test
    fun `tryUpsync returns true when all unsynced sessions sync successfully`() = runTest {
        loggedIn()
        val cache = mockk<UwinCache>(relaxed = true)
        val network = mockk<UwinNetwork>(relaxed = true)
        every { network.id } returns 21
        every { cache.asDomainModel() } returns network
        coEvery { uwinDao.getUnsyncedSessions(any()) } returns listOf(cache)
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("""{"statusCode":200}""")

        assertTrue(repo.tryUpsync())

        coVerify { uwinDao.updateSyncState(21, SyncState.SYNCED) }
    }

    @Test
    fun `tryUpsync returns false when a session fails to sync`() = runTest {
        loggedIn()
        val cache = mockk<UwinCache>(relaxed = true)
        val network = mockk<UwinNetwork>(relaxed = true)
        every { cache.asDomainModel() } returns network
        coEvery { uwinDao.getUnsyncedSessions(any()) } returns listOf(cache)
        coEvery {
            amritApiService.saveUwinSession(any(), any(), any(), any(), any(), any())
        } returns successfulResponse("""{"statusCode":9999}""")

        assertFalse(repo.tryUpsync())
    }
}
