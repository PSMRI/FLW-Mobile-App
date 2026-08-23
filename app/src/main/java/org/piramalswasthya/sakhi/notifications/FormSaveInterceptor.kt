package org.piramalswasthya.sakhi.notifications

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FormSaveLogCache
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces the eligible activity stream (Notification LLD §5/§6).
 *
 * Rather than hooking all 40 repository save paths, it derives today's new
 * records straight from the included health tables (id/date columns resolved
 * at runtime, isDraft filtered) and INSERT-OR-IGNOREs them into
 * form_save_log — whose unique constraint IS the edit-vs-new rule: a record
 * created before today never appears, a second save of the same
 * (beneficiary, form, day) is ignored.
 */
@Singleton
class FormSaveInterceptor @Inject constructor(
    private val db: InAppDb,
    private val dao: EveningNotifDao,
    private val pref: PreferenceDao
) {

    private val sql get() = db.openHelper.readableDatabase

    private fun columns(table: String): Map<String, String> =
        try {
            val map = mutableMapOf<String, String>()
            sql.query("PRAGMA table_info(`$table`)").use { c ->
                val n = c.getColumnIndex("name")
                val t = c.getColumnIndex("type")
                while (c.moveToNext()) map[c.getString(n).lowercase()] =
                    (c.getString(t) ?: "").uppercase()
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }

    private fun resolve(cols: Map<String, String>, candidates: List<String>, intOnly: Boolean) =
        candidates.firstOrNull { cols[it] != null && (!intOnly || cols[it]!!.contains("INT")) }

    /** Scans included tables and logs today's new records. Safe on any schema. */
    suspend fun captureToday() = withContext(Dispatchers.IO) {
        val ashaId = try {
            pref.getLoggedInUser()?.userId
        } catch (e: Exception) {
            null
        } ?: return@withContext
        val now = System.currentTimeMillis()
        val dateKey = ActivityClassifier.dateKey(now)
        val dayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val saves = mutableListOf<FormSaveLogCache>()
        for ((formType, table) in ActivityClassifier.INCLUDED) {
            try {
                val cols = columns(table)
                if (cols.isEmpty()) continue
                val idCol = resolve(cols, ID_CANDIDATES, intOnly = false) ?: continue
                val dateCol = resolve(cols, DATE_CANDIDATES, intOnly = true) ?: continue
                val draft = if (cols.containsKey("isdraft")) " AND isDraft = 0" else ""
                sql.query(
                    "SELECT DISTINCT `$idCol` FROM `$table` WHERE `$dateCol` >= $dayStart$draft"
                ).use { c ->
                    while (c.moveToNext()) {
                        saves += FormSaveLogCache(
                            formType = formType,
                            beneficiaryId = c.getLong(0),
                            ashaWorkerId = ashaId,
                            savedAt = now,
                            dateKey = dateKey
                        )
                    }
                }
            } catch (e: Exception) {
                // one table failing must not block the day's count (LLD §6)
                Timber.w(e, "EveningNotif: capture failed for $table")
            }
        }
        if (saves.isNotEmpty()) dao.logSaves(saves)
    }

    companion object {
        private val ID_CANDIDATES =
            listOf("benid", "beneficiaryid", "childbenid", "householdid", "hhid", "id")
        private val DATE_CANDIDATES =
            listOf("createddate", "createdat", "visitdate", "date", "updateddate", "updatedat")
    }
}
