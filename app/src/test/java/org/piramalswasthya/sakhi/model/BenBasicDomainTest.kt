package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class BenBasicDomainTest {

    private fun benBasicDomain() = BenBasicDomain(
        benId = 1L,
        hhId = 2L,
        isDeath = false,
        isDeathValue = "No",
        dateOfDeath = "2024-01-01",
        timeOfDeath = "10:00",
        reasonOfDeath = "reason",
        reasonOfDeathId = 1,
        placeOfDeath = "place",
        placeOfDeathId = 2,
        otherPlaceOfDeath = "other",
        reproductiveStatusId = 3,
        regDate = "2024-01-02",
        benName = "Jane",
        benSurname = "Doe",
        benFullName = "Jane Doe",
        gender = "FEMALE",
        dob = 500_000_000_000L,
        ageInt = 25,
        ageUnit = AgeUnit.YEARS,
        age = "25 YEARS",
        relToHeadId = 4,
        mobileNo = "9998887776",
        abhaId = "abha1",
        isNewAbha = true,
        fatherName = "Bob",
        motherName = "Mary",
        familyHeadName = "Head",
        spouseName = "Spouse",
        rchId = "RCH1",
        hrpStatus = true,
        syncState = SyncState.UNSYNCED,
        isConsent = true,
        isSpouseAdded = true,
        isChildrenAdded = true,
        isMarried = true,
        doYouHavechildren = true,
        noOfChildren = 2,
        noOfAliveChildren = 2,
        isDeactivate = false
    )

    @Test fun `BenBasicDomain copy and equality with all fields set`() {
        val a = benBasicDomain()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("BenBasicDomain"))

        assertNotEquals(a, a.copy(benId = 999L))
        assertNotEquals(a, a.copy(hhId = 999L))
        assertNotEquals(a, a.copy(isDeath = true))
        assertNotEquals(a, a.copy(isDeathValue = "Other"))
        assertNotEquals(a, a.copy(dateOfDeath = "Other"))
        assertNotEquals(a, a.copy(timeOfDeath = "Other"))
        assertNotEquals(a, a.copy(reasonOfDeath = "Other"))
        assertNotEquals(a, a.copy(reasonOfDeathId = 999))
        assertNotEquals(a, a.copy(placeOfDeath = "Other"))
        assertNotEquals(a, a.copy(placeOfDeathId = 999))
        assertNotEquals(a, a.copy(otherPlaceOfDeath = "Other"))
        assertNotEquals(a, a.copy(reproductiveStatusId = 999))
        assertNotEquals(a, a.copy(regDate = "Other"))
        assertNotEquals(a, a.copy(benName = "Other"))
        assertNotEquals(a, a.copy(benSurname = "Other"))
        assertNotEquals(a, a.copy(benFullName = "Other"))
        assertNotEquals(a, a.copy(gender = "MALE"))
        assertNotEquals(a, a.copy(dob = 999L))
        assertNotEquals(a, a.copy(ageInt = 999))
        assertNotEquals(a, a.copy(ageUnit = AgeUnit.DAYS))
        assertNotEquals(a, a.copy(age = "Other"))
        assertNotEquals(a, a.copy(relToHeadId = 999))
        assertNotEquals(a, a.copy(mobileNo = "0000000000"))
        assertNotEquals(a, a.copy(abhaId = "Other"))
        assertNotEquals(a, a.copy(isNewAbha = false))
        assertNotEquals(a, a.copy(fatherName = "Other"))
        assertNotEquals(a, a.copy(motherName = "Other"))
        assertNotEquals(a, a.copy(familyHeadName = "Other"))
        assertNotEquals(a, a.copy(spouseName = "Other"))
        assertNotEquals(a, a.copy(rchId = "Other"))
        assertNotEquals(a, a.copy(hrpStatus = false))
        assertNotEquals(a, a.copy(syncState = SyncState.SYNCED))
        assertNotEquals(a, a.copy(isConsent = false))
        assertNotEquals(a, a.copy(isSpouseAdded = false))
        assertNotEquals(a, a.copy(isChildrenAdded = false))
        assertNotEquals(a, a.copy(isMarried = false))
        assertNotEquals(a, a.copy(doYouHavechildren = false))
        assertNotEquals(a, a.copy(noOfChildren = 999))
        assertNotEquals(a, a.copy(noOfAliveChildren = 999))
        assertNotEquals(a, a.copy(isDeactivate = true))
    }

    @Test fun `BenBasicDomain dobString formats dob as dd-MM-yyyy`() {
        val a = benBasicDomain().copy(dob = 0L)
        assertTrue(a.dobString.matches(Regex("\\d{2}-\\d{2}-\\d{4}")))
    }

    @Test fun `BenBasicDomain getters and setters mutate and read back`() {
        val a = benBasicDomain()
        assertEquals(25, a.ageInt)
        assertEquals(AgeUnit.YEARS, a.ageUnit)
        assertTrue(a.hrpStatus)
        assertTrue(a.isConsent)

        a.isDeath = true
        assertTrue(a.isDeath)
        a.isDeathValue = "Yes"
        assertEquals("Yes", a.isDeathValue)
        a.dateOfDeath = "2024-05-05"
        assertEquals("2024-05-05", a.dateOfDeath)
        a.timeOfDeath = "12:00"
        assertEquals("12:00", a.timeOfDeath)
        a.reasonOfDeath = "illness"
        assertEquals("illness", a.reasonOfDeath)
        a.reasonOfDeathId = 9
        assertEquals(9, a.reasonOfDeathId)
        a.placeOfDeath = "hospital"
        assertEquals("hospital", a.placeOfDeath)
        a.placeOfDeathId = 8
        assertEquals(8, a.placeOfDeathId)
        a.otherPlaceOfDeath = "home"
        assertEquals("home", a.otherPlaceOfDeath)
        a.reproductiveStatusId = 7
        assertEquals(7, a.reproductiveStatusId)
        a.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, a.syncState)
        a.isSpouseAdded = false
        assertFalse(a.isSpouseAdded)
        a.isChildrenAdded = false
        assertFalse(a.isChildrenAdded)
        a.isMarried = false
        assertFalse(a.isMarried)
        a.doYouHavechildren = false
        assertFalse(a.doYouHavechildren)
        a.noOfChildren = 5
        assertEquals(5, a.noOfChildren)
        a.noOfAliveChildren = 4
        assertEquals(4, a.noOfAliveChildren)
        a.isDeactivate = true
        assertTrue(a.isDeactivate)
    }

    @Test fun `BenChildCount holds benId and childCount`() {
        val a = BenChildCount(benId = 5L, childCount = 3)
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(benId = 999L))
        assertNotEquals(a, a.copy(childCount = 999))
        assertTrue(a.toString().contains("BenChildCount"))
    }

    private fun benBasicDomainForForm() = BenBasicDomainForForm(
        benId = 1L,
        hhId = 2L,
        isDeath = false,
        isDeathValue = "No",
        dateOfDeath = "2024-01-01",
        timeOfDeath = "10:00",
        reasonOfDeath = "reason",
        reasonOfDeathId = 1,
        placeOfDeath = "place",
        placeOfDeathId = 2,
        otherPlaceOfDeath = "other",
        regDate = "2024-01-02",
        benName = "Jane",
        benSurname = "Doe",
        gender = "FEMALE",
        dob = 500_000_000_000L,
        ageInt = 25,
        ageUnit = AgeUnit.YEARS,
        age = "25 YEARS",
        mobileNo = "9998887776",
        fatherName = "Bob",
        spouseName = "Spouse",
        familyHeadName = "Head",
        lastMenstrualPeriod = "2024-01-01",
        edd = "2024-09-01",
        rchId = "RCH1",
        hrpStatus = true,
        form1Filled = true,
        form2Filled = true,
        form3Filled = true,
        form1Enabled = true,
        form2Enabled = true,
        form3Enabled = true,
        formsFilled = 2,
        syncState = SyncState.UNSYNCED,
        isConsent = true
    )

    @Test fun `BenBasicDomainForForm copy and equality with all fields set`() {
        val a = benBasicDomainForForm()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("BenBasicDomainForForm"))

        assertNotEquals(a, a.copy(benId = 999L))
        assertNotEquals(a, a.copy(hhId = 999L))
        assertNotEquals(a, a.copy(isDeath = true))
        assertNotEquals(a, a.copy(isDeathValue = "Other"))
        assertNotEquals(a, a.copy(dateOfDeath = "Other"))
        assertNotEquals(a, a.copy(timeOfDeath = "Other"))
        assertNotEquals(a, a.copy(reasonOfDeath = "Other"))
        assertNotEquals(a, a.copy(reasonOfDeathId = 999))
        assertNotEquals(a, a.copy(placeOfDeath = "Other"))
        assertNotEquals(a, a.copy(placeOfDeathId = 999))
        assertNotEquals(a, a.copy(otherPlaceOfDeath = "Other"))
        assertNotEquals(a, a.copy(regDate = "Other"))
        assertNotEquals(a, a.copy(benName = "Other"))
        assertNotEquals(a, a.copy(benSurname = "Other"))
        assertNotEquals(a, a.copy(gender = "MALE"))
        assertNotEquals(a, a.copy(dob = 999L))
        assertNotEquals(a, a.copy(ageInt = 999))
        assertNotEquals(a, a.copy(ageUnit = AgeUnit.DAYS))
        assertNotEquals(a, a.copy(age = "Other"))
        assertNotEquals(a, a.copy(mobileNo = "0000000000"))
        assertNotEquals(a, a.copy(fatherName = "Other"))
        assertNotEquals(a, a.copy(spouseName = "Other"))
        assertNotEquals(a, a.copy(familyHeadName = "Other"))
        assertNotEquals(a, a.copy(lastMenstrualPeriod = "Other"))
        assertNotEquals(a, a.copy(edd = "Other"))
        assertNotEquals(a, a.copy(rchId = "Other"))
        assertNotEquals(a, a.copy(hrpStatus = false))
        assertNotEquals(a, a.copy(form1Filled = false))
        assertNotEquals(a, a.copy(form2Filled = false))
        assertNotEquals(a, a.copy(form3Filled = false))
        assertNotEquals(a, a.copy(form1Enabled = false))
        assertNotEquals(a, a.copy(form2Enabled = false))
        assertNotEquals(a, a.copy(form3Enabled = false))
        assertNotEquals(a, a.copy(formsFilled = 999))
        assertNotEquals(a, a.copy(syncState = SyncState.SYNCED))
        assertNotEquals(a, a.copy(isConsent = false))
    }

    @Test fun `BenBasicDomainForForm getters and setters mutate and read back`() {
        val f = benBasicDomainForForm()
        assertFalse(f.isDeath)
        assertEquals("No", f.isDeathValue)
        assertEquals("2024-01-02", f.regDate)
        assertEquals("FEMALE", f.gender)
        assertEquals(500_000_000_000L, f.dob)
        assertEquals(25, f.ageInt)
        assertEquals(AgeUnit.YEARS, f.ageUnit)
        assertEquals("25 YEARS", f.age)
        assertEquals("9998887776", f.mobileNo)
        assertTrue(f.hrpStatus)
        assertTrue(f.form3Filled)
        assertTrue(f.form3Enabled)
        assertEquals(2, f.formsFilled)

        f.isDeath = true
        assertTrue(f.isDeath)
        f.isDeathValue = "Yes"
        assertEquals("Yes", f.isDeathValue)
        f.dateOfDeath = "2024-06-06"
        assertEquals("2024-06-06", f.dateOfDeath)
        f.timeOfDeath = "13:00"
        assertEquals("13:00", f.timeOfDeath)
        f.reasonOfDeath = "illness"
        assertEquals("illness", f.reasonOfDeath)
        f.reasonOfDeathId = 3
        assertEquals(3, f.reasonOfDeathId)
        f.placeOfDeath = "hospital"
        assertEquals("hospital", f.placeOfDeath)
        f.placeOfDeathId = 4
        assertEquals(4, f.placeOfDeathId)
        f.otherPlaceOfDeath = "home"
        assertEquals("home", f.otherPlaceOfDeath)
        f.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, f.syncState)
    }
}
