package org.piramalswasthya.sakhi.notifications

import org.json.JSONArray
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.model.NotifTemplateCache
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Template/timing sync (Notification LLD §7). Piggybacks on the existing pull
 * payload — no new endpoints, no new workers. Call [handlePullPayload] from
 * the pull path once the server adds the fields; a missing/malformed payload
 * is a graceful no-op and the module continues on bundled defaults.
 *
 * Expected fields: notif_library_version (Int), notif_library_data (JSON array
 * of {template_id, bucket, language, body_template}), notif_evening_time ("HH:mm").
 */
@Singleton
class TemplateSyncHandler @Inject constructor(
    private val dao: EveningNotifDao,
    private val scheduler: NotificationScheduler
) {

    suspend fun handlePullPayload(
        libraryVersion: Int?,
        libraryDataJson: String?,
        eveningTime: String?
    ) {
        try {
            eveningTime?.let { scheduler.updateEveningTime(it) }

            if (libraryVersion == null || libraryDataJson.isNullOrBlank()) return
            val localVersion = dao.libraryVersion() ?: 0
            if (libraryVersion <= localVersion) return

            val array = JSONArray(libraryDataJson)
            // All or nothing. Dropping the malformed entries and keeping the rest installs a
            // partial library under a version number that claims to be complete - and since
            // sync only applies a strictly higher version, whichever buckets went missing
            // stay missing until the next bump, showing the ASHA nothing on those evenings.
            val templates = ArrayList<NotifTemplateCache>(array.length())
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i)
                val templateId = o?.optString("template_id").orEmpty()
                val bucket = o?.optString("bucket").orEmpty()
                val body = o?.optString("body_template").orEmpty()
                if (templateId.isEmpty() || bucket.isEmpty() || body.isEmpty()) {
                    Timber.w(
                        "EveningNotif: template library v%d rejected - entry %d is incomplete",
                        libraryVersion, i
                    )
                    return
                }
                templates += NotifTemplateCache(
                    templateId = templateId,
                    bucket = bucket,
                    language = o.optString("language", "en").ifEmpty { "en" },
                    bodyTemplate = body,
                    libraryVersion = libraryVersion
                )
            }
            if (templates.isEmpty()) return
            // server is the authoritative source of truth: complete overwrite, in one
            // transaction so the alarm can never read a half-empty library
            dao.replaceTemplates(templates)
            Timber.d("EveningNotif: template library updated to v$libraryVersion (${templates.size} templates)")
        } catch (e: Exception) {
            Timber.w(e, "EveningNotif: template sync skipped")
        }
    }
}
