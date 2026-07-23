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
}
