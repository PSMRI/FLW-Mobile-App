package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.repositories.UwinRepo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class PullUwinFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val uwinRepo: UwinRepo
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullUwinFromAmritWorker"
    }

    override suspend fun doWork(): Result {
        Timber.d("🚀 Starting PullUwinFromAmritWorker...")

        // Safely set foreground notification
        try {
            setForeground(createForegroundInfo("Downloading UWIN Session Data"))
        } catch (t: Throwable) {
            Timber.w(t, "⚠️ Foreground notification setup failed")
        }

        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                // Prepare async tasks (scalable for future expansion)
                val results = awaitAll(
                    async { fetchUwinSessions() }
                )

                val endTime = System.currentTimeMillis()
                val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
                Timber.d("✅ UWIN session download completed in $timeTaken seconds, results=$results")

                if (results.all { it }) Result.success() else Result.failure()
            } catch (e: SQLiteConstraintException) {
                Timber.e(e, "❌ Database constraint issue during UWIN sync")
                Result.failure()
            } catch (e: Exception) {
                Timber.e(e, "❌ Unexpected error during UWIN sync")
                Result.failure()
            }
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            appContext,
            appContext.getString(R.string.notification_sync_channel_id)
        )
            .setContentTitle("Syncing Data")
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .build()

        return ForegroundInfo(0, notification)
    }

    private suspend fun fetchUwinSessions(): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("⬇️ Fetching UWIN sessions from server...")
            uwinRepo.downSyncAndPersist()
            Timber.d("✅ Successfully downloaded and persisted UWIN data.")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Error fetching UWIN sessions from server")
            false
        }
    }
}

