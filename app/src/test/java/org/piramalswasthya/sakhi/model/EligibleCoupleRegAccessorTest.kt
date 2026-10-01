package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class EligibleCoupleRegAccessorTest {

    private fun base() = EligibleCoupleRegCache(
        benId = 91L,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test
    fun `identity and sync fields are readable`() {
        val cache = base()
        assertEquals(0, cache.id)
        assertEquals(91L, cache.benId)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
        assertEquals("creator", cache.createdBy)
        assertEquals("modifier", cache.updatedBy)
        assertTrue(cache.updatedDate > 0L)
        assertTrue(cache.createdDate > 0L)
        assertEquals("N", cache.processed)
    }

    @Test
    fun `registration and bank fields round trip through setters`() {
        val cache = base()
        cache.dateOfReg = 1_700_000_000_000L
        cache.bankAccount = 555444333L
        cache.bankName = "Canara"
        cache.branchName = "Central"
        cache.ifsc = "CNRB0009"
        cache.lmpDate = 1_690_000_000_000L
        cache.lmp_date = 1_691_000_000_000L
        cache.isRegistered = false
        cache.processed = "U"
        cache.createdBy = "newCreator"
        cache.createdDate = 1_600_000_000_000L
        cache.updatedBy = "newModifier"
        cache.syncState = SyncState.SYNCED

        assertEquals(1_700_000_000_000L, cache.dateOfReg)
        assertEquals(555444333L, cache.bankAccount)
        assertEquals("Canara", cache.bankName)
        assertEquals("Central", cache.branchName)
        assertEquals("CNRB0009", cache.ifsc)
        assertEquals(1_690_000_000_000L, cache.lmpDate)
        assertEquals(1_691_000_000_000L, cache.lmp_date)
        assertFalse(cache.isRegistered)
        assertEquals("U", cache.processed)
        assertEquals("newCreator", cache.createdBy)
        assertEquals(1_600_000_000_000L, cache.createdDate)
        assertEquals("newModifier", cache.updatedBy)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `child count fields round trip through setters`() {
        val cache = base()
        cache.noOfChildren = 9
        cache.noOfLiveChildren = 7
        cache.noOfMaleChildren = 4
        cache.noOfFemaleChildren = 3

        assertEquals(9, cache.noOfChildren)
        assertEquals(7, cache.noOfLiveChildren)
        assertEquals(4, cache.noOfMaleChildren)
        assertEquals(3, cache.noOfFemaleChildren)
    }

    @Test
    fun `kit fields round trip through setters`() {
        val cache = base()
        cache.isKitHandedOver = true
        cache.kitHandedOverDate = 1_710_000_000_000L
        cache.kitPhoto1 = "p1"
        cache.kitPhoto2 = "p2"

        assertEquals(true, cache.isKitHandedOver)
        assertEquals(1_710_000_000_000L, cache.kitHandedOverDate)
        assertEquals("p1", cache.kitPhoto1)
        assertEquals("p2", cache.kitPhoto2)
    }

    @Test
    fun `first four children fields round trip through setters`() {
        val cache = base()
        cache.dob1 = 1L
        cache.age1 = 11
        cache.gender1 = Gender.FEMALE
        cache.marriageFirstChildGap = 1
        cache.dob2 = 2L
        cache.age2 = 12
        cache.gender2 = Gender.MALE
        cache.firstAndSecondChildGap = 2
        cache.dob3 = 3L
        cache.age3 = 13
        cache.gender3 = Gender.TRANSGENDER
        cache.secondAndThirdChildGap = 3
        cache.dob4 = 4L
        cache.age4 = 14
        cache.gender4 = Gender.FEMALE
        cache.thirdAndFourthChildGap = 4

        assertEquals(1L, cache.dob1)
        assertEquals(11, cache.age1)
        assertEquals(Gender.FEMALE, cache.gender1)
        assertEquals(1, cache.marriageFirstChildGap)
        assertEquals(2L, cache.dob2)
        assertEquals(12, cache.age2)
        assertEquals(Gender.MALE, cache.gender2)
        assertEquals(2, cache.firstAndSecondChildGap)
        assertEquals(3L, cache.dob3)
        assertEquals(13, cache.age3)
        assertEquals(Gender.TRANSGENDER, cache.gender3)
        assertEquals(3, cache.secondAndThirdChildGap)
        assertEquals(4L, cache.dob4)
        assertEquals(14, cache.age4)
        assertEquals(Gender.FEMALE, cache.gender4)
        assertEquals(4, cache.thirdAndFourthChildGap)
    }

    @Test
    fun `fifth to ninth children fields round trip through setters`() {
        val cache = base()
        cache.dob5 = 5L
        cache.age5 = 15
        cache.gender5 = Gender.MALE
        cache.fourthAndFifthChildGap = 5
        cache.dob6 = 6L
        cache.age6 = 16
        cache.gender6 = Gender.FEMALE
        cache.fifthANdSixthChildGap = 6
        cache.dob7 = 7L
        cache.age7 = 17
        cache.gender7 = Gender.MALE
        cache.sixthAndSeventhChildGap = 7
        cache.dob8 = 8L
        cache.age8 = 18
        cache.gender8 = Gender.TRANSGENDER
        cache.seventhAndEighthChildGap = 8
        cache.dob9 = 9L
        cache.age9 = 19
        cache.gender9 = Gender.FEMALE
        cache.eighthAndNinthChildGap = 9

        assertEquals(5L, cache.dob5)
        assertEquals(15, cache.age5)
        assertEquals(Gender.MALE, cache.gender5)
        assertEquals(5, cache.fourthAndFifthChildGap)
        assertEquals(6L, cache.dob6)
        assertEquals(16, cache.age6)
        assertEquals(Gender.FEMALE, cache.gender6)
        assertEquals(6, cache.fifthANdSixthChildGap)
        assertEquals(7L, cache.dob7)
        assertEquals(17, cache.age7)
        assertEquals(Gender.MALE, cache.gender7)
        assertEquals(7, cache.sixthAndSeventhChildGap)
        assertEquals(8L, cache.dob8)
        assertEquals(18, cache.age8)
        assertEquals(Gender.TRANSGENDER, cache.gender8)
        assertEquals(8, cache.seventhAndEighthChildGap)
        assertEquals(9L, cache.dob9)
        assertEquals(19, cache.age9)
        assertEquals(Gender.FEMALE, cache.gender9)
        assertEquals(9, cache.eighthAndNinthChildGap)
    }

    @Test
    fun `nullable fields default to null`() {
        val cache = base()
        assertNull(cache.bankAccount)
        assertNull(cache.bankName)
        assertNull(cache.branchName)
        assertNull(cache.ifsc)
        assertNull(cache.dob1)
        assertNull(cache.age1)
        assertNull(cache.gender1)
        assertNull(cache.marriageFirstChildGap)
        assertNull(cache.dob5)
        assertNull(cache.gender9)
        assertNull(cache.kitHandedOverDate)
        assertNull(cache.kitPhoto1)
        assertNull(cache.kitPhoto2)
        assertEquals(false, cache.isKitHandedOver)
        assertEquals(0L, cache.dateOfReg)
        assertEquals(0L, cache.lmpDate)
        assertEquals(0L, cache.lmp_date)
        assertTrue(cache.isRegistered)
    }

    private fun benDomain() = BenBasicDomain(
        benId = 91L,
        hhId = 5L,
        reproductiveStatusId = 1,
        regDate = "01-01-2024",
        benName = "Asha",
        benSurname = "Devi",
        gender = "FEMALE",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        rchId = "RCH1",
        hrpStatus = false,
        syncState = SyncState.UNSYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = true
    )

    @Test
    fun `BenWithEcrDomain exposes ecr and child count`() {
        val ecr = base()
        val domain = BenWithEcrDomain(ben = benDomain(), ecr = ecr, childCount = 3)
        assertEquals(3, domain.childCount)
        assertEquals(ecr, domain.ecr)
        assertEquals("Asha", domain.ben.benName)
    }

    @Test
    fun `BenWithEcrDomain defaults child count to zero`() {
        val domain = BenWithEcrDomain(ben = benDomain(), ecr = null)
        assertEquals(0, domain.childCount)
        assertNull(domain.ecr)
    }
}
