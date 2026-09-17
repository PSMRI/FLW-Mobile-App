package org.piramalswasthya.sakhi.helpers

import android.content.Context

/**
 * Tiny standalone "have we already invited her to this month's recap" flag.
 * Deliberately its own SharedPreferences file rather than a Room table or a
 * PreferenceDao addition — this is diagnostics-adjacent state for ONE worker,
 * not something any other part of the app needs to read.
 *
 * Scoped by (userId, yearMonth) so a shared/re-logged-in device never spams a
 * second ASHA with a different ASHA's recap reminder, and so re-notifying only
 * ever happens once a NEW month's recap exists.
 */
object RecapNotificationState {
    private const val PREFS_NAME = "recap_notification_state"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(userId: Int, yearMonth: Int) = "notified_${userId}_$yearMonth"

    fun hasNotified(context: Context, userId: Int, yearMonth: Int): Boolean =
        prefs(context).getBoolean(key(userId, yearMonth), false)

    fun markNotified(context: Context, userId: Int, yearMonth: Int) {
        prefs(context).edit().putBoolean(key(userId, yearMonth), true).apply()
    }
}
