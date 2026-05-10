package org.piramalswasthya.sakhi.gamification

/**
 * Centralised catalogue of every badge the engine can award.
 *
 * Adding a new badge = one entry here + one rule in GamificationEngine.
 * Nothing else changes.
 *
 * All three language names are stored here so the UI can render
 * badges completely offline without any string resource lookup at runtime.
 */
object BadgeCatalog {

    data class BadgeDef(
        val type: String,
        val nameEn: String,
        val nameHi: String,
        val nameAs: String,
        val descEn: String
    )

    // ── First-time badges ─────────────────────────────────────────────────────

    val FIRST_HOUSEHOLD = BadgeDef(
        type    = "FIRST_HOUSEHOLD",
        nameEn  = "First Home",
        nameHi  = "पहला घर",
        nameAs  = "প্ৰথম ঘৰ",
        descEn  = "Registered your first household"
    )

    val FIRST_BENEFICIARY = BadgeDef(
        type    = "FIRST_BENEFICIARY",
        nameEn  = "First Step",
        nameHi  = "पहला कदम",
        nameAs  = "প্ৰথম পদক্ষেপ",
        descEn  = "Registered your first beneficiary"
    )

    // ── Streak badges ─────────────────────────────────────────────────────────

    val STREAK_3 = BadgeDef(
        type    = "STREAK_3",
        nameEn  = "3-Day Streak",
        nameHi  = "3 दिन की लय",
        nameAs  = "3 দিনৰ ধাৰা",
        descEn  = "Active for 3 days in a row"
    )

    val STREAK_7 = BadgeDef(
        type    = "STREAK_7",
        nameEn  = "Week Warrior",
        nameHi  = "सप्ताह योद्धा",
        nameAs  = "সপ্তাহৰ যোদ্ধা",
        descEn  = "Active for 7 days in a row"
    )

    val STREAK_30 = BadgeDef(
        type    = "STREAK_30",
        nameEn  = "Monthly Champion",
        nameHi  = "मासिक चैंपियन",
        nameAs  = "মাহিলী চেম্পিয়ন",
        descEn  = "Active for 30 days in a row"
    )

    // ── Health activity badges ────────────────────────────────────────────────

    val ANC_HERO = BadgeDef(
        type    = "ANC_HERO",
        nameEn  = "ANC Hero",
        nameHi  = "ANC नायक",
        nameAs  = "ANC বীৰ",
        descEn  = "Completed your first ANC visit"
    )

    val IMMUNIZATION_GUARDIAN = BadgeDef(
        type    = "IMMUNIZATION_GUARDIAN",
        nameEn  = "Immunization Guardian",
        nameHi  = "टीकाकरण संरक्षक",
        nameAs  = "টিকাকৰণৰ ৰক্ষক",
        descEn  = "Recorded your first immunization"
    )

    val HRP_IDENTIFIER = BadgeDef(
        type    = "HRP_IDENTIFIER",
        nameEn  = "Life Saver",
        nameHi  = "जीवन रक्षक",
        nameAs  = "জীৱন ৰক্ষক",
        descEn  = "Identified your first high-risk pregnancy"
    )

    val NCD_CHAMPION = BadgeDef(
        type    = "NCD_CHAMPION",
        nameEn  = "NCD Champion",
        nameHi  = "NCD चैंपियन",
        nameAs  = "NCD চেম্পিয়ন",
        descEn  = "Completed your first NCD screening"
    )

    // ── Level badges ──────────────────────────────────────────────────────────

    val LEVEL_2 = BadgeDef(
        type    = "LEVEL_2",
        nameEn  = "Rising Star",
        nameHi  = "उभरता सितारा",
        nameAs  = "উদীয়মান তৰা",
        descEn  = "Reached Level 2"
    )

    val LEVEL_5 = BadgeDef(
        type    = "LEVEL_5",
        nameEn  = "Health Champion",
        nameHi  = "स्वास्थ्य चैंपियन",
        nameAs  = "স্বাস্থ্য চেম্পিয়ন",
        descEn  = "Reached Level 5"
    )
}
