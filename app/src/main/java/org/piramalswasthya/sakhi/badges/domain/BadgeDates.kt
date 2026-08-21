package org.piramalswasthya.sakhi.badges.domain

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Device-local period keys (LLD §5.2 "Clock skew / week boundaries"):
 * work done at 23:59 on the last day of a period counts for that period.
 * Weeks are ISO-8601 (Monday start, week 1 contains Jan 4th).
 */
object BadgeDates {

    private fun isoCalendar(time: Long): GregorianCalendar =
        GregorianCalendar().apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
            timeInMillis = time
        }

    /** e.g. "2026-W34" */
    fun weekKey(time: Long): String {
        val cal = isoCalendar(time)
        return String.format(
            Locale.US, "%04d-W%02d", cal.weekYear, cal.get(Calendar.WEEK_OF_YEAR)
        )
    }

    /** e.g. "2026-08" */
    fun monthKey(time: Long): String {
        val cal = isoCalendar(time)
        return String.format(
            Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1
        )
    }

    /** e.g. "2026-Q3" */
    fun quarterKey(time: Long): String {
        val cal = isoCalendar(time)
        return String.format(
            Locale.US, "%04d-Q%d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) / 3 + 1
        )
    }

    fun weekKeyAt(offsetWeeks: Int, now: Long): String {
        val cal = isoCalendar(now)
        cal.add(Calendar.WEEK_OF_YEAR, offsetWeeks)
        return weekKey(cal.timeInMillis)
    }

    fun monthKeyAt(offsetMonths: Int, now: Long): String {
        val cal = isoCalendar(now)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, offsetMonths)
        return monthKey(cal.timeInMillis)
    }

    fun weekIntervalAt(offsetWeeks: Int, now: Long): LongRange {
        val cal = isoCalendar(now)
        cal.add(Calendar.WEEK_OF_YEAR, offsetWeeks)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        startOfDay(cal)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return start until cal.timeInMillis
    }

    fun monthIntervalAt(offsetMonths: Int, now: Long): LongRange {
        val cal = isoCalendar(now)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startOfDay(cal)
        cal.add(Calendar.MONTH, offsetMonths)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start until cal.timeInMillis
    }

    fun quarterStart(now: Long): Long {
        val cal = isoCalendar(now)
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startOfDay(cal)
        return cal.timeInMillis
    }

    private fun startOfDay(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
}
