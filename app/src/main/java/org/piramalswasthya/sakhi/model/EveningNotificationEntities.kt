package org.piramalswasthya.sakhi.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Evening Notification module tables (Notification LLD §4).
 * All notification assembly happens on-device from these tables;
 * the server only distributes the versioned template library.
 */

/** Versioned template library; replaced wholesale on sync (replaceAll). */
@Entity(
    tableName = "notification_templates",
    indices = [Index(value = ["bucket", "language"])]
)
data class NotifTemplateCache(
    @PrimaryKey
    val templateId: String,
    /** ZERO / ONE / TWO_PLUS activity bucket */
    val bucket: String,
    /** en / hi / as / bn */
    val language: String,
    val bodyTemplate: String,
    val libraryVersion: Int
)

/** Delivery log: dedupes per-day delivery and drives template rotation. */
@Entity(
    tableName = "notification_history",
    indices = [Index(value = ["bucket"])]
)
data class NotifHistoryCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: String,
    /** "2026-08-23" */
    val shownDate: String,
    val bucket: String
)

/**
 * Eligible activity log. The UNIQUE constraint IS the edit-vs-new dedup rule:
 * the second save of the same (beneficiary, form, day) is ignored.
 */
@Entity(
    tableName = "form_save_log",
    indices = [
        Index(value = ["beneficiaryId", "formType", "dateKey"], unique = true),
        Index(value = ["dateKey"])
    ]
)
data class FormSaveLogCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val formType: String,
    val beneficiaryId: Long,
    val ashaWorkerId: Int,
    val savedAt: Long,
    /** "2026-08-23" */
    val dateKey: String
)
