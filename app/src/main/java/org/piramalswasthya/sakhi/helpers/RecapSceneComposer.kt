package org.piramalswasthya.sakhi.helpers

import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import org.piramalswasthya.sakhi.model.RecapBandDefinition
import org.piramalswasthya.sakhi.model.RecapContentLibrary
import org.piramalswasthya.sakhi.model.RecapMetricStatus

/** One playback scene of the personalised Monthly Recap story. */
data class RecapScene(
    val type: Type,
    /** Resolved, display-ready text ({count} already substituted). */
    val text: String,
    /** Lottie raw resource, or null for the animation-less WELCOME scene. */
    val lottieRawRes: Int?,
    val categoryId: String? = null,
    val count: Int? = null,
    /** Stable content id (e.g. "hi_ncd_medium_2"); null for intro/closing lines. */
    val sentenceId: String? = null,
) {
    enum class Type { WELCOME, CATEGORY, GOODBYE }
}

/**
 * Phase 7 — composes the personalised scene list for one frozen recap snapshot.
 *
 * PURE + DETERMINISTIC: output depends only on (frozen metrics payload,
 * language token, frozen variantSeed, content library). The same snapshot
 * always replays the exact same story — no randomness, no clock — which is why
 * nothing new needs persisting (Phase 8 collapses into this determinism; the
 * DB stays at v61).
 *
 * Composition rules (all user-approved):
 * - Scene order = the content file's category order; only categories that are
 *   AVAILABLE with count > 0 appear (deferred/zero are silently omitted).
 * - Band = the file's own bandDefinitions (SINGULAR/DOUBLE/MEDIUM/HIGH).
 * - Sentence variant = variantSeed-rotated pick from the band's variants.
 * - Animation: the welcome scene shows the dedicated welcome animation (girl_8);
 *   girl_1..girl_6 rotate per category scene starting at a seed offset so
 *   consecutive scenes NEVER repeat an action; girl_7 is reserved for goodbye.
 * - EMPTY MONTH: if no category qualifies, returns an EMPTY list — the recap
 *   does not open at all (no fabricated celebration).
 */
class RecapSceneComposer(private val library: RecapContentLibrary) {

    fun compose(
        payload: MonthlyRecapMetricsPayload,
        languageToken: String?,
        variantSeed: Long,
    ): List<RecapScene> {
        val language = library.languageOrNull(languageToken)
            ?: library.languageOrNull(FALLBACK_LANGUAGE)
            ?: return emptyList()

        val categoryScenes = language.categories.mapIndexedNotNull { index, categoryContent ->
            val metric = payload.categories.firstOrNull { it.categoryId == categoryContent.id }
                ?: return@mapIndexedNotNull null
            if (metric.status != RecapMetricStatus.AVAILABLE.name) return@mapIndexedNotNull null
            val count = metric.categoryTotal
            if (count <= 0) return@mapIndexedNotNull null

            val bandId = bandIdFor(count, library.bandDefinitions) ?: return@mapIndexedNotNull null
            val sentences = categoryContent.bandOrNull(bandId)?.sentences
                ?.takeIf { it.isNotEmpty() } ?: return@mapIndexedNotNull null
            val sentence = sentences[variantIndex(variantSeed, index, sentences.size)]

            RecapScene(
                type = RecapScene.Type.CATEGORY,
                text = sentence.text.replace(COUNT_PLACEHOLDER, count.toString()),
                lottieRawRes = null, // assigned below once the qualifying set is known
                categoryId = categoryContent.id,
                count = count,
                sentenceId = sentence.id,
            )
        }

        // User decision: an all-zero month means the recap does not open at all.
        if (categoryScenes.isEmpty()) return emptyList()

        // Rotate girl_1..girl_6 from a seed offset — consecutive scenes always differ.
        val animationStart = ((variantSeed % STORY_ANIMATIONS.size) + STORY_ANIMATIONS.size)
            .toInt() % STORY_ANIMATIONS.size
        val animatedScenes = categoryScenes.mapIndexed { i, scene ->
            scene.copy(lottieRawRes = STORY_ANIMATIONS[(animationStart + i) % STORY_ANIMATIONS.size])
        }

        val welcome = language.intro.takeIf { it.isNotEmpty() }?.let {
            RecapScene(
                type = RecapScene.Type.WELCOME,
                text = it[variantIndex(variantSeed, WELCOME_SALT, it.size)],
                lottieRawRes = WELCOME_ANIMATION,
            )
        }
        val goodbye = language.closing.takeIf { it.isNotEmpty() }?.let {
            RecapScene(
                type = RecapScene.Type.GOODBYE,
                text = it[variantIndex(variantSeed, GOODBYE_SALT, it.size)],
                lottieRawRes = GOODBYE_ANIMATION,
            )
        }

        return listOfNotNull(welcome) + animatedScenes + listOfNotNull(goodbye)
    }

    private fun bandIdFor(count: Int, definitions: List<RecapBandDefinition>): String? =
        definitions.firstOrNull { it.contains(count) }?.id

    /** Seed-stable, always-in-range variant pick; salt de-correlates positions. */
    private fun variantIndex(seed: Long, salt: Int, size: Int): Int {
        if (size <= 1) return 0
        val mixed = seed + salt * 31L
        return (((mixed % size) + size) % size).toInt()
    }

    companion object {
        const val COUNT_PLACEHOLDER = "{count}"
        private const val FALLBACK_LANGUAGE = "hi"
        private const val WELCOME_SALT = 101
        private const val GOODBYE_SALT = 202

        /** The six interchangeable story actions; girl_7 goodbye, girl_8 welcome. */
        val STORY_ANIMATIONS = listOf(
            R.raw.recap_girl_1,
            R.raw.recap_girl_2,
            R.raw.recap_girl_3,
            R.raw.recap_girl_4,
            R.raw.recap_girl_5,
            R.raw.recap_girl_6,
        )
        val GOODBYE_ANIMATION = R.raw.recap_girl_7
        val WELCOME_ANIMATION = R.raw.recap_girl_8
    }
}
