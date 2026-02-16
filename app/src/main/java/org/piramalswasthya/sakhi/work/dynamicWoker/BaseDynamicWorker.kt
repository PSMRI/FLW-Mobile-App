package org.piramalswasthya.sakhi.work.dynamicWoker

import android.app.ServiceInfo
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Base class for all dynamic workers (dynamicWoker/ directory).
// Provides foreground service protection (prevents OS from killing workers
// on aggressive-battery devices like Xiaomi MIUI, Oppo ColorOS, Vivo
// FunTouchOS), centralized token initialization, and structured error
// handling with a 3-retry limit for transient failures.
//
// Subclasses implement doSyncWork() and provide workerName and preferenceDao
// (via Hilt DI with `override val`).
//
// Note: initTokens() and createForegroundInfo() mirror BasePushWorker
// (work/ package). Intentionally not extracted to a shared base to avoid
// coupling the two worker hierarchies.
abstract class BaseDynamicWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val MAX_RETRY_COUNT = 5
    }

    protected abstract val preferenceDao: PreferenceDao
    abstract val workerName: String

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRY_COUNT) {
            Timber.e("[$workerName] Max retries ($MAX_RETRY_COUNT) exceeded, giving up")
            return Result.failure()
        }
        initTokens()
        try {
            setForeground(createForegroundInfo("Syncing $workerName..."))
        } catch (e: Throwable) {
            Timber.w(e, "[$workerName] Could not set foreground notification")
        }
        return try {
            doSyncWork()
        } catch (e: IllegalStateException) {
            Timber.e(e, "[$workerName] failed: ${e.message}")
            Result.failure()
        } catch (e: UnknownHostException) {
            Timber.w(e, "[$workerName] Network unavailable, will retry")
            Result.retry()
        } catch (e: SocketTimeoutException) {
            Timber.e("[$workerName] Socket timeout, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "[$workerName] failed with unexpected error")
            Result.retry()
        }
    }

    abstract suspend fun doSyncWork(): Result

    private fun initTokens() {
        if (TokenInsertTmcInterceptor.getToken() == "")
            preferenceDao.getAmritToken()?.let { TokenInsertTmcInterceptor.setToken(it) }
        if (TokenInsertTmcInterceptor.getJwt() == "")
            preferenceDao.getJWTAmritToken()?.let { TokenInsertTmcInterceptor.setJwt(it) }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_sync_channel_id)
        )
            .setContentTitle("Data Sync")
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                0,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(0, notification)
        }
    }
}
