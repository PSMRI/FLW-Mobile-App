package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import org.piramalswasthya.sakhi.model.RecapActivityMetric
import org.piramalswasthya.sakhi.model.RecapCategoryMetric
import org.piramalswasthya.sakhi.model.RecapContentLibrary
import org.piramalswasthya.sakhi.model.RecapMetricStatus
import java.io.File

/**
 * Phase 7 — behaviour tests for [RecapSceneComposer], run against the REAL
 * bundled content library so the story the tests prove is the story the ASHA
 * sees. Covers: scene order (welcome → categories → goodbye), zero/UNAVAILABLE
 * omission, the empty-month no-open rule, {count} substitution, band mapping,
 * animation rotation (girl_7 reserved for goodbye; no consecutive repeats),
 * language fallback and full determinism.
 */
class RecapSceneComposerTest {

    private lateinit var library: RecapContentLibrary
    private lateinit var composer: RecapSceneComposer

    @Before
    fun setUp() {
        val file = listOf(
            File("src/main/res/raw/recap_content.json"),
            File("app/src/main/res/raw/recap_content.json"),
        ).firstOrNull { it.exists() } ?: error("bundled content not found")
        library = RecapContentCodec.decodeOrNull(file.readText(Charsets.UTF_8))!!
        composer = RecapSceneComposer(library)
    }

    private fun payload(vararg counts: Pair<String, Int>) = MonthlyRecapMetricsPayload(
        payloadSchemaVersion = 2,
        calculationVersion = 2,
        recapYearMonth = 202606,
        generatedAt = 0L,
        windowStartMillis = 0L,
        windowEndMillisExclusive = 1L,
        categories = counts.map { (id, count) ->
            RecapCategoryMetric.from(
                id,
                listOf(RecapActivityMetric(activityId = "${id}_ACT", unit = "EVENT", count = count)),
            )
        },
    )

    // ---- story structure ----

    @Test
    fun `story is welcome, then categories, then girl7 goodbye`() {
        val scenes = composer.compose(payload("NCD" to 5, "HOUSEHOLD" to 2), "HI", 42L)
        assertEquals(RecapScene.Type.WELCOME, scenes.first().type)
        assertEquals(RecapScene.Type.GOODBYE, scenes.last().type)
        assertEquals(
            listOf(RecapScene.Type.CATEGORY, RecapScene.Type.CATEGORY),
            scenes.subList(1, scenes.size - 1).map { it.type },
        )
        assertEquals(RecapSceneComposer.WELCOME_ANIMATION, scenes.first().lottieRawRes) // greeting animation
        assertEquals(RecapSceneComposer.GOODBYE_ANIMATION, scenes.last().lottieRawRes)
    }

    @Test
    fun `scene order follows the content file category order`() {
        val scenes = composer.compose(
            payload("IMMUNIZATION" to 7, "NCD" to 3, "MATERNAL_HEALTH" to 1),
            "HI",
            0L,
        )
        val order = scenes.filter { it.type == RecapScene.Type.CATEGORY }.map { it.categoryId }
        assertEquals(listOf("NCD", "MATERNAL_HEALTH", "IMMUNIZATION"), order)
    }

    // ---- omission rules ----

    @Test
    fun `zero-count and absent categories are silently omitted`() {
        val scenes = composer.compose(payload("NCD" to 5, "HOUSEHOLD" to 0), "HI", 1L)
        val ids = scenes.filter { it.type == RecapScene.Type.CATEGORY }.map { it.categoryId }
        assertEquals(listOf("NCD"), ids)
    }

    @Test
    fun `UNAVAILABLE category is omitted even with a nonzero count`() {
        val unavailable = RecapCategoryMetric(
            categoryId = "NCD",
            activities = listOf(
                RecapActivityMetric("NCD_ACT", "EVENT", 9, RecapMetricStatus.UNAVAILABLE.name),
            ),
            categoryTotal = 9,
            status = RecapMetricStatus.UNAVAILABLE.name,
        )
        val scenes = composer.compose(
            payload("HOUSEHOLD" to 2).copy(categories = listOf(unavailable) + payload("HOUSEHOLD" to 2).categories),
            "HI",
            1L,
        )
        assertEquals(
            listOf("HOUSEHOLD"),
            scenes.filter { it.type == RecapScene.Type.CATEGORY }.map { it.categoryId },
        )
    }

