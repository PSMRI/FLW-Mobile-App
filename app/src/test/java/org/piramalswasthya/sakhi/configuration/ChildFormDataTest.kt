package org.piramalswasthya.sakhi.configuration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ChildFormDataTest {

    @Test
    fun `constructor assigns all properties`() {
        val data = ChildFormData(firstName = "Riya", dob = "01-01-2026", age = 0, gender = "Female")
        assertEquals("Riya", data.firstName)
        assertEquals("01-01-2026", data.dob)
        assertEquals(0, data.age)
        assertEquals("Female", data.gender)
    }

    @Test
    fun `equals and hashCode are based on all properties`() {
        val a = ChildFormData("Riya", "01-01-2026", 0, "Female")
        val b = ChildFormData("Riya", "01-01-2026", 0, "Female")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `instances differing by a single field are not equal`() {
        val a = ChildFormData("Riya", "01-01-2026", 0, "Female")
        val b = ChildFormData("Riya", "01-01-2026", 1, "Female")
        assertNotEquals(a, b)
    }

    @Test
    fun `copy overrides only the requested field`() {
        val a = ChildFormData("Riya", "01-01-2026", 0, "Female")
        val copy = a.copy(gender = "Male")
        assertEquals("Riya", copy.firstName)
        assertEquals("01-01-2026", copy.dob)
        assertEquals(0, copy.age)
        assertEquals("Male", copy.gender)
    }
}
