package org.piramalswasthya.sakhi.model

/**
 * Aggregate Monthly Recap metrics — Phase 4.3.
 *
 * These types are the ONLY thing written into MonthlyRecapCache.metricsJson.
 * They are strictly privacy-safe aggregates: counts, identifiers and window
 * bounds only. They MUST NOT carry beneficiary ids/names, household ids, health
 * scores, clinical answers, referral reasons or any record-level detail.
 *
 * Phase 4.3 ships ONE verified activity under the future NCD category:
 * CBAC screening events. NCD referral is deferred (its local model has no ASHA
 * ownership field and no genuine referral-activity date), so this is a CBAC
 * activity metric — NOT the complete NCD category total.
 */

/** Semantic counting unit for a recap activity metric. */
enum class RecapCountingUnit { EVENT, UNIQUE_BENEFICIARY, FORM_SUBMISSION }

/** One privacy-safe aggregate activity metric (counts + identity only). */
data class RecapActivityMetric(
    /** Stable activity id, e.g. [MonthlyRecapMetricsContract.ACTIVITY_CBAC_SCREENINGS]. */
    val activityId: String,
    /** [RecapCountingUnit] name, e.g. "EVENT". */
    val unit: String,
    val count: Int,
)

/**
 * Versioned aggregate payload persisted in MonthlyRecapCache.metricsJson and
 * frozen once generated for a (userId, recapYearMonth).
 */
data class MonthlyRecapMetricsPayload(
    val payloadSchemaVersion: Int,
    val calculationVersion: Int,
    val recapYearMonth: Int,
    val generatedAt: Long,
    val windowStartMillis: Long,
    val windowEndMillisExclusive: Long,
    val activities: List<RecapActivityMetric>,
)

object MonthlyRecapMetricsContract {
    /** JSON envelope version; bump only when the payload shape changes. */
    const val PAYLOAD_SCHEMA_VERSION = 1

    /** Semantic calculation version; bump when counting rules change. */
    const val CALCULATION_VERSION = 1

    /** Verified first activity: completed CBAC (NCD screening) events. */
    const val ACTIVITY_CBAC_SCREENINGS = "CBAC_SCREENINGS"
}
