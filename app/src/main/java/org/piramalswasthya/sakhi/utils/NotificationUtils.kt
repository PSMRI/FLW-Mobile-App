package org.piramalswasthya.sakhi.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import org.piramalswasthya.sakhi.R
import timber.log.Timber
import java.io.File

object NotificationUtils {

    fun showDownloadedFile(appContext: Context, fileName: String, title: String) {
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(title, title, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(appContext, title)
            .setSmallIcon(R.drawable.ic_download)
            .setChannelId(title)
            .setContentTitle(fileName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(1, notificationBuilder.build())

        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)
        Timber.d(file.path)

        MediaScannerConnection.scanFile(
            appContext,
            arrayOf(file.toString()),
            null,
        ) { _, uri ->
            run {
                // Update the notification after the file has been scanned
                notificationBuilder.setContentTitle(fileName)
                    .setContentText(title)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            appContext,
                            0,
                            Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                notificationManager.notify(1, notificationBuilder.build())
            }
        }
    }
}