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
 * Verified so far: NCD → CBAC screenings; Household → new households registered;
 * Beneficiary → new family members registered (drafts excluded); Eligible Couple →
 * distinct couples registered OR tracked for FP; Maternal Health → distinct mothers
 * supported via any maternal activity; Immunization → vaccine doses administered
 * (child + mother). Ownership is `ashaId = :userId` (registrations) / CBAC's
 * record-level predicate / `createdBy = :userName` (EC, Maternal Health, Immunization);
 * see the DAO docs. More confirmed categories are added by counting them and
 * appending their [RecapCategoryMetric].
 */
class MonthlyRecapMetricsCalculator @Inject constructor(
    private val cbacRecapDataSource: CbacRecapDataSource,
    private val householdRecapDataSource: HouseholdRecapDataSource,
    private val beneficiaryRecapDataSource: BeneficiaryRecapDataSource,
    private val eligibleCoupleRecapDataSource: EligibleCoupleRecapDataSource,
    private val maternalHealthRecapDataSource: MaternalHealthRecapDataSource,
    private val immunizationRecapDataSource: ImmunizationRecapDataSource,
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

        // ---- Household: new households registered ----
        val householdRegistrations = householdRecapDataSource.countRegistrations(
            userId = userId,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        categories += RecapCategoryMetric.from(
            categoryId = MonthlyRecapMetricsContract.CATEGORY_HOUSEHOLD,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_HOUSEHOLD_REGISTRATIONS,
                    unit = RecapCountingUnit.REGISTRATION.name,
                    count = householdRegistrations,
                    status = RecapMetricStatus.AVAILABLE.name,
                ),
            ),
        )

        // ---- Beneficiary: new family members registered (drafts excluded) ----
        val beneficiaryRegistrations = beneficiaryRecapDataSource.countRegistrations(
            userId = userId,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        categories += RecapCategoryMetric.from(
            categoryId = MonthlyRecapMetricsContract.CATEGORY_BENEFICIARY,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_BENEFICIARY_REGISTRATIONS,
                    unit = RecapCountingUnit.REGISTRATION.name,
                    count = beneficiaryRegistrations,
                    status = RecapMetricStatus.AVAILABLE.name,
                ),
            ),
        )

        // ---- Eligible Couple: distinct couples registered OR tracked for FP ----
        val eligibleCouples = eligibleCoupleRecapDataSource.countCouples(
            userName = userName,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        categories += RecapCategoryMetric.from(
            categoryId = MonthlyRecapMetricsContract.CATEGORY_ELIGIBLE_COUPLE,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_ELIGIBLE_COUPLE_FP,
                    unit = RecapCountingUnit.UNIQUE_BENEFICIARY.name,
                    count = eligibleCouples,
                    status = RecapMetricStatus.AVAILABLE.name,
                ),
            ),
        )

        // ---- Maternal Health: distinct mothers supported via any maternal activity ----
        val mothersSupported = maternalHealthRecapDataSource.countMothersSupported(
            userName = userName,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        categories += RecapCategoryMetric.from(
            categoryId = MonthlyRecapMetricsContract.CATEGORY_MATERNAL_HEALTH,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_MATERNAL_HEALTH_MOTHERS,
                    unit = RecapCountingUnit.UNIQUE_BENEFICIARY.name,
                    count = mothersSupported,
                    status = RecapMetricStatus.AVAILABLE.name,
                ),
            ),
        )

        // ---- Immunization: vaccine doses administered (child + mother) ----
        val dosesAdministered = immunizationRecapDataSource.countDosesAdministered(
            userName = userName,
            startMillis = windowStartMillis,
            endMillisExclusive = windowEndMillisExclusive,
        )
        categories += RecapCategoryMetric.from(
            categoryId = MonthlyRecapMetricsContract.CATEGORY_IMMUNIZATION,
            activities = listOf(
                RecapActivityMetric(
                    activityId = MonthlyRecapMetricsContract.ACTIVITY_IMMUNIZATION_DOSES,
                    unit = RecapCountingUnit.DOSE.name,
                    count = dosesAdministered,
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
