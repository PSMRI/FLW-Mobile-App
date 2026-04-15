package org.piramalswasthya.sakhi.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity

class HomeVisitHelperTest {

    // --- extractVisitNumber ---

    @Test
    fun `extractVisitNumber returns correct number from valid JSON`() {
        val json = """{"visitNumber": 3, "otherField": "value"}"""
        assertEquals(3, HomeVisitHelper.extractVisitNumber(json))
    }

    @Test
    fun `extractVisitNumber returns 1 when field is missing`() {
        val json = """{"otherField": "value"}"""
        assertEquals(1, HomeVisitHelper.extractVisitNumber(json))
    }

    @Test
    fun `extractVisitNumber returns 1 for malformed JSON`() {
        assertEquals(1, HomeVisitHelper.extractVisitNumber("not valid json"))
    }

    @Test
    fun `extractVisitNumber returns 1 for empty string`() {
        assertEquals(1, HomeVisitHelper.extractVisitNumber(""))
    }

    @Test
    fun `extractVisitNumber returns visit number 1`() {
        val json = """{"visitNumber": 1}"""
        assertEquals(1, HomeVisitHelper.extractVisitNumber(json))
    }

    @Test
    fun `extractVisitNumber returns large visit number`() {
        val json = """{"visitNumber": 15}"""
        assertEquals(15, HomeVisitHelper.extractVisitNumber(json))
    }

    // --- isHomeVisit ---

    @Test
    fun `isHomeVisit returns true for home visit form`() {
        val entity = createAncEntity(formId = "anc_form_001")
        assertTrue(HomeVisitHelper.isHomeVisit(entity))
    }

    @Test
    fun `isHomeVisit returns false for other forms`() {
        val entity = createAncEntity(formId = "anc_form_002")
        assertFalse(HomeVisitHelper.isHomeVisit(entity))
    }

    @Test
    fun `isHomeVisit returns false for empty form id`() {
        val entity = createAncEntity(formId = "")
        assertFalse(HomeVisitHelper.isHomeVisit(entity))
    }

    // --- getNextVisitNumber ---

    @Test
    fun `getNextVisitNumber with 4 home visits returns 5`() {
        val entities = listOf(
            createAncEntity(formId = "anc_form_001"),
            createAncEntity(formId = "anc_form_001"),
            createAncEntity(formId = "anc_form_002"),  // not a home visit
            createAncEntity(formId = "anc_form_001"),
            createAncEntity(formId = "anc_form_001")
        )
        assertEquals(5, HomeVisitHelper.getNextVisitNumber(entities))
    }

    @Test
    fun `getNextVisitNumber with no home visits returns 1`() {
        val entities = listOf(
            createAncEntity(formId = "anc_form_002"),
            createAncEntity(formId = "anc_form_003")
        )
        assertEquals(1, HomeVisitHelper.getNextVisitNumber(entities))
    }

    @Test
    fun `getNextVisitNumber with empty list returns 1`() {
        assertEquals(1, HomeVisitHelper.getNextVisitNumber(emptyList()))
    }

    // --- getVisitDate ---

    @Test
    fun `getVisitDate parses valid date string`() {
        val entity = createAncEntity(visitDate = "17-03-2026")
        val result = HomeVisitHelper.getVisitDate(entity)
        assertTrue(result > 0)
    }

    @Test
    fun `getVisitDate falls back to createdAt for invalid date`() {
        val createdAt = 1234567890L
        val entity = createAncEntity(visitDate = "invalid-date", createdAt = createdAt)
        assertEquals(createdAt, HomeVisitHelper.getVisitDate(entity))
    }

    // --- getSortedHomeVisits ---

    @Test
    fun `getSortedHomeVisits filters non home visits`() {
        val entities = listOf(
            createAncEntity(formId = "anc_form_001", visitDate = "17-03-2026"),
            createAncEntity(formId = "anc_form_002", visitDate = "18-03-2026"),
            createAncEntity(formId = "anc_form_001", visitDate = "19-03-2026")
        )
        val result = HomeVisitHelper.getSortedHomeVisits(entities)
        assertEquals(2, result.size)
    }

    @Test
    fun `getSortedHomeVisits sorts by date ascending`() {
        val entities = listOf(
            createAncEntity(formId = "anc_form_001", visitDate = "19-03-2026", createdAt = 3000L),
            createAncEntity(formId = "anc_form_001", visitDate = "17-03-2026", createdAt = 1000L),
            createAncEntity(formId = "anc_form_001", visitDate = "18-03-2026", createdAt = 2000L)
        )
        val result = HomeVisitHelper.getSortedHomeVisits(entities)
        assertEquals(3, result.size)
        // Visit numbers should be assigned sequentially after sorting
        assertEquals(1, result[0].visitNumber)
        assertEquals(2, result[1].visitNumber)
        assertEquals(3, result[2].visitNumber)
    }

    @Test
    fun `getSortedHomeVisits with empty list returns empty`() {
        val result = HomeVisitHelper.getSortedHomeVisits(emptyList())
        assertTrue(result.isEmpty())
    }

    // --- getANCSortedHomeVisits ---

    @Test
    fun `getANCSortedHomeVisits sorts descending by date`() {
        val entities = listOf(
            createAncEntity(visitDate = "17-03-2026", createdAt = 1000L),
            createAncEntity(visitDate = "19-03-2026", createdAt = 3000L),
            createAncEntity(visitDate = "18-03-2026", createdAt = 2000L)
        )
        val result = HomeVisitHelper.getANCSortedHomeVisits(entities)
        assertEquals(3, result.size)
        // First should be latest date (highest createdAt due to visit date ordering)
        assertTrue(HomeVisitHelper.getVisitDate(result[0]) >= HomeVisitHelper.getVisitDate(result[1]))
    }

    // --- Helper ---

    private fun createAncEntity(
        benId: Long = 1L,
        formId: String = "anc_form_001",
        visitDate: String = "17-03-2026",
        createdAt: Long = System.currentTimeMillis()
    ): ANCFormResponseJsonEntity {
        return ANCFormResponseJsonEntity(
            benId = benId,
            visitDay = "Day 1",
            visitDate = visitDate,
            formId = formId,
            version = 1,
            formDataJson = """{"visitNumber": 1}""",
            createdAt = createdAt
        )
    }
}
