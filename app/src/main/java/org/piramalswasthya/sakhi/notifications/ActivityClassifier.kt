package org.piramalswasthya.sakhi.notifications

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity classification (Notification LLD §2).
 *
 * Included: direct, per-beneficiary health service actions (new saves only).
 * Excluded by omission: death reports (CDR, MDSR — ethically excluded from
 * all gamification), village/community-level forms (VHND, VHNC, PHC Review,
 * AHD, Saas-Bahu, NDD, MAA, Pulse Polio, ORS, Filaria MDA, U-WIN), and edits
 * (enforced by form_save_log's unique constraint + created-today rule).
 */
object ActivityClassifier {

    /** formType → health table it is recorded in. */
    val INCLUDED: Map<String, String> = mapOf(
        // Household / Beneficiary
        "household_registration" to "HOUSEHOLD",
        "beneficiary_registration" to "BENEFICIARY",
        // Eligible Couple
        "ec_registration" to "ELIGIBLE_COUPLE_REG",
        "ec_tracking" to "ELIGIBLE_COUPLE_TRACKING",
        // Maternal Health
        "pw_registration" to "PREGNANCY_REGISTER",
        "anc_visit" to "PREGNANCY_ANC",
        "anc_visit_dynamic" to "ALL_VISIT_HISTORY_ANC",
        "delivery_outcome" to "DELIVERY_OUTCOME",
        "pnc_mother_care" to "PNC_VISIT",
        "infant_registration" to "INFANT_REG",
        "child_registration" to "CHILD_REG",
        "pmsma_high_risk" to "PMSMA",
        "hwc_referral" to "NCD_REFER",
        // Child Care
        "hbnc_visit" to "HBNC",
        "hbyc_visit" to "ALL_VISIT_HISTORY_HBYC",
        "under_five_care" to "children_under_five_all_visit",
        "adolescent_care" to "Adolescent_Health_Form_Data",
        // Disease Control
        "ncd_screening" to "CBAC",
        "malaria_screening" to "MALARIA_SCREENING",
        "kala_azar_screening" to "KALAZAR_SCREENING",
        "aes_je_screening" to "AES_SCREENING",
        "filaria_screening" to "FILARIA_SCREENING",
        "leprosy_screening" to "LEPROSY_SCREENING",
        // Communicable Disease / TB
        "tb_screening" to "TB_SCREENING",
        "tb_suspected" to "TB_SUSPECTED",
        "tb_confirmed" to "TB_CONFIRMED_TREATMENT",
        // Immunization
        "child_immunization" to "IMMUNIZATION",
        // HRP
        "hrp_pregnant_assess" to "HRP_PREGNANT_ASSESS",
        "hrp_non_pregnant_assess" to "HRP_NON_PREGNANT_ASSESS",
        "hrp_pregnant_track" to "HRP_PREGNANT_TRACK",
        "hrp_non_pregnant_track" to "HRP_NON_PREGNANT_TRACK",
        // General OP Care
        "general_opd" to "GENERAL_OPD_ACTIVITY"
    )

    /** Activity bands (LLD §5): tone without exposing numerical counts. */
    enum class Bucket { ZERO, ONE, TWO_PLUS }

    fun bucketFor(todayCount: Int): Bucket = when {
        todayCount <= 0 -> Bucket.ZERO
        todayCount == 1 -> Bucket.ONE
        else -> Bucket.TWO_PLUS
    }

    private val dateKeyFormat get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun dateKey(time: Long): String = dateKeyFormat.format(Date(time))
}
