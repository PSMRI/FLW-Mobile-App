package org.piramalswasthya.sakhi.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
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
}
