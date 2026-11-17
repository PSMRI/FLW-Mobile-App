package org.piramalswasthya.sakhi.helpers

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.R
import java.io.File

/**
 * Structural rejections in [RecapContentCodec] not covered by RecapContentCodecTest, plus
 * [RecapContentCodec.loadBundled]. Each case edits one field of the real bundled library.
 */
class RecapContentCodecValidationTest {

    private val bundledFile = listOf(
        File("src/main/res/raw/recap_content.json"),
        File("app/src/main/res/raw/recap_content.json"),
    ).first { it.exists() }

    private fun rejectedAfter(edit: (JsonObject) -> Unit) {
        val root = JsonParser.parseString(bundledFile.readText(Charsets.UTF_8)).asJsonObject
        edit(root)
        assertNull(RecapContentCodec.decodeOrNull(root.toString()))
    }

    private val JsonObject.firstLanguage get() = getAsJsonArray("languages")[0].asJsonObject
    private val JsonObject.firstCategory get() = firstLanguage.getAsJsonArray("categories")[0].asJsonObject
    private val JsonObject.firstBand get() = firstCategory.getAsJsonArray("bands")[0].asJsonObject
    private val JsonObject.firstSentence get() = firstBand.getAsJsonArray("sentences")[0].asJsonObject

    @Test
    fun `json null literal is rejected`() {
        assertNull(RecapContentCodec.decodeOrNull("null"))
    }

    @Test
    fun `non positive content version is rejected`() = rejectedAfter {
        it.addProperty("contentVersion", 0)
    }

    @Test
    fun `empty band definitions are rejected`() = rejectedAfter {
        it.add("bandDefinitions", JsonArray())
    }

    @Test
    fun `duplicate band ids are rejected`() = rejectedAfter {
        val bands = it.getAsJsonArray("bandDefinitions")
        bands[1].asJsonObject.addProperty("id", bands[0].asJsonObject["id"].asString)
    }

    @Test
    fun `no open ended top band is rejected`() = rejectedAfter {
        it.getAsJsonArray("bandDefinitions").forEach { b ->
            if (b.asJsonObject["maxCount"]?.isJsonNull != false) b.asJsonObject.addProperty("maxCount", 999)
        }
    }

    @Test
    fun `band starting below one is rejected`() = rejectedAfter {
        it.getAsJsonArray("bandDefinitions")[0].asJsonObject.addProperty("minCount", 0)
    }

    @Test
    fun `band with max below min is rejected`() = rejectedAfter {
        it.getAsJsonArray("bandDefinitions")[1].asJsonObject.addProperty("maxCount", 1)
    }

    @Test
    fun `language without closing lines is rejected`() = rejectedAfter {
        it.firstLanguage.add("closing", JsonArray())
    }

    @Test
    fun `blank intro line is rejected`() = rejectedAfter {
        it.firstLanguage.getAsJsonArray("intro").add(" ")
    }

    @Test
    fun `duplicate category is rejected`() = rejectedAfter {
        val categories = it.firstLanguage.getAsJsonArray("categories")
        categories.add(categories[0].deepCopy())
    }

    @Test
    fun `duplicate band inside a category is rejected`() = rejectedAfter {
        val bands = it.firstCategory.getAsJsonArray("bands")
        bands.add(bands[0].deepCopy())
    }

    @Test
    fun `category missing a band is rejected`() = rejectedAfter {
        it.firstCategory.getAsJsonArray("bands").remove(0)
    }

    @Test
    fun `band without sentences is rejected`() = rejectedAfter {
        it.firstBand.add("sentences", JsonArray())
    }

    @Test
    fun `blank sentence id is rejected`() = rejectedAfter {
        it.firstSentence.addProperty("id", " ")
    }

    @Test
    fun `blank sentence text is rejected`() = rejectedAfter {
        it.firstSentence.addProperty("text", " ")
    }

    @Test
    fun `loads the bundled raw resource`() {
        val context = mockk<Context>()
        every { context.resources.openRawResource(R.raw.recap_content) } answers { bundledFile.inputStream() }
        assertNotNull(RecapContentCodec.loadBundled(context))
    }

    @Test
    fun `unreadable raw resource gives null`() {
        val context = mockk<Context>()
        every { context.resources } throws IllegalStateException("no resources")
        assertNull(RecapContentCodec.loadBundled(context))
    }
}
