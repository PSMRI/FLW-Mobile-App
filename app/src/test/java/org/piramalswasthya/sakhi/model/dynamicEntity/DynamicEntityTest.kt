package org.piramalswasthya.sakhi.model.dynamicEntity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicEntityTest {

    // =====================================================
    // FormResponseJsonEntity Tests
    // =====================================================

    @Test fun `FormResponseJsonEntity creation`() {
        val entity = FormResponseJsonEntity(benId = 1L, hhId = 2L, visitDay = "Day 1", visitDate = "14-04-2026", formId = "form_001", version = 1, formDataJson = "{}")
        assertEquals(1L, entity.benId)
        assertEquals(2L, entity.hhId)
        assertEquals("Day 1", entity.visitDay)
        assertEquals("14-04-2026", entity.visitDate)
        assertEquals("form_001", entity.formId)
        assertEquals(1, entity.version)
        assertEquals("{}", entity.formDataJson)
    }

    @Test fun `FormResponseJsonEntity default id is 0`() {
        val entity = FormResponseJsonEntity(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, entity.id)
    }

    @Test fun `FormResponseJsonEntity default isSynced is false`() {
        val entity = FormResponseJsonEntity(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(entity.isSynced)
    }

    @Test fun `FormResponseJsonEntity default syncedAt is null`() {
        val entity = FormResponseJsonEntity(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertNull(entity.syncedAt)
    }

    @Test fun `FormResponseJsonEntity copy changes benId`() {
        val entity = FormResponseJsonEntity(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        val copy = entity.copy(benId = 99L)
        assertEquals(99L, copy.benId)
    }

    @Test fun `FormResponseJsonEntity same key fields`() {
        val ts = 1000L
        val a = FormResponseJsonEntity(id = 1, benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}", createdAt = ts)
        val b = FormResponseJsonEntity(id = 1, benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}", createdAt = ts)
        assertEquals(a, b)
    }

    @Test fun `FormResponseJsonEntity not equals`() {
        val a = FormResponseJsonEntity(id = 1, benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        val b = FormResponseJsonEntity(id = 2, benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertNotEquals(a, b)
    }

    @Test fun `FormResponseJsonEntity createdAt has value`() {
        val entity = FormResponseJsonEntity(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertTrue(entity.createdAt > 0)
    }

    // =====================================================
    // FormSubmitRequest Tests
    // =====================================================

    @Test fun `FormSubmitRequest creation`() {
        val req = FormSubmitRequest("user", "form1", 1L, 2L, "2026-04-14", emptyMap())
        assertEquals("user", req.userName)
        assertEquals("form1", req.formId)
        assertEquals(1L, req.beneficiaryId)
        assertEquals(2L, req.houseHoldId)
        assertEquals("2026-04-14", req.visitDate)
        assertTrue(req.fields.isEmpty())
    }

    @Test fun `FormSubmitRequest with fields`() {
        val fields = mapOf("name" to "John" as Any?, "age" to 30 as Any?)
        val req = FormSubmitRequest("user", "form1", 1L, 2L, "2026-04-14", fields)
        assertEquals(2, req.fields.size)
        assertEquals("John", req.fields["name"])
        assertEquals(30, req.fields["age"])
    }

    @Test fun `FormSubmitRequest copy`() {
        val req = FormSubmitRequest("user", "form1", 1L, 2L, "date", emptyMap())
        val copy = req.copy(userName = "admin")
        assertEquals("admin", copy.userName)
        assertEquals("form1", copy.formId)
    }

    @Test fun `FormSubmitRequest equals`() {
        val a = FormSubmitRequest("u", "f", 1L, 2L, "d", emptyMap())
        val b = FormSubmitRequest("u", "f", 1L, 2L, "d", emptyMap())
        assertEquals(a, b)
    }

    // =====================================================
    // FormSchemaEntity Tests
    // =====================================================

    @Test fun `FormSchemaEntity creation`() {
        val entity = FormSchemaEntity(formId = "f1", formName = "Form", language = "en", schemaJson = "{}")
        assertEquals("f1", entity.formId)
        assertEquals("Form", entity.formName)
        assertEquals("en", entity.language)
        assertEquals(1, entity.version)
        assertEquals("{}", entity.schemaJson)
    }

    @Test fun `FormSchemaEntity with custom version`() {
        val entity = FormSchemaEntity(formId = "f1", formName = "Form", language = "en", version = 5, schemaJson = "{}")
        assertEquals(5, entity.version)
    }

    @Test fun `FormSchemaEntity copy`() {
        val entity = FormSchemaEntity(formId = "f1", formName = "Form", language = "en", schemaJson = "{}")
        val copy = entity.copy(language = "hi")
        assertEquals("hi", copy.language)
        assertEquals("f1", copy.formId)
    }

    @Test fun `FormSchemaEntity equals`() {
        val a = FormSchemaEntity("f1", "Form", "en", 1, "{}")
        val b = FormSchemaEntity("f1", "Form", "en", 1, "{}")
        assertEquals(a, b)
    }

    @Test fun `FormSchemaEntity not equals for different formId`() {
        val a = FormSchemaEntity("f1", "Form", "en", 1, "{}")
        val b = FormSchemaEntity("f2", "Form", "en", 1, "{}")
        assertNotEquals(a, b)
    }
}
