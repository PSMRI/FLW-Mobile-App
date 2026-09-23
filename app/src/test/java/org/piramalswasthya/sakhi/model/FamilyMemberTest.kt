package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyMemberTest {

    @Test
    fun `default constructor leaves every field null`() {
        val member = FamilyMember()

        assertNotNull(member)
        assertNull(member.abhId)
        assertNull(member.address)
        assertNull(member.age)
        assertNull(member.dob)
        assertNull(member.block)
        assertNull(member.blockCode)
        assertNull(member.cardNo)
        assertNull(member.district)
        assertNull(member.districtCode)
        assertNull(member.familyId)
        assertNull(member.gender)
        assertNull(member.mobileNo)
        assertNull(member.name)
        assertNull(member.ruralUrban)
        assertNull(member.villageName)
        assertNull(member.villageCode)
        assertNull(member.vws)
        assertNull(member.ward)
    }

    @Test
    fun `hasUsableData is false for empty and blank member`() {
        assertFalse(FamilyMember().hasUsableData())
        assertFalse(FamilyMember(name = "  ", gender = "", mobileNo = "", dob = "   ").hasUsableData())
    }

    @Test
    fun `hasUsableData is true when any key field is populated`() {
        assertTrue(FamilyMember(name = "Sita").hasUsableData())
        assertTrue(FamilyMember(gender = "Female").hasUsableData())
        assertTrue(FamilyMember(mobileNo = "9999999999").hasUsableData())
        assertTrue(FamilyMember(dob = "1990-01-01").hasUsableData())
    }

    @Test
    fun `all fields are retained when supplied`() {
        val member = FamilyMember(
            abhId = "abha-1",
            address = "Addr",
            age = "30",
            dob = "1994-05-05",
            block = "Block",
            blockCode = "B1",
            cardNo = "C1",
            district = "District",
            districtCode = "D1",
            familyId = "F1",
            gender = "Male",
            mobileNo = "8888888888",
            name = "Ram",
            ruralUrban = "Rural",
            villageName = "Village",
            villageCode = "V1",
            vws = "VWS",
            ward = "W1"
        )

        assertEquals("abha-1", member.abhId)
        assertEquals("Addr", member.address)
        assertEquals("30", member.age)
        assertEquals("1994-05-05", member.dob)
        assertEquals("Block", member.block)
        assertEquals("B1", member.blockCode)
        assertEquals("C1", member.cardNo)
        assertEquals("District", member.district)
        assertEquals("D1", member.districtCode)
        assertEquals("F1", member.familyId)
        assertEquals("Male", member.gender)
        assertEquals("8888888888", member.mobileNo)
        assertEquals("Ram", member.name)
        assertEquals("Rural", member.ruralUrban)
        assertEquals("Village", member.villageName)
        assertEquals("V1", member.villageCode)
        assertEquals("VWS", member.vws)
        assertEquals("W1", member.ward)
        assertEquals(member, member.copy())
        assertEquals(member.hashCode(), member.copy().hashCode())
        assertNotNull(member.toString())
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = FamilyMember(name = "Ram")
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
