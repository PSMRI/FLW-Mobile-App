package org.piramalswasthya.sakhi.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.app.NotificationCompat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class NotificationUtilsTest {

    private val context: Context = mockk(relaxed = true)
    private val notificationManager: NotificationManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager

        mockkStatic(Environment::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns
            File(System.getProperty("java.io.tmpdir"), "notification_utils_test")

        mockkConstructor(NotificationCompat.Builder::class)
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setChannelId(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setContentTitle(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setPriority(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setContentText(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setAutoCancel(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setOngoing(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setContentIntent(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().build() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun showDownloadedFile_postsInitialNotification() {
        mockkStatic(MediaScannerConnection::class)
        every { MediaScannerConnection.scanFile(any(), any(), any(), any()) } just Runs

        NotificationUtils.showDownloadedFile(context, "report.pdf", "Report")

        verify(atLeast = 1) { notificationManager.notify(any(), any()) }
    }

    @Test
    fun showDownloadedFile_updatesNotification_whenScanCompletes() {
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk(relaxed = true)

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setDataAndType(any(), any()) } answers { self as Intent }
        every { anyConstructed<Intent>().addFlags(any()) } answers { self as Intent }

        val uri: Uri = mockk(relaxed = true)
        mockkStatic(MediaScannerConnection::class)
        every {
            MediaScannerConnection.scanFile(any(), any(), any(), any())
        } answers {
            val listener = arg<MediaScannerConnection.OnScanCompletedListener>(3)
            listener.onScanCompleted("report.pdf", uri)
        }

        NotificationUtils.showDownloadedFile(context, "report.pdf", "Report")

        verify(exactly = 2) { notificationManager.notify(any(), any()) }
    }
}
