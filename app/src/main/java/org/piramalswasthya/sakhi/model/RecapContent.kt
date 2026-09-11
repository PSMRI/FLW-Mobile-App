package org.piramalswasthya.sakhi.model

/**
 * Phase 7 — typed model of the bundled Monthly Recap content library
 * (`res/raw/recap_content.json`): the personalised Hindi/Assamese sentence
 * matrix (category × band × 3 variants), the band definitions, and the
 * intro/closing lines.
 *
 * This file is the SINGLE source of recap wording. The JSON shape is exactly
 * what a future server-hosted content endpoint would return, so lifting the
 * library server-side later only swaps the SOURCE — never these models nor the
 * composer that consumes them. Text placeholders: `{count}` only (the "this
 * month" phrasing is literal text inside each sentence, by design).
 *
 * ⚠️ EVERY property here MUST keep a default value. Kotlin only emits a synthetic
 * no-arg constructor when all parameters have defaults; without one, Gson falls
 * back to `Unsafe.allocateInstance` and leaves absent JSON keys as **null even in
 * non-null `String` fields**. Those nulls then crash the validator instead of
 * being rejected by it. With defaults, a missing key arrives as `""` or `0`, which
 * [org.piramalswasthya.sakhi.helpers.RecapContentCodec] already rejects — so
 * malformed content fails closed rather than throwing. Do not "tidy" these away.
 */
data class RecapContentLibrary(
    val schemaVersion: Int = 0,
    val contentVersion: Int = 0,
    val bandDefinitions: List<RecapBandDefinition> = emptyList(),
    val languages: List<RecapLanguageContent> = emptyList(),
) {
    /** Case-insensitive lookup so the app's "HI"/"AS" tokens match the file's "hi"/"as". */
    fun languageOrNull(token: String?): RecapLanguageContent? =
        token?.let { t -> languages.firstOrNull { it.id.equals(t, ignoreCase = true) } }
}

/**
 * One count band. [maxCount] == null means open-ended (e.g. HIGH = 6+).
 * Bands are uniform across categories — a grammatical/magnitude model
 * (SINGULAR / DOUBLE / MEDIUM / HIGH), not per-category performance tiers.
 */
data class RecapBandDefinition(
    val id: String = "",
    val minCount: Int = 0,
    val maxCount: Int? = null,
) {
    fun contains(count: Int): Boolean =
        count >= minCount && (maxCount == null || count <= maxCount)
}

data class RecapLanguageContent(
    val id: String = "",
    val name: String = "",
    /** Warm welcome line(s) shown before the first scene (no animation). */
    val intro: List<String> = emptyList(),
    /** Warm goodbye line(s) for the final scene. */
    val closing: List<String> = emptyList(),
    val categories: List<RecapCategoryContent> = emptyList(),
) {
    fun categoryOrNull(categoryId: String): RecapCategoryContent? =
        categories.firstOrNull { it.id == categoryId }
}

data class RecapCategoryContent(
    val id: String = "",
    val name: String = "",
    val countMeaning: String = "",
    val unit: String = "",
    val bands: List<RecapBandContent> = emptyList(),
) {
    fun bandOrNull(bandId: String): RecapBandContent? =
        bands.firstOrNull { it.id == bandId }
}

data class RecapBandContent(
    val id: String = "",
    val sentences: List<RecapSentence> = emptyList(),
)

data class RecapSentence(
    val id: String = "",
    val text: String = "",
)
