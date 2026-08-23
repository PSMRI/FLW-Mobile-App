package org.piramalswasthya.sakhi.notifications

import org.json.JSONArray
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
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
            val templates = (0 until array.length()).mapNotNull { i ->
                val o = array.optJSONObject(i) ?: return@mapNotNull null
                org.piramalswasthya.sakhi.model.NotifTemplateCache(
                    templateId = o.optString("template_id").ifEmpty { return@mapNotNull null },
                    bucket = o.optString("bucket").ifEmpty { return@mapNotNull null },
                    language = o.optString("language", "en"),
                    bodyTemplate = o.optString("body_template").ifEmpty { return@mapNotNull null },
                    libraryVersion = libraryVersion
                )
            }
            if (templates.isEmpty()) return
            // server is the authoritative source of truth: complete overwrite
            dao.clearTemplates()
            dao.insertTemplates(templates)
            Timber.d("EveningNotif: template library updated to v$libraryVersion (${templates.size} templates)")
        } catch (e: Exception) {
            Timber.w(e, "EveningNotif: template sync skipped")
        }
    }
}
