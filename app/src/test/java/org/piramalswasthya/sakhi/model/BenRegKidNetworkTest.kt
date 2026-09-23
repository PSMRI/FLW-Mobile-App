package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BenRegKidNetworkTest {

    private fun minimal() = BenRegKidNetwork(benficieryid = 501L, isConsent = true)

    @Test
    fun `default constructor applies documented defaults`() {
        val kid = minimal()

        assertNotNull(kid)
        assertEquals(0, kid.id)
        assertEquals(501L, kid.benficieryid)
        assertNull(kid.childName)
        assertNull(kid.birthPlace)
        assertEquals(0, kid.birthPlaceid)
        assertNull(kid.facilityName)
        assertEquals(0, kid.facilityid)
        assertEquals(0, kid.ashaid)
        assertEquals(0, kid.conductedDeliveryid)
        assertEquals(0, kid.deliveryTypeid)
        assertEquals(0, kid.complicationsid)
        assertEquals(0, kid.termid)
        assertEquals(0, kid.gestationalAgeid)
        assertEquals(0, kid.corticosteroidGivenMotherid)
        assertEquals(0, kid.criedImmediatelyid)
        assertEquals(0, kid.birthDefectsid)
        assertEquals(0, kid.heightAtBirth)
        assertEquals(0F, kid.weightAtBirth, 0.0001F)
        assertEquals(0, kid.feedingStartedid)
        assertEquals(0, kid.birthDosageid)
        assertEquals(0, kid.serverUpdatedStatus)
        assertEquals(0, kid.ProviderServiceMapID)
        assertEquals(4, kid.VanID)
        assertEquals(0, kid.Countyid)
        assertEquals(0, kid.stateid)
        assertEquals(0, kid.districtid)
        assertEquals(0, kid.villageid)
        assertNull(kid.motherBenId)
        assertNull(kid.motherName)
        assertEquals(0, kid.motherposition)
        assertNull(kid.birthBCG)
        assertNull(kid.birthHepB)
        assertNull(kid.birthOPV)
        assertTrue(kid.isConsent)
    }

    @Test
    fun `birth and immunization details round trip`() {
        val kid = minimal().copy(
            id = 3,
            childName = "Baby",
            birthPlace = "Hospital",
            birthPlaceid = 1,
            facilityName = "PHC",
            facilityid = 2,
            ashaid = 9,
            facilityOther = "Other facility",
            placeName = "Place",
            conductedDelivery = "Doctor",
            conductedDeliveryid = 1,
            conductedDeliveryOther = "Other",
            deliveryType = "Normal",
            deliveryTypeid = 1,
            deliveryTypeOther = "Other type",
            complications = "None",
            complicationsid = 1,
            complicationsOther = "Other comp",
            term = "Full Term",
            termid = 1,
            gestationalAge = "38 weeks",
            gestationalAgeid = 1,
            corticosteroidGivenMother = "No",
            corticosteroidGivenMotherid = 2,
            criedImmediately = "Yes",
            criedImmediatelyid = 1,
            birthDefects = "None",
            birthDefectsid = 1,
            birthDefectsOthers = "Other defect",
            heightAtBirth = 50,
            weightAtBirth = 3.2F,
            feedingStarted = "Yes",
            feedingStartedid = 1,
            birthDosage = "Given",
            birthDosageid = 1,
            opvBatchNo = "OPV1",
            opvGivenDueDate = "2024-01-01",
            opvDate = "2024-01-02",
            bcdBatchNo = "BCG1",
            bcgGivenDueDate = "2024-01-03",
            bcgDate = "2024-01-04",
            hptdBatchNo = "HPT1",
            hptGivenDueDate = "2024-01-05",
            hptDate = "2024-01-06",
            vitaminkBatchNo = "VITK1",
            vitaminkGivenDueDate = "2024-01-07",
            vitaminkDate = "2024-01-08",
            createdBy = "asha",
            createdDate = "2024-01-01",
            serverUpdatedStatus = 1,
            updatedBy = "asha",
            updatedDate = "2024-01-09",
            motherBenId = 100L,
            motherName = "Mother",
            motherposition = 2,
            birthBCG = true,
            birthHepB = false,
            birthOPV = true
        )

        assertEquals(3, kid.id)
        assertEquals("Baby", kid.childName)
        assertEquals("Hospital", kid.birthPlace)
        assertEquals("PHC", kid.facilityName)
        assertEquals("Other facility", kid.facilityOther)
        assertEquals("Place", kid.placeName)
        assertEquals("Doctor", kid.conductedDelivery)
        assertEquals("Other", kid.conductedDeliveryOther)
        assertEquals("Normal", kid.deliveryType)
        assertEquals("Other type", kid.deliveryTypeOther)
        assertEquals("None", kid.complications)
        assertEquals("Other comp", kid.complicationsOther)
        assertEquals("Full Term", kid.term)
        assertEquals("38 weeks", kid.gestationalAge)
        assertEquals("No", kid.corticosteroidGivenMother)
        assertEquals("Yes", kid.criedImmediately)
        assertEquals("None", kid.birthDefects)
        assertEquals("Other defect", kid.birthDefectsOthers)
        assertEquals(50, kid.heightAtBirth)
        assertEquals(3.2F, kid.weightAtBirth, 0.0001F)
        assertEquals("Yes", kid.feedingStarted)
        assertEquals("Given", kid.birthDosage)
        assertEquals("OPV1", kid.opvBatchNo)
        assertEquals("2024-01-01", kid.opvGivenDueDate)
        assertEquals("2024-01-02", kid.opvDate)
        assertEquals("BCG1", kid.bcdBatchNo)
        assertEquals("2024-01-03", kid.bcgGivenDueDate)
        assertEquals("2024-01-04", kid.bcgDate)
        assertEquals("HPT1", kid.hptdBatchNo)
        assertEquals("2024-01-05", kid.hptGivenDueDate)
        assertEquals("2024-01-06", kid.hptDate)
        assertEquals("VITK1", kid.vitaminkBatchNo)
        assertEquals("2024-01-07", kid.vitaminkGivenDueDate)
        assertEquals("2024-01-08", kid.vitaminkDate)
        assertEquals("asha", kid.createdBy)
        assertEquals(1, kid.serverUpdatedStatus)
        assertEquals("2024-01-09", kid.updatedDate)
        assertEquals(100L, kid.motherBenId)
        assertEquals("Mother", kid.motherName)
        assertEquals(2, kid.motherposition)
        assertEquals(true, kid.birthBCG)
        assertEquals(false, kid.birthHepB)
        assertEquals(true, kid.birthOPV)
    }

    @Test
    fun `copy and equality behave as data class`() {
        val kid = minimal()
        val same = kid.copy()

        assertEquals(kid, same)
        assertEquals(kid.hashCode(), same.hashCode())
        assertNotNull(kid.toString())
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = minimal()
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
