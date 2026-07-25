package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.NotificationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.model.NotificationEntity
import org.piramalswasthya.sakhi.model.toDomain
import org.piramalswasthya.sakhi.model.toEntities
import org.piramalswasthya.sakhi.network.AmritApiService
import timber.log.Timber
import java.net.SocketTimeoutException
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
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo
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

    suspend fun pullAndSaveNotifications(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser() ?: return@withContext false
            try {
                val response = amritApiService.getNotifications()
                Timber.i("getNotifications123 HTTP=${response.code()} body=${response.body()} error=${response.errorBody()?.string()}")
                val envelope = response.body()
                if (response.code() == 200 && envelope != null) {
                    Timber.i("getNotifications123 statusCode=${envelope.statusCode} status=${envelope.status} dataCount=${envelope.data?.notifications?.size}")
                    when (envelope.statusCode) {
                        200 -> {
                            val rows = envelope.toEntities(
                                userId = user.userId.toLong(),
                                createdTs = System.currentTimeMillis()
                            )
                            Timber.i("getNotifications123 mappedRows=${rows.size}")
                            notificationDao.upsert(rows)
                            return@withContext true
                        }

                        401, 5002 -> {
                            if (userRepo.refreshTokenTmc(user.userName, user.password))
                                throw SocketTimeoutException("Refreshed Token!")
                            else throw IllegalStateException("User Logged out!!")
                        }

                        5000 -> return@withContext true

                        else -> throw IllegalStateException("${envelope.statusCode} received, don't know what to do!?")
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.e("notifications pull error, retrying: $e")
                return@withContext pullAndSaveNotifications()
            } catch (e: Exception) {
                Timber.d("Caught $e at notifications pull!")
                return@withContext false
            }
            false
        }
    }

    suspend fun markRead(notificationId: Long) {
        withContext(Dispatchers.IO) {
            notificationDao.markRead(listOf(notificationId))
            try {
                val response = amritApiService.markNotificationRead(notificationId)
                Timber.i("markAsRead123 id=$notificationId HTTP=${response.code()} body=${response.body()?.string()} error=${response.errorBody()?.string()}")
            } catch (e: Exception) {
                Timber.e(e, "markAsRead failed for $notificationId")
            }
        }
    }

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