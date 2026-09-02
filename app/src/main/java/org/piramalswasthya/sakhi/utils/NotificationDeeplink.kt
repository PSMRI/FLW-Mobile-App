package org.piramalswasthya.sakhi.utils

import android.content.Intent
import android.os.SystemClock
import org.piramalswasthya.sakhi.model.NotificationKeys
import timber.log.Timber

/** Routing context carried by a notification tap. */
data class NotificationDeeplink(
    val navTarget: String?,
    val eventType: String?,
    val notificationId: Long
)

/**
 * Extracts the routing context from a notification-tap Intent, covering BOTH delivery paths:
 *
 *  1. **Our own tray notification** (`FBMessaging.showNotification`) — `EXTRA_*` keys, flagged with
 *     [NotificationKeys.EXTRA_FROM_NOTIFICATION]. Only used for data-only pushes, and for any push
 *     received while the app is in the foreground.
 *  2. **A `notification`-type push displayed by the Firebase SDK itself** while the app is in the
 *     background or killed. `onMessageReceived` never runs, so our PendingIntent doesn't exist; the
 *     SDK launches the LAUNCHER activity (LoginActivity here) and copies the push's `data` payload
 *     into the Intent extras as raw snake_case strings.
 *
 * Returns null when the Intent isn't a notification tap. The keys are consumed so a re-delivered
 * Intent (configuration change, process-death restore from recents) can't route a second time.
 */
fun notificationDeeplinkFrom(intent: Intent?): NotificationDeeplink? {
    if (intent == null) return null

    val fromOurTray = intent.getBooleanExtra(NotificationKeys.EXTRA_FROM_NOTIFICATION, false)
    val navTarget = intent.getStringExtra(NotificationKeys.EXTRA_NAV_ID)
        ?: intent.getStringExtra(NotificationKeys.NAV_ID)
        ?: intent.getStringExtra(NotificationKeys.REDIRECT)
    val eventType = intent.getStringExtra(NotificationKeys.EXTRA_EVENT_TYPE)
        ?: intent.getStringExtra(NotificationKeys.NOTIFICATION_TYPE)
    if (!fromOurTray && navTarget == null && eventType == null) return null

    val notificationId = if (intent.hasExtra(NotificationKeys.EXTRA_NOTIFICATION_ID)) {
        intent.getLongExtra(NotificationKeys.EXTRA_NOTIFICATION_ID, -1L)
    } else {
        // SDK-displayed path: every data value arrives stringified.
        intent.getStringExtra(NotificationKeys.NOTIFICATION_ID)?.toLongOrNull() ?: -1L
    }

    listOf(
        NotificationKeys.EXTRA_FROM_NOTIFICATION,
        NotificationKeys.EXTRA_NAV_ID,
        NotificationKeys.EXTRA_EVENT_TYPE,
        NotificationKeys.EXTRA_NOTIFICATION_ID,
        NotificationKeys.NAV_ID,
        NotificationKeys.REDIRECT,
        NotificationKeys.NOTIFICATION_TYPE,
        NotificationKeys.NOTIFICATION_ID
    ).forEach { intent.removeExtra(it) }

    return NotificationDeeplink(navTarget, eventType, notificationId)
        .also { Timber.d("NAVTRACE 2/4 extracted from tap intent: $it") }
}

/**
 * Holds a deeplink captured by the LAUNCHER activity until the screen that can act on it exists.
 *
 * An SDK-displayed notification tap lands on `LoginActivity`, which reaches `HomeActivity` only
 * after a multi-hop start-up (sign-in → ServiceLocationActivity → HomeActivity). Threading extras
 * through every hop would be fragile, so the launcher parks the deeplink here and HomeActivity
 * consumes it whenever it is finally created.
 *
 * Deliberately in-memory only: a deeplink must not survive process death and surprise the user on
 * an unrelated launch. It also expires after [TTL_MS] so a tap that was never followed through
 * can't navigate much later in the same session.
 */
object PendingNotificationDeeplink {

    private const val TTL_MS = 5 * 60 * 1000L

    @Volatile
    private var pending: NotificationDeeplink? = null

    @Volatile
    private var capturedAtElapsed = 0L

    fun capture(intent: Intent?) {
        val deeplink = notificationDeeplinkFrom(intent) ?: return
        pending = deeplink
        capturedAtElapsed = SystemClock.elapsedRealtime()
        Timber.d("Parked notification deeplink from launcher: $deeplink")
    }

    fun consume(): NotificationDeeplink? {
        val deeplink = pending ?: return null
        clear()
        if (SystemClock.elapsedRealtime() - capturedAtElapsed > TTL_MS) {
            Timber.d("Discarding expired notification deeplink: $deeplink")
            return null
        }
        return deeplink
    }

    fun clear() {
        pending = null
        capturedAtElapsed = 0L
    }
}
