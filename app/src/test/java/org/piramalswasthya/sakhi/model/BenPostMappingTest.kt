package org.piramalswasthya.sakhi.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.ImageUtils

/**
 * Exercises [BenRegCache.asNetworkPostModel] which builds a [BenPost] (and the
 * nested [BenDemographics]/[BenPhoneMaps]). genDetails/kidDetails are left null
 * to keep the fixture minimal - the mapper is null-safe for those paths.
 */
class BenPostMappingTest {

    private lateinit var context: Context

    private val loc = LocationEntity(id = 11, name = "Village-A")
    private val locationRecord = LocationRecord(
        country = LocationEntity(id = 1, name = "India"),
        state = LocationEntity(id = 2, name = "State"),
        district = LocationEntity(id = 3, name = "District"),
        block = LocationEntity(id = 4, name = "Block"),
        village = loc
    )

    private val user = User(
        userId = 1,
        name = "Asha Name",
        userName = "asha",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 123,
        state = loc,
        district = loc,
        block = loc,
        villages = listOf(loc)
    )

    private fun ben() = BenRegCache(
        householdId = 1L,
        beneficiaryId = 100L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 7,
        isKid = false,
        isAdult = true,
        gender = Gender.MALE,
        ageUnit = AgeUnit.YEARS,
        age = 30,
        genderId = 0,
        firstName = "John",
        lastName = "Doe",
        fatherName = "Father",
        motherName = "Mother",
        contactNumber = 9999999999L,
        regDate = 1_700_000_000_000L,
        createdBy = "creator",
        createdDate = 1_700_000_000_000L,
        locationRecord = locationRecord,
        syncState = SyncState.UNSYNCED,
        isDraft = false
    )

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockkObject(ImageUtils)
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns "encoded"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `asNetworkPostModel maps identifiers and household`() {
        val post = ben().asNetworkPostModel(context, user)

        assertEquals("1", post.householdId)
        assertEquals(100L, post.benId)
        assertEquals(7, post.ashaId)
        assertEquals(123, post.providerServiceMapID)
        assertEquals(4, post.vanID)
    }

    @Test
    fun `asNetworkPostModel maps names and gender`() {
        val post = ben().asNetworkPostModel(context, user)

        assertEquals("John", post.firstName)
        assertEquals("Doe", post.lastName)
        assertEquals("Father", post.fatherName)
        assertEquals("Mother", post.motherName)
        assertEquals("MALE", post.gender)
        assertEquals(0, post.genderId)
        // genDetails null -> spouseName blank
        assertEquals("", post.spouseName)
    }

    @Test
    fun `asNetworkPostModel maps age fields for years`() {
        val post = ben().asNetworkPostModel(context, user)

        assertEquals(30, post.age)
        assertEquals("Year(s)", post.age_unit)
        assertEquals(AgeUnit.YEARS.ordinal, post.age_unitId)
    }

    @Test
    fun `asNetworkPostModel maps aadhaar absent and general registration type`() {
        val post = ben().asNetworkPostModel(context, user)

        // hasAadhar default false -> "No"
        assertEquals("No", post.aadha_no)
        // kidDetails null -> general beneficiary
        assertEquals("General Beneficiary", post.registrationType)
    }

    @Test
    fun `asNetworkPostModel builds demographics from location`() {
        val post = ben().asNetworkPostModel(context, user)

        assertEquals(2, post.benDemographics.stateID)
        assertEquals("State", post.benDemographics.stateName)
        assertEquals(11, post.benDemographics.districtBranchID)
        assertEquals("Village-A", post.villageName)
        assertEquals("0", post.benDemographics.communityID)
    }

    @Test
    fun `asNetworkPostModel uses encoded ben image and phone map`() {
        val post = ben().asNetworkPostModel(context, user)

        assertEquals("encoded", post.userImage)
        assertEquals("9999999999", post.benPhoneMaps[0].phoneNo)
        assertEquals("asha", post.benPhoneMaps[0].createdBy)
    }

    @Test
    fun `asNetworkPostModel maps months age unit`() {
        val post = ben().copy(ageUnit = AgeUnit.MONTHS).asNetworkPostModel(context, user)
        assertEquals("Month(s)", post.age_unit)
        assertEquals(AgeUnit.MONTHS.ordinal, post.age_unitId)
    }

