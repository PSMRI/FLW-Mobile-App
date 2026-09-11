package org.piramalswasthya.sakhi.notifications

import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.model.NotifTemplateCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Template selection policy (Notification LLD §5/§6): random pick from the
 * activity-band pool in the worker's language, excluding recently shown
 * templates; when all were recently used, reset the filter. Falls back to
 * English, then to the bundled library.
 */
@Singleton
class TemplateSelector @Inject constructor(
    private val dao: EveningNotifDao
) {

    suspend fun select(bucket: String, language: String): NotifTemplateCache {
        val pool = dao.templatesFor(bucket, language)
            .ifEmpty { dao.templatesFor(bucket, "en") }
            .ifEmpty { BundledTemplates.ALL.filter { it.bucket == bucket && it.language == language } }
            .ifEmpty { BundledTemplates.ALL.filter { it.bucket == bucket && it.language == "en" } }

        val recent = dao.recentTemplateIds(bucket, RECENT_WINDOW).toSet()
        val fresh = pool.filterNot { it.templateId in recent }
        return (fresh.ifEmpty { pool }).random()
    }

    companion object {
        private const val RECENT_WINDOW = 3
    }
}
