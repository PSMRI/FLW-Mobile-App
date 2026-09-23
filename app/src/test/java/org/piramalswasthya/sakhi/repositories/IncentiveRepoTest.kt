package org.piramalswasthya.sakhi.repositories

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.IncentiveActivityWithRecords
import org.piramalswasthya.sakhi.model.IncentiveCache
import org.piramalswasthya.sakhi.model.UploadResponse
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.io.File
import java.io.InputStream
import java.nio.file.Files

/**
 * Unit tests for [IncentiveRepo]. Consolidated from the previously separate
 * IncentiveRepoTest + Extra* files into a single class.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncentiveRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var incentiveDao: IncentiveDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var context: Context
    private val recordsFlow = flowOf(emptyList<IncentiveCache>())
    private val activityFlow = flowOf(emptyList<IncentiveActivityWithRecords>())

    private lateinit var repo: IncentiveRepo

    @Before
    override fun setUp() {
        super.setUp()
        // These are captured in the constructor, so stub before building the repo.
        every { incentiveDao.getAllRecords() } returns recordsFlow
        every { incentiveDao.getAllActivity() } returns activityFlow
        repo = IncentiveRepo(amritApiService, incentiveDao, preferenceDao, userRepo, context)
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
    fun `list and activity_list expose dao flows`() {
        assertEquals(recordsFlow, repo.list)
        assertEquals(activityFlow, repo.activity_list)
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities returns true on no record found`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities saves empty master data and returns true`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords returns true on no record found`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `uploadIncentiveFiles fails when there are no valid files`() = runTest {
        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a", fileUris = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities returns false on unexpected status code`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords saves empty data and returns true when no local records`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords uses cursor timestamp when local records exist`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 5
        every { preferenceDao.lastIncentivePullTimestamp } returns 1_600_000_000_000L
        val json = """{"statusCode":200,"errorMessage":"","data":"[]"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords returns false on unexpected status code`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":1234,"errorMessage":""}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `activities returns false when token refresh fails on 5002`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns
            jsonResponse("""{"statusCode":5002,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `activities returns true when token refresh succeeds then retry succeeds`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `activities returns true on non-200 http code`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns
            jsonResponse("""{"statusCode":200}""", code = 500)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `activities returns true on null response body`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns nullBodyResponse()

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `records returns false when token refresh fails on 401`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns
            jsonResponse("""{"statusCode":401,"errorMessage":""}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `records returns true when token refresh succeeds then retry succeeds`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002,"errorMessage":""}"""),
            jsonResponse("""{"statusCode":200,"errorMessage":"","data":"[]"}""")
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `records returns true on non-200 http code`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns
            jsonResponse("""{"statusCode":200}""", code = 500)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `records returns true on null response body`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns nullBodyResponse()

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveActivities returns true on 5000 with other message`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":5000,"errorMessage":"something else"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `pullAndSaveAllIncentiveRecords returns true on 5000 with other message`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":5000,"errorMessage":"something else"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertTrue(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    @Test
    fun `activities returns false when master data payload is malformed`() = runTest {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val json = """{"statusCode":200,"errorMessage":"","data":"garbage"}"""
        coEvery { amritApiService.getAllIncentiveActivities(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveActivities(mockk(relaxed = true)))
    }

    @Test
    fun `records returns false when records data payload is malformed`() = runTest {
        coEvery { incentiveDao.getRecordCount() } returns 0
        val json = """{"statusCode":200,"errorMessage":"","data":"garbage"}"""
        coEvery { amritApiService.getAllIncentiveRecords(requestBody = any()) } returns jsonResponse(json)

        assertFalse(repo.pullAndSaveAllIncentiveRecords(mockk<User>(relaxed = true)))
    }

    private val cacheDir: File by lazy { Files.createTempDirectory("incentive_repo_test").toFile() }

    private fun stubUploadUri(path: String?): Uri {
        mockkStatic(Uri::class)
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns path
        every { Uri.parse(any()) } returns uri
        return uri
    }

    private fun stubResolver(uri: Uri, inputStream: InputStream?, mimeType: String? = "image/jpeg") {
        val resolver = mockk<ContentResolver>(relaxed = true)
        every { context.contentResolver } returns resolver
        every { resolver.openInputStream(uri) } returns inputStream
        every { resolver.getType(uri) } returns mimeType
    }

    private fun successfulUploadResponse(uploadResponse: UploadResponse = mockk(relaxed = true)): Response<UploadResponse> {
        val response = mockk<Response<UploadResponse>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns uploadResponse
        return response
    }

    @Test
    fun `uploadIncentiveFiles succeeds and deletes the temp file afterwards`() = runTest {
        val uri = stubUploadUri("/storage/emulated/0/photo.jpg")
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, "hello".byteInputStream(), "image/jpeg")
        val uploadResponse = mockk<UploadResponse>(relaxed = true)
        coEvery {
            amritApiService.uploadIncentiveDocuments(any(), any(), any(), any(), any())
        } returns successfulUploadResponse(uploadResponse)

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/1")
        )

        assertTrue(result.isSuccess)
        assertEquals(uploadResponse, result.getOrNull())
        assertFalse(File(cacheDir, "photo.jpg").exists())
    }

    @Test
    fun `uploadIncentiveFiles falls back to a generated name and generic mime type`() = runTest {
        val uri = stubUploadUri(null)
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, "data".byteInputStream(), null)
        coEvery {
            amritApiService.uploadIncentiveDocuments(any(), any(), any(), any(), any())
        } returns successfulUploadResponse()

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/2")
        )

        assertTrue(result.isSuccess)
        assertTrue(cacheDir.listFiles()?.isEmpty() ?: true)
    }

    @Test
    fun `uploadIncentiveFiles returns failure when the response is not successful`() = runTest {
        val uri = stubUploadUri("/storage/photo.jpg")
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, "hello".byteInputStream())
        val response = mockk<Response<UploadResponse>>(relaxed = true)
        every { response.isSuccessful } returns false
        every { response.code() } returns 400
        every { response.message() } returns "Bad Request"
        coEvery {
            amritApiService.uploadIncentiveDocuments(any(), any(), any(), any(), any())
        } returns response

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/3")
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("400") == true)
    }

    @Test
    fun `uploadIncentiveFiles returns failure when the response body is null`() = runTest {
        val uri = stubUploadUri("/storage/photo.jpg")
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, "hello".byteInputStream())
        val response = mockk<Response<UploadResponse>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery {
            amritApiService.uploadIncentiveDocuments(any(), any(), any(), any(), any())
        } returns response

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/4")
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `uploadIncentiveFiles returns failure and still cleans up temp files when the api call throws`() = runTest {
        val uri = stubUploadUri("/storage/photo.jpg")
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, "hello".byteInputStream())
        coEvery {
            amritApiService.uploadIncentiveDocuments(any(), any(), any(), any(), any())
        } throws RuntimeException("boom")

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/5")
        )

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
        assertFalse(File(cacheDir, "photo.jpg").exists())
    }

    @Test
    fun `uploadIncentiveFiles returns failure when the input stream is null`() = runTest {
        val uri = stubUploadUri("/storage/photo.jpg")
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, null)

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/6")
        )

        assertTrue(result.isFailure)
        assertEquals("No valid files to upload", result.exceptionOrNull()?.message)
    }

    @Test
    fun `uploadIncentiveFiles discards an empty temp file`() = runTest {
        val uri = stubUploadUri("/storage/empty.jpg")
        every { context.cacheDir } returns cacheDir
        stubResolver(uri, "".byteInputStream())

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/7")
        )

        assertTrue(result.isFailure)
        assertEquals("No valid files to upload", result.exceptionOrNull()?.message)
        assertFalse(File(cacheDir, "empty.jpg").exists())
    }

    @Test
    fun `uploadIncentiveFiles returns failure when the cache directory does not exist`() = runTest {
        val uri = stubUploadUri("/storage/photo.jpg")
        every { context.cacheDir } returns File("flw_missing_cache_dir_for_incentive_repo_test")
        stubResolver(uri, "hello".byteInputStream())

        val result = repo.uploadIncentiveFiles(
            id = 1L, userId = 2L, moduleName = "m", activityName = "a",
            fileUris = listOf("content://media/8")
        )

        assertTrue(result.isFailure)
        assertEquals("No valid files to upload", result.exceptionOrNull()?.message)
    }
}
