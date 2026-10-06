package org.piramalswasthya.sakhi.badges.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.InAppDb
import timber.log.Timber
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read-only facts over the existing health tables (LLD §3). Never writes.
 *
 * Guards (LLD §5.2): every fact is wrapped per-table in try/catch and date
 * columns are resolved at runtime (INTEGER columns only) — a missing table,
 * renamed column or TEXT date makes that table contribute 0, never crash.
 */
@Singleton
class BadgeFactsReader @Inject constructor(
    private val db: InAppDb,
    @ApplicationContext private val context: Context
) {

    private val sql get() = db.openHelper.readableDatabase

    // ─────────────────────────────────────────────────────────────
    // Runtime schema resolution
    // ─────────────────────────────────────────────────────────────

    private val columnCache = mutableMapOf<String, Map<String, String>>()

    /** column name (lowercase) → declared type (uppercase), empty map if table missing. */
    private fun columns(table: String): Map<String, String> =
        columnCache.getOrPut(table) {
            try {
                val map = mutableMapOf<String, String>()
                sql.query("PRAGMA table_info(`$table`)").use { c ->
                    val nameIdx = c.getColumnIndex("name")
                    val typeIdx = c.getColumnIndex("type")
                    while (c.moveToNext()) {
                        map[c.getString(nameIdx).lowercase()] =
                            (c.getString(typeIdx) ?: "").uppercase()
                    }
                }
                map
            } catch (e: Exception) {
                Timber.w(e, "Badges: cannot inspect table $table")
                emptyMap()
            }
        }

    private fun tableExists(table: String) = columns(table).isNotEmpty()

    /** First INTEGER-typed creation-date column, or null. */
    private fun integerDateColumn(table: String): String? {
        val cols = columns(table)
        return DATE_COLUMN_CANDIDATES.firstOrNull { cols[it]?.contains("INT") == true }
    }

    /** "AND isDraft = 0" where the column exists (LLD §3 enforcement). */
    private fun draftFilter(table: String, alias: String = ""): String {
        val prefix = if (alias.isEmpty()) "" else "$alias."
        return if (columns(table).containsKey("isdraft")) " AND ${prefix}isDraft = 0" else ""
    }

    private fun <T> safely(what: String, fallback: T, block: () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            Timber.w(e, "Badges: fact '$what' failed — contributing $fallback")
            fallback
        }

    private fun queryStrings(query: String, args: Array<Any> = emptyArray()): List<String> {
        val out = mutableListOf<String>()
        sql.query(query, args).use { c ->
            while (c.moveToNext()) c.getString(0)?.let { out.add(it) }
        }
        return out
    }

    private fun queryLong(query: String): Long =
        sql.query(query).use { c -> if (c.moveToFirst()) c.getLong(0) else 0L }

    /** Count of non-draft records in [table] created in [since, until); 0 when unresolvable. */
    private fun countNewRecordsSince(table: String, since: Long, until: Long = Long.MAX_VALUE): Long =
        safely("count $table", 0L) {
            if (!tableExists(table)) return@safely 0L
            val dateCol = integerDateColumn(table) ?: return@safely 0L
            queryLong(
                "SELECT COUNT(*) FROM `$table` WHERE `$dateCol` >= $since AND `$dateCol` < $until${draftFilter(table)}"
            )
        }

    // ─────────────────────────────────────────────────────────────
    // Badge signals (LLD §3.1)
    // ─────────────────────────────────────────────────────────────

    /** Timely Reporter: months whose incentive record was created on time (createdDate ≤ endDate). */
    fun onTimeIncentiveMonths(): Set<String> = safely("onTimeIncentiveMonths", emptySet()) {
        val out = mutableSetOf<String>()
        sql.query(
            "SELECT createdDate, endDate FROM INCENTIVE_RECORD WHERE createdDate <= endDate"
        ).use { c ->
            while (c.moveToNext()) out.add(BadgeDates.monthKey(c.getLong(1)))
        }
        out
    }

    /** Complete Worker: distinct health domains with at least one new record in [since, until). */
    fun activeDomainsSince(since: Long, until: Long = Long.MAX_VALUE): Long =
        safely("activeDomains", 0L) {
            DOMAIN_TABLES.count { (_, tables) ->
                tables.any { countNewRecordsSince(it, since, until) > 0 }
            }.toLong()
        }

    /** Community Voice: distinct meeting types with at least one record in [since, until). */
    fun meetingTypesSince(since: Long, until: Long = Long.MAX_VALUE): Long =
        safely("meetingTypes", 0L) {
            MEETING_TABLES.count { countNewRecordsSince(it, since, until) > 0 }.toLong()
        }

    /**
     * Weeks with any recorded work, derived from record timestamps across all
     * badge-mapped tables. Auto-sync fires on every save, so an activity week
     * is a sync week in practice — and because re-downloaded records keep
     * their original dates, this restores Steady Syncer history after a
     * reinstall without any backend (union with the local BADGE_SYNC_LOG).
     */
    fun activityWeeks(): Set<String> = safely("activityWeeks", emptySet()) {
        val out = mutableSetOf<String>()
        val dayMs = TimeUnit.DAYS.toMillis(1)
        // Bucket by LOCAL day, not UTC. Dividing an epoch millisecond by a day gives the UTC
        // day, so in IST every record saved between 00:00 and 05:30 falls into the previous
        // one; when that local day is a Monday the reconstructed instant lands in the
        // previous ISO week and Steady Syncer silently loses an activity week after a
        // reinstall. India does not observe DST, so one current offset is exact here rather
        // than merely close.
        val offset = TimeZone.getDefault().getOffset(System.currentTimeMillis()).toLong()
        for (table in MAPPED_TABLES) {
            if (!tableExists(table)) continue
            val dateCol = integerDateColumn(table) ?: continue
            safely("activityWeeks:$table", Unit) {
                sql.query(
                    "SELECT DISTINCT (`$dateCol` + $offset) / $dayMs FROM `$table` " +
                            "WHERE `$dateCol` > 0${draftFilter(table)}"
                ).use { c ->
                    while (c.moveToNext()) {
                        // Midday of that local day: far enough from either boundary that
                        // rounding cannot push the key into a neighbouring week.
                        out.add(BadgeDates.weekKey(c.getLong(0) * dayMs - offset + dayMs / 2))
                    }
                }
            }
        }
        out
    }

    /**
     * Maternal Journey: distinct completed journeys — registered pregnancy with
     * ≥4 ANC visits (structured + dynamic history aggregated) ending in an
     * institutional delivery. First-trimester check applies only when an
     * INTEGER ANC date column is resolvable.
     */
    fun completedMaternalJourneys(): List<String> = safely("maternalJourneys", emptyList()) {
        if (!tableExists("PREGNANCY_REGISTER") || !tableExists("DELIVERY_OUTCOME")) {
            return@safely emptyList()
        }
        val ancStructured = if (tableExists("PREGNANCY_ANC"))
            "SELECT COUNT(*) FROM PREGNANCY_ANC a WHERE a.benId = pr.benId${draftFilter("PREGNANCY_ANC", "a")}"
        else "SELECT 0"
        val ancDynamic = if (tableExists("ALL_VISIT_HISTORY_ANC"))
            "SELECT COUNT(DISTINCT v.visitDay) FROM ALL_VISIT_HISTORY_ANC v WHERE v.benId = pr.benId"
        else "SELECT 0"

        val firstTrimester =
            if (columns("PREGNANCY_ANC")["ancdate"]?.contains("INT") == true &&
                columns("PREGNANCY_REGISTER")["lmp"]?.contains("INT") == true
            ) {
                val trimesterMs = TimeUnit.DAYS.toMillis(97)
                " AND EXISTS(SELECT 1 FROM PREGNANCY_ANC t WHERE t.benId = pr.benId " +
                        "AND t.ancDate <= pr.lmp + $trimesterMs)"
            } else ""

        queryStrings(
            """
            SELECT DISTINCT pr.benId FROM PREGNANCY_REGISTER pr
            JOIN DELIVERY_OUTCOME d ON d.benId = pr.benId
            WHERE IFNULL(d.placeOfDelivery, '') != ''
              AND LOWER(d.placeOfDelivery) NOT LIKE '%home%'
              ${draftFilter("PREGNANCY_REGISTER", "pr")}
              ${draftFilter("DELIVERY_OUTCOME", "d")}
              AND (($ancStructured) + ($ancDynamic)) >= 4
              $firstTrimester
            """.trimIndent()
        )
    }

    /**
     * Child Fully Protected: beneficiaries whose administered doses cover every
     * CHILD vaccine expected within the 0–1 year schedule.
     */
    fun fullyImmunizedChildren(): List<String> = safely("fullyImmunized", emptyList()) {
        if (!tableExists("IMMUNIZATION") || !tableExists("VACCINE")) return@safely emptyList()
        val oneYearMs = TimeUnit.DAYS.toMillis(365)
        val expected = queryLong(
            "SELECT COUNT(*) FROM VACCINE WHERE category = 'CHILD' AND maxAllowedAgeInMillis <= $oneYearMs"
        )
        if (expected == 0L) return@safely emptyList()
        queryStrings(
            """
            SELECT i.beneficiaryId FROM IMMUNIZATION i
            JOIN VACCINE v ON v.vaccineId = i.vaccineId
            WHERE v.category = 'CHILD' AND v.maxAllowedAgeInMillis <= $oneYearMs
              AND i.date IS NOT NULL
            GROUP BY i.beneficiaryId
            HAVING COUNT(DISTINCT i.vaccineId) >= $expected
            """.trimIndent()
        )
    }

    /** Digital Identity: beneficiaries with a valid generated ABHA. */
    fun abhaGeneratedBens(): List<String> = safely("abhaGenerated", emptyList()) {
        if (!tableExists("ABHA_GENERATED")) return@safely emptyList()
        queryStrings(
            "SELECT DISTINCT beneficiaryID FROM ABHA_GENERATED WHERE IFNULL(healthIdNumber, '') != ''"
        )
    }

    /**
     * Vulnerable Baby Cared For: LBW or SNCU babies with all seven HBNC visits
     * (days 1,3,7,14,21,28,42) completed. HBNC.benId is the infant's
     * beneficiary id → INFANT_REG.childBenId.
     *
     * Two sources, because the HBNC form moved. The legacy `HBNC` table numbers
     * its visits (homeVisitDate 1..42, with the visit card and parts I/II kept
     * at 0/-1/-2), while the dynamic form the Infant list opens today writes
     * `all_visit_history` rows under form hbnc_form_001 with visitDay as text
     * ("1st Day" … "42nd Day"). Counting only the old table missed every visit
     * entered through the current screen, so both are unioned and the seven
     * days are counted once each.
     *
     * INFANT_REG.weight is GRAMS, not kilograms: the registration form is
     * "Weight at Birth ( grams )" with a 500..6000 range. Comparing it against
     * 2.5 matched no real LBW baby (2200 grams is not < 2.5) while matching
     * every unfilled row, whose weight defaults to 0. Hence 2500, with 0 and
     * NULL treated as "weight unknown" — such a baby qualifies only via SNCU.
     */
    fun vulnerableBabiesCaredFor(): List<String> = safely("vulnerableBabies", emptyList()) {
        if (!tableExists("INFANT_REG")) return@safely emptyList()
        val legacy = if (tableExists("HBNC")) {
            """
            SELECT benId AS benId, homeVisitDate AS visitNo FROM HBNC
            WHERE homeVisitDate IN (1,3,7,14,21,28,42)
            """.trimIndent()
        } else null
        val dynamic = if (tableExists("all_visit_history")) {
            """
            SELECT benId AS benId, CASE visitDay
                WHEN '1st Day' THEN 1 WHEN '3rd Day' THEN 3 WHEN '7th Day' THEN 7
                WHEN '14th Day' THEN 14 WHEN '21st Day' THEN 21 WHEN '28th Day' THEN 28
                WHEN '42nd Day' THEN 42 END AS visitNo
            FROM all_visit_history
            WHERE formId = 'hbnc_form_001'
              AND visitDay IN ('1st Day','3rd Day','7th Day','14th Day','21st Day','28th Day','42nd Day')
            """.trimIndent()
        } else null
        val visits = listOfNotNull(legacy, dynamic).joinToString("\n            UNION\n            ")
        if (visits.isEmpty()) return@safely emptyList()
        queryStrings(
            """
            SELECT v.benId FROM (
            $visits
            ) v
            JOIN INFANT_REG i ON i.childBenId = v.benId
            WHERE (i.isSNCU = 'Yes' OR (IFNULL(i.weight, 0) > 0 AND i.weight < 2500))
            GROUP BY v.benId
            HAVING COUNT(DISTINCT v.visitNo) >= 7
            """.trimIndent()
        )
    }

    /**
     * Critical Referral Made: SAM child referred to NRC, parsed from the
     * dynamic form JSON (same contract as CUFYFormRepository).
     */
    fun nrcReferredChildren(): List<String> = safely("nrcReferred", emptyList()) {
        if (!tableExists("children_under_five_all_visit")) return@safely emptyList()
        val key = context.getString(R.string.is_child_referred_nrc)
        val out = mutableSetOf<String>()
        sql.query("SELECT benId, formDataJson FROM children_under_five_all_visit").use { c ->
            while (c.moveToNext()) {
                try {
                    val fields = JSONObject(c.getString(1)).optJSONObject("fields") ?: continue
                    if (fields.optString(key) == "Yes") out.add(c.getString(0))
                } catch (_: Exception) {
                    // malformed free-form JSON — skip record (LLD §5.2)
                }
            }
        }
        out.toList()
    }

    companion object {
        private val DATE_COLUMN_CANDIDATES =
            listOf("createddate", "createdat", "updateddate", "updatedat", "date", "visitdate")

        /** Complete Worker domains (LLD §1.1): Child, Maternal, Immunization, FP, Disease. */
        private val DOMAIN_TABLES = mapOf(
            "child" to listOf("INFANT_REG", "CHILD_REG", "HBNC", "children_under_five_all_visit"),
            "maternal" to listOf(
                "PREGNANCY_REGISTER", "PREGNANCY_ANC", "ALL_VISIT_HISTORY_ANC",
                "DELIVERY_OUTCOME", "PNC_VISIT", "PMSMA"
            ),
            "immunization" to listOf("IMMUNIZATION"),
            "family_planning" to listOf("ELIGIBLE_COUPLE_REG", "ELIGIBLE_COUPLE_TRACKING", "FPOT"),
            "disease" to listOf(
                "TB_SCREENING", "TB_SUSPECTED", "MALARIA_SCREENING", "AES_SCREENING",
                "KALAZAR_SCREENING", "FILARIA_SCREENING", "LEPROSY_SCREENING"
            )
        )

        /** Community Voice meeting types (LLD §3.1): MAA, NDD (deworming), AHD, U-WIN. */
        private val MEETING_TABLES =
            listOf("MAA_MEETING", "DewormingMeeting", "AHDMeeting", "UWIN_SESSION")

        /**
         * Tables whose invalidation should wake the evaluator (TaskCompletionBus).
         * `all_visit_history` carries the dynamic HBNC form, so saving the seventh
         * visit re-evaluates Vulnerable Baby there and then, rather than at the
         * next app launch.
         */
        val MAPPED_TABLES: Array<String> = (
                DOMAIN_TABLES.values.flatten() + MEETING_TABLES + listOf(
                    "INCENTIVE_RECORD", "IMMUNIZATION", "ABHA_GENERATED", "all_visit_history"
                )
                ).distinct().toTypedArray()
    }
}
