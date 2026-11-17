package org.piramalswasthya.sakhi.helpers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.sakhi.badges.domain.TaskCompletionBus
import org.piramalswasthya.sakhi.notifications.NotificationScheduler
import org.piramalswasthya.sakhi.work.WorkerUtils
import javax.inject.Inject

/**
 * FLW-1251: starts or stops the background parts of gamification to match the server
 * switches ([GamificationConfigProvider.State]).
 *
 * - Badges ON: badge workers + the health-table watcher run. OFF: both cancelled.
 * - Evening notification ON: the 21:00 alarm is armed. OFF: it is removed.
 * - Monthly Recap needs nothing here: its card and worker check the switch themselves.
 *
 * Local badge / recap data is never deleted, so switching back on resumes as before.
 *
 * Deliberately NOT a singleton: HomeActivity gets a fresh instance, so after a logout
 * (which cancels all work) the next Home applies the state again instead of thinking it
 * already did.
 */
class GamificationRuntime @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskCompletionBus: TaskCompletionBus,
    private val eveningNotificationScheduler: NotificationScheduler,
) {

    private var applied: GamificationConfigProvider.State? = null

    /** Applies [state]; does nothing when it equals the last applied one. Returns true if applied. */
    fun apply(state: GamificationConfigProvider.State): Boolean {
        if (state == applied) return false
        applied = state

        if (state.badges) {
            WorkerUtils.triggerBadgeWorkers(context)
            taskCompletionBus.start()
        } else {
            WorkerUtils.cancelBadgeWorkers(context)
            taskCompletionBus.stop()
        }

        if (state.notifications) {
            eveningNotificationScheduler.schedule()
        } else {
            eveningNotificationScheduler.cancel()
        }
        return true
    }
}
