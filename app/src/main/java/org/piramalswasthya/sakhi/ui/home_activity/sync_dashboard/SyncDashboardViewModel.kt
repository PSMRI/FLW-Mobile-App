package org.piramalswasthya.sakhi.ui.home_activity.sync_dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.SyncLogManager
import org.piramalswasthya.sakhi.model.FailedWorkerInfo
import org.piramalswasthya.sakhi.model.SyncLogEntry
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import org.piramalswasthya.sakhi.work.BasePushWorker
import org.piramalswasthya.sakhi.work.WorkerUtils
import javax.inject.Inject

@HiltViewModel
class SyncDashboardViewModel @Inject constructor(
    syncDao: SyncDao,
    private val preferenceDao: PreferenceDao,
    private val syncLogManager: SyncLogManager,
    application: Application
) : AndroidViewModel(application) {

    // Tab 1: Status
    val syncStatus: Flow<List<SyncStatusCache>> = syncDao.getSyncStatus()

    val overallProgress: Flow<Pair<Int, Int>> = syncStatus.map { list ->
        val synced = list.filter { it.syncState == org.piramalswasthya.sakhi.database.room.SyncState.SYNCED }
            .sumOf { it.count }
        val total = list.sumOf { it.count }
        Pair(synced, total)
    }

    val workerStates: LiveData<List<WorkInfo>> = WorkManager.getInstance(application)
        .getWorkInfosLiveData(WorkQuery.fromUniqueWorkNames(WorkerUtils.pushWorkerUniqueName, WorkerUtils.pullWorkerUniqueName, WorkerUtils.syncWorkerUniqueName))

    val failedWorkerDetails: LiveData<List<FailedWorkerInfo>> = workerStates.map { workInfoList ->
        workInfoList
            .filter { it.state == WorkInfo.State.FAILED }
            .map { workInfo ->
                val outputName = workInfo.outputData.getString(BasePushWorker.KEY_WORKER_NAME)
                val error = workInfo.outputData.getString(BasePushWorker.KEY_ERROR)
                // Fallback: extract class name from tags if outputData wasn't set
                val name = outputName ?: workInfo.tags
                    .firstOrNull { it.startsWith("org.piramalswasthya.sakhi.work.") }
                    ?.substringAfterLast(".")
                    ?: "Unknown Worker"
                FailedWorkerInfo(
                    workerName = name,
                    error = error ?: "No error details available"
                )
            }
    }

    // Tab 2: Logs
    val syncLogs: StateFlow<List<SyncLogEntry>> = syncLogManager.logs

    fun clearLogs() = syncLogManager.clearLogs()

    // Localization helpers (same pattern as SyncViewModel)
    val lang = preferenceDao.getCurrentLanguage()

    fun getLocalNames(context: Context): Array<String> {
        return getLocalizedResources(context, lang).getStringArray(R.array.sync_records)
    }

    fun getEnglishNames(context: Context): Array<String> {
        return getLocalizedResources(context, Languages.ENGLISH).getStringArray(R.array.sync_records)
    }
}
