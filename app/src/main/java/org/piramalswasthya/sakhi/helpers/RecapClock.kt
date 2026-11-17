package org.piramalswasthya.sakhi.helpers

import java.util.Calendar
import javax.inject.Inject

/**
 * Small testable time source for the Monthly Recap. Production uses the device
 * clock/timezone; tests supply fixed calendars — no emulator clock changes needed.
 * (Calendar, not java.time: the project has minSdk 25 and no core-library
 * desugaring, matching existing date utilities.)
 */
interface RecapClock {
    fun now(): Calendar
}

class SystemRecapClock @Inject constructor() : RecapClock {
    override fun now(): Calendar = Calendar.getInstance()
}

/**
 * The immediately previous completed calendar month, with its frozen
 * start-inclusive / end-exclusive activity window in device-local time.
 */
data class RecapMonthWindow(
    val year: Int,
    /** 1..12 */
    val month: Int,
    /** Canonical key, e.g. 202606 for June 2026. Numeric — never compared as text. */
    val yearMonth: Int,
    /** Inclusive: first instant of the recap month. */
    val startMillis: Long,
    /** Exclusive: first instant of the following month. */
    val endMillisExclusive: Long,
)

/**
 * Resolves the immediately previous completed calendar month for [now].
 * Handles January → previous-December rollover and leap years via Calendar
 * arithmetic (no hardcoded dates, no string month comparison).
 */
fun previousMonthWindow(now: Calendar): RecapMonthWindow {
    val startOfCurrentMonth = startOfCurrentMonth(now)
    val startOfPreviousMonth = (startOfCurrentMonth.clone() as Calendar).apply {
        add(Calendar.MONTH, -1)
    }
    val year = startOfPreviousMonth.get(Calendar.YEAR)
    val month = startOfPreviousMonth.get(Calendar.MONTH) + 1
    return RecapMonthWindow(
        year = year,
        month = month,
        yearMonth = year * 100 + month,
        startMillis = startOfPreviousMonth.timeInMillis,
        endMillisExclusive = startOfCurrentMonth.timeInMillis,
    )
}

/**
 * Number of calendar days the dashboard strip stays available at the start of
 * each month (days 1..[ACTIVE_WINDOW_DAYS]). Single source of truth for the
 * product rule; a future backend config can replace this without touching the
 * strip. Kept as a value, not scattered literals.
 */
const val ACTIVE_WINDOW_DAYS = 7

/**
 * True when [now] falls in the current month's active strip window:
 * `[00:00 on day 1, 00:00 on day ACTIVE_WINDOW_DAYS+1)` in device-local time
 * (start-inclusive, end-exclusive). e.g. with 7 days, open on days 1..7 and
 * closed from 00:00 of the 8th. This is a fixed calendar window — it does NOT
 * depend on when the ASHA first opened the app or tapped the strip.
 */
fun isMonthlyRecapWindowOpen(now: Calendar): Boolean {
    val windowStart = startOfCurrentMonth(now)
    val windowEndExclusive = (windowStart.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, ACTIVE_WINDOW_DAYS)
    }
    val nowMillis = now.timeInMillis
    return nowMillis >= windowStart.timeInMillis && nowMillis < windowEndExclusive.timeInMillis
}

/** First instant (00:00) of [now]'s calendar month, in device-local time. */
private fun startOfCurrentMonth(now: Calendar): Calendar =
    (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
