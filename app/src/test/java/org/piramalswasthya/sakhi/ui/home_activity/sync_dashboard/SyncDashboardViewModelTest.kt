package org.piramalswasthya.sakhi.ui.home_activity.sync_dashboard

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.SyncLogExporter
import org.piramalswasthya.sakhi.helpers.SyncLogManager
import org.piramalswasthya.sakhi.model.SyncLogEntry
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.work.BasePushWorker
import org.piramalswasthya.sakhi.work.WorkerUtils

@OptIn(ExperimentalCoroutinesApi::class)
class SyncDashboardViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var syncDao: SyncDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var syncLogManager: SyncLogManager
    @MockK private lateinit var syncLogExporter: SyncLogExporter
    private lateinit var application: Application
    private lateinit var workManager: WorkManager

    private lateinit var viewModel: SyncDashboardViewModel

    @Before
    override fun setUp() {
        super.setUp()
        application = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns workManager
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(emptyList())
        every { syncDao.getSyncStatus() } returns flowOf(emptyList())
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { syncLogManager.logs } returns MutableStateFlow(emptyList())
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
    }

    @After
    fun unmockWorkManager() {
        unmockkObject(WorkManager.Companion)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `syncStatus is not null`() {
        assertNotNull(viewModel.syncStatus)
    }

    @Test
    fun `lang reflects preference language`() {
        assertEquals(Languages.ENGLISH, viewModel.lang)
    }

    @Test
    fun `syncLogs delegates to syncLogManager`() {
        assertNotNull(viewModel.syncLogs)
    }

    @Test
    fun `clearLogs delegates to syncLogManager`() {
        viewModel.clearLogs()
        verify { syncLogManager.clearLogs() }
    }

    @Test
    fun `overallProgress sums counts by sync state`() = runTest {
        every { syncDao.getSyncStatus() } returns flowOf(
            listOf(
                SyncStatusCache(1, "Ben", SyncState.SYNCED, 5),
                SyncStatusCache(2, "HH", SyncState.UNSYNCED, 3)
            )
        )
        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        val result = vm.overallProgress
        var pair: Pair<Int, Int>? = null
        result.collect { pair = it }
        assertEquals(5, pair?.first)
        assertEquals(8, pair?.second)
    }

    private fun failedWorkInfo(
        outputName: String? = null,
        outputError: String? = null,
        tags: Set<String> = emptySet(),
        emptyOutput: Boolean = false
    ): WorkInfo {
        val data = mockk<Data>(relaxed = true)
        every { data.getString(BasePushWorker.KEY_WORKER_NAME) } returns outputName
        every { data.getString(BasePushWorker.KEY_ERROR) } returns outputError
        every { data.keyValueMap } returns if (emptyOutput) emptyMap() else mapOf("k" to "v")
        val workInfo = mockk<WorkInfo>(relaxed = true)
        every { workInfo.state } returns WorkInfo.State.FAILED
        every { workInfo.outputData } returns data
        every { workInfo.tags } returns tags
        return workInfo
    }

    @Test
    fun `failedWorkerDetails maps failed workers using outputData name and error`() {
        val info = failedWorkInfo(outputName = "PushWorker", outputError = "network down")
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(listOf(info))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        vm.failedWorkerDetails.observeForever { }
        val details = vm.failedWorkerDetails.value

        assertEquals(1, details?.size)
        assertEquals("PushWorker", details?.get(0)?.workerName)
        assertEquals("network down", details?.get(0)?.error)
    }

    @Test
    fun `failedWorkerDetails falls back to tag derived name when outputData name missing`() {
        val info = failedWorkInfo(
            outputName = null,
            outputError = "boom",
            tags = setOf("org.piramalswasthya.sakhi.work.PullFromAmritWorker")
        )
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(listOf(info))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        vm.failedWorkerDetails.observeForever { }
        val details = vm.failedWorkerDetails.value

        assertEquals("PullFromAmritWorker", details?.get(0)?.workerName)
    }

    @Test
    fun `failedWorkerDetails uses Unknown Worker when no name can be derived`() {
        val info = failedWorkInfo(outputName = null, outputError = "boom", tags = setOf("unrelated.tag"))
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(listOf(info))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        vm.failedWorkerDetails.observeForever { }
        val details = vm.failedWorkerDetails.value

        assertEquals("Unknown Worker", details?.get(0)?.workerName)
    }

    @Test
    fun `failedWorkerDetails reports cascade failure when outputData is empty`() {
        val info = failedWorkInfo(outputName = "PushWorker", outputError = null, emptyOutput = true)
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(listOf(info))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        vm.failedWorkerDetails.observeForever { }
        val details = vm.failedWorkerDetails.value

        assertEquals("Blocked by earlier failure in sync chain", details?.get(0)?.error)
    }

    @Test
    fun `failedWorkerDetails reports no error details when output has data but no error key`() {
        val info = failedWorkInfo(outputName = "PushWorker", outputError = null, emptyOutput = false)
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(listOf(info))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        vm.failedWorkerDetails.observeForever { }
        val details = vm.failedWorkerDetails.value

        assertEquals("No error details available", details?.get(0)?.error)
    }

    @Test
    fun `failedWorkerDetails excludes non-failed workers`() {
        val succeeded = mockk<WorkInfo>(relaxed = true)
        every { succeeded.state } returns WorkInfo.State.SUCCEEDED
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(listOf(succeeded))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)
        vm.failedWorkerDetails.observeForever { }
        val details = vm.failedWorkerDetails.value

        assertTrue(details?.isEmpty() == true)
    }

    @Test
    fun `exportLogs sets exportIntent when exporter returns an intent`() = runTest {
        val intent = mockk<Intent>(relaxed = true)
        every { syncLogExporter.createShareIntent(any()) } returns intent

        viewModel.exportLogs()
        advanceUntilIdle()

        assertEquals(intent, viewModel.exportIntent.value)
    }

    @Test
    fun `exportLogs sets exportEmpty when exporter returns null`() = runTest {
        every { syncLogExporter.createShareIntent(any()) } returns null

        viewModel.exportLogs()
        advanceUntilIdle()

        assertTrue(viewModel.exportEmpty.value)
    }

    @Test
    fun `onExportHandled clears exportIntent`() = runTest {
        val intent = mockk<Intent>(relaxed = true)
        every { syncLogExporter.createShareIntent(any()) } returns intent
        viewModel.exportLogs()
        advanceUntilIdle()

        viewModel.onExportHandled()

        assertEquals(null, viewModel.exportIntent.value)
    }

    @Test
    fun `onExportEmptyHandled resets exportEmpty`() = runTest {
        every { syncLogExporter.createShareIntent(any()) } returns null
        viewModel.exportLogs()
        advanceUntilIdle()

        viewModel.onExportEmptyHandled()

        assertEquals(false, viewModel.exportEmpty.value)
    }

    @Test
    fun `syncLogs reflects entries from syncLogManager`() {
        val entry = SyncLogEntry(1L, 100L, org.piramalswasthya.sakhi.model.LogLevel.INFO, "TAG", "message")
        every { syncLogManager.logs } returns MutableStateFlow(listOf(entry))

        val vm = SyncDashboardViewModel(syncDao, preferenceDao, syncLogManager, syncLogExporter, application)

        assertEquals(listOf(entry), vm.syncLogs.value)
    }
}
