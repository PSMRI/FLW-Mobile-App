package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HouseholdMappingTest {

    private fun location() = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(2, "Chhattisgarh"),
        district = LocationEntity(3, "Raipur"),
        block = LocationEntity(4, "Block1"),
        village = LocationEntity(5, "Village1")
    )

    private fun user() = User(
        userId = 100,
        name = "ASHA",
        userName = "asha1",
        password = "pw",
        role = "ASHA",
        serviceMapId = 55,
        state = LocationEntity(2, "Chhattisgarh"),
        district = LocationEntity(3, "Raipur"),
        block = LocationEntity(4, "Block1"),
        villages = listOf(LocationEntity(5, "Village1"))
    )

    private fun household(
        family: HouseholdFamily? = null,
        details: HouseholdDetails? = null,
        amenities: HouseholdAmenities? = null
    ) = HouseholdCache(
        householdId = 9001L,
        ashaId = 7,
        family = family,
        details = details,
        amenities = amenities,
        locationRecord = location(),
        processed = "N",
        isDraft = false
    )

    // =====================================================
    // HouseholdCache.asNetworkModel()
    // =====================================================

    @Test fun `asNetworkModel maps householdId to string`() {
        assertEquals("9001", household().asNetworkModel(user()).householdId)
    }

    @Test fun `asNetworkModel maps populated details, family and amenities`() {
        val details = HouseholdDetails(
            residentialArea = "Urban",
            residentialAreaId = 1,
            otherResidentialArea = "Other",
            houseType = "Pucca",
            houseTypeId = 2,
            otherHouseType = "OtherHouse",
            isHouseOwned = "Yes",
            isHouseOwnedId = 3,
            isLandOwned = true,
            isLandIrrigated = false,
            isLivestockOwned = true,
            street = "Main St",
            colony = "Colony1",
            pincode = 492001
        )
        val family = HouseholdFamily(
            familyHeadName = "Head",
            familyName = "Family1",
            familyHeadPhoneNo = 9999999999L,
            houseNo = "12",
            wardNo = "W1",
            wardName = "Ward1",
            mohallaName = "Mohalla1",
            rationCardDetails = "RC1",
            povertyLine = "BPL",
            povertyLineId = 1
        )
        val amenities = HouseholdAmenities(
            separateKitchen = "Yes",
            separateKitchenId = 1,
            fuelUsed = "Wood",
            fuelUsedId = 2,
            otherFuelUsed = "OtherFuel",
            sourceOfDrinkingWater = "Well",
            sourceOfDrinkingWaterId = 3,
            otherSourceOfDrinkingWater = "OtherSource",
            availabilityOfElectricity = "Yes",
            availabilityOfElectricityId = 1,
            otherAvailabilityOfElectricity = "OtherElec",
            availabilityOfToilet = "Yes",
            availabilityOfToiletId = 1,
            otherAvailabilityOfToilet = "OtherToilet",
            motorizedVehicle = "Bike",
            motorizedVehicleId = 1,
            otherMotorizedVehicle = "OtherVehicle"
        )
        val network = household(family = family, details = details, amenities = amenities).asNetworkModel(user())

        assertEquals("Urban", network.residentialArea)
        assertEquals("Pucca", network.houseType)
        assertEquals("Yes", network.isHouseOwned)
        assertEquals("Wood", network.fuelUsed)
        assertEquals("Head", network.familyHeadName)
        assertEquals("Family1", network.familyName)
        assertEquals("RC1", network.rationCardDetails)
        assertEquals("Well", network.sourceOfDrinkingWater)
        assertEquals("Yes", network.availabilityOfToilet)
    }

    @Test fun `HouseholdDetails copy toString and equality`() {
        val a = HouseholdDetails(
            residentialArea = "Urban", residentialAreaId = 1, otherResidentialArea = "other1",
            houseType = "Pucca", houseTypeId = 2, otherHouseType = "other2",
            isHouseOwned = "Yes", isHouseOwnedId = 3, isLandOwned = true, isLandIrrigated = true,
            isLivestockOwned = true, street = "MainSt", colony = "ColonyA", pincode = 123456
        )
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assert(a.toString().contains("HouseholdDetails"))

        assertNotEquals(a, a.copy(residentialArea = "Other"))
        assertNotEquals(a, a.copy(residentialAreaId = 999))
        assertNotEquals(a, a.copy(otherResidentialArea = "Other"))
        assertNotEquals(a, a.copy(houseType = "Other"))
        assertNotEquals(a, a.copy(houseTypeId = 999))
        assertNotEquals(a, a.copy(otherHouseType = "Other"))
        assertNotEquals(a, a.copy(isHouseOwned = "No"))
        assertNotEquals(a, a.copy(isHouseOwnedId = 999))
        assertNotEquals(a, a.copy(isLandOwned = false))
        assertNotEquals(a, a.copy(isLandIrrigated = false))
        assertNotEquals(a, a.copy(isLivestockOwned = false))
        assertNotEquals(a, a.copy(street = "Other"))
        assertNotEquals(a, a.copy(colony = "Other"))
        assertNotEquals(a, a.copy(pincode = 999))
    }

    @Test fun `asNetworkModel maps ashaId`() {
        assertEquals(7, household().asNetworkModel(user()).ashaId)
    }

    @Test fun `asNetworkModel maps provider service map id from user`() {
        assertEquals(55, household().asNetworkModel(user()).providerServiceMapID)
    }

    @Test fun `asNetworkModel maps location ids`() {
        val net = household().asNetworkModel(user())
        assertEquals(1, net.Countyid)
        assertEquals(2, net.stateid)
        assertEquals(3, net.districtid)
        assertEquals(4, net.blockid)
        assertEquals(5, net.villageid)
    }

    @Test fun `asNetworkModel maps location names`() {
        val net = household().asNetworkModel(user())
        assertEquals("Chhattisgarh", net.state)
        assertEquals("Raipur", net.districtname)
        assertEquals("Village1", net.village)
    }

    @Test fun `asNetworkModel maps processed`() {
        assertEquals("N", household().asNetworkModel(user()).Processed)
    }

    @Test fun `asNetworkModel null family yields default houseNo`() {
        assertEquals("null", household().asNetworkModel(user()).houseNo)
    }

    @Test fun `asNetworkModel maps populated family`() {
        val fam = HouseholdFamily(
            familyHeadName = "Ram",
            familyName = "Kumar",
            familyHeadPhoneNo = 9999999999L,
            houseNo = "12A",
            povertyLineId = 2
        )
        val net = household(family = fam).asNetworkModel(user())
        assertEquals("Ram", net.familyHeadName)
        assertEquals("Kumar", net.familyName)
        assertEquals("9999999999", net.familyHeadPhoneNo)
        assertEquals("12A", net.houseNo)
        assertEquals(2, net.povertyLineId)
    }

    @Test fun `asNetworkModel maps amenities`() {
        val amn = HouseholdAmenities(
            availabilityOfToilet = "Yes",
            availabilityOfToiletId = 1,
            fuelUsed = "LPG",
            fuelUsedId = 3
        )
        val net = household(amenities = amn).asNetworkModel(user())
        assertEquals("Yes", net.availabilityOfToilet)
        assertEquals(1, net.availabilityofToiletId)
        assertEquals("LPG", net.fuelUsed)
        assertEquals(3, net.fuelUsedId)
    }

    @Test fun `asNetworkModel maps deactivate flag`() {
        val net = household().copy(isDeactivate = true).asNetworkModel(user())
        assertEquals(true, net.isDeactivate)
    }

    // =====================================================
    // HouseholdBasicCache.asBasicDomainModel()
    // =====================================================

    @Test fun `asBasicDomainModel maps hhId and numMembers`() {
        val basic = HouseholdBasicCache(household = household(), numMembers = 4)
        val domain = basic.asBasicDomainModel()
        assertEquals(9001L, domain.hhId)
        assertEquals(4, domain.numMembers)
        assertEquals(9001L, basic.household.householdId)
        assertEquals(4, basic.numMembers)
    }

    @Test fun `asBasicDomainModel null family gives Not Available`() {
        val domain = HouseholdBasicCache(household = household(), numMembers = 0).asBasicDomainModel()
        assertEquals("Not Available", domain.headName)
        assertEquals("Not Available", domain.contactNumber)
        assertEquals("Not Available", domain.headSurname)
    }

    @Test fun `asBasicDomainModel populated family maps head details`() {
        val fam = HouseholdFamily(
            familyHeadName = "Ram",
            familyName = "Kumar",
            familyHeadPhoneNo = 9999999999L
        )
        val domain = HouseholdBasicCache(household = household(family = fam), numMembers = 3).asBasicDomainModel()
        assertEquals("Ram", domain.headName)
        assertEquals("Kumar", domain.headSurname)
        assertEquals("9999999999", domain.contactNumber)
        assertEquals("Ram Kumar", domain.headFullName)
    }

    @Test fun `asBasicDomainModel carries deactivate flag`() {
        val hh = household().copy(isDeactivate = true)
        val domain = HouseholdBasicCache(household = hh, numMembers = 1).asBasicDomainModel()
        assertEquals(true, domain.isDeactivate)
    }
}