    @Test
    fun `asNetworkPostModel maps days age unit`() {
        val post = ben().copy(ageUnit = AgeUnit.DAYS).asNetworkPostModel(context, user)
        assertEquals("Day(s)", post.age_unit)
        assertEquals(AgeUnit.DAYS.ordinal, post.age_unitId)
    }

    @Test
    fun `asNetworkPostModel throws for null age unit`() {
        try {
            ben().copy(ageUnit = null).asNetworkPostModel(context, user)
            fail("Expected IllegalStateException for null ageUnit")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `asNetworkPostModel maps aadhaar present`() {
        val post = ben().copy(hasAadhar = true, aadharNum = "123456789012").asNetworkPostModel(context, user)
        assertEquals("Yes", post.aadha_no)
        assertEquals("123456789012", post.aadhaNo)
    }

    @Test
    fun `asNetworkPostModel maps aadhaar unknown tri-state`() {
        val post = ben().copy(hasAadhar = null).asNetworkPostModel(context, user)
        assertEquals("null", post.aadha_no)
    }

    @Test
    fun `asNetworkPostModel maps aadharNum null to empty string`() {
        val post = ben().copy(aadharNum = null).asNetworkPostModel(context, user)
        assertEquals("", post.aadhaNo)
    }

    @Test
    fun `asNetworkPostModel maps null name fields to empty strings`() {
        val post = ben().copy(firstName = null, lastName = null, fatherName = null, motherName = null)
            .asNetworkPostModel(context, user)
        assertEquals("", post.firstName)
        assertEquals("", post.lastName)
        assertEquals("", post.fatherName)
        assertEquals("", post.motherName)
    }

    @Test
    fun `asNetworkPostModel maps null familyHeadRelation and guidelineId to defaults`() {
        val post = ben().copy(familyHeadRelation = null, guidelineId = null).asNetworkPostModel(context, user)
        assertEquals("Other", post.familyHeadRelation)
        assertEquals("0", post.guidelineId)
    }

    @Test
    fun `asNetworkPostModel maps populated familyHeadRelation and guidelineId`() {
        val post = ben().copy(familyHeadRelation = "Self", guidelineId = "G-1").asNetworkPostModel(context, user)
        assertEquals("Self", post.familyHeadRelation)
        assertEquals("G-1", post.guidelineId)
    }

    @Test
    fun `asNetworkPostModel maps null rchId, religionOthers, mobileOthers, needOpCare to defaults`() {
        val post = ben().copy(
            rchId = null,
            religionOthers = null,
            mobileOthers = null,
            needOpCare = null
        ).asNetworkPostModel(context, user)
        assertEquals("", post.rchId)
        assertEquals("", post.religionOthers)
        assertEquals("", post.mobileOthers)
        assertEquals("null", post.needOpCare)
    }

    @Test
    fun `asNetworkPostModel maps populated rchId, religionOthers, mobileOthers, needOpCare`() {
        val post = ben().copy(
            rchId = "RCH-1",
            religionOthers = "Other religion",
            mobileOthers = "8888888888",
            needOpCare = "Yes"
        ).asNetworkPostModel(context, user)
        assertEquals("RCH-1", post.rchId)
        assertEquals("Other religion", post.religionOthers)
        assertEquals("8888888888", post.mobileOthers)
        assertEquals("Yes", post.needOpCare)
    }

    @Test
    fun `asNetworkPostModel maps genDetails when present and not kid`() {
        val gen = BenRegGen(
            maritalStatus = "Married",
            maritalStatusId = 1,
            spouseName = "Jane",
            ageAtMarriage = 25,
            marriageDate = 1_600_000_000_000L,
            lastMenstrualPeriod = 1_650_000_000_000L,
            reproductiveStatus = "Antenatal Mother",
            reproductiveStatusId = 2
        )
        val post = ben().copy(isKid = false, genDetails = gen).asNetworkPostModel(context, user)

        assertEquals(25, post.ageAtMarriage)
        assertNotNull(post.marriageDate)
        assertNotNull(post.lastMenstrualPeriod)
        assertEquals(2, post.reproductiveStatusId)
        assertEquals("Antenatal Mother", post.reproductiveStatus)
        assertEquals("Jane", post.spouseName)
        assertEquals("1", post.maritalStatusID)
        assertEquals("Married", post.maritalStatusName)
    }

    @Test
    fun `asNetworkPostModel nulls marital status when isKid true`() {
        val gen = BenRegGen(maritalStatus = "Married", maritalStatusId = 1)
        val post = ben().copy(isKid = true, genDetails = gen).asNetworkPostModel(context, user)

        assertNull(post.maritalStatusID)
        assertNull(post.maritalStatusName)
    }

    @Test
    fun `asNetworkPostModel defaults ageAtMarriage and reproductiveStatusId when genDetails absent`() {
        val post = ben().copy(genDetails = null).asNetworkPostModel(context, user)

        assertEquals(0, post.ageAtMarriage)
        assertEquals(0, post.reproductiveStatusId)
        assertEquals("", post.spouseName)
    }

    @Test
    fun `asNetworkPostModel maps unknown reproductiveStatusId to fallback 6`() {
        val gen = BenRegGen(reproductiveStatusId = 42)
        val post = ben().copy(genDetails = gen).asNetworkPostModel(context, user)

        assertEquals(6, post.reproductiveStatusId)
    }

    @Test
    fun `asNetworkPostModel maps kidDetails to NewBorn registration type and school info`() {
        val kid = BenRegKid(
            childRegisteredSchoolId = 5,
            typeOfSchoolId = 2,
            childRegisteredSchool = "ABC School",
            typeOfSchool = "Primary"
        )
        val post = ben().copy(isKid = true, kidDetails = kid).asNetworkPostModel(context, user)

        assertEquals("NewBorn", post.registrationType)
        assertEquals(5, post.childRegisteredSchoolID)
        assertEquals(2, post.typeOfSchoolId)
        assertEquals("ABC School", post.childRegisteredSchool)
        assertEquals("Primary", post.typeofSchool)
    }

    @Test
    fun `asNetworkPostModel maps death fields when populated`() {
        val post = ben().copy(
            isDeath = true,
            isDeathValue = "Yes",
            dateOfDeath = "01-01-2020",
            timeOfDeath = "10:00",
            reasonOfDeath = "Illness",
            placeOfDeath = "Home",
            otherPlaceOfDeath = "N/A"
        ).asNetworkPostModel(context, user)

        assertTrue(post.isDeath)
        assertEquals("Yes", post.isDeathValue)
        assertEquals("01-01-2020", post.dateOfDeath)
        assertEquals("10:00", post.timeOfDeath)
        assertEquals("Illness", post.reasonOfDeath)
        assertEquals("Home", post.placeOfDeath)
        assertEquals("N/A", post.otherPlaceOfDeath)
    }

    @Test
    fun `asNetworkPostModel maps death fields to empty string defaults when null`() {
        val post = ben().copy(
            isDeath = false,
            isDeathValue = null,
            dateOfDeath = null,
            timeOfDeath = null,
            reasonOfDeath = null,
            placeOfDeath = null,
            otherPlaceOfDeath = null
        ).asNetworkPostModel(context, user)

        assertFalse(post.isDeath)
        assertEquals("", post.isDeathValue)
        assertEquals("", post.dateOfDeath)
        assertEquals("", post.timeOfDeath)
        assertEquals("", post.reasonOfDeath)
        assertEquals("", post.placeOfDeath)
        assertEquals("", post.otherPlaceOfDeath)
    }

    @Test
    fun `asNetworkPostModel maps healthIdDetails with valid abhaId and familyId`() {
        val health = BenHealthIdDetails(healthIdNumber = "1234567890123", familyId = "FAM-1")
        val post = ben().copy(healthIdDetails = health).asNetworkPostModel(context, user)

        assertEquals("1234567890123", post.abhaId)
        assertEquals("FAM-1", post.familyId)
    }

    @Test
    fun `asNetworkPostModel nulls abhaId and familyId when healthIdDetails absent`() {
        val post = ben().copy(healthIdDetails = null).asNetworkPostModel(context, user)

        assertNull(post.abhaId)
        assertNull(post.familyId)
    }

    @Test
    fun `asNetworkPostModel nulls abhaId when healthIdNumber is blank or undefined`() {
        val blankHealth = BenHealthIdDetails(healthIdNumber = "", familyId = "")
        val undefinedHealth = BenHealthIdDetails(healthIdNumber = "undefined", familyId = "FAM-2")

        val blankPost = ben().copy(healthIdDetails = blankHealth).asNetworkPostModel(context, user)
        val undefinedPost = ben().copy(healthIdDetails = undefinedHealth).asNetworkPostModel(context, user)

        assertNull(blankPost.abhaId)
        assertNull(blankPost.familyId)
        assertNull(undefinedPost.abhaId)
        assertEquals("FAM-2", undefinedPost.familyId)
    }

    @Test
    fun `asNetworkPostModel maps flags for children, marriage and deactivation`() {
        val post = ben().copy(
            noOfAliveChildren = 2,
            noOfChildren = 3,
            isMarried = true,
            isChildrenAdded = true,
            doYouHavechildren = true,
            isSpouseAdded = true,
            isDeactivate = true
        ).asNetworkPostModel(context, user)

        assertEquals(2, post.noofAlivechildren)
        assertEquals(3, post.noOfchildren)
        assertTrue(post.isMarried)
        assertTrue(post.isChildrenAdded)
        assertTrue(post.doYouHavechildren)
        assertTrue(post.isSpouseAdded)
        assertTrue(post.isDeactivate)
    }

    @Test
    fun `asNetworkPostModel maps literacy, community, religion and processed passthrough fields`() {
        val post = ben().copy(
            literacy = "Graduate",
            literacyId = 4,
            community = "OBC",
            communityId = 2,
            religion = "Hindu",
            religionId = 1,
            processed = "P",
            ncdPriority = 7,
            isHrpStatus = true
        ).asNetworkPostModel(context, user)

        assertEquals("Graduate", post.literacy)
        assertEquals(4, post.literacyId)
        assertEquals("OBC", post.benDemographics.communityName)
        assertEquals("2", post.benDemographics.communityID)
        assertEquals("Hindu", post.benDemographics.religionName)
        assertEquals("1", post.benDemographics.religionID)
        assertEquals("P", post.processed)
        assertEquals(7, post.ncdPriority)
        assertTrue(post.isHrpStatus)
    }

    @Test
    fun `asNetworkPostModel maps reproductiveStatusId eligible couple branch`() {
        val gen = BenRegGen(reproductiveStatusId = 0)
        val post = ben().copy(genDetails = gen).asNetworkPostModel(context, user)
        assertEquals(0, post.reproductiveStatusId)
    }

    @Test
    fun `asNetworkPostModel maps reproductiveStatusId antenatal eligible branch`() {
        val gen = BenRegGen(reproductiveStatusId = 1)
        val post = ben().copy(genDetails = gen).asNetworkPostModel(context, user)
        assertEquals(1, post.reproductiveStatusId)
    }

    @Test
    fun `asNetworkPostModel maps reproductiveStatusId postnatal branch`() {
        val gen = BenRegGen(reproductiveStatusId = 3)
        val post = ben().copy(genDetails = gen).asNetworkPostModel(context, user)
        assertEquals(3, post.reproductiveStatusId)
    }

    @Test
    fun `asNetworkPostModel maps reproductiveStatusId menopause branch`() {
        val gen = BenRegGen(reproductiveStatusId = 4)
        val post = ben().copy(genDetails = gen).asNetworkPostModel(context, user)
        assertEquals(4, post.reproductiveStatusId)
    }

    @Test
    fun `asNetworkPostModel maps reproductiveStatusId sterilised branch`() {
        val gen = BenRegGen(reproductiveStatusId = 5)
        val post = ben().copy(genDetails = gen).asNetworkPostModel(context, user)
        assertEquals(5, post.reproductiveStatusId)
    }

    @Test
    fun `asNetworkPostModel maps reproductiveStatus string to literal null when genDetails absent`() {
        val post = ben().copy(genDetails = null).asNetworkPostModel(context, user)
        assertEquals("null", post.reproductiveStatus)
    }

    // ---------------------------------------------------------------
    // Generated members (equals/hashCode/copy/toString/componentN)
    // ---------------------------------------------------------------

    @Test
    fun `BenPost copy overrides every constructor field`() {
        val base = ben().asNetworkPostModel(context, user)
        val overriddenDemographics = base.benDemographics.copy(
            addressLine1 = "Line1-Ovr",
            communityID = "9",
            communityName = "OvrCommunity",
            religionID = "3",
            religionName = "OvrReligion",
            stateID = 99,
            stateName = "OvrState",
        )

        val overridden = base.copy(
            benRegId = 999L,
            countyid = 9,
            processed = "OVR",
            isDeath = true,
            isDeathValue = "OverrideDeathValue",
            dateOfDeath = "2099-01-01",
            timeOfDeath = "23:59",
            reasonOfDeath = "OverrideReason",
            reasonOfDeathId = 5,
            placeOfDeath = "OverridePlace",
            placeOfDeathId = 6,
            otherPlaceOfDeath = "OverrideOther",
            providerServiceMapID = 111,
            vanID = 222,
            aadhaNo = "999999999999",
            aadha_no = "Yes",
            aadha_noId = 1,
            age = 99,
            ageAtMarriage = 21,
            age_unit = "Month(s)",
            age_unitId = 1,
            ashaId = 77,
            benId = 777L,
            childRegisteredAWCID = 3,
            childRegisteredSchoolID = 4,
            createdBy = "OverrideCreator",
            createdDate = "2099-01-01T00:00:00.000Z",
            currSubDistrictId = 8,
            expectedDateOfDelivery = "2099-02-02",
            facilitySelection = "OverrideFacility",
            familyHeadRelation = "OverrideRelation",
            familyHeadRelationPosition = 2,
            fatherName = "OverrideFather",
            firstName = "OverrideFirst",
            guidelineId = "G-99",
            householdId = "999",
            isHrpStatus = true,
            dummyIdMayBe = 2,
            isImmunizationStatus = true,
            lastDeliveryConducted = "OverrideDelivery",
            lastDeliveryConductedID = 3,
            lastMenstrualPeriod = "2098-01-01",
            lastName = "OverrideLast",
            latitude = 12.34,
            longitude = 56.78,
            marriageDate = "2000-01-01",
            mobileOthers = "8888888888",
            mobileNoOfRelation = "7777777777",
            mobileNoOfRelationId = 1,
            motherName = "OverrideMother",
            nayiPahalDeliveryStatus = "OverrideNayi",
            nayiPahalDeliveryStatusPosition = 1,
            ncdPriority = 9,
            needOpCare = "Yes",
            needOpCareId = 1,
            nishchayPregnancyStatus = "OverrideNishchayPreg",
            nishchayPregnancyStatusPosition = 1,
            nishchayDeliveryStatus = "OverrideNishchayDel",
            nishchayDeliveryStatusPosition = 1,
            noOfDaysForDelivery = 10,
            previousLiveBirth = "1",
            rchId = "RCH-99",
            registrationDate = "2099-03-03T00:00:00.000Z",
            registrationType = "OverrideType",
            religionOthers = "OverrideReligionOthers",
            reproductiveStatus = "OverrideStatus",
            reproductiveStatusId = 3,
            serverUpdatedStatus = 2,
            spouseName = "OverrideSpouse",
            typeOfSchoolId = 5,
            userImage = "overrideEncoded",
            villageId = 55,
            villageName = "OverrideVillage",
            whoConductedDeliveryID = 3,
            whoConductedDelivery = "OverrideConducted",
            literacy = "OverrideLiteracy",
            literacyId = 5,
            menstrualStatusId = 1,
            regularityofMenstrualCycleId = 1,
            lengthofMenstrualCycleId = 1,
            menstrualBFDId = 1,
            menstrualProblemId = 1,
            formStatus = "OverrideFormStatus",
            formType = "OverrideFormType",
            childRegisteredSchool = "OverrideSchool",
            typeofSchool = "OverrideTypeSchool",
            menstrualStatus = "OverrideMenstrual",
            dateMarriage = "2001-01-01",
            deliveryDate = "2002-01-01",
            suspected_hrp = "Y",
            suspected_ncd = "Y",
            suspected_tb = "Y",
            suspected_ncd_diseases = "Diabetes",
            confirmed_ncd = "Y",
            confirmed_hrp = "Y",
            confirmed_tb = "Y",
            confirmed_ncd_diseases = "Diabetes",
            diagnosis_status = "Confirmed",
            benPhoneMaps = arrayOf(BenPhoneMaps(createdBy = "x", phoneNo = "1231231234")),
            dob = "1990-01-01",
            isEmergencyRegistration = true,
            genderId = 1,
            gender = "FEMALE",
            maritalStatusID = "2",
            maritalStatusName = "Married",
            benDemographics = overriddenDemographics,
            isSpouseAdded = true,
            isChildrenAdded = true,
            isMarried = true,
            doYouHavechildren = true,
            noOfchildren = 2,
            noofAlivechildren = 2,
            isDeactivate = true,
            abhaId = "ABHA123",
            familyId = "FAM123",
        )

        assertFalse(base == overridden)
        assertEquals(999L, overridden.benRegId)
        assertEquals("FEMALE", overridden.gender)
        assertEquals("OvrState", overridden.benDemographics.stateName)
        assertEquals("ABHA123", overridden.abhaId)
        assertEquals("FAM123", overridden.familyId)
        assertTrue(overridden.isDeactivate)
        assertTrue(overridden.isEmergencyRegistration)
    }

    @Test
    fun `BenDemographics generated members - copy, equals, hashCode, toString, components`() {
        val demographics = BenDemographics(
            addressLine1 = "D.No 3-160E",
            addressLine2 = "ARS Road",
            addressLine3 = "Neggipudi",
            blockID = 4,
            communityID = "1",
            communityName = "General",
            countryID = 1,
            countryName = "India",
            districtBranchID = 11,
            districtBranchName = "Village-A",
            districtID = 3,
            religionID = "2",
            religionName = "Hindu",
            stateID = 2,
            stateName = "State",
            incomeStatusName = "Low",
            blockName = "Block",
            occupationName = "Farmer",
            incomeStatusID = "1",
            educationName = "Graduate",
            districtName = "District",
            habitation = "Rural",
            educationID = "4",
            occupationID = "5",
            pinCode = "500001",
        )

        val sameValues = demographics.copy()
        val different = demographics.copy(stateName = "OtherState")

        assertEquals(demographics, sameValues)
        assertEquals(demographics.hashCode(), sameValues.hashCode())
        assertTrue(demographics.toString().contains("BenDemographics"))
        assertFalse(demographics == different)
        assertFalse(demographics.equals(null))
        assertFalse(demographics.equals("not a demographics"))
        assertEquals(demographics, demographics)
        assertEquals("OtherState", different.stateName)

        demographics.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(demographics) } }
    }

