package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
