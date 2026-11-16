package org.piramalswasthya.sakhi.notifications

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotificationSchedulerTest {

    private val store = mutableMapOf<String, String?>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val prefs = mockk<SharedPreferences>()
    private val context = mockk<Context>(relaxed = true)
    private lateinit var scheduler: NotificationScheduler

    @Before
    fun setUp() {
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.getString(any(), any()) } answers { store[firstArg()] ?: secondArg() }
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers {
            store[firstArg()] = secondArg(); editor
        }
        scheduler = NotificationScheduler(context)
    }

    @Test
    fun `defaults to nine pm`() {
        assertEquals("21:00", scheduler.eveningTime())
    }

    @Test
    fun `valid new time is stored and the alarm re-armed`() {
        scheduler.updateEveningTime("19:45")

        assertEquals("19:45", scheduler.eveningTime())
        verify { editor.apply() }
        // schedule() reaches the system alarm service (framework stubs make it a no-op here)
        verify { context.getSystemService(Context.ALARM_SERVICE) }
    }

    @Test
    fun `unchanged time is not rewritten`() {
        scheduler.updateEveningTime("21:00")
        verify(exactly = 0) { editor.putString(any(), any()) }
    }

    @Test
    fun `out of range or malformed times are ignored`() {
        listOf("99:99", "24:00", "12:60", "-1:30", "ab:cd", "21", "21:00:00", "").forEach {
            scheduler.updateEveningTime(it)
        }
        assertEquals("21:00", scheduler.eveningTime())
        verify(exactly = 0) { editor.putString(any(), any()) }
    }

    @Test
    fun `schedule and cancel never throw`() {
        scheduler.schedule()
        scheduler.cancel()
        store["evening_time"] = "garbage"
        scheduler.schedule()
    }
}
