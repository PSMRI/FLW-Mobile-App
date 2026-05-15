package org.piramalswasthya.sakhi.gamification

/**
 * Sealed hierarchy of every ASHA worker action that can trigger a reward.
 *
 * Health form code calls:
 *     gamificationEngine.process(userId, GamificationEvent.AncVisitCompleted(benId.toString()))
 *
 * The engine is the ONLY place that knows point values and badge rules.
 * Health forms stay completely decoupled — they just fire an event.
 *
 * [refId] is optional — pass the beneficiary ID / visit ID / household ID
 * for audit trail and backend deduplication.
 */
sealed class GamificationEvent(
    val eventType: String,
    val refId: String? = null
) {
    // ── Household ─────────────────────────────────────────────────────────────
    class HouseholdRegistered(refId: String) :
        GamificationEvent("HOUSEHOLD_REGISTERED", refId)

    // ── Beneficiary ───────────────────────────────────────────────────────────
    class BeneficiaryRegistered(refId: String) :
        GamificationEvent("BENEFICIARY_REGISTERED", refId)

    // ── Maternal Health ───────────────────────────────────────────────────────
    class AncVisitCompleted(refId: String) :
        GamificationEvent("ANC_VISIT_COMPLETED", refId)

    class DeliveryOutcomeRecorded(refId: String) :
        GamificationEvent("DELIVERY_OUTCOME_RECORDED", refId)

    class PncVisitCompleted(refId: String) :
        GamificationEvent("PNC_VISIT_COMPLETED", refId)

    // ── Child Care ────────────────────────────────────────────────────────────
    class ImmunizationRecorded(refId: String) :
        GamificationEvent("IMMUNIZATION_RECORDED", refId)

    // ── HRP ───────────────────────────────────────────────────────────────────
    /** High-risk pregnancy identification — highest point value by design.
     *  Early identification of HRP directly reduces maternal mortality. */
    class HrpCaseIdentified(refId: String) :
        GamificationEvent("HRP_CASE_IDENTIFIED", refId)

    // ── NCD ───────────────────────────────────────────────────────────────────
    class NcdScreeningCompleted(refId: String) :
        GamificationEvent("NCD_SCREENING_COMPLETED", refId)

    // ── CBAC ──────────────────────────────────────────────────────────────────
    class CbacFormFilled(refId: String) :
        GamificationEvent("CBAC_FORM_FILLED", refId)

    // ── Daily login ───────────────────────────────────────────────────────────
    /** Awarded once per calendar day. Engine deduplicates automatically. */
    object DailyLogin : GamificationEvent("DAILY_LOGIN")
}
