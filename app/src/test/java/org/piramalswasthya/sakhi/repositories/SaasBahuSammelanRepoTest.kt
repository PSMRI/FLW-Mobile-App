package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.model.SaasBahuSammelanGetAllResponse
import org.piramalswasthya.sakhi.model.SaasBahuSammelanServerItem
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SaasBahuSammelanRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var appContext: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var saasBahuDao: SaasBahuSammelanDao
    @MockK private lateinit var moshi: Moshi

    private lateinit var repo: SaasBahuSammelanRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.d(any(), any()) } returns 0
        repo = SaasBahuSammelanRepo(userRepo, appContext, preferenceDao, api, saasBahuDao, moshi)
    }

    private fun loggedIn(): User {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 1
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        return user
    }

    @Test
    fun `saveSammelanForm inserts into dao`() = runTest {
        val cache = mockk<SaasBahuSammelanCache>(relaxed = true)

        repo.saveSammelanForm(cache)

        coVerify { saasBahuDao.insertSammelan(cache) }
    }

    @Test(expected = IllegalStateException::class)
    fun `pushUnSyncedRecords throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        repo.pushUnSyncedRecordsSaasBahuSammelan()
    }

    @Test
    fun `pushUnSyncedRecords returns true when nothing unsynced`() = runTest {
        loggedIn()
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns emptyList()

        assertTrue(repo.pushUnSyncedRecordsSaasBahuSammelan())
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer stops on unsuccessful response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery { api.getSaasBahuSammelans(any()) } returns response

        repo.SaasBahuSamelanGettDataFromServer()

        coVerify(exactly = 0) { saasBahuDao.clearAll() }
    }

    @Test
    fun `pushUnSyncedRecords returns true when upload gets non-200 response`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns response

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(result)
        coVerify { api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any()) }
    }

    private fun sammelanResponse(body: String, httpCode: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns httpCode
        every { response.body() } returns responseBody
        return response
    }

    @Test
    fun `pushUnSyncedRecords marks records synced on statusCode 200`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns sammelanResponse("""{"statusCode":200}""")

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(result)
        verify { row.syncState = SyncState.SYNCED }
        coVerify { saasBahuDao.insertSammelan(row) }
    }

    @Test
    fun `pushUnSyncedRecords returns false when token refresh succeeds on 5002`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns sammelanResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(!result)
        coVerify { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { saasBahuDao.insertSammelan(row) }
    }

    @Test
    fun `pushUnSyncedRecords returns false when token refresh fails on 5002`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns sammelanResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(!result)
        coVerify { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `pushUnSyncedRecords returns false when server reports no record found`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns sammelanResponse("""{"statusCode":5000,"errorMessage":"No record found"}""")

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(!result)
    }

    @Test
    fun `pushUnSyncedRecords returns false on unexpected statusCode`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns sammelanResponse("""{"statusCode":9999}""")

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(!result)
    }

    @Test
    fun `pushUnSyncedRecords returns true when response body string is null`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns response

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(result)
        coVerify(exactly = 0) { saasBahuDao.insertSammelan(row) }
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer stops when body is null`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { api.getSaasBahuSammelans(any()) } returns response

        repo.SaasBahuSamelanGettDataFromServer()

        coVerify(exactly = 0) { saasBahuDao.clearAll() }
    }

    private fun successfulResponse(json: String): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns json
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns responseBody
        return response
    }

    private fun stubParsedResponse(parsed: SaasBahuSammelanGetAllResponse?) {
        val adapter = mockk<JsonAdapter<SaasBahuSammelanGetAllResponse>>(relaxed = true)
        every { moshi.adapter(SaasBahuSammelanGetAllResponse::class.java) } returns adapter
        every { adapter.fromJson(any<String>()) } returns parsed
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer stops when parsed body is null`() = runTest {
        loggedIn()
        coEvery { api.getSaasBahuSammelans(any()) } returns successfulResponse("not json")
        stubParsedResponse(null)

        repo.SaasBahuSamelanGettDataFromServer()

        coVerify(exactly = 0) { saasBahuDao.clearAll() }
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer clears dao and inserts nothing when data is null`() = runTest {
        loggedIn()
        coEvery { api.getSaasBahuSammelans(any()) } returns successfulResponse("{}")
        val parsed = SaasBahuSammelanGetAllResponse(data = null, statusCode = 200, status = "OK")
        assertEquals(200, parsed.statusCode)
        assertEquals("OK", parsed.status)
        stubParsedResponse(parsed)

        repo.SaasBahuSamelanGettDataFromServer()

        coVerify { saasBahuDao.clearAll() }
        coVerify(exactly = 0) { saasBahuDao.insertSammelan(any()) }
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer persists entity with empty images when meetingImages is null`() =
        runTest {
            loggedIn()
            val item = SaasBahuSammelanServerItem(
                id = 5,
                meetingDate = 123456L,
                place = "Village Hall",
                participants = 10,
                ashaId = 42,
                meetingImages = null
            )
            coEvery { api.getSaasBahuSammelans(any()) } returns successfulResponse("{}")
            stubParsedResponse(
                SaasBahuSammelanGetAllResponse(data = listOf(item), statusCode = 200, status = "OK")
            )
            val slot = slot<SaasBahuSammelanCache>()
            coEvery { saasBahuDao.insertSammelan(capture(slot)) } just Runs

            repo.SaasBahuSamelanGettDataFromServer()

            coVerify { saasBahuDao.clearAll() }
            assertEquals(5L, slot.captured.id)
            assertEquals(42, slot.captured.ashaId)
            assertEquals("Village Hall", slot.captured.place)
            assertEquals(10, slot.captured.participants)
            assertEquals(123456L, slot.captured.date)
            assertTrue(slot.captured.sammelanImages.isNullOrEmpty())
            assertEquals(SyncState.SYNCED, slot.captured.syncState)
        }

    @Test
    fun `SaasBahuSamelanGettDataFromServer filters out images that fail to decode`() = runTest {
        loggedIn()
        val item = SaasBahuSammelanServerItem(
            id = 7,
            meetingDate = 111L,
            place = "Panchayat Bhawan",
            participants = 20,
            ashaId = 9,
            meetingImages = listOf("data:image/png;base64,QUJDRA==")
        )
        coEvery { api.getSaasBahuSammelans(any()) } returns successfulResponse("{}")
        stubParsedResponse(
            SaasBahuSammelanGetAllResponse(data = listOf(item), statusCode = 200, status = "OK")
        )
        val slot = slot<SaasBahuSammelanCache>()
        coEvery { saasBahuDao.insertSammelan(capture(slot)) } just Runs

        repo.SaasBahuSamelanGettDataFromServer()

        assertTrue(slot.captured.sammelanImages.isNullOrEmpty())
    }

    @Test(expected = NullPointerException::class)
    fun `SaasBahuSamelanGettDataFromServer throws when item id is null`() = runTest {
        loggedIn()
        val item = SaasBahuSammelanServerItem(
            id = null,
            meetingDate = 1L,
            place = "P",
            participants = 1,
            ashaId = 5,
            meetingImages = null
        )
        coEvery { api.getSaasBahuSammelans(any()) } returns successfulResponse("{}")
        stubParsedResponse(
            SaasBahuSammelanGetAllResponse(data = listOf(item), statusCode = 200, status = "OK")
        )

        repo.SaasBahuSamelanGettDataFromServer()
    }

    @Test
    fun `pushUnSyncedRecords attaches multipart image parts when local record has images`() = runTest {
        loggedIn()
        io.mockk.mockkObject(org.piramalswasthya.sakhi.utils.HelperUtil)
        io.mockk.mockkStatic(android.net.Uri::class)
        val parsedUri = mockk<android.net.Uri>(relaxed = true)
        every { android.net.Uri.parse("content://provider/img1") } returns parsedUri
        every {
            org.piramalswasthya.sakhi.utils.HelperUtil.getFileName(parsedUri, appContext)
        } returns "img1.jpg"
        every { appContext.contentResolver.getType(parsedUri) } returns "image/jpeg"
        val tempFile = java.io.File.createTempFile("saas_test_", ".jpg")
        tempFile.writeBytes(byteArrayOf(1, 2, 3))
        every {
            org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp(parsedUri, "img1.jpg", appContext)
        } returns tempFile

        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns listOf("content://provider/img1")
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns sammelanResponse("""{"statusCode":200}""")

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(result)
        coVerify {
            api.postSaasBahuSammelanMultipart(
                any(), any(), any(), any(), match { it.isNotEmpty() }
            )
        }
        tempFile.delete()
    }

    @Test(expected = NullPointerException::class)
    fun `SaasBahuSamelanGettDataFromServer throws when item ashaId is null`() = runTest {
        loggedIn()
        val item = SaasBahuSammelanServerItem(
            id = 1,
            meetingDate = 1L,
            place = "P",
            participants = 1,
            ashaId = null,
            meetingImages = null
        )
        coEvery { api.getSaasBahuSammelans(any()) } returns successfulResponse("{}")
        stubParsedResponse(
            SaasBahuSammelanGetAllResponse(data = listOf(item), statusCode = 200, status = "OK")
        )

        repo.SaasBahuSamelanGettDataFromServer()
    }

}
