package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 7 — contract tests binding the REAL bundled content library
 * (`res/raw/recap_content.json`) to the codec/model. If anyone edits the file
 * into a shape the engine can't consume (missing band, empty variants, lost
 * {count}), these tests fail at build time rather than silently at runtime.
 */
class RecapContentCodecTest {

    private fun readBundledJson(): String {
        val candidates = listOf(
            File("src/main/res/raw/recap_content.json"),
            File("app/src/main/res/raw/recap_content.json"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("recap_content.json not found (cwd=${File(".").absolutePath})")
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun `bundled library decodes and passes structural sanity`() {
        val library = RecapContentCodec.decodeOrNull(readBundledJson())
        assertNotNull(library)
        assertEquals(1, library!!.schemaVersion)
        assertEquals(4, library.bandDefinitions.size)
        assertEquals(listOf("SINGULAR", "DOUBLE", "MEDIUM", "HIGH"), library.bandDefinitions.map { it.id })
        assertEquals(listOf("hi", "as"), library.languages.map { it.id })
    }

    @Test
    fun `every language has all 6 categories, 4 bands each, 3 variants each, all with count placeholder`() {
        val library = RecapContentCodec.decodeOrNull(readBundledJson())!!
        val expectedCategories =
            setOf("NCD", "HOUSEHOLD", "BENEFICIARY", "ELIGIBLE_COUPLE", "MATERNAL_HEALTH", "IMMUNIZATION")
        library.languages.forEach { language ->
            assertEquals(expectedCategories, language.categories.map { it.id }.toSet())
            language.categories.forEach { category ->
                assertEquals(
                    setOf("SINGULAR", "DOUBLE", "MEDIUM", "HIGH"),
                    category.bands.map { it.id }.toSet(),
                )
                category.bands.forEach { band ->
                    assertEquals(3, band.sentences.size)
                    band.sentences.forEach { sentence ->
                        assertTrue(
                            "${sentence.id} must contain {count}",
                            sentence.text.contains(RecapSceneComposer.COUNT_PLACEHOLDER),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `bundled library carries intro and closing lines for both languages`() {
        val library = RecapContentCodec.decodeOrNull(readBundledJson())!!
        library.languages.forEach { language ->
            assertTrue("${language.id} intro missing", language.intro.isNotEmpty())
            assertTrue("${language.id} closing missing", language.closing.isNotEmpty())
        }
    }

    @Test
    fun `band definitions cover every count from 1 upward without gaps`() {
        val library = RecapContentCodec.decodeOrNull(readBundledJson())!!
        (1..100).forEach { count ->
            assertEquals(
                "count $count must map to exactly one band",
                1,
                library.bandDefinitions.count { it.contains(count) },
            )
        }
        assertTrue(library.bandDefinitions.any { it.maxCount == null }) // open-ended top band
    }

    @Test
    fun `language lookup is case-insensitive and null-safe`() {
        val library = RecapContentCodec.decodeOrNull(readBundledJson())!!
        assertNotNull(library.languageOrNull("HI"))
        assertNotNull(library.languageOrNull("as"))
        assertNull(library.languageOrNull("EN"))
        assertNull(library.languageOrNull(null))
    }

    @Test
    fun `malformed or empty json decodes to null, never throws`() {
        assertNull(RecapContentCodec.decodeOrNull(null))
        assertNull(RecapContentCodec.decodeOrNull(""))
        assertNull(RecapContentCodec.decodeOrNull("not json at all"))
        assertNull(RecapContentCodec.decodeOrNull("{}")) // structurally empty
        assertNull(RecapContentCodec.decodeOrNull("""{"schemaVersion":1,"languages":[]}"""))
    }

    // ---- strict validation: bad content must be REJECTED, not silently degraded ----

    /** Mutates the real bundled JSON so each test changes exactly one thing. */
    private fun mutatedBundle(transform: (String) -> String): String? =
        RecapContentCodec.decodeOrNull(transform(readBundledJson()))?.let { "accepted" }

    /**
     * Rewrites the text of the FIRST sentence only.
     *
     * Deliberately surgical: a naive replaceFirst("{count}") would hit the
     * top-level "placeholder" metadata field rather than a sentence, mutating
     * something the validator does not inspect and silently passing the test.
     */
    private fun mutatedFirstSentence(transform: (String) -> String): String? {
        val json = readBundledJson()
        val marker = "\"text\": \""
        val start = json.indexOf(marker).also { require(it >= 0) { "no sentence text found" } } + marker.length
        val end = json.indexOf('"', start)
        val mutated = json.substring(0, start) + transform(json.substring(start, end)) + json.substring(end)
        return RecapContentCodec.decodeOrNull(mutated)?.let { "accepted" }
    }

    @Test
    fun `unsupported schema version is rejected`() {
        assertNull(mutatedBundle { it.replace("\"schemaVersion\": 1", "\"schemaVersion\": 99") })
    }

    @Test
    fun `missing a required language is rejected`() {
        // Truncate the Assamese language block by renaming its id to a stray value.
        assertNull(mutatedBundle { it.replace("\"id\": \"as\"", "\"id\": \"xx\"") })
    }

    @Test
    fun `missing a required category is rejected`() {
        assertNull(mutatedBundle { it.replace("\"id\": \"IMMUNIZATION\"", "\"id\": \"NOT_A_CATEGORY\"") })
    }

    @Test
    fun `duplicate sentence id is rejected`() {
        assertNull(mutatedBundle { it.replace("\"id\": \"as_ncd_singular_2\"", "\"id\": \"as_ncd_singular_1\"") })
    }

    @Test
    fun `sentence missing the count placeholder is rejected`() {
        assertNull(mutatedFirstSentence { it.replace("{count}", "some") })
    }

    @Test
    fun `sentence with two count placeholders is rejected`() {
        assertNull(mutatedFirstSentence { "$it {count}" })
    }

    @Test
    fun `sentence with an unsupported token is rejected`() {
        assertNull(mutatedFirstSentence { "$it {month}" })
    }

    @Test
    fun `band gap leaves counts unmatched and is rejected`() {
        // MEDIUM 3..5 -> 4..5 leaves count=3 unmatched.
        assertNull(
            mutatedBundle {
                it.replace(
                    "\"id\": \"MEDIUM\",\n      \"minCount\": 3",
                    "\"id\": \"MEDIUM\",\n      \"minCount\": 4",
                )
            }
        )
    }
}
