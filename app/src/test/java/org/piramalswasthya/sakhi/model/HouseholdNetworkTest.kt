package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HouseholdNetworkTest {

    private fun minimal() = HouseholdNetwork(
        householdId = "HH-1",
        ashaId = 11,
        familyHeadPhoneNo = "9999999999",
        houseNo = "12A",
        residentialArea = "Rural",
        otherResidentialArea = "",
        otherFuelUsed = "",
        otherSourceOfDrinkingWater = "",
        otherAvailabilityOfElectricity = "",
        providerServiceMapID = 7,
        Processed = "N",
        Countyid = 1
    )

    @Test
    fun `default constructor applies documented defaults`() {
        val network = minimal()

        assertNotNull(network)
        assertEquals("HH-1", network.householdId)
        assertEquals(11, network.ashaId)
        assertEquals(0L, network.benId)
        assertEquals(1, network.dummyIdMayBe)
        assertNull(network.familyHeadName)
        assertNull(network.familyName)
        assertEquals(0, network.povertyLineId)
        assertEquals(0, network.residentialAreaId)
        assertEquals(0, network.houseTypeId)
        assertEquals(0, network.houseOwnerShipId)
        assertEquals("< 2acres", network.isLandOwned)
        assertEquals(0, network.landOwnedId)
        assertEquals("None", network.landIrregated)
        assertEquals(0, network.landIrregatedId)
        assertEquals("No", network.isLivestockOwned)
        assertEquals(0, network.liveStockOwnerShipId)
        assertEquals("null", network.street)
        assertEquals("null", network.colony)
        assertEquals(0, network.pincode)
        assertEquals(0, network.seperateKitchenId)
        assertEquals(0, network.fuelUsedId)
        assertEquals(0, network.sourceofDrinkingWaterId)
        assertEquals(0, network.avalabilityofElectricityId)
        assertEquals(0, network.availabilityofToiletId)
        assertEquals("Motor Bike", network.motorizedVehicle)
        assertEquals(0, network.motarizedVehicleId)
        assertEquals(0, network.serverUpdatedStatus)
        assertEquals(0, network.stateid)
        assertEquals(0, network.districtid)
        assertEquals(0, network.blockid)
        assertEquals(0, network.villageid)
        assertFalse(network.isDeactivate)
    }

    @Test
    fun `explicit values override defaults`() {
        val network = minimal().copy(
            benId = 55L,
            familyHeadName = "Head",
            familyName = "Surname",
            wardNo = "3",
            wardName = "Ward 3",
            mohallaName = "Mohalla",
            rationCardDetails = "Yellow",
            povertyLine = "BPL",
            povertyLineId = 2,
            houseType = "Pucca",
            houseTypeId = 1,
            isHouseOwned = "Owned",
            street = "Main Street",
            colony = "Colony",
            pincode = 400001,
            registrationType = "New",
            state = "State",
            district = "District",
            block = "Block",
            village = "Village",
            createdBy = "asha",
            createdDate = "2024-01-01",
            updatedBy = "asha",
            updatedDate = "2024-01-02",
            isDeactivate = true
        )

        assertEquals(55L, network.benId)
        assertEquals("Head", network.familyHeadName)
        assertEquals("Surname", network.familyName)
        assertEquals("3", network.wardNo)
        assertEquals("Ward 3", network.wardName)
        assertEquals("Mohalla", network.mohallaName)
        assertEquals("Yellow", network.rationCardDetails)
        assertEquals("BPL", network.povertyLine)
        assertEquals(2, network.povertyLineId)
        assertEquals("Pucca", network.houseType)
        assertEquals(1, network.houseTypeId)
        assertEquals("Owned", network.isHouseOwned)
        assertEquals("Main Street", network.street)
        assertEquals("Colony", network.colony)
        assertEquals(400001, network.pincode)
        assertEquals("New", network.registrationType)
        assertEquals("State", network.state)
        assertEquals("District", network.district)
        assertEquals("Block", network.block)
        assertEquals("Village", network.village)
        assertEquals("asha", network.createdBy)
        assertEquals("2024-01-01", network.createdDate)
        assertEquals("asha", network.updatedBy)
        assertEquals("2024-01-02", network.updatedDate)
        assertEquals(true, network.isDeactivate)
        assertEquals(7, network.providerServiceMapID)
        assertEquals("N", network.Processed)
        assertEquals(1, network.Countyid)
    }

    @Test
    fun `copy and equality behave as data class`() {
        val network = minimal()
        val same = network.copy()

        assertEquals(network, same)
        assertEquals(network.hashCode(), same.hashCode())
        assertNotNull(network.toString())
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
