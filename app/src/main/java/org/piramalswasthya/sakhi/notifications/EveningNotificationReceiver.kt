package org.piramalswasthya.sakhi.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Dependencies fetched via @EntryPoint instead of @AndroidEntryPoint:
 * Hilt 2.48's receiver bytecode injection is incompatible with AGP 8.x
 * javac output paths (fails the ASM transform).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface EveningNotifEntryPoint {
    fun engine(): NotificationEngine
    fun scheduler(): NotificationScheduler
}

/** Fired by AlarmManager at the district's evening time. Assembles offline. */
class EveningNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, EveningNotifEntryPoint::class.java
        )
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                entryPoint.engine().deliverEveningNotification()
                entryPoint.scheduler().schedule() // re-arm for tomorrow
            } finally {
                result.finish()
            }
        }
    }
}
