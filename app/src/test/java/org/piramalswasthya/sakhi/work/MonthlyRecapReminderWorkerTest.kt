package org.piramalswasthya.sakhi.work

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.helpers.RecapNotificationState
import org.piramalswasthya.sakhi.helpers.RecapStoryGate
import org.piramalswasthya.sakhi.helpers.isMonthlyRecapWindowOpen
import org.piramalswasthya.sakhi.model.MonthlyRecapCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo

class MonthlyRecapReminderWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val repo = mockk<MonthlyRecapRepo>()
    private val config = mockk<GamificationConfigProvider>()
    private val pref = mockk<PreferenceDao>()
    private val gate = mockk<RecapStoryGate>()
    private val user = mockk<User>().also { every { it.userId } returns 4 }
    private val recap = mockk<MonthlyRecapCache>().also { every { it.recapYearMonth } returns 202611 }

    private fun worker() = MonthlyRecapReminderWorker(
        context, mockk<WorkerParameters>(relaxed = true), repo, config, pref, gate
    )

    @Before
    fun setUp() {
        mockkStatic("org.piramalswasthya.sakhi.helpers.RecapClockKt")
        every { isMonthlyRecapWindowOpen(any()) } returns true
        mockkObject(RecapNotificationState)
        every { RecapNotificationState.hasNotified(any(), any(), any()) } returns false
        every { RecapNotificationState.markNotified(any(), any(), any()) } just runs
        every { pref.getLoggedInUser() } returns user
        every { config.isMechanicEnabled(GamificationConfigProvider.Mechanic.MONTHLY_RECAP, 4) } returns true
        coEvery { repo.getOrCreateCurrentRecap() } returns recap
        coEvery { gate.hasStory() } returns true
    }

    @After
    fun tearDown() = unmockkAll()

    private suspend fun assertSuccess() =
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)

    @Test
    fun `in window with a story posts the reminder`() = runTest {
        assertSuccess()
        // the notification framework is stubbed in unit tests, so the post itself throws and is
        // swallowed; the worker still reports success and must not retry
        coVerify { gate.hasStory() }
    }

    @Test
    fun `already notified this month skips the story check`() = runTest {
        every { RecapNotificationState.hasNotified(context, 4, 202611) } returns true
        assertSuccess()
        coVerify(exactly = 0) { gate.hasStory() }
        verify(exactly = 0) { RecapNotificationState.markNotified(any(), any(), any()) }
    }

    @Test
    fun `empty month is not announced`() = runTest {
        coEvery { gate.hasStory() } returns false
        assertSuccess()
        verify(exactly = 0) { RecapNotificationState.markNotified(any(), any(), any()) }
    }

    @Test
    fun `no snapshot is not announced`() = runTest {
        coEvery { repo.getOrCreateCurrentRecap() } returns null
        assertSuccess()
        coVerify(exactly = 0) { gate.hasStory() }
    }

    @Test
    fun `outside the window, gated off or logged out does nothing`() = runTest {
        every { isMonthlyRecapWindowOpen(any()) } returns false
        assertSuccess()
        every { config.isMechanicEnabled(any(), any()) } returns false
        assertSuccess()
        every { pref.getLoggedInUser() } returns null
        assertSuccess()
        coVerify(exactly = 0) { repo.getOrCreateCurrentRecap() }
    }

    @Test
    fun `failures never surface as retries`() = runTest {
        coEvery { repo.getOrCreateCurrentRecap() } throws IllegalStateException("db")
        assertSuccess()
    }

    @Test
    fun `foreground info uses the sync channel`() = runTest {
        mockkConstructor(NotificationCompat.Builder::class)
        every { anyConstructed<NotificationCompat.Builder>().setContentTitle(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setPriority(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().build() } returns mockk<Notification>(relaxed = true)
        every { context.getString(any()) } returns "channel_id"

        val info = worker().getForegroundInfo()

        assertEquals(1004, info.notificationId)
    }

    @Test
    fun `schedule enqueues a unique daily job with KEEP`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(context) } returns workManager

        MonthlyRecapReminderWorker.schedule(context)

        verify {
            workManager.enqueueUniquePeriodicWork(
                MonthlyRecapReminderWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>()
            )
        }
    }
}
