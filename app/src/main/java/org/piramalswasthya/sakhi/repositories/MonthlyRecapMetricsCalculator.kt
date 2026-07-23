package org.piramalswasthya.sakhi.repositories

import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsContract
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import org.piramalswasthya.sakhi.model.RecapActivityMetric
import org.piramalswasthya.sakhi.model.RecapCategoryMetric
import org.piramalswasthya.sakhi.model.RecapCountingUnit
import org.piramalswasthya.sakhi.model.RecapMetricStatus
import javax.inject.Inject

/**
 * Builds the privacy-safe aggregate metrics payload for one recap month from
 * verified, ASHA-owned local activity sources.
 *
 * Shape (Phase 6): produces a list of CATEGORIES. Each verified activity is a
 * [RecapActivityMetric] rolled into its [RecapCategoryMetric] via
 * [RecapCategoryMetric.from] (which computes the total + AVAILABLE/UNAVAILABLE
 * status). Only categories with at least one verified activity are emitted;
 * deferred categories (TB, HRP, HBNC, Child-reg, CUFY, Adolescent, NCD referral,
 * General OP) are intentionally absent — never fabricated as 0.
 *
 * Currently implemented: NCD → CBAC screening events. Additional confirmed
 * activities/categories are added by counting them and appending their
 * [RecapCategoryMetric] to [categories].
 */
class MonthlyRecapMetricsCalculator @Inject constructor(
    private val cbacRecapDataSource: CbacRecapDataSource,
) {
    suspend fun calculate(
        userId: Int,
        userName: String,
        recapYearMonth: Int,
        windowStartMillis: Long,
        windowEndMillisExclusive: Long,
        generatedAt: Long,
    ): MonthlyRecapMetricsPayload {
        val categories = mutableListOf<RecapCategoryMetric>()

        // ---- NCD: CBAC screening events (verified, ASHA-owned) ----
        val cbacScreenings = cbacRecapDataSource.countScreeningEvents(
            userId = userId,
            userName = userName,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        categories += RecapCategoryMetric.from(
            categoryId = MonthlyRecapMetricsContract.CATEGORY_NCD,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_CBAC_SCREENINGS,
                    unit = RecapCountingUnit.EVENT.name,
                    count = cbacScreenings,
                    status = RecapMetricStatus.AVAILABLE.name,
                ),
            ),
        )

        return MonthlyRecapMetricsPayload(
            payloadSchemaVersion = MonthlyRecapMetricsContract.PAYLOAD_SCHEMA_VERSION,
            calculationVersion = MonthlyRecapMetricsContract.CALCULATION_VERSION,
            recapYearMonth = recapYearMonth,
            generatedAt = generatedAt,
            windowStartMillis = windowStartMillis,
            windowEndMillisExclusive = windowEndMillisExclusive,
            categories = categories,
        )
    }
}
