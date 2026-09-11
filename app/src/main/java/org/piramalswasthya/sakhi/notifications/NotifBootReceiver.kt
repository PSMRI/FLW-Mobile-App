package org.piramalswasthya.sakhi.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors

/** Alarms don't survive reboot — re-arm the evening delivery. */
class NotifBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        EntryPointAccessors.fromApplication(
            context.applicationContext, EveningNotifEntryPoint::class.java
        ).scheduler().schedule()
    }
}
