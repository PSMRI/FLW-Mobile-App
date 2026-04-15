package org.piramalswasthya.sakhi.helpers

import android.util.Log
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.LogLevel
import timber.log.Timber

class SyncLogTreeTest {

    @MockK(relaxed = true)
    private lateinit var syncLogManager: SyncLogManager

    private lateinit var tree: SyncLogTree

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(Log::class)
        every { Log.println(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        tree = SyncLogTree(syncLogManager)
        Timber.uprootAll()
        Timber.plant(tree)
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
        unmockkAll()
    }

    // =====================================================
    // isSyncRelated — Tag Keyword Tests
    // =====================================================

    @Test
    fun `logs with Worker tag are captured`() {
        Timber.tag("SyncWorker").d("starting task")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `logs with Sync tag are captured`() {
        Timber.tag("SyncService").d("pulling data")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `logs with Repo tag are captured`() {
        Timber.tag("BenRepo").d("fetching beneficiaries")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `logs with Pull tag are captured`() {
        Timber.tag("PullService").d("downloading")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `logs with Amrit tag are captured`() {
        Timber.tag("AmritApi").d("calling api")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    // =====================================================
    // isSyncRelated — Message Keyword Tests
    // =====================================================

    @Test
    fun `message containing sync keyword is captured`() {
        Timber.tag("OkHttp").d("starting sync operation")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `message containing beneficiary keyword is captured`() {
        Timber.tag("OkHttp").d("processing beneficiary data")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `message containing push keyword is captured`() {
        Timber.tag("OkHttp").d("push data to server")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `message containing pull keyword is captured`() {
        Timber.tag("OkHttp").d("pull records from server")

        verify { syncLogManager.addLog(any(), any(), any()) }
    }

    // =====================================================
    // Non-Sync Messages
    // =====================================================

    @Test
    fun `non sync message is not captured`() {
        Timber.tag("OkHttp").d("UI button clicked")

        verify(exactly = 0) { syncLogManager.addLog(any(), any(), any()) }
    }

    @Test
    fun `empty message is not captured`() {
        Timber.tag("OkHttp").d("")

        verify(exactly = 0) { syncLogManager.addLog(any(), any(), any()) }
    }

    // =====================================================
    // Log Level Mapping Tests
    // =====================================================

    @Test
    fun `error priority maps to ERROR level`() {
        Timber.tag("SyncWorker").e("sync error occurred")

        verify { syncLogManager.addLog(LogLevel.ERROR, any(), any()) }
    }

    @Test
    fun `warn priority maps to WARN level`() {
        Timber.tag("SyncWorker").w("sync warning message")

        verify { syncLogManager.addLog(LogLevel.WARN, any(), any()) }
    }

    @Test
    fun `info priority maps to INFO level`() {
        Timber.tag("SyncWorker").i("sync info message")

        verify { syncLogManager.addLog(LogLevel.INFO, any(), any()) }
    }

    @Test
    fun `debug priority maps to DEBUG level`() {
        Timber.tag("SyncWorker").d("sync debug message")

        verify { syncLogManager.addLog(LogLevel.DEBUG, any(), any()) }
    }

    // =====================================================
    // promoteIfError Tests
    // =====================================================

    @Test
    fun `message with exception keyword promoted to ERROR`() {
        Timber.tag("SyncWorker").d("sync exception occurred during pull")

        verify { syncLogManager.addLog(LogLevel.ERROR, any(), any()) }
    }

    @Test
    fun `message with failed keyword promoted to WARN`() {
        Timber.tag("SyncWorker").d("sync worker failed")

        verify { syncLogManager.addLog(LogLevel.WARN, any(), any()) }
    }

    @Test
    fun `message with failed but also succeeded is not promoted`() {
        Timber.tag("SyncWorker").d("sync 2 failed out of 10 succeeded")

        verify { syncLogManager.addLog(LogLevel.DEBUG, any(), any()) }
    }

    @Test
    fun `throwable promotes to ERROR`() {
        Timber.tag("SyncWorker").d(RuntimeException("test"), "sync operation")

        verify { syncLogManager.addLog(LogLevel.ERROR, any(), any()) }
    }

    // =====================================================
    // Throwable Message Tests
    // =====================================================

    @Test
    fun `throwable message is appended to log`() {
        Timber.tag("SyncWorker").d(RuntimeException("network timeout"), "sync failed")

        verify { syncLogManager.addLog(any(), any(), match { it.contains("network timeout") }) }
    }

    @Test
    fun `null throwable does not append extra text`() {
        Timber.tag("SyncWorker").d("sync completed")

        verify { syncLogManager.addLog(any(), any(), "sync completed") }
    }
}
