package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the [NewBornRegNetwork] data class. It carries no mapping logic, so
 * these exercise the generated data-class members (constructor, copy, equals,
 * hashCode, toString and the component accessors) across default and populated
 * instances.
 */
class NewBornRegNetworkTest {

    private fun populated() = NewBornRegNetwork(
        id = 5,
        benficieryid = 100L,
        childName = "Baby",
        birthPlace = "Home",
        birthPlaceid = 1,
        facilityName = "PHC",
        facilityid = 2,
        ashaid = 7,
        facilityOther = "other",
        placeName = "place",
        conductedDelivery = "Doctor",
        conductedDeliveryid = 3,
        conductedDeliveryOther = "co",
        deliveryType = "Normal",
        deliveryTypeid = 1,
        complecations = "None",
        complecationsid = 0,
        complicationsOther = "none",
        term = "Full",
        termid = 1,
        gestationalAge = "39",
        gestationalAgeid = 2,
        corticosteroidGivenMother = "No",
        corticosteroidGivenMotherid = 2,
        criedImmediately = "Yes",
        criedImmediatelyid = 1,
        birthDefects = "No",
        birthDefectsid = 2,
        birthDefectsOthers = "nd",
        heightAtBirth = 50,
        weightAtBirth = 3.2F,
        feedingStarted = "Yes",
        feedingStartedid = 1,
        birthDosage = "Given",
        birthDosageid = 1,
        opvBatchNo = "OPV1",
        opvGivenDueDate = "2026-01-01",
        opvDate = "2026-01-02",
        bcdBatchNo = "BCG1",
        bcgGivenDueDate = "2026-01-03",
        bcgDate = "2026-01-04",
        hptdBatchNo = "HPT1",
        hptGivenDueDate = "2026-01-05",
        hptDate = "2026-01-06",
        vitaminkBatchNo = "VK1",
        vitaminkGivenDueDate = "2026-01-07",
        vitaminkDate = "2026-01-08",
        createdBy = "asha",
        createdDate = "2026-01-01",
        serverUpdatedStatus = 0,
        updatedBy = "asha",
        updatedDate = "2026-01-01",
        deliveryTypeOther = "dto",
        providerServiceMapID = 11,
        vanID = 22,
        processed = "P",
        countyid = 1,
        stateid = 2,
        districtid = 3,
        districtname = "District",
        villageid = 4,
        motherBenId = 200L,
        motherName = "Mother",
        motherposition = 1,
        birthBCG = true,
        birthHepB = false,
        birthOPV = true
    )

    @Test
    fun `constructor with only required booleans applies defaults`() {
        val n = NewBornRegNetwork(birthBCG = false, birthHepB = false, birthOPV = false)

        assertEquals(0, n.id)
        assertEquals(0L, n.benficieryid)
        assertEquals(0F, n.weightAtBirth)
        assertEquals(null, n.childName)
        assertEquals(false, n.birthBCG)
    }

    @Test
    fun `populated constructor exposes all supplied values`() {
        val n = populated()

        assertEquals(5, n.id)
        assertEquals(100L, n.benficieryid)
        assertEquals("Baby", n.childName)
        assertEquals(3.2F, n.weightAtBirth)
        assertEquals("District", n.districtname)
        assertEquals(200L, n.motherBenId)
        assertTrue(n.birthBCG)
        assertTrue(n.birthOPV)
    }

    @Test
    fun `copy overrides selected fields and keeps the rest`() {
        val original = populated()
        val copy = original.copy(childName = "Renamed", birthHepB = true)

        assertEquals("Renamed", copy.childName)
        assertTrue(copy.birthHepB)
        // untouched fields carry over
        assertEquals(original.benficieryid, copy.benficieryid)
        assertEquals(original.facilityName, copy.facilityName)
        assertNotEquals(original, copy)
    }

    @Test
    fun `equals and hashCode agree for identical instances`() {
        val a = populated()
        val b = populated()

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `component accessors return positional values`() {
        val n = populated()

        assertEquals(5, n.component1())
        assertEquals(100L, n.component2())
        assertEquals("Baby", n.component3())
    }

    @Test
    fun `toString contains key field data`() {
        val text = populated().toString()

        assertTrue(text.contains("NewBornRegNetwork"))
        assertTrue(text.contains("childName=Baby"))
    }
}