    @Test
    fun `empty month composes an EMPTY story - recap does not open`() {
        assertTrue(composer.compose(payload("NCD" to 0, "HOUSEHOLD" to 0), "HI", 5L).isEmpty())
        assertTrue(composer.compose(payload(), "HI", 5L).isEmpty())
    }

    // ---- text resolution ----

    @Test
    fun `count placeholder is substituted and sentence matches the band`() {
        val one = composer.compose(payload("NCD" to 1), "HI", 3L)
        val oneScene = one.first { it.type == RecapScene.Type.CATEGORY }
        assertTrue(oneScene.sentenceId!!.contains("singular"))
        assertTrue(oneScene.text.contains("1"))
        assertTrue(!oneScene.text.contains(RecapSceneComposer.COUNT_PLACEHOLDER))

        val two = composer.compose(payload("NCD" to 2), "HI", 3L)
        assertTrue(two.first { it.type == RecapScene.Type.CATEGORY }.sentenceId!!.contains("double"))
        val four = composer.compose(payload("NCD" to 4), "HI", 3L)
        assertTrue(four.first { it.type == RecapScene.Type.CATEGORY }.sentenceId!!.contains("medium"))
        val many = composer.compose(payload("NCD" to 60), "HI", 3L)
        assertTrue(many.first { it.type == RecapScene.Type.CATEGORY }.sentenceId!!.contains("high"))
    }

    @Test
    fun `assamese token selects assamese sentences, unknown token falls back to hindi`() {
        val assamese = composer.compose(payload("NCD" to 5), "AS", 7L)
        assertTrue(assamese.first { it.type == RecapScene.Type.CATEGORY }.sentenceId!!.startsWith("as_"))
        val fallback = composer.compose(payload("NCD" to 5), "EN", 7L)
        assertTrue(fallback.first { it.type == RecapScene.Type.CATEGORY }.sentenceId!!.startsWith("hi_"))
        val nullToken = composer.compose(payload("NCD" to 5), null, 7L)
        assertTrue(nullToken.first { it.type == RecapScene.Type.CATEGORY }.sentenceId!!.startsWith("hi_"))
    }

    // ---- animation rules ----

    @Test
    fun `category animations rotate with no consecutive repeats and never use girl7`() {
        val scenes = composer.compose(
            payload(
                "NCD" to 5, "HOUSEHOLD" to 2, "BENEFICIARY" to 11,
                "ELIGIBLE_COUPLE" to 1, "MATERNAL_HEALTH" to 4, "IMMUNIZATION" to 30,
            ),
            "HI",
            99L,
        )
        val anims = scenes.filter { it.type == RecapScene.Type.CATEGORY }.map { it.lottieRawRes!! }
        assertEquals(6, anims.size)
        assertEquals(6, anims.toSet().size) // all six actions distinct across the story
        anims.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
        assertTrue(anims.none { it == RecapSceneComposer.GOODBYE_ANIMATION })
        assertTrue(anims.all { it in RecapSceneComposer.STORY_ANIMATIONS })
        // Welcome (girl_8) is dedicated and distinct from story + goodbye animations.
        assertTrue(RecapSceneComposer.WELCOME_ANIMATION !in RecapSceneComposer.STORY_ANIMATIONS)
        assertNotEquals(RecapSceneComposer.WELCOME_ANIMATION, RecapSceneComposer.GOODBYE_ANIMATION)
    }

    // ---- determinism ----

    @Test
    fun `same inputs always compose the identical story`() {
        val p = payload("NCD" to 5, "MATERNAL_HEALTH" to 3, "IMMUNIZATION" to 21)
        assertEquals(composer.compose(p, "HI", 1234L), composer.compose(p, "HI", 1234L))
    }

    @Test
    fun `negative variant seed is handled safely`() {
        val scenes = composer.compose(payload("NCD" to 5), "HI", Long.MIN_VALUE + 7)
        assertEquals(3, scenes.size) // welcome + 1 category + goodbye
        assertTrue(scenes[1].lottieRawRes!! in RecapSceneComposer.STORY_ANIMATIONS)
    }

    @Test
    fun `different seeds can pick different variants but never break structure`() {
        val p = payload("NCD" to 5)
        (0L..11L).forEach { seed ->
            val scenes = composer.compose(p, "HI", seed)
            assertEquals(3, scenes.size)
            assertEquals(RecapScene.Type.WELCOME, scenes.first().type)
            assertEquals(RecapScene.Type.GOODBYE, scenes.last().type)
        }
    }
}
