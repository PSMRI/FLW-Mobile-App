package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.NotificationEntity

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notifications: List<NotificationEntity>)

    /** Visible list for a user: not soft-cleared, newest first. */
    @Query("SELECT * FROM NOTIFICATION WHERE userId = :userId AND cleared = 0 ORDER BY notificationId DESC")
    fun getForUser(userId: Long): Flow<List<NotificationEntity>>

    /** Badge count: unread and not soft-cleared. */
    @Query("SELECT COUNT(*) FROM NOTIFICATION WHERE userId = :userId AND cleared = 0 AND read = 0")
    fun unreadCount(userId: Long): Flow<Int>

    @Query("UPDATE NOTIFICATION SET read = 1 WHERE notificationId IN (:ids)")
    suspend fun markRead(ids: List<Long>)

    @Query("UPDATE NOTIFICATION SET read = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: Long)

    @Query("UPDATE NOTIFICATION SET cleared = 1 WHERE notificationId IN (:ids)")
    suspend fun softClear(ids: List<Long>)

    @Query("UPDATE NOTIFICATION SET cleared = 1 WHERE userId = :userId")
    suspend fun softClearAll(userId: Long)

    @Query("UPDATE NOTIFICATION SET viewed = 1 WHERE notificationId IN (:ids)")
    suspend fun markViewed(ids: List<Long>)

    /** Housekeeping: drop rows older than [ts] (epoch millis). */
    @Query("DELETE FROM NOTIFICATION WHERE createdTs < :ts")
    suspend fun deleteOlderThan(ts: Long)
}