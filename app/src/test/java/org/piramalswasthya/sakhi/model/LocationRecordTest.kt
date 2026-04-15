package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LocationRecordTest {

    // =====================================================
    // LocationEntity Tests
    // =====================================================

    @Test fun `LocationEntity creation with required fields`() {
        val entity = LocationEntity(1, "Test")
        assertEquals(1, entity.id)
        assertEquals("Test", entity.name)
        assertNull(entity.nameHindi)
        assertNull(entity.nameAssamese)
    }

    @Test fun `LocationEntity creation with all fields`() {
        val entity = LocationEntity(1, "Test", "टेस्ट", "পরীক্ষা")
        assertEquals("टेस्ट", entity.nameHindi)
        assertEquals("পরীক্ষা", entity.nameAssamese)
    }

    @Test fun `LocationEntity equals for same data`() {
        val a = LocationEntity(1, "Test")
        val b = LocationEntity(1, "Test")
        assertEquals(a, b)
    }

    @Test fun `LocationEntity not equals for different id`() {
        val a = LocationEntity(1, "Test")
        val b = LocationEntity(2, "Test")
        assertNotEquals(a, b)
    }

    @Test fun `LocationEntity copy changes name`() {
        val entity = LocationEntity(1, "Test")
        val copy = entity.copy(name = "Updated")
        assertEquals("Updated", copy.name)
        assertEquals(1, copy.id)
    }

    // =====================================================
    // LocationRecord Tests
    // =====================================================

    @Test fun `LocationRecord creation`() {
        val country = LocationEntity(1, "India")
        val state = LocationEntity(2, "Chhattisgarh")
        val district = LocationEntity(3, "Raipur")
        val block = LocationEntity(4, "Block1")
        val village = LocationEntity(5, "Village1")
        val record = LocationRecord(country, state, district, block, village)
        assertNotNull(record)
        assertEquals("India", record.country.name)
        assertEquals("Chhattisgarh", record.state.name)
        assertEquals("Raipur", record.district.name)
        assertEquals("Block1", record.block.name)
        assertEquals("Village1", record.village.name)
    }

    @Test fun `LocationRecord equals for same data`() {
        val a = LocationRecord(LocationEntity(1, "IN"), LocationEntity(2, "S"), LocationEntity(3, "D"), LocationEntity(4, "B"), LocationEntity(5, "V"))
        val b = LocationRecord(LocationEntity(1, "IN"), LocationEntity(2, "S"), LocationEntity(3, "D"), LocationEntity(4, "B"), LocationEntity(5, "V"))
        assertEquals(a, b)
    }

    @Test fun `LocationRecord copy changes village`() {
        val record = LocationRecord(LocationEntity(1, "IN"), LocationEntity(2, "S"), LocationEntity(3, "D"), LocationEntity(4, "B"), LocationEntity(5, "V"))
        val copy = record.copy(village = LocationEntity(6, "V2"))
        assertEquals("V2", copy.village.name)
        assertEquals("IN", copy.country.name)
    }
}
