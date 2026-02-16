package org.piramalswasthya.sakhi.work

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

// Base class for all push workers. Provides foreground service protection
// (prevents OS from killing workers on aggressive-battery devices like
// Xiaomi MIUI, Oppo ColorOS, Vivo FunTouchOS), centralized token
// initialization, and structured error handling.
//
// Subclasses only need to implement doSyncWork() and provide workerName
// and preferenceDao (via Hilt DI with `override val`).
abstract class BasePushWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val MAX_RETRY_COUNT = 5
    }

    // Subclass provides via Hilt DI constructor with `override val preferenceDao`
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
            // Foreground may fail if app is in background on some OEMs — continue anyway
            Timber.w(e, "[$workerName] Could not set foreground notification")
        }
        return try {
            doSyncWork()
        } catch (e: SocketTimeoutException) {
            Timber.e("[$workerName] Socket timeout, will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "[$workerName] Sync failed")
            Result.failure()
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

        // Android 14+ (SDK 34) requires foreground service type declaration
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
