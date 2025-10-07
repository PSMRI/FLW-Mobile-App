package org.piramalswasthya.sakhi.ui.Fragments

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class MaaMeetingRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("maa_meeting_prefs", Context.MODE_PRIVATE)

    private fun key(year: Int, quarter: Int) = "meeting_${year}_Q$quarter"

    fun hasMeetingInSameQuarter(cal: Calendar): Boolean {
        val y = cal.get(Calendar.YEAR)
        val q = ((cal.get(Calendar.MONTH)) / 3) + 1
        return prefs.getBoolean(key(y, q), false)
    }

    fun recordMeeting(cal: Calendar) {
        val y = cal.get(Calendar.YEAR)
        val q = ((cal.get(Calendar.MONTH)) / 3) + 1
        prefs.edit().putBoolean(key(y, q), true).apply()
    }
}


