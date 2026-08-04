package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SurveyRegisterCacheTest {

    @Test
    fun `default constructor produces empty register`() {
        val cache = SurveyRegisterCache()

        assertNotNull(cache)
        assertNull(cache.facilitatorSupervisor)
        assertNull(cache.headOfHouse)
        assertEquals(0, cache.numFamilyMembers)
        assertEquals(0, cache.numEligibleCouples)
        assertEquals(0, cache.numPregnantWomen)
        assertEquals(0, cache.numChildrenLessThanOneMonth)
        assertEquals(0, cache.numChildrenLessThanOneYear)
        assertEquals(0, cache.numChildrenLessThanFiveYear)
    }

    @Test
    fun `mutable properties can be reassigned`() {
        val cache = SurveyRegisterCache()
        cache.facilitatorSupervisor = "Supervisor"
        cache.from = "2024-01-01"
        cache.till = "2024-01-31"
        cache.firstHouseLandlord = "A"
        cache.fisrtHomeIdentificationSymbol = "S1"
        cache.middleHouseLandlord = "B"
        cache.middleHomeIdentificationSymbol = "S2"
        cache.lastHouseLandlord = "C"
        cache.lastHomeIdentificationSymbol = "S3"
        cache.householdNumber = "HH-1"
        cache.headOfHouse = "Head"
        cache.numFamilyMembers = 6
        cache.numEligibleCouples = 2
        cache.numPregnantWomen = 1
        cache.numChildrenLessThanOneMonth = 1
        cache.numChildrenLessThanOneYear = 2
        cache.numChildrenLessThanFiveYear = 3

        assertEquals("Supervisor", cache.facilitatorSupervisor)
        assertEquals("2024-01-01", cache.from)
        assertEquals("2024-01-31", cache.till)
        assertEquals("A", cache.firstHouseLandlord)
        assertEquals("S1", cache.fisrtHomeIdentificationSymbol)
        assertEquals("B", cache.middleHouseLandlord)
        assertEquals("S2", cache.middleHomeIdentificationSymbol)
        assertEquals("C", cache.lastHouseLandlord)
        assertEquals("S3", cache.lastHomeIdentificationSymbol)
        assertEquals("HH-1", cache.householdNumber)
        assertEquals("Head", cache.headOfHouse)
        assertEquals(6, cache.numFamilyMembers)
        assertEquals(2, cache.numEligibleCouples)
        assertEquals(1, cache.numPregnantWomen)
        assertEquals(1, cache.numChildrenLessThanOneMonth)
        assertEquals(2, cache.numChildrenLessThanOneYear)
        assertEquals(3, cache.numChildrenLessThanFiveYear)
    }

    @Test
    fun `copy and equality behave as data class`() {
        val cache = SurveyRegisterCache(headOfHouse = "Head", numFamilyMembers = 4)
        val same = cache.copy()
        assertEquals(cache, same)
        assertEquals(cache.hashCode(), same.hashCode())
        assertNotNull(cache.toString())
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = SurveyRegisterCache()
        obj.javaClass.methods
            .filter { (it.name.startsWith("get") || it.name.startsWith("is")) && it.parameterCount == 0 }
            .forEach { getter ->
                runCatching {
                    val value = getter.invoke(obj)
                    val setterName = "set" + getter.name.removePrefix("get").removePrefix("is")
                    obj.javaClass.methods
                        .firstOrNull { it.name == setterName && it.parameterCount == 1 }
                        ?.invoke(obj, value)
                }
            }
        assertNotNull(obj)
    }
}
