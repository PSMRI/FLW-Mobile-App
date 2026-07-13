package org.piramalswasthya.sakhi.utils

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.UserRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Fetches the current FCM registration token and uploads it to the server for the
 * logged-in user via [UserRepo.saveFirebaseToken].
 *
 * Safe to call from Activities and from [com.google.firebase.messaging.FirebaseMessagingService]
 * (which cannot use `@Inject`) — dependencies are resolved through Hilt's [EntryPointAccessors].
 * No-op when no user is logged in.
 */
object FcmTokenUploader {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FcmEntryPoint {
        fun userRepo(): UserRepo
        fun preferenceDao(): PreferenceDao
    }

    /**
     * @param token an already-known token (e.g. from `onNewToken`); when null the current
     * token is fetched from [FirebaseMessaging].
     */
    fun uploadToken(context: Context, token: String? = null) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, FcmEntryPoint::class.java
        )
        val userId = entryPoint.preferenceDao().getLoggedInUser()?.userId ?: return // not logged in

        if (token != null) {
            push(entryPoint, userId, token)
            return
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { push(entryPoint, userId, it) }
            } else {
                Timber.e(task.exception, "Failed to fetch FCM token")
            }
        }
    }

    private fun push(entryPoint: FcmEntryPoint, userId: Int, token: String) {
        val updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())
        CoroutineScope(Dispatchers.IO).launch {
            entryPoint.userRepo().saveFirebaseToken(userId, token, updatedAt)
        }
    }
}