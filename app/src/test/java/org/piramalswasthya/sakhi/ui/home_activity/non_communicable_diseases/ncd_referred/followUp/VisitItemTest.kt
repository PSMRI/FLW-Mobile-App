package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitItemTest {

    @Test
    fun constructor_setsFields() {
        val item = VisitItem("Visit 1", listOf("Follow up A", "Follow up B"))

        assertEquals("Visit 1", item.visitHeader)
        assertEquals(listOf("Follow up A", "Follow up B"), item.followUps)
    }

    @Test
    fun equals_sameValues_returnsTrue() {
        val item1 = VisitItem("Visit 1", listOf("A"))
        val item2 = VisitItem("Visit 1", listOf("A"))

        assertTrue(item1 == item2)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun equals_differentHeader_returnsFalse() {
        val item1 = VisitItem("Visit 1", listOf("A"))
        val item2 = VisitItem("Visit 2", listOf("A"))

        assertFalse(item1 == item2)
    }

    @Test
    fun equals_differentFollowUps_returnsFalse() {
        val item1 = VisitItem("Visit 1", listOf("A"))
        val item2 = VisitItem("Visit 1", listOf("B"))

        assertNotEquals(item1, item2)
    }

    @Test
    fun copy_withNewHeader_updatesOnlyHeader() {
        val original = VisitItem("Visit 1", listOf("A"))
        val copy = original.copy(visitHeader = "Visit 2")

        assertEquals("Visit 2", copy.visitHeader)
        assertEquals(original.followUps, copy.followUps)
    }

    @Test
    fun toString_containsFieldValues() {
        val item = VisitItem("Visit 1", listOf("A"))

        val result = item.toString()

        assertTrue(result.contains("Visit 1"))
        assertTrue(result.contains("A"))
    }

    @Test
    fun emptyFollowUps_isAllowed() {
        val item = VisitItem("Visit 1", emptyList())

        assertTrue(item.followUps.isEmpty())
    }
}
