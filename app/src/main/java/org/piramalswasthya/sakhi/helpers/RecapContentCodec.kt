package org.piramalswasthya.sakhi.helpers

import android.content.Context
import com.google.gson.Gson
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.RecapContentLibrary
import timber.log.Timber

/**
 * Phase 7 — decodes AND strictly validates the Monthly Recap content library.
 *
 * [decodeOrNull] is a pure String→model function (JVM-unit-testable against the
 * real bundled file). [loadBundled] reads `res/raw/recap_content.json` — the
 * bundled library is the offline/default source; a future server-hosted library
 * would be decoded by this same codec and simply take precedence.
 *
 * Validation is deliberately STRICT and all-or-nothing: bad content must fail
 * loudly here (→ null → recap stays closed) rather than silently dropping a
 * category and quietly shrinking an ASHA's story. Every rejection is logged
 * with its reason so a bad content push is diagnosable from logcat.
 */
object RecapContentCodec {

    /** Schema versions this build knows how to render. */
    private val SUPPORTED_SCHEMA_VERSIONS = setOf(1)

    /** Languages the app can present; both must be fully populated. */
    private val REQUIRED_LANGUAGES = setOf("hi", "as")

    /** Categories the metrics engine can emit; content is required for each. */
    private val REQUIRED_CATEGORIES = setOf(
        "NCD", "HOUSEHOLD", "BENEFICIARY", "ELIGIBLE_COUPLE", "MATERNAL_HEALTH", "IMMUNIZATION",
    )

    private val gson = Gson()

    /**
     * Decodes and fully validates the content library, or returns null.
     *
     * Validation runs INSIDE the try deliberately. Gson can hand back objects that
     * violate their own Kotlin types (see the warning on [RecapContentLibrary]), so
     * validation itself is a place a runtime failure can surface. Keeping it inside
     * means this never throws at the caller: bad content always degrades to null,
     * and the recap simply does not open.
     */
    fun decodeOrNull(json: String?): RecapContentLibrary? {
        if (json.isNullOrBlank()) return null
        return try {
            val library = gson.fromJson(json, RecapContentLibrary::class.java)
                ?: return null
            val rejection = validate(library)
            if (rejection == null) {
                library
            } else {
                Timber.e("Recap content rejected: $rejection")
                null
            }
        } catch (e: Exception) {
            Timber.w(e, "Recap content: unusable library")
            null
        }
    }

    /** Returns null when the library is fully usable, else the reason it was rejected. */
    private fun validate(library: RecapContentLibrary): String? {
        if (library.schemaVersion !in SUPPORTED_SCHEMA_VERSIONS) {
            return "unsupported schemaVersion=${library.schemaVersion} (supported=$SUPPORTED_SCHEMA_VERSIONS)"
        }
        if (library.contentVersion <= 0) return "contentVersion must be positive"

        // --- band definitions: non-empty, unique ids, and gapless from 1 upward ---
        val bands = library.bandDefinitions
        if (bands.isEmpty()) return "no bandDefinitions"
        val bandIds = bands.map { it.id }
        if (bandIds.size != bandIds.toSet().size) return "duplicate band ids: $bandIds"
        if (bands.none { it.maxCount == null }) return "no open-ended top band (every high count would be unmatched)"
        bands.forEach { band ->
            if (band.minCount < 1) return "band ${band.id} has minCount<1 (0 is the omit case)"
            if (band.maxCount != null && band.maxCount < band.minCount) {
                return "band ${band.id} has maxCount<minCount"
            }
        }
        // Every plausible count must resolve to exactly one band — no gaps, no overlaps.
        for (count in 1..BAND_COVERAGE_PROBE) {
            val matches = bands.count { it.contains(count) }
            if (matches != 1) return "count=$count matches $matches bands (must be exactly 1)"
        }

        // --- languages: both required, none duplicated ---
        val languageIds = library.languages.map { it.id.lowercase() }
        if (languageIds.size != languageIds.toSet().size) return "duplicate language ids: $languageIds"
        val missingLanguages = REQUIRED_LANGUAGES - languageIds.toSet()
        if (missingLanguages.isNotEmpty()) return "missing language content: $missingLanguages"

        // --- per language: intro/closing, all categories, all bands, valid sentences ---
        val seenSentenceIds = mutableSetOf<String>()
        library.languages.forEach { language ->
            if (language.intro.isEmpty()) return "language ${language.id}: no intro line"
            if (language.closing.isEmpty()) return "language ${language.id}: no closing line"
            if (language.intro.any { it.isBlank() } || language.closing.any { it.isBlank() }) {
                return "language ${language.id}: blank intro/closing line"
            }

            val categoryIds = language.categories.map { it.id }
            if (categoryIds.size != categoryIds.toSet().size) {
                return "language ${language.id}: duplicate category ids"
            }
            val missingCategories = REQUIRED_CATEGORIES - categoryIds.toSet()
            if (missingCategories.isNotEmpty()) {
                return "language ${language.id}: missing categories $missingCategories"
            }

            language.categories.forEach { category ->
                val categoryBandIds = category.bands.map { it.id }
                if (categoryBandIds.size != categoryBandIds.toSet().size) {
                    return "${language.id}/${category.id}: duplicate band ids"
                }
                val missingBands = bandIds.toSet() - categoryBandIds.toSet()
                if (missingBands.isNotEmpty()) {
                    return "${language.id}/${category.id}: missing bands $missingBands"
                }
                category.bands.forEach { band ->
                    if (band.sentences.isEmpty()) {
                        return "${language.id}/${category.id}/${band.id}: no sentences"
                    }
                    band.sentences.forEach { sentence ->
                        if (sentence.id.isBlank()) {
                            return "${language.id}/${category.id}/${band.id}: blank sentence id"
                        }
                        if (!seenSentenceIds.add(sentence.id)) {
                            return "duplicate sentence id: ${sentence.id}"
                        }
                        if (sentence.text.isBlank()) return "sentence ${sentence.id}: blank text"
                        // Extract every {token}; require exactly one and only {count}.
                        val tokens = BRACE_TOKEN_REGEX.findAll(sentence.text).map { it.value }.toList()
                        val countTokens = tokens.count { it == RecapSceneComposer.COUNT_PLACEHOLDER }
                        if (countTokens != 1) {
                            return "sentence ${sentence.id}: expected exactly one ${RecapSceneComposer.COUNT_PLACEHOLDER}, found $countTokens"
                        }
                        tokens.firstOrNull { it != RecapSceneComposer.COUNT_PLACEHOLDER }?.let {
                            return "sentence ${sentence.id}: unsupported token $it"
                        }
                    }
                }
            }
        }
        return null
    }

    fun loadBundled(context: Context): RecapContentLibrary? = try {
        context.resources.openRawResource(R.raw.recap_content)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .let(::decodeOrNull)
    } catch (e: Exception) {
        Timber.w(e, "Failed to read bundled recap content")
        null
    }

    /** Counts high enough to cover any realistic monthly total. */
    private const val BAND_COVERAGE_PROBE = 5_000

    /**
     * Matches any single `{token}`. Both braces are escaped so the pattern is
     * valid on BOTH the JVM regex engine (unit tests) AND Android's stricter
     * ICU engine (device) — an unescaped `}` compiles on the JVM but throws
     * PatternSyntaxException on device. The validator inspects each match and
     * requires it to equal {count}; this avoids a lookahead entirely.
     */
    private val BRACE_TOKEN_REGEX = Regex("\\{[^}]*\\}")
}