    @Test
    fun `BenDemographics copy overrides every constructor field including nullable ones`() {
        val base = BenDemographics(
            addressLine1 = "L1",
            addressLine2 = "L2",
            addressLine3 = "L3",
            blockID = 1,
            communityID = "1",
            communityName = "C1",
            countryID = 1,
            countryName = "India",
            districtBranchID = 1,
            districtBranchName = "D1",
            districtID = 1,
            religionID = "1",
            religionName = "R1",
            stateID = 1,
            stateName = "S1",
        )

        val overridden = base.copy(
            addressLine1 = "OvrL1",
            addressLine2 = "OvrL2",
            addressLine3 = "OvrL3",
            blockID = 2,
            communityID = "2",
            communityName = "OvrC",
            countryID = 2,
            countryName = "OvrCountry",
            districtBranchID = 2,
            districtBranchName = "OvrD",
            districtID = 2,
            religionID = "2",
            religionName = "OvrR",
            stateID = 2,
            stateName = "OvrS",
            incomeStatusName = "Medium",
            blockName = "OvrBlock",
            occupationName = "Trader",
            incomeStatusID = "2",
            educationName = "PostGraduate",
            districtName = "OvrDistrict",
            habitation = "Urban",
            educationID = "6",
            occupationID = "7",
            pinCode = "600001",
        )

        assertFalse(base == overridden)
        assertEquals("OvrS", overridden.stateName)
        assertEquals("Medium", overridden.incomeStatusName)
        assertEquals("600001", overridden.pinCode)
        assertNull(base.incomeStatusName)
    }

    @Test
    fun `BenPost generated members - copy, equals, hashCode, toString, components`() {
        val post = ben().asNetworkPostModel(context, user)
        val copy = post.copy()

        assertEquals(post, copy)
        assertEquals(post.hashCode(), copy.hashCode())
        assertTrue(post.toString().contains("BenPost"))

        val different = post.copy(firstName = "Different")
        assertFalse(post == different)
        assertEquals("Different", different.firstName)

        post.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(post) } }
    }
}
