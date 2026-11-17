package org.piramalswasthya.sakhi.notifications

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.model.NotifTemplateCache
import java.util.Calendar

class ActivityClassifierTest {

    @Test
    fun `buckets map counts to activity bands`() {
        assertEquals(ActivityClassifier.Bucket.ZERO, ActivityClassifier.bucketFor(-1))
        assertEquals(ActivityClassifier.Bucket.ZERO, ActivityClassifier.bucketFor(0))
        assertEquals(ActivityClassifier.Bucket.ONE, ActivityClassifier.bucketFor(1))
        assertEquals(ActivityClassifier.Bucket.TWO_PLUS, ActivityClassifier.bucketFor(2))
        assertEquals(ActivityClassifier.Bucket.TWO_PLUS, ActivityClassifier.bucketFor(40))
    }

    @Test
    fun `date key is the local calendar day`() {
        val time = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 5, 23, 59) }.timeInMillis
        assertEquals("2026-03-05", ActivityClassifier.dateKey(time))
    }

    @Test
    fun `included forms exclude death reports and village level forms`() {
        val included = ActivityClassifier.INCLUDED
        assertEquals("IMMUNIZATION", included["child_immunization"])
        assertEquals("CBAC", included["ncd_screening"])
        assertTrue(included.keys.none { it.contains("cdr") || it.contains("mdsr") || it.contains("vhnd") })
        assertTrue(included.values.none { it.isBlank() })
    }
}

class BundledTemplatesTest {

    @Test
    fun `every bucket has templates in every language`() {
        val all = BundledTemplates.ALL
        for (bucket in ActivityClassifier.Bucket.values().map { it.name }) {
            for (lang in listOf("en", "hi", "as", "bn")) {
                assertEquals(
                    "$bucket/$lang",
                    3,
                    all.count { it.bucket == bucket && it.language == lang }
                )
            }
        }
    }

    @Test
    fun `bundled templates are version zero, unique and personalised`() {
        val all = BundledTemplates.ALL
        assertEquals(all.size, all.map { it.templateId }.toSet().size)
        assertTrue(all.all { it.libraryVersion == 0 })
        assertTrue(all.all { it.bodyTemplate.contains("{name}") })
    }
}

class TemplateFillerTest {

    private val filler = TemplateFiller()

    @Test
    fun `fills the name slot`() {
        assertEquals("Hi Rani!", filler.fill("Hi {name}!", "  Rani "))
    }

    @Test
    fun `missing or blank name falls back to ASHA`() {
        assertEquals("Hi ASHA", filler.fill("Hi {name}", null))
        assertEquals("Hi ASHA", filler.fill("Hi {name}", "   "))
    }
}

class TemplateSelectorTest {

    private val dao = mockk<EveningNotifDao>()
    private val selector = TemplateSelector(dao)

    private fun tpl(id: String, bucket: String = "ONE", lang: String = "hi") =
        NotifTemplateCache(id, bucket, lang, "body {name}", libraryVersion = 3)

    @Test
    fun `picks from the language pool excluding recently shown`() = runTest {
        coEvery { dao.templatesFor("ONE", "hi") } returns listOf(tpl("a"), tpl("b"), tpl("c"))
        coEvery { dao.recentTemplateIds("ONE", 3) } returns listOf("a", "b")

        repeat(5) { assertEquals("c", selector.select("ONE", "hi").templateId) }
    }

    @Test
    fun `resets the recent filter when everything was shown`() = runTest {
        coEvery { dao.templatesFor("ONE", "hi") } returns listOf(tpl("a"), tpl("b"))
        coEvery { dao.recentTemplateIds("ONE", 3) } returns listOf("a", "b")

        assertTrue(selector.select("ONE", "hi").templateId in setOf("a", "b"))
    }

    @Test
    fun `falls back to synced English templates`() = runTest {
        coEvery { dao.templatesFor("ZERO", "as") } returns emptyList()
        coEvery { dao.templatesFor("ZERO", "en") } returns listOf(tpl("en1", "ZERO", "en"))
        coEvery { dao.recentTemplateIds(any(), any()) } returns emptyList()

        assertEquals("en1", selector.select("ZERO", "as").templateId)
    }

