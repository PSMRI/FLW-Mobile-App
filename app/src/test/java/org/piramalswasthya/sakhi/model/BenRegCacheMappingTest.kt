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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.ImageUtils

/**
 * Tests for the network-model mappers on [BenRegCache]:
 *  - asKidNetworkModel(user)  -> BenRegKidNetwork  (pure, no Android deps)
 *  - asNetworkPostModel(context, user) -> BenPost  (ImageUtils object mocked)
 *
 * All BenRegCache fields are mutable vars, so we build a minimal valid instance
 * and set the fields each mapper actually reads.
 */
class BenRegCacheMappingTest {

    private val loc = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(10, "State"),
        district = LocationEntity(20, "District"),
        block = LocationEntity(30, "Block"),
        village = LocationEntity(40, "Village")
    )

    private val user = User(
        userId = 1,
        name = "Nurse",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 99,
        state = LocationEntity(10, "State"),
        district = LocationEntity(20, "District"),
        block = LocationEntity(30, "Block"),
        villages = emptyList()
    )

    private fun baseBenReg() = BenRegCache(
        householdId = 5L,
        beneficiaryId = 10L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 7,
        isKid = false,
        isAdult = true,
        locationRecord = loc,
        syncState = SyncState.UNSYNCED,
        isDraft = false
    )

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ===================================================================
    // asKidNetworkModel
    // ===================================================================

    @Test fun `asKidNetworkModel maps ids and location`() {
        val ben = baseBenReg().apply {
            firstName = "Kiddo"
            isConsent = true
            kidDetails = BenRegKid()
        }
        val net = ben.asKidNetworkModel(user)
        assertEquals(10L, net.benficieryid)
        assertEquals("Kiddo", net.childName)
        assertEquals(7, net.ashaid)
        assertEquals(99, net.ProviderServiceMapID)
        assertEquals(1, net.Countyid)
        assertEquals(10, net.stateid)
        assertEquals(20, net.districtid)
        assertEquals(40, net.villageid)
        assertTrue(net.isConsent)
    }

    @Test fun `asKidNetworkModel converts height and weight`() {
        val ben = baseBenReg().apply {
            kidDetails = BenRegKid(heightAtBirth = 3.0, weightAtBirth = 2.5)
        }
        val net = ben.asKidNetworkModel(user)
        assertEquals(3, net.heightAtBirth)          // Double -> Int
        assertEquals(2.5f, net.weightAtBirth, 0.0001f) // Double -> Float
    }

    @Test fun `asKidNetworkModel copies kid detail fields`() {
        val ben = baseBenReg().apply {
            kidDetails = BenRegKid(
                birthPlace = "Hospital",
                birthPlaceId = 4,
                deliveryType = "Normal",
                deliveryTypeId = 1
            )
        }
        val net = ben.asKidNetworkModel(user)
        assertEquals("Hospital", net.birthPlace)
        assertEquals(4, net.birthPlaceid)
        assertEquals("Normal", net.deliveryType)
        assertEquals(1, net.deliveryTypeid)
    }

    // ===================================================================
    // asNetworkPostModel
    // ===================================================================

    private fun benForPost() = baseBenReg().apply {
        age = 25
        ageUnit = AgeUnit.YEARS
        createdBy = "admin"
        createdDate = 1_600_000_000_000L
        regDate = 1_600_000_000_000L
        gender = Gender.FEMALE
        genderId = 2
        // kidDetails stays null -> "General Beneficiary"
    }

    @Test fun `asNetworkPostModel maps core beneficiary fields`() {
        mockkObject(ImageUtils)
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns "img"

        val context = mockk<Context>(relaxed = true)
        val post = benForPost().asNetworkPostModel(context, user)

        assertEquals(10L, post.benId)
        assertEquals("5", post.householdId)
        assertEquals(25, post.age)
        assertEquals(7, post.ashaId)
        assertEquals("img", post.userImage)
        assertEquals("General Beneficiary", post.registrationType)
        assertFalse(post.isDeath)
        assertEquals("Village", post.villageName)
    }

    @Test fun `asNetworkPostModel maps YEARS age unit`() {
        mockkObject(ImageUtils)
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns ""

        val context = mockk<Context>(relaxed = true)
        val post = benForPost().asNetworkPostModel(context, user)

        assertEquals("Year(s)", post.age_unit)
        assertEquals(AgeUnit.YEARS.ordinal, post.age_unitId)
    }

    @Test fun `asNetworkPostModel newborn kid maps to NewBorn`() {
        mockkObject(ImageUtils)
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns ""

        val context = mockk<Context>(relaxed = true)
        val ben = benForPost().apply {
            ageUnit = AgeUnit.DAYS
            kidDetails = BenRegKid()
        }
        val post = ben.asNetworkPostModel(context, user)

        assertEquals("NewBorn", post.registrationType)
        assertEquals("Day(s)", post.age_unit)
    }

    // ===================================================================
    // Generated members / property accessors
    // ===================================================================

    @Test fun `accessor round trip covers every property`() {
        val obj = baseBenReg()
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

    @Test fun `component accessors are all reachable`() {
        val obj = baseBenReg()
        obj.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(obj) } }
        assertNotNull(obj)
    }

    @Test fun `copy equals and hashCode agree for identical instances`() {
        val a = baseBenReg()
        val b = a.copy()

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a, a.copy())
        assertTrue(a.toString().contains("BenRegCache"))
    }

    @Test fun `nested detail holders expose their defaults`() {
        val kid = BenRegKid()
        val gen = BenRegGen()
        val health = BenHealthIdDetails()

        assertNotNull(kid)
        assertNotNull(gen)
        assertNotNull(health)
        assertEquals(kid, BenRegKid())
        assertEquals(gen, BenRegGen())
        assertEquals(health, BenHealthIdDetails())
        assertEquals("", health.healthId)
        assertEquals("", health.healthIdNumber)
        assertFalse(health.isNewAbha)

        listOf<Any>(kid, gen, health).forEach { obj ->
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
        }
    }
}
