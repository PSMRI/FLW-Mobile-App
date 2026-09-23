package org.piramalswasthya.sakhi.utils

import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

/**
 * Centralizes the user-scoped FCM topic convention and its subscribe/unsubscribe lifecycle.
 *
 * FCM topic subscriptions are **device-scoped and sticky** — they survive logout, app restarts
 * and credential wipes. On a shared/reassigned device that means a subsequent user keeps
 * receiving the previous user's notifications unless we explicitly unsubscribe on logout.
 *
 * These calls need no [android.content.Context] — only the user id — so they are safe to invoke
 * from Activities, Fragments and ViewModels alike. Callers MUST unsubscribe **before** the
 * logged-in user is cleared from preferences (otherwise the user id — and hence the topic name —
 * is gone).
 */
object FcmTopicManager {

    /** The per-user topic every device subscribes to while that user is logged in. */
    fun topicForUser(userId: Any?): String = "user_$userId"

    fun subscribe(userId: Any?) {
        if (userId == null) return
        val topic = topicForUser(userId)
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                Timber.d("FCM subscribe to $topic success=${task.isSuccessful}")
            }
    }

    fun unsubscribe(userId: Any?) {
        if (userId == null) return
        val topic = topicForUser(userId)
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                Timber.d("FCM unsubscribe from $topic success=${task.isSuccessful}")
            }
    }
}