    @Test
    fun `falls back to bundled templates in the worker's language`() = runTest {
        coEvery { dao.templatesFor(any(), any()) } returns emptyList()
        coEvery { dao.recentTemplateIds(any(), any()) } returns emptyList()

        val picked = selector.select("TWO_PLUS", "bn")
        assertEquals("TWO_PLUS", picked.bucket)
        assertEquals("bn", picked.language)
    }

    @Test
    fun `unknown language falls back to bundled English`() = runTest {
        coEvery { dao.templatesFor(any(), any()) } returns emptyList()
        coEvery { dao.recentTemplateIds(any(), any()) } returns emptyList()

        val picked = selector.select("ONE", "ta")
        assertEquals("en", picked.language)
        assertEquals("ONE", picked.bucket)
    }
}

class TemplateSyncHandlerTest {

    private val dao = mockk<EveningNotifDao>(relaxed = true)
    private val scheduler = mockk<NotificationScheduler>(relaxed = true)
    private val handler = TemplateSyncHandler(dao, scheduler)

    private val validLibrary = """
        [
          {"template_id":"t1","bucket":"ONE","language":"hi","body_template":"नमस्ते {name}"},
          {"template_id":"t2","bucket":"ZERO","language":"","body_template":"Hello {name}"},
          {"template_id":"t3","bucket":"TWO_PLUS","body_template":"Busy {name}"}
        ]
    """.trimIndent()

    @Test
    fun `newer library replaces templates with language defaults`() = runTest {
        coEvery { dao.libraryVersion() } returns 1

        handler.handlePullPayload(2, validLibrary, "20:30")

        coVerify { scheduler.updateEveningTime("20:30") }
        coVerify {
            dao.replaceTemplates(match { list ->
                list.map { it.templateId } == listOf("t1", "t2", "t3") &&
                        list.map { it.language } == listOf("hi", "en", "en") &&
                        list.all { it.libraryVersion == 2 }
            })
        }
    }

    @Test
    fun `first library installs when nothing is stored`() = runTest {
        coEvery { dao.libraryVersion() } returns null

        handler.handlePullPayload(1, validLibrary, null)

        coVerify(exactly = 0) { scheduler.updateEveningTime(any()) }
        coVerify { dao.replaceTemplates(any()) }
    }

    @Test
    fun `same or older version is ignored`() = runTest {
        coEvery { dao.libraryVersion() } returns 4

        handler.handlePullPayload(4, validLibrary, null)
        handler.handlePullPayload(3, validLibrary, null)

        coVerify(exactly = 0) { dao.replaceTemplates(any()) }
    }

    @Test
    fun `missing payload only updates timing`() = runTest {
        handler.handlePullPayload(null, validLibrary, "21:15")
        handler.handlePullPayload(5, "  ", null)
        handler.handlePullPayload(5, null, null)

        coVerify(exactly = 1) { scheduler.updateEveningTime("21:15") }
        coVerify(exactly = 0) { dao.libraryVersion() }
        coVerify(exactly = 0) { dao.replaceTemplates(any()) }
    }

    @Test
    fun `an incomplete entry rejects the whole library`() = runTest {
        coEvery { dao.libraryVersion() } returns 0
        val partial = """
            [{"template_id":"t1","bucket":"ONE","body_template":"ok"},
             {"template_id":"t2","bucket":"","body_template":"no bucket"}]
        """.trimIndent()

        handler.handlePullPayload(2, partial, null)
        handler.handlePullPayload(2, "[\"not an object\"]", null)

        coVerify(exactly = 0) { dao.replaceTemplates(any()) }
    }

    @Test
    fun `empty array and malformed json are no-ops`() = runTest {
        coEvery { dao.libraryVersion() } returns 0

        handler.handlePullPayload(2, "[]", null)
        handler.handlePullPayload(2, "{oops", null)

        coVerify(exactly = 0) { dao.replaceTemplates(any()) }
    }
}
