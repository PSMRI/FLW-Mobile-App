package org.piramalswasthya.sakhi.model

/**
 * Aggregate Monthly Recap metrics — the ONLY thing written into
 * MonthlyRecapCache.metricsJson. Strictly privacy-safe aggregates: counts,
 * stable identifiers, window bounds and availability status only. MUST NOT carry
 * beneficiary ids/names, household ids, health scores, clinical answers, referral
 * reasons or any record-level detail.
 *
 * Shape (Phase 6): the payload is a list of CATEGORIES; each category rolls up one
 * or more ACTIVITIES into a category total. Only categories with at least one
 * verified, ASHA-owned activity are emitted; deferred/unverifiable categories are
 * simply absent (or marked UNAVAILABLE) — never fabricated as 0.
 */

/** Semantic counting unit for a recap activity metric. */
enum class RecapCountingUnit {
    EVENT,
    UNIQUE_BENEFICIARY,
    FORM_SUBMISSION,
    REGISTRATION,
    VISIT,
    DOSE,
}

/**
 * Whether a metric/category could be safely computed.
 *
 * INTERNAL ONLY — this status exists so a deferred category is never mis-rendered
 * as a real `0`. It MUST NOT be surfaced in any ASHA-facing text: the UI silently
 * shows AVAILABLE results and omits UNAVAILABLE ones (no "we couldn't include X").
 */
enum class RecapMetricStatus { AVAILABLE, UNAVAILABLE }

/** One privacy-safe aggregate activity metric. [count] is meaningful only when [status] is AVAILABLE. */
data class RecapActivityMetric(
    /** Stable activity id, e.g. [MonthlyRecapMetricsContract.ACTIVITY_CBAC_SCREENINGS]. */
    val activityId: String,
    /** [RecapCountingUnit] name, e.g. "EVENT". */
    val unit: String,
    val count: Int,
    /** [RecapMetricStatus] name; defaults to AVAILABLE. */
    val status: String = RecapMetricStatus.AVAILABLE.name,
)

/**
 * One recap category (e.g. Maternal Health) rolling up its activities.
 *
 * [categoryTotal] = sum of the counts of AVAILABLE activities. Because each
 * activity is a distinct Room table, no single record can be counted under two
 * activities, so the sum never double-counts. [status] is AVAILABLE when at least
 * one activity is available, otherwise UNAVAILABLE.
 */
data class RecapCategoryMetric(
    val categoryId: String,
    val activities: List<RecapActivityMetric>,
    val categoryTotal: Int,
    val status: String,
) {
    companion object {
        /** Builds a category from its activities, computing the total + status per the aggregation rule. */
        fun from(categoryId: String, activities: List<RecapActivityMetric>): RecapCategoryMetric {
            val available = activities.filter { it.status == RecapMetricStatus.AVAILABLE.name }
            val total = available.sumOf { it.count }
            val status =
                if (available.isNotEmpty()) RecapMetricStatus.AVAILABLE.name
                else RecapMetricStatus.UNAVAILABLE.name
            return RecapCategoryMetric(categoryId, activities, total, status)
        }
    }
}

/**
 * Why a calculated payload was NOT frozen. Ordered most-fundamental first; the
 * first matching reason is the one reported.
 *
 * INTERNAL ONLY — like [RecapMetricStatus] this never reaches ASHA-facing text.
 * A blocked recap is silently hidden and re-evaluated on the next open.
 */
enum class RecapFreezeBlocker {
    /** Ready to freeze. */
    NONE,

    /** The one-shot server download has not finished on this install. */
    FULL_PULL_INCOMPLETE,

    /**
     * This install did not exist for the whole recap month, so its local database
     * cannot be trusted to hold every record the ASHA created that month
     * (re-install, new device, or an ASHA who joined mid-month).
     */
    DEVICE_MISSED_PART_OF_MONTH,

    /** Nothing countable happened — the existing celebration-only/zero rule. */
    NO_COUNTABLE_WORK,
}

/**
 * Why and under what conditions a recap payload was produced. Written into the
 * payload so a wrong count found during UAT is investigable after the fact
 * instead of unfalsifiable.
 *
 * Privacy-safe by the same rule as the rest of the payload: flags, timestamps
 * and a version only — never record-level data.
 */
data class RecapDiagnostics(
    /** [RecapFreezeBlocker] name. NONE means the payload was frozen. */
    val freezeBlocker: String,
    val isFullPullComplete: Boolean,
    /** Millis from which this install's local data is considered complete. */
    val localDataSince: Long,
    /** When readiness was evaluated (device-local). */
    val evaluatedAt: Long,
    /** Build that produced this evaluation, so field reports can be pinned to a build. */
    val appVersionCode: Int,
)

/**
 * Versioned aggregate payload persisted in MonthlyRecapCache.metricsJson and
 * frozen once generated for a (userId, recapYearMonth).
 *
 * [diagnostics] is additive and nullable: payloads frozen before it existed decode
 * with null, so PAYLOAD_SCHEMA_VERSION deliberately does NOT change (bumping it
 * would invalidate every already-frozen snapshot for a backward-compatible field).
 */
data class MonthlyRecapMetricsPayload(
    val payloadSchemaVersion: Int,
    val calculationVersion: Int,
    val recapYearMonth: Int,
    val generatedAt: Long,
    val windowStartMillis: Long,
    val windowEndMillisExclusive: Long,
    val categories: List<RecapCategoryMetric>,
    val diagnostics: RecapDiagnostics? = null,
)

object MonthlyRecapMetricsContract {
    /** JSON envelope version; bump only when the payload SHAPE changes. */
    const val PAYLOAD_SCHEMA_VERSION = 2

    /** Semantic calculation version; bump when counting rules change. */
    const val CALCULATION_VERSION = 2

    // ---- Category ids (only categories with >=1 verified activity are emitted) ----
    const val CATEGORY_NCD = "NCD"
    const val CATEGORY_HOUSEHOLD = "HOUSEHOLD"
    const val CATEGORY_BENEFICIARY = "BENEFICIARY"
    const val CATEGORY_ELIGIBLE_COUPLE = "ELIGIBLE_COUPLE"
    const val CATEGORY_MATERNAL_HEALTH = "MATERNAL_HEALTH"
    const val CATEGORY_IMMUNIZATION = "IMMUNIZATION"

    // ---- Activity ids ----
    /** Verified first activity: completed CBAC (NCD screening) events. */
    const val ACTIVITY_CBAC_SCREENINGS = "CBAC_SCREENINGS"
    const val ACTIVITY_HOUSEHOLD_REGISTRATIONS = "HOUSEHOLD_REGISTRATIONS"
    const val ACTIVITY_BENEFICIARY_REGISTRATIONS = "BENEFICIARY_REGISTRATIONS"

    /** Distinct couples registered OR tracked for family planning (single number). */
    const val ACTIVITY_ELIGIBLE_COUPLE_FP = "ELIGIBLE_COUPLE_FP"

    /**
     * Distinct pregnant women / mothers the ASHA supported via ANY maternal activity
     * (pregnancy registration, ANC, PMSMA, delivery outcome, PNC) — a single number.
     */
    const val ACTIVITY_MATERNAL_HEALTH_MOTHERS = "MATERNAL_HEALTH_MOTHERS_SUPPORTED"

    /** Vaccine doses the ASHA administered (child + mother), counted per dose. */
    const val ACTIVITY_IMMUNIZATION_DOSES = "IMMUNIZATION_DOSES_ADMINISTERED"
}
