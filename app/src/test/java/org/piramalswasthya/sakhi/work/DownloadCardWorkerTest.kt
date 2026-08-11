package org.piramalswasthya.sakhi.work

import android.app.NotificationManager
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadCardWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val abhaIdRepo: AbhaIdRepo = mockk(relaxed = true)

    @Before
    fun stubNotificationService() {
        every { context.getSystemService(any<String>()) } returns
            mockk<NotificationManager>(relaxed = true)
    }

    private fun worker(attempt: Int = 0, fileName: String? = null): DownloadCardWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        if (fileName != null) {
            val data: Data = mockk(relaxed = true)
            every { data.getString(DownloadCardWorker.file_name) } returns fileName
            every { params.inputData } returns data
        }
        return DownloadCardWorker(context, params, abhaIdRepo)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    @Test
    fun doWork_returnsFailure_whenRepoThrowsException() = runTest {
        coEvery { abhaIdRepo.downloadPdfCard() } throws RuntimeException("download failed")

        val result = worker(fileName = "card.pdf").doWork() as ListenableWorker.Result.Failure

        assertEquals("DownloadCardWorker", result.outputData.getString("worker_name"))
        assertEquals("download failed", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsSuccess_whenPdfDownloadedAndSaved() = runTest {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "download_card_worker_test")
        tempDir.mkdirs()

        mockkStatic(Environment::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns tempDir

        mockkStatic(MediaScannerConnection::class)
        every {
            MediaScannerConnection.scanFile(any(), any(), any(), any())
        } just Runs

        val content = "pdf-bytes".toByteArray()
        val responseBody: ResponseBody = mockk()
        every { responseBody.byteStream() } returns ByteArrayInputStream(content)
        val response: Response<ResponseBody> = mockk()
        every { response.body() } returns responseBody
        coEvery { abhaIdRepo.downloadPdfCard() } returns response

        val result = worker(fileName = "abha_card.pdf").doWork()

        assertTrue(result is ListenableWorker.Result.Success)

        File(tempDir, "abha_card.pdf").delete()
        tempDir.delete()
    }

    @Test
    fun doWork_returnsFailure_whenResponseBodyIsNull() = runTest {
        mockkStatic(Environment::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns
            File(System.getProperty("java.io.tmpdir"), "download_card_worker_test_null")

        val response: Response<ResponseBody> = mockk()
        every { response.body() } returns null
        coEvery { abhaIdRepo.downloadPdfCard() } returns response

        val result = worker(fileName = "abha_card.pdf").doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
    }
}
