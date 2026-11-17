package org.piramalswasthya.sakhi.helpers

import android.content.Context
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.badges.domain.TaskCompletionBus
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider.State
import org.piramalswasthya.sakhi.notifications.NotificationScheduler
import org.piramalswasthya.sakhi.work.WorkerUtils

/** FLW-1251: background badge work and the 21:00 alarm follow the server switches. */
class GamificationRuntimeTest {

    private val context: Context = mockk(relaxed = true)
    private val bus: TaskCompletionBus = mockk(relaxed = true)
    private val scheduler: NotificationScheduler = mockk(relaxed = true)
    private lateinit var runtime: GamificationRuntime

    @Before
    fun setUp() {
        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerBadgeWorkers(any()) } just Runs
        every { WorkerUtils.cancelBadgeWorkers(any()) } just Runs
        runtime = GamificationRuntime(context, bus, scheduler)
    }

    @After
    fun tearDown() {
        unmockkObject(WorkerUtils)
    }

    @Test
    fun `all off cancels badge work, stops the watcher and removes the alarm`() {
        runtime.apply(State.ALL_OFF)

        verify { WorkerUtils.cancelBadgeWorkers(context) }
        verify { bus.stop() }
        verify { scheduler.cancel() }
        verify(exactly = 0) { WorkerUtils.triggerBadgeWorkers(any()) }
        verify(exactly = 0) { bus.start() }
        verify(exactly = 0) { scheduler.schedule() }
    }

    @Test
    fun `all on starts badge work, the watcher and the alarm`() {
        runtime.apply(State.ALL_ON)

        verify { WorkerUtils.triggerBadgeWorkers(context) }
        verify { bus.start() }
        verify { scheduler.schedule() }
        verify(exactly = 0) { WorkerUtils.cancelBadgeWorkers(any()) }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `switching off after on cancels everything that was started`() {
        runtime.apply(State.ALL_ON)
        runtime.apply(State.ALL_OFF)

        verify { WorkerUtils.cancelBadgeWorkers(context) }
        verify { bus.stop() }
        verify { scheduler.cancel() }
    }

    @Test
    fun `each part is switched on its own`() {
        runtime.apply(State(badges = false, notifications = true))

        verify { WorkerUtils.cancelBadgeWorkers(context) }
        verify { bus.stop() }
        verify { scheduler.schedule() }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `the same state is applied only once`() {
        assertTrue(runtime.apply(State.ALL_ON))
        assertFalse(runtime.apply(State.ALL_ON))

        verify(exactly = 1) { WorkerUtils.triggerBadgeWorkers(any()) }
        verify(exactly = 1) { bus.start() }
        verify(exactly = 1) { scheduler.schedule() }
    }

    @Test
    fun `a recap-only change does not touch badge work or the alarm again`() {
        runtime.apply(State.ALL_ON)
        runtime.apply(State(badges = true, monthlyRecap = false, notifications = true))

        // applied again, but every call is idempotent and nothing was cancelled
        verify(exactly = 0) { WorkerUtils.cancelBadgeWorkers(any()) }
        verify(exactly = 0) { scheduler.cancel() }
    }
}
