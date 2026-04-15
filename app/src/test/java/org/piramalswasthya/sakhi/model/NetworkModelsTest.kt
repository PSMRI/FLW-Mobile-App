package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class NetworkModelsTest {

    // =====================================================
    // LocationEntity Tests
    // =====================================================

    @Test fun `LocationEntity can be created`() {
        val entity = LocationEntity(id = 1, name = "India")
        assertNotNull(entity)
        assertEquals(1, entity.id)
        assertEquals("India", entity.name)
    }

    @Test fun `LocationEntity default nameHindi is null`() {
        val entity = LocationEntity(id = 1, name = "UP")
        assertNull(entity.nameHindi)
    }

    @Test fun `LocationEntity default nameAssamese is null`() {
        val entity = LocationEntity(id = 1, name = "UP")
        assertNull(entity.nameAssamese)
    }

    @Test fun `LocationEntity with all names`() {
        val entity = LocationEntity(id = 1, name = "Lucknow", nameHindi = "लखनऊ", nameAssamese = "লখনৌ")
        assertEquals("लखनऊ", entity.nameHindi)
        assertEquals("লখনৌ", entity.nameAssamese)
    }

    @Test fun `LocationEntity equality`() {
        val a = LocationEntity(id = 1, name = "India")
        val b = LocationEntity(id = 1, name = "India")
        assertEquals(a, b)
    }

    @Test fun `LocationEntity inequality`() {
        val a = LocationEntity(id = 1, name = "Lucknow")
        val b = LocationEntity(id = 2, name = "Kanpur")
        assertNotEquals(a, b)
    }

    @Test fun `LocationEntity copy works`() {
        val entity = LocationEntity(id = 1, name = "VillageA")
        val copy = entity.copy(name = "VillageB")
        assertEquals("VillageB", copy.name)
        assertEquals(1, copy.id)
    }

    @Test fun `LocationRecord can be created with entities`() {
        val entity = LocationEntity(id = 1, name = "Test")
        val record = LocationRecord(country = entity, state = entity, district = entity, block = entity, village = entity)
        assertNotNull(record)
        assertEquals("Test", record.country.name)
    }

    @Test fun `LocationRecord equality`() {
        val entity = LocationEntity(id = 1, name = "Test")
        val a = LocationRecord(country = entity, state = entity, district = entity, block = entity, village = entity)
        val b = LocationRecord(country = entity, state = entity, district = entity, block = entity, village = entity)
        assertEquals(a, b)
    }

    @Test fun `LocationRecord copy works`() {
        val entity = LocationEntity(id = 1, name = "Test")
        val entity2 = LocationEntity(id = 2, name = "Other")
        val record = LocationRecord(country = entity, state = entity, district = entity, block = entity, village = entity)
        val copy = record.copy(village = entity2)
        assertEquals("Other", copy.village.name)
    }

    // =====================================================
    // SyncStatusCache Tests (extended)
    // =====================================================

    @Test fun `SyncStatusCache can be created`() {
        val cache = SyncStatusCache(id = 1, name = "household", syncState = SyncState.UNSYNCED, count = 5)
        assertNotNull(cache)
    }

    @Test fun `SyncStatusCache fields are correct`() {
        val cache = SyncStatusCache(id = 1, name = "ben", syncState = SyncState.SYNCED, count = 20)
        assertEquals("ben", cache.name)
        assertEquals(20, cache.count)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `SyncStatusCache equality`() {
        val a = SyncStatusCache(id = 1, name = "household", syncState = SyncState.UNSYNCED, count = 5)
        val b = SyncStatusCache(id = 1, name = "household", syncState = SyncState.UNSYNCED, count = 5)
        assertEquals(a, b)
    }

    @Test fun `SyncStatusCache inequality`() {
        val a = SyncStatusCache(id = 1, name = "household", syncState = SyncState.UNSYNCED, count = 5)
        val b = SyncStatusCache(id = 2, name = "ben", syncState = SyncState.SYNCED, count = 20)
        assertNotEquals(a, b)
    }

    @Test fun `SyncStatusCache copy works`() {
        val cache = SyncStatusCache(id = 1, name = "household", syncState = SyncState.UNSYNCED, count = 5)
        val copy = cache.copy(count = 0, syncState = SyncState.SYNCED)
        assertEquals(0, copy.count)
        assertEquals(SyncState.SYNCED, copy.syncState)
    }

    @Test fun `SyncStatusCache with zero count`() {
        val cache = SyncStatusCache(id = 1, name = "empty", syncState = SyncState.SYNCED, count = 0)
        assertEquals(0, cache.count)
    }

    // =====================================================
    // FormElement Tests (extended)
    // =====================================================

    @Test fun `FormElement with basic params`() {
        val elem = FormElement(id = 1, inputType = InputType.EDIT_TEXT, title = "Name", required = true)
        assertEquals(1, elem.id)
        assertEquals(InputType.EDIT_TEXT, elem.inputType)
        assertEquals("Name", elem.title)
        assertTrue(elem.required)
    }

    @Test fun `FormElement with required false`() {
        val elem = FormElement(id = 1, inputType = InputType.TEXT_VIEW, title = "Info", required = false)
        assertFalse(elem.required)
    }

    @Test fun `FormElement with DROPDOWN type`() {
        val elem = FormElement(id = 2, inputType = InputType.DROPDOWN, title = "Gender", required = false, entries = arrayOf("M", "F"))
        assertEquals(InputType.DROPDOWN, elem.inputType)
    }

    @Test fun `FormElement with RADIO type`() {
        val elem = FormElement(id = 3, inputType = InputType.RADIO, title = "YesNo", required = false, entries = arrayOf("Yes", "No"))
        assertEquals(InputType.RADIO, elem.inputType)
    }

    @Test fun `FormElement with DATE_PICKER type`() {
        val elem = FormElement(id = 4, inputType = InputType.DATE_PICKER, title = "DOB", required = false)
        assertEquals(InputType.DATE_PICKER, elem.inputType)
    }

    @Test fun `FormElement with HEADLINE type`() {
        val elem = FormElement(id = 5, inputType = InputType.HEADLINE, title = "Section Header", required = false)
        assertEquals(InputType.HEADLINE, elem.inputType)
    }

    @Test fun `FormElement with TEXT_VIEW type`() {
        val elem = FormElement(id = 6, inputType = InputType.TEXT_VIEW, title = "Readonly", required = false)
        assertEquals(InputType.TEXT_VIEW, elem.inputType)
    }

    @Test fun `FormElement with TIME_PICKER type`() {
        val elem = FormElement(id = 7, inputType = InputType.TIME_PICKER, title = "Time", required = false)
        assertEquals(InputType.TIME_PICKER, elem.inputType)
    }

    @Test fun `FormElement with CHECKBOXES type`() {
        val elem = FormElement(id = 8, inputType = InputType.CHECKBOXES, title = "Select", required = false, entries = arrayOf("A", "B", "C"))
        assertEquals(InputType.CHECKBOXES, elem.inputType)
    }

    @Test fun `FormElement with IMAGE_VIEW type`() {
        val elem = FormElement(id = 9, inputType = InputType.IMAGE_VIEW, title = "Photo", required = false)
        assertEquals(InputType.IMAGE_VIEW, elem.inputType)
    }

    @Test fun `FormElement entries default is null`() {
        val elem = FormElement(id = 1, inputType = InputType.EDIT_TEXT, title = "Name", required = false)
        assertNull(elem.entries)
    }

    @Test fun `FormElement value default is null`() {
        val elem = FormElement(id = 1, inputType = InputType.EDIT_TEXT, title = "Name", required = false)
        assertNull(elem.value)
    }

    // =====================================================
    // SaasBahuSammelanCache Extended Tests
    // =====================================================

    @Test fun `SaasBahuSammelanCache with full data`() {
        val cache = SaasBahuSammelanCache(ashaId = 42, place = "Community Hall", participants = 30, date = System.currentTimeMillis())
        assertEquals(42, cache.ashaId)
        assertEquals("Community Hall", cache.place)
        assertEquals(30, cache.participants)
    }

    @Test fun `SaasBahuSammelanCache default id is 0`() {
        val cache = SaasBahuSammelanCache(ashaId = 1)
        assertEquals(0L, cache.id)
    }

    @Test fun `SaasBahuSammelanCache copy changes place`() {
        val cache = SaasBahuSammelanCache(ashaId = 1, place = "Old Place")
        val copy = cache.copy(place = "New Place")
        assertEquals("New Place", copy.place)
    }
}
