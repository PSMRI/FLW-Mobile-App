package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BenPostAccessorTest {

    private fun demographics() = BenDemographics(
        addressLine1 = "A1",
        addressLine2 = "A2",
        addressLine3 = "A3",
        blockID = 4,
        communityID = "C1",
        communityName = "Community",
        countryID = 1,
        countryName = "India",
        districtBranchID = 11,
        districtBranchName = "Branch",
        districtID = 3,
        religionID = "R1",
        religionName = "Religion",
        stateID = 2,
        stateName = "State"
    )

    private fun benPost() = BenPost(
        benRegId = 500L,
        isDeath = false,
        isDeathValue = "N",
        dateOfDeath = "",
        timeOfDeath = "",
        reasonOfDeath = "",
        reasonOfDeathId = 0,
        placeOfDeath = "",
        placeOfDeathId = 0,
        otherPlaceOfDeath = "",
        aadhaNo = "1234",
        aadha_no = "1234",
        aadha_noId = 7,
        age = 25,
        age_unit = "Years",
        age_unitId = 3,
        ashaId = 9,
        benId = 501L,
        createdBy = "creator",
        createdDate = "01-01-2024",
        currSubDistrictId = 12,
        familyHeadRelation = "Self",
        familyHeadRelationPosition = 1,
        fatherName = "Father",
        firstName = "First",
        guidelineId = "G1",
        householdId = "H1",
        isHrpStatus = true,
        lastName = "Last",
        latitude = 12.5,
        longitude = 77.5,
        marriageDate = "01-01-2020",
        motherName = "Mother",
        needOpCare = "No",
        rchId = "RCH1",
        registrationDate = "02-01-2024",
        registrationType = "New",
        religionOthers = "Other",
        reproductiveStatus = "Eligible",
        reproductiveStatusId = 2,
        serverUpdatedStatus = 1,
        spouseName = "Spouse",
        typeOfSchoolId = 0,
        userImage = "img",
        villageId = 11,
        benPhoneMaps = arrayOf(BenPhoneMaps(createdBy = "creator", phoneNo = "9999999999")),
        dob = "01-01-1999",
        gender = "Female",
        benDemographics = demographics()
    )

    @Test
    fun `BenPost identity fields are readable`() {
        val post = benPost()
        assertEquals(500L, post.benRegId)
        assertEquals(501L, post.benId)
        assertEquals(9, post.ashaId)
        assertEquals("H1", post.householdId)
        assertEquals("G1", post.guidelineId)
        assertEquals("RCH1", post.rchId)
        assertEquals("creator", post.createdBy)
        assertEquals("01-01-2024", post.createdDate)
        assertEquals("02-01-2024", post.registrationDate)
        assertEquals("New", post.registrationType)
        assertEquals(1, post.serverUpdatedStatus)
        assertEquals("img", post.userImage)
        assertEquals(11, post.villageId)
        assertEquals(12, post.currSubDistrictId)
    }

    @Test
    fun `BenPost name and demographic fields are readable`() {
        val post = benPost()
        assertEquals("First", post.firstName)
        assertEquals("Last", post.lastName)
        assertEquals("Father", post.fatherName)
        assertEquals("Mother", post.motherName)
        assertEquals("Spouse", post.spouseName)
        assertEquals("Self", post.familyHeadRelation)
        assertEquals(1, post.familyHeadRelationPosition)
        assertEquals("01-01-1999", post.dob)
        assertEquals("Female", post.gender)
        assertEquals(25, post.age)
        assertEquals("Years", post.age_unit)
        assertEquals(3, post.age_unitId)
        assertEquals("1234", post.aadhaNo)
        assertEquals("1234", post.aadha_no)
        assertEquals(7, post.aadha_noId)
        assertEquals(12.5, post.latitude, 0.0001)
        assertEquals(77.5, post.longitude, 0.0001)
        assertEquals("01-01-2020", post.marriageDate)
        assertEquals("Other", post.religionOthers)
        assertEquals("Eligible", post.reproductiveStatus)
        assertEquals(2, post.reproductiveStatusId)
        assertEquals("No", post.needOpCare)
        assertEquals(0, post.typeOfSchoolId)
        assertEquals("Community", post.benDemographics.communityName)
        assertEquals("9999999999", post.benPhoneMaps[0].phoneNo)
    }

    @Test
    fun `BenPost death fields are readable`() {
        val post = benPost()
        assertFalse(post.isDeath)
        assertEquals("N", post.isDeathValue)
        assertEquals("", post.dateOfDeath)
        assertEquals("", post.timeOfDeath)
        assertEquals("", post.reasonOfDeath)
        assertEquals(0, post.reasonOfDeathId)
        assertEquals("", post.placeOfDeath)
        assertEquals(0, post.placeOfDeathId)
        assertEquals("", post.otherPlaceOfDeath)
    }

    @Test
    fun `BenPost omitted parameters fall back to declared defaults`() {
        val post = benPost()
        assertEquals(0, post.countyid)
        assertNull(post.processed)
        assertEquals(0, post.providerServiceMapID)
        assertEquals(4, post.vanID)
        assertEquals(0, post.ageAtMarriage)
        assertEquals(0, post.childRegisteredAWCID)
        assertEquals(0, post.childRegisteredSchoolID)
        assertNull(post.expectedDateOfDelivery)
        assertEquals("", post.facilitySelection)
        assertEquals(1, post.dummyIdMayBe)
        assertFalse(post.isImmunizationStatus)
        assertNull(post.lastDeliveryConducted)
        assertEquals(0, post.lastDeliveryConductedID)
        assertNull(post.lastMenstrualPeriod)
        assertNull(post.mobileOthers)
        assertNull(post.mobileNoOfRelation)
        assertEquals(0, post.mobileNoOfRelationId)
        assertEquals("Select", post.nayiPahalDeliveryStatus)
        assertEquals(0, post.nayiPahalDeliveryStatusPosition)
        assertEquals(0, post.ncdPriority)
        assertEquals(0, post.needOpCareId)
        assertTrue(post.isHrpStatus)
    }

    @Test
    fun `BenPost nishchay and menstrual defaults are applied`() {
        val post = benPost()
        assertEquals("", post.nishchayPregnancyStatus)
        assertEquals(0, post.nishchayPregnancyStatusPosition)
        assertEquals("", post.nishchayDeliveryStatus)
        assertEquals(0, post.nishchayDeliveryStatusPosition)
        assertNull(post.noOfDaysForDelivery)
        assertEquals("", post.previousLiveBirth)
        assertNull(post.villageName)
        assertEquals(0, post.whoConductedDeliveryID)
        assertNull(post.whoConductedDelivery)
        assertNull(post.literacy)
        assertEquals(0, post.literacyId)
        assertEquals(0, post.menstrualStatusId)
        assertEquals(0, post.regularityofMenstrualCycleId)
        assertEquals(0, post.lengthofMenstrualCycleId)
        assertEquals(0, post.menstrualBFDId)
        assertEquals(0, post.menstrualProblemId)
        assertNull(post.menstrualStatus)
    }

    @Test
    fun `BenPost suspected and confirmed defaults are null`() {
        val post = benPost()
        assertNull(post.formStatus)
        assertNull(post.formType)
        assertNull(post.childRegisteredSchool)
        assertNull(post.typeofSchool)
        assertNull(post.dateMarriage)
        assertNull(post.deliveryDate)
        assertNull(post.suspected_hrp)
        assertNull(post.suspected_ncd)
        assertNull(post.suspected_tb)
        assertNull(post.suspected_ncd_diseases)
        assertNull(post.confirmed_ncd)
        assertNull(post.confirmed_hrp)
        assertNull(post.confirmed_tb)
        assertNull(post.confirmed_ncd_diseases)
        assertNull(post.diagnosis_status)
    }

    @Test
    fun `BenPost family status defaults are applied`() {
        val post = benPost()
        assertFalse(post.isEmergencyRegistration)
        assertEquals(0, post.genderId)
        assertNull(post.maritalStatusID)
        assertNull(post.maritalStatusName)
        assertFalse(post.isSpouseAdded)
        assertFalse(post.isChildrenAdded)
        assertFalse(post.isMarried)
        assertFalse(post.doYouHavechildren)
        assertEquals(0, post.noOfchildren)
        assertEquals(0, post.noofAlivechildren)
        assertFalse(post.isDeactivate)
        assertNull(post.abhaId)
        assertNull(post.familyId)
    }

    @Test
    fun `BenDemographics required fields are readable`() {
        val demo = demographics()
        assertEquals("A1", demo.addressLine1)
        assertEquals("A2", demo.addressLine2)
        assertEquals("A3", demo.addressLine3)
        assertEquals(4, demo.blockID)
        assertEquals("C1", demo.communityID)
        assertEquals("Community", demo.communityName)
        assertEquals(1, demo.countryID)
        assertEquals("India", demo.countryName)
        assertEquals(11, demo.districtBranchID)
        assertEquals("Branch", demo.districtBranchName)
        assertEquals(3, demo.districtID)
        assertEquals("R1", demo.religionID)
        assertEquals("Religion", demo.religionName)
        assertEquals(2, demo.stateID)
        assertEquals("State", demo.stateName)
    }

    @Test
    fun `BenDemographics optional fields default to null`() {
        val demo = demographics()
        assertNull(demo.incomeStatusName)
        assertNull(demo.blockName)
        assertNull(demo.occupationName)
        assertNull(demo.incomeStatusID)
        assertNull(demo.educationName)
        assertNull(demo.districtName)
        assertNull(demo.habitation)
        assertNull(demo.educationID)
        assertNull(demo.occupationID)
        assertNull(demo.pinCode)
    }

    @Test
    fun `BenDemographics address and location setters round trip`() {
        val demo = demographics()
        demo.addressLine1 = "B1"
        demo.addressLine2 = "B2"
        demo.addressLine3 = "B3"
        demo.blockID = 40
        demo.communityID = "C2"
        demo.communityName = "Community2"
        demo.countryID = 10
        demo.countryName = "Bharat"
        demo.districtBranchID = 110
        demo.districtBranchName = "Branch2"
        demo.districtID = 30
        demo.stateID = 20
        demo.stateName = "State2"

        assertEquals("B1", demo.addressLine1)
        assertEquals("B2", demo.addressLine2)
        assertEquals("B3", demo.addressLine3)
        assertEquals(40, demo.blockID)
        assertEquals("C2", demo.communityID)
        assertEquals("Community2", demo.communityName)
        assertEquals(10, demo.countryID)
        assertEquals("Bharat", demo.countryName)
        assertEquals(110, demo.districtBranchID)
        assertEquals("Branch2", demo.districtBranchName)
        assertEquals(30, demo.districtID)
        assertEquals(20, demo.stateID)
        assertEquals("State2", demo.stateName)
    }

    @Test
    fun `BenDemographics optional setters round trip`() {
        val demo = demographics()
        demo.religionID = "R2"
        demo.religionName = "Religion2"
        demo.incomeStatusName = "Low"
        demo.blockName = "BlockName"
        demo.occupationName = "Farmer"
        demo.incomeStatusID = "IS1"
        demo.educationName = "Primary"
        demo.districtName = "DistrictName"
        demo.habitation = "Habitation"
        demo.educationID = "ED1"
        demo.occupationID = "OC1"
        demo.pinCode = "560001"

        assertEquals("R2", demo.religionID)
        assertEquals("Religion2", demo.religionName)
        assertEquals("Low", demo.incomeStatusName)
        assertEquals("BlockName", demo.blockName)
        assertEquals("Farmer", demo.occupationName)
        assertEquals("IS1", demo.incomeStatusID)
        assertEquals("Primary", demo.educationName)
        assertEquals("DistrictName", demo.districtName)
        assertEquals("Habitation", demo.habitation)
        assertEquals("ED1", demo.educationID)
        assertEquals("OC1", demo.occupationID)
        assertEquals("560001", demo.pinCode)
    }

    @Test
    fun `BeneficiaryIdentities defaults and setters round trip`() {
        val identity = BeneficiaryIdentities(identityType = "AADHAAR", createdBy = "creator")
        assertEquals(0, identity.govtIdentityNo)
        assertEquals(0, identity.govtIdentityTypeID)
        assertNull(identity.govtIdentityTypeName)
        assertEquals("AADHAAR", identity.identityType)
        assertEquals("creator", identity.createdBy)

        identity.govtIdentityNo = 12
        identity.govtIdentityTypeID = 34
        identity.govtIdentityTypeName = "Aadhaar Card"
        identity.identityType = "PAN"
        identity.createdBy = "other"

        assertEquals(12, identity.govtIdentityNo)
        assertEquals(34, identity.govtIdentityTypeID)
        assertEquals("Aadhaar Card", identity.govtIdentityTypeName)
        assertEquals("PAN", identity.identityType)
        assertEquals("other", identity.createdBy)
    }

    @Test
    fun `BenPhoneMaps setters round trip`() {
        val phone = BenPhoneMaps(createdBy = "creator", phoneNo = "1111111111")
        assertEquals("creator", phone.createdBy)
        assertEquals("1111111111", phone.phoneNo)

        phone.createdBy = "other"
        phone.phoneNo = "2222222222"

        assertEquals("other", phone.createdBy)
        assertEquals("2222222222", phone.phoneNo)
    }
}
