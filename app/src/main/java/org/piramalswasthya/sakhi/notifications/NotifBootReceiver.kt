package org.piramalswasthya.sakhi.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider

/** Alarms don't survive reboot — re-arm the evening delivery. */
class NotifBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, EveningNotifEntryPoint::class.java
        )
        // FLW-1251: re-arm only if the server has the evening notification on for this ASHA.
        if (entryPoint.gamificationConfig().isEnabledForLoggedInUser(
                GamificationConfigProvider.Mechanic.NOTIFICATIONS
            )
        ) {
            entryPoint.scheduler().schedule()
        }
    }
}
