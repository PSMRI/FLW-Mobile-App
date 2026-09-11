package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.sakhi.model.FormSaveLogCache
import org.piramalswasthya.sakhi.model.NotifHistoryCache
import org.piramalswasthya.sakhi.model.NotifTemplateCache

@Dao
interface EveningNotifDao {

    // ─── form_save_log ───
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun logSaves(saves: List<FormSaveLogCache>)

    @Query("SELECT COUNT(*) FROM form_save_log WHERE dateKey = :dateKey")
    suspend fun countForDay(dateKey: String): Int

    @Query("SELECT COUNT(*) FROM form_save_log")
    suspend fun lifetimeCount(): Long

    // ─── notification_templates ───
    @Query("DELETE FROM notification_templates")
    suspend fun clearTemplates()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<NotifTemplateCache>)

    /**
     * Swap the library in one transaction.
     *
     * Apart, the gap between the delete and the insert is a state the 9 PM alarm can read:
     * it finds no template for the bucket and the evening notification is silently skipped
     * for that day. A crash in the gap leaves the table empty until the next version bump,
     * which - because sync only applies a strictly higher version - may be never.
     */
    @Transaction
    suspend fun replaceTemplates(templates: List<NotifTemplateCache>) {
        clearTemplates()
        insertTemplates(templates)
    }

    @Query("SELECT * FROM notification_templates WHERE bucket = :bucket AND language = :language")
    suspend fun templatesFor(bucket: String, language: String): List<NotifTemplateCache>

    @Query("SELECT MAX(libraryVersion) FROM notification_templates")
    suspend fun libraryVersion(): Int?

    // ─── notification_history ───
    @Insert
    suspend fun logShown(entry: NotifHistoryCache)

    @Query("SELECT COUNT(*) FROM notification_history WHERE shownDate = :dateKey")
    suspend fun shownCountForDay(dateKey: String): Int

    @Query("SELECT templateId FROM notification_history WHERE bucket = :bucket ORDER BY id DESC LIMIT :recent")
    suspend fun recentTemplateIds(bucket: String, recent: Int): List<String>

    @Query("SELECT * FROM notification_history ORDER BY id DESC LIMIT 1")
    suspend fun lastShown(): NotifHistoryCache?
}
