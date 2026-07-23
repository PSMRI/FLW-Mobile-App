package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
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
