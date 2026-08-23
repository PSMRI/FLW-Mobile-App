package org.piramalswasthya.sakhi.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules, reschedules and cancels daily evening delivery (Notification
 * LLD §3/§7). District timing arrives via sync into TimingStore
 * (SharedPreferences); default is 9 PM until configured. Alarms are re-armed
 * on every app open, after each delivery, and on boot.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** "HH:mm", e.g. "21:00". Called by TemplateSyncHandler on timing change. */
    fun updateEveningTime(time: String) {
        if (time.matches(Regex("""\d{1,2}:\d{2}""")) && time != eveningTime()) {
            prefs.edit().putString(KEY_TIME, time).apply()
            schedule() // cancel pending schedule and re-schedule with latest timing (LLD §6)
        }
    }

    fun eveningTime(): String = prefs.getString(KEY_TIME, DEFAULT_TIME) ?: DEFAULT_TIME

    /** Idempotent: always (re)sets the alarm for the next evening occurrence. */
    fun schedule() {
        try {
            val (hour, minute) = eveningTime().split(":").map { it.toInt() }
            val at = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pending = PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(context, EveningNotificationReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // exact when permitted, inexact otherwise — a few minutes' drift is fine
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            }
            Timber.d("EveningNotif: scheduled for $at")
        } catch (e: Exception) {
            Timber.e(e, "EveningNotif: scheduling failed")
        }
    }

    companion object {
        private const val PREFS = "evening_notif_timing"
        private const val KEY_TIME = "evening_time"
        private const val DEFAULT_TIME = "21:00"
        private const val REQUEST_CODE = 20002
    }
}
