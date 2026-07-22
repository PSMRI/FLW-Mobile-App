package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.piramalswasthya.sakhi.database.room.dao.NotificationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.model.NotificationEntity
import org.piramalswasthya.sakhi.model.toDomain
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for in-app notifications, shared by the toolbar bell badge and the
 * notification panel so their state stays in sync.
 *
 * Backed by Room ([NotificationDao]); rows are scoped to the locally logged-in user
 * ([PreferenceDao.getLoggedInUser]). The poll worker (T10) and FCM receiver (T11) upsert rows via
 * [upsert]; the panel reads them through [notifications] / [unreadCount] and mutates local
 * interaction flags through the action methods below. Server sync of those interactions is wired
 * once the list/mark APIs land (T8).
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val preferenceDao: PreferenceDao
) {

    /** Logged-in user id scoping every read/write, or null when no user is logged in. */
    private val userId: Long?
        get() = preferenceDao.getLoggedInUser()?.userId?.toLong()

    /** Visible notifications for the logged-in user, newest first; empty when logged out. */
    val notifications: Flow<List<NotificationDomain>> = flow {
        val id = userId
        if (id == null) {
            emitAll(flowOf(emptyList()))
        } else {
            emitAll(notificationDao.getForUser(id).map { rows -> rows.map { it.toDomain() } })
        }
    }

    /** Unread badge count for the logged-in user; 0 when logged out. */
    val unreadCount: Flow<Int> = flow {
        val id = userId
        if (id == null) emitAll(flowOf(0)) else emitAll(notificationDao.unreadCount(id))
    }

    /** Insert / update rows (idempotent PK = server notificationId). Used by poll (T10) & FCM (T11). */
    suspend fun upsert(notifications: List<NotificationEntity>) = notificationDao.upsert(notifications)

    suspend fun upsert(notification: NotificationEntity) = notificationDao.upsert(notification)

    suspend fun markRead(notificationId: Long) = notificationDao.markRead(listOf(notificationId))

    suspend fun markAllRead() {
        userId?.let { notificationDao.markAllRead(it) }
    }

    /** Panel swipe-to-dismiss: soft-clear so a later poll can't resurrect the row. */
    suspend fun dismiss(notificationId: Long) = notificationDao.softClear(listOf(notificationId))

    suspend fun clearAll() {
        userId?.let { notificationDao.softClearAll(it) }
    }

    suspend fun markViewed(notificationIds: List<Long>) = notificationDao.markViewed(notificationIds)
}