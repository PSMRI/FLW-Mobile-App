package org.piramalswasthya.sakhi.database.converters

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.LocationEntity

class LocationEntityListConverterTest {

    private val gson = Gson()

    private fun entities() = listOf(
        LocationEntity(1, "Assam", nameHindi = "असम"),
        LocationEntity(2, "Kamrup")
    )

    @Test
    fun `fromLocationEntityList serialises the list to json`() {
        val list = entities()

        assertEquals(gson.toJson(list), LocationEntityListConverter.fromLocationEntityList(list))
    }

    @Test
    fun `fromLocationEntityList of an empty list returns an empty json array`() {
        assertEquals("[]", LocationEntityListConverter.fromLocationEntityList(emptyList()))
    }

    @Test
    fun `toLocationEntityList parses a json array`() {
        val list = entities()

        val parsed = LocationEntityListConverter.toLocationEntityList(gson.toJson(list))

        assertEquals(list, parsed)
    }

    @Test
    fun `toLocationEntityList parses an empty json array`() {
        assertTrue(LocationEntityListConverter.toLocationEntityList("[]").isEmpty())
    }

    @Test
    fun `location entity list round trips through the converter`() {
        val list = entities()

        val json = LocationEntityListConverter.fromLocationEntityList(list)

        assertEquals(list, LocationEntityListConverter.toLocationEntityList(json))
    }

    @Test
    fun `toLocationEntityList keeps optional localised names`() {
        val parsed = LocationEntityListConverter.toLocationEntityList(
            """[{"id":7,"name":"Village","nameHindi":"गाँव","nameAssamese":"গাঁও"}]"""
        )

        assertEquals(1, parsed.size)
        assertEquals(7, parsed[0].id)
        assertEquals("Village", parsed[0].name)
        assertEquals("गाँव", parsed[0].nameHindi)
        assertEquals("গাঁও", parsed[0].nameAssamese)
    }

    @Test
    fun `fromIntList serialises ints to json`() {
        assertEquals("[1,2,3]", LocationEntityListConverter.fromIntList(listOf(1, 2, 3)))
    }

    @Test
    fun `fromIntList of an empty list returns an empty json array`() {
        assertEquals("[]", LocationEntityListConverter.fromIntList(emptyList()))
    }

    @Test
    fun `toIntList parses a json array of ints`() {
        assertEquals(listOf(1, 2, 3), LocationEntityListConverter.toIntList("[1,2,3]"))
    }

    @Test
    fun `toIntList parses an empty json array`() {
        assertTrue(LocationEntityListConverter.toIntList("[]").isEmpty())
    }

    @Test
    fun `int list round trips through the converter`() {
        val list = listOf(10, 20, 30)

        assertEquals(list, LocationEntityListConverter.toIntList(
            LocationEntityListConverter.fromIntList(list)
        ))
    }
}
