package org.piramalswasthya.sakhi.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.ImageUtils

/**
 * Exercises [BenRegCache.asNetworkSendingModel] which builds a
 * [BeneficiaryDataSending] plus the nested [BenDemographics], [BenPhoneMaps]
 * and [BeneficiaryIdentities]. genDetails is left null (null-safe paths).
 */
class BenSendingMappingTest {

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

    private fun benCho() = BenRegCache(
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
        genderId = 5,
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
    fun `asNetworkSendingModel maps names and image`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)

        assertEquals("John", sending.firstName)
        assertEquals("Doe", sending.lastName)
        assertEquals("Father", sending.fatherName)
        assertEquals("Mother", sending.motherName)
        assertEquals("encoded", sending.benImage)
    }

    @Test
    fun `asNetworkSendingModel maps male gender name`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)
        assertEquals("Male", sending.genderName)
    }

    @Test
    fun `asNetworkSendingModel maps female gender name`() {
        val sending = ben().copy(gender = Gender.FEMALE)
            .asNetworkSendingModel(user, locationRecord, context)
        assertEquals("Female", sending.genderName)
    }

    @Test
    fun `asNetworkSendingModel maps null gender to NA`() {
        val sending = ben().copy(gender = null)
            .asNetworkSendingModel(user, locationRecord, context)
        assertEquals("NA", sending.genderName)
    }

    @Test
    fun `asNetworkSendingModel maps provider and created by from user`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)

        assertEquals("123", sending.providerServiceMapID)
        assertEquals("123", sending.providerServiceMapId)
        assertEquals("asha", sending.createdBy)
    }

    @Test
    fun `asNetworkSendingModel builds phone map and identities`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)

        assertEquals("9999999999", sending.benPhoneMaps[0].phoneNo)
        assertEquals("asha", sending.benPhoneMaps[0].createdBy)
        assertEquals("National ID", sending.beneficiaryIdentities[0].identityType)
    }

    @Test
    fun `asNetworkSendingModel defaults consent and age at marriage`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)

        // isConsent default false
        assertEquals(false, sending.beneficiaryConsent)
        // genDetails null -> "0"
        assertEquals("0", sending.ageAtMarriage)
    }

    @Test
    fun `asNetworkSendingModel builds demographics from location`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)

        assertEquals(2, sending.benDemographics.stateID)
        assertEquals("State", sending.benDemographics.stateName)
        assertEquals(11, sending.benDemographics.districtBranchID)
        assertEquals("0", sending.benDemographics.communityID)
    }

    // --- asNetworkSendingModelCHO (builds BenCHOPost with nested BenDemographicsCHO / BenPhoneMapCHO) ---

    @Test
    fun `CHO maps names and image`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        assertEquals("John", post.firstName)
        assertEquals("Doe", post.lastName)
        assertEquals("encoded", post.benImage)
    }

    @Test
    fun `CHO maps male gender name`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        assertEquals("Male", post.genderName)
        assertEquals(5, post.genderID)
    }

    @Test
    fun `CHO maps female gender name`() {
        val post = benCho().copy(gender = Gender.FEMALE).asNetworkSendingModelCHO(user, context)
        assertEquals("Female", post.genderName)
    }

    @Test
    fun `CHO maps transgender gender name`() {
        val post = benCho().copy(gender = Gender.TRANSGENDER).asNetworkSendingModelCHO(user, context)
        assertEquals("Transgender", post.genderName)
    }

    @Test
    fun `CHO maps null gender to NA`() {
        val post = benCho().copy(gender = null).asNetworkSendingModelCHO(user, context)
        assertEquals("NA", post.genderName)
    }

    @Test
    fun `CHO sets fixed marital status married`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        assertEquals(2, post.maritalStatusID)
        assertEquals("Married", post.maritalStatusName)
    }

    @Test
    fun `CHO maps demographics from location record`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        val demo = post.benDemographics
        assertEquals(2, demo.stateID)
        assertEquals("State", demo.stateName)
        assertEquals(3, demo.districtID)
        assertEquals(4, demo.blockID)
        assertEquals(11, demo.districtBranchID)
        assertEquals("Village-A", demo.districtBranchName)
        assertEquals(1, demo.countryID)
        assertEquals("India", demo.countryName)
    }

    @Test
    fun `CHO maps phone map with user and contact`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        assertEquals(1, post.benPhoneMaps.size)
        val phone = post.benPhoneMaps[0]
        assertEquals("9999999999", phone.phoneNo)
        assertEquals("asha", phone.createdBy)
        assertEquals(1, phone.phoneTypeID)
    }

    @Test
    fun `CHO maps createdBy from user and defaults`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        assertEquals("asha", post.createdBy)
        assertEquals(false, post.emergencyRegistration)
        assertEquals(true, post.beneficiaryIdentities?.isEmpty())
    }

    @Test
    fun `CHO null spouseName when genDetails null`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        assertEquals(null, post.spouseName)
    }

    @Test
    fun `BeneficiaryDataSending copy toString and equality`() {
        val sending = ben().asNetworkSendingModel(user, locationRecord, context)
        val same = sending.copy()
        assertEquals(sending, same)
        assertEquals(sending.hashCode(), same.hashCode())
        assertNotEquals(sending, sending.copy(firstName = "Other"))
        assertTrue(sending.toString().contains("BeneficiaryDataSending"))
    }

    @Test
    fun `BenCHOPost copy toString and equality`() {
        val post = benCho().asNetworkSendingModelCHO(user, context)
        val same = post.copy()
        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, post.copy(firstName = "Other"))
        assertTrue(post.toString().contains("BenCHOPost"))
    }

    private fun demographicsCHO() = BenDemographicsCHO(
        incomeStatusID = "1",
        incomeStatusName = "Low",
        occupationID = "2",
        occupationName = "Farmer",
        educationID = "3",
        educationName = "Primary",
        communityID = "4",
        communityName = "General",
        religionID = "5",
        religionName = "Hindu",
        countryID = 1,
        countryName = "India",
        stateID = 2,
        stateName = "State",
        districtID = 3,
        districtName = "District",
        blockID = 4,
        blockName = "Block",
        districtBranchID = 5,
        districtBranchName = "Branch",
        habitation = "Habitation",
        pinCode = "123456",
        addressLine1 = "Line1",
        addressLine2 = "Line2",
        addressLine3 = "Line3"
    )

    @Test
    fun `BenDemographicsCHO holds field values`() {
        val d = demographicsCHO()
        assertEquals("Low", d.incomeStatusName)
        assertEquals("India", d.countryName)
        assertEquals("123456", d.pinCode)
    }

    @Test
    fun `BenDemographicsCHO copy toString and equality`() {
        val d = demographicsCHO()
        val same = d.copy()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertNotEquals(d, d.copy(stateName = "Other"))
        assertTrue(d.toString().contains("BenDemographicsCHO"))

        assertNotEquals(d, d.copy(incomeStatusID = "Other"))
        assertNotEquals(d, d.copy(incomeStatusName = "Other"))
        assertNotEquals(d, d.copy(occupationID = "Other"))
        assertNotEquals(d, d.copy(occupationName = "Other"))
        assertNotEquals(d, d.copy(educationID = "Other"))
        assertNotEquals(d, d.copy(educationName = "Other"))
        assertNotEquals(d, d.copy(communityID = "Other"))
        assertNotEquals(d, d.copy(communityName = "Other"))
        assertNotEquals(d, d.copy(religionID = "Other"))
        assertNotEquals(d, d.copy(religionName = "Other"))
        assertNotEquals(d, d.copy(countryID = 999))
        assertNotEquals(d, d.copy(countryName = "Other"))
        assertNotEquals(d, d.copy(stateID = 999))
        assertNotEquals(d, d.copy(districtID = 999))
        assertNotEquals(d, d.copy(districtName = "Other"))
        assertNotEquals(d, d.copy(blockID = 999))
        assertNotEquals(d, d.copy(blockName = "Other"))
        assertNotEquals(d, d.copy(districtBranchID = 999))
        assertNotEquals(d, d.copy(districtBranchName = "Other"))
        assertNotEquals(d, d.copy(habitation = "Other"))
        assertNotEquals(d, d.copy(pinCode = "Other"))
        assertNotEquals(d, d.copy(addressLine1 = "Other"))
        assertNotEquals(d, d.copy(addressLine2 = "Other"))
        assertNotEquals(d, d.copy(addressLine3 = "Other"))
    }
}
