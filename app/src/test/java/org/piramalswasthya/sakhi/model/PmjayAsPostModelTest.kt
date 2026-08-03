package org.piramalswasthya.sakhi.model

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises [PMJAYCache.asPostModel], which was previously skipped because it needs a
 * User / HouseholdCache / BenRegCache graph. The graph objects are mockk-ed and only the
 * properties the mapper reads are stubbed.
 */
class PmjayAsPostModelTest {

    private val locationRecord = LocationRecord(
        country = LocationEntity(id = 1, name = "India"),
        state = LocationEntity(id = 2, name = "State"),
        district = LocationEntity(id = 3, name = "District"),
        block = LocationEntity(id = 4, name = "Block"),
        village = LocationEntity(id = 77, name = "Village-Z")
    )

    private fun household(): HouseholdCache {
        val hh = mockk<HouseholdCache>()
        every { hh.locationRecord } returns locationRecord
        return hh
    }

    private fun user(): User {
        val u = mockk<User>()
        every { u.userName } returns "asha_user"
        return u
    }

    private fun ben(gender: Gender? = Gender.FEMALE, age: Int = 32): BenRegCache {
        val b = mockk<BenRegCache>()
        every { b.gender } returns gender
        every { b.age } returns age
        return b
    }

    private fun cache() = PMJAYCache(
        id = 9,
        benId = 100L,
        hhId = 5L,
        registrationDate = 1_700_000_000_000L,
        registeredHospital = "Hospital X",
        contactNumber = 9998887776L,
        communicationContactNumber = 1112223334L,
        patientAddress = "Addr 1",
        communicationAddress = "Comm Addr",
        hospitalAddress = "Hosp Addr",
        familyId = 4242L,
        isAadhaarBeneficiary = 1L,
        memberType = "member",
        patientType = "patient",
        scheme = "PMJAY",
        createdBy = "creator"
    )

    @Test
    fun `asPostModel maps identifiers and addresses`() {
        val post = cache().asPostModel(user(), household(), ben(), pmjayCount = 3)
        assertEquals(100L, post.beneficiaryid)
        assertEquals("5", post.houseoldId)
        assertEquals("9", post.idNumber)
        assertEquals("Addr 1", post.patientAddress)
        assertEquals("Comm Addr", post.communicationAddress)
        assertEquals("Hosp Addr", post.hospitalAddress)
        assertEquals("4242", post.familyId)
    }

    @Test
    fun `asPostModel maps user and village and login count`() {
        val post = cache().asPostModel(user(), household(), ben(), pmjayCount = 7)
        assertEquals("asha_user", post.name)
        assertEquals("asha_user", post.updatedBy)
        assertEquals(77, post.villageid)
        assertEquals(7, post.loginId)
    }

    @Test
    fun `asPostModel maps contact numbers as strings`() {
        val post = cache().asPostModel(user(), household(), ben(), pmjayCount = 1)
        assertEquals("9998887776", post.contactNumber)
        assertEquals("1112223334", post.communicationContactNumber)
        assertEquals("1", post.isAadhaarBeneficiary)
    }

    @Test
    fun `asPostModel maps age and scheme and hospital`() {
        val post = cache().asPostModel(user(), household(), ben(age = 45), pmjayCount = 1)
        assertEquals("45", post.age)
        assertEquals("PMJAY", post.scheme)
        assertEquals("Hospital X", post.registered_hospital)
        assertEquals("creator", post.createdBy)
        assertTrue(post.isEditable == true)
    }

    @Test
    fun `asPostModel maps female gender`() {
        val post = cache().asPostModel(user(), household(), ben(gender = Gender.FEMALE), pmjayCount = 1)
        assertEquals("Female", post.gender)
    }

    @Test
    fun `asPostModel maps male gender`() {
        val post = cache().asPostModel(user(), household(), ben(gender = Gender.MALE), pmjayCount = 1)
        assertEquals("Male", post.gender)
    }

    @Test
    fun `asPostModel maps transgender gender`() {
        val post = cache().asPostModel(user(), household(), ben(gender = Gender.TRANSGENDER), pmjayCount = 1)
        assertEquals("Transgender", post.gender)
    }

    @Test
    fun `asPostModel maps null gender to Other`() {
        val post = cache().asPostModel(user(), household(), ben(gender = null), pmjayCount = 1)
        assertEquals("Other", post.gender)
    }

    @Test
    fun `asPostModel formats registration date to non-null string`() {
        val post = cache().asPostModel(user(), household(), ben(), pmjayCount = 1)
        assertNotNull(post.registrationDate)
    }
}
