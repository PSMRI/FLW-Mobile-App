package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileDomainDTOTest {

    private fun build(profileImage: String? = null) = ProfileDomainDTO(
        id = 1L,
        name = "Asha Worker",
        profileImage = profileImage,
        village = "Village",
        employeeId = 42,
        dob = "1990-01-01",
        age = 34,
        mobileNumber = "9999999999",
        alternateMobileNumber = "8888888888",
        fatherOrSpouseName = "Spouse",
        dateOfJoining = "2015-06-01",
        bankAccount = "1234567890",
        ifsc = "IFSC0001",
        populationCovered = 1200,
        choName = "CHO",
        choMobile = "7777777777",
        awwName = "AWW",
        awwMobile = "6666666666",
        anm1Name = "ANM1",
        anm1Mobile = "5555555555",
        anm2Name = "ANM2",
        anm2Mobile = "4444444444",
        abhaNumber = "11-2222-3333-4444",
        ashaHouseholdRegistration = "50",
        ashaFamilyMember = "200",
        providerServiceMapID = "PSM-1",
        isFatherOrSpouse = true,
        supervisorName = "Supervisor",
        supervisorMobile = "3333333333"
    )

    @Test
    fun `default constructor omits profile image`() {
        val dto = build()

        assertNotNull(dto)
        assertNull(dto.profileImage)
        assertEquals(1L, dto.id)
        assertEquals("Asha Worker", dto.name)
        assertTrue(dto.isFatherOrSpouse)
    }

    @Test
    fun `all supplied fields are retained`() {
        val dto = build(profileImage = "image.png")

        assertEquals("image.png", dto.profileImage)
        assertEquals("Village", dto.village)
        assertEquals(42, dto.employeeId)
        assertEquals("1990-01-01", dto.dob)
        assertEquals(34, dto.age)
        assertEquals("9999999999", dto.mobileNumber)
        assertEquals("8888888888", dto.alternateMobileNumber)
        assertEquals("Spouse", dto.fatherOrSpouseName)
        assertEquals("2015-06-01", dto.dateOfJoining)
        assertEquals("1234567890", dto.bankAccount)
        assertEquals("IFSC0001", dto.ifsc)
        assertEquals(1200, dto.populationCovered)
        assertEquals("CHO", dto.choName)
        assertEquals("7777777777", dto.choMobile)
        assertEquals("AWW", dto.awwName)
        assertEquals("6666666666", dto.awwMobile)
        assertEquals("ANM1", dto.anm1Name)
        assertEquals("5555555555", dto.anm1Mobile)
        assertEquals("ANM2", dto.anm2Name)
        assertEquals("4444444444", dto.anm2Mobile)
        assertEquals("11-2222-3333-4444", dto.abhaNumber)
        assertEquals("50", dto.ashaHouseholdRegistration)
        assertEquals("200", dto.ashaFamilyMember)
        assertEquals("PSM-1", dto.providerServiceMapID)
        assertEquals("Supervisor", dto.supervisorName)
        assertEquals("3333333333", dto.supervisorMobile)
    }

    @Test
    fun `copy and equality behave as data class`() {
        val dto = build()
        val same = dto.copy()
        val other = dto.copy(name = "Other")

        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertNotNull(dto.toString())
        assertEquals("Other", other.name)
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = build()
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
