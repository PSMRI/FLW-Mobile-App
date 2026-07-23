package org.piramalswasthya.sakhi.repositories

import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsContract
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import org.piramalswasthya.sakhi.model.RecapActivityMetric
import org.piramalswasthya.sakhi.model.RecapCountingUnit
import javax.inject.Inject

/**
 * Builds the privacy-safe aggregate metrics payload for one recap month from
 * verified local activity sources.
 *
 * Phase 4.3 covers ONE verified activity: CBAC screening events (EVENT unit).
 * NCD referral is deferred (no verified ASHA-ownership field, no genuine
 * referral-activity date), so this produces a CBAC activity metric — NOT the
 * complete NCD category total. Adding NCD_REFERRAL later is a matter of adding a
 * second activity to the returned list.
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
        val cbacScreenings = cbacRecapDataSource.countScreeningEvents(
            userId = userId,
            userName = userName,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        return MonthlyRecapMetricsPayload(
            payloadSchemaVersion = MonthlyRecapMetricsContract.PAYLOAD_SCHEMA_VERSION,
            calculationVersion = MonthlyRecapMetricsContract.CALCULATION_VERSION,
            recapYearMonth = recapYearMonth,
            generatedAt = generatedAt,
            windowStartMillis = windowStartMillis,
            windowEndMillisExclusive = windowEndMillisExclusive,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_CBAC_SCREENINGS,
                    unit = RecapCountingUnit.EVENT.name,
                    count = cbacScreenings,
                ),
            ),
        )
    }
}
