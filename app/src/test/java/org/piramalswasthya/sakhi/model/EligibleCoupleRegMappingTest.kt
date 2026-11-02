package org.piramalswasthya.sakhi.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.ImageUtils

class EligibleCoupleRegMappingTest {

    private lateinit var context: Context

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

    private fun cache() = EligibleCoupleRegCache(
        benId = 33L,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `asPostModel maps benId`() {
        assertEquals(33L, cache().asPostModel(context).benId)
    }

    @Test fun `asPostModel maps createdBy and updatedBy`() {
        val post = cache().asPostModel(context)
        assertEquals("creator", post.createdBy)
        assertEquals("modifier", post.updatedBy)
    }

    @Test fun `asPostModel default child counts are zero`() {
        val post = cache().asPostModel(context)
        assertEquals(0, post.numChildren)
        assertEquals(0, post.numLiveChildren)
        assertEquals(0, post.numMaleChildren)
        assertEquals(0, post.numFemaleChildren)
    }

    @Test fun `asPostModel default kit handed over is false`() {
        assertEquals(false, cache().asPostModel(context).isKitHandedOver)
    }

    @Test fun `asPostModel uses encoded kit photos from ImageUtils`() {
        val post = cache().asPostModel(context)
        assertEquals("encoded", post.kitPhoto1)
        assertEquals("encoded", post.kitPhoto2)
    }

    @Test fun `asPostModel formats registration date`() {
        assertNotNull(cache().asPostModel(context).dateOfReg)
    }

    @Test fun `asPostModel maps populated child counts`() {
        val post = cache().copy(
            noOfChildren = 3,
            noOfLiveChildren = 2,
            noOfMaleChildren = 1,
            noOfFemaleChildren = 1
        ).asPostModel(context)
        assertEquals(3, post.numChildren)
        assertEquals(2, post.numLiveChildren)
        assertEquals(1, post.numMaleChildren)
        assertEquals(1, post.numFemaleChildren)
    }

    @Test fun `asPostModel maps child ages and gender`() {
        val post = cache().copy(age1 = 5, gender1 = Gender.FEMALE).asPostModel(context)
        assertEquals(5, post.age1)
        assertEquals(Gender.FEMALE, post.gender1)
    }

    @Test fun `asPostModel maps bank details`() {
        val post = cache().copy(
            bankAccount = 12345L,
            bankName = "SBI",
            branchName = "Main",
            ifsc = "SBIN0001"
        ).asPostModel(context)
        assertEquals(12345L, post.bankAccount)
        assertEquals("SBI", post.bankName)
        assertEquals("Main", post.branchName)
        assertEquals("SBIN0001", post.ifsc)
    }

    private fun fullyPopulated() = EligibleCoupleRegCache(
        id = 7,
        benId = 33L,
        dateOfReg = 1_600_000_000_000L,
        bankAccount = 998877L,
        bankName = "SBI",
        branchName = "Main",
        ifsc = "SBIN0001",
        lmpDate = 1_650_000_000_000L,
        noOfChildren = 9,
        noOfLiveChildren = 8,
        noOfMaleChildren = 5,
        noOfFemaleChildren = 3,
        isRegistered = true,
        dob1 = 1_000_000_000_000L, age1 = 20, gender1 = Gender.FEMALE, marriageFirstChildGap = 1,
        dob2 = 1_010_000_000_000L, age2 = 18, gender2 = Gender.MALE, firstAndSecondChildGap = 2,
        dob3 = 1_020_000_000_000L, age3 = 16, gender3 = Gender.FEMALE, secondAndThirdChildGap = 2,
        dob4 = 1_030_000_000_000L, age4 = 14, gender4 = Gender.MALE, thirdAndFourthChildGap = 2,
        dob5 = 1_040_000_000_000L, age5 = 12, gender5 = Gender.FEMALE, fourthAndFifthChildGap = 2,
        dob6 = 1_050_000_000_000L, age6 = 10, gender6 = Gender.MALE, fifthANdSixthChildGap = 2,
        dob7 = 1_060_000_000_000L, age7 = 8, gender7 = Gender.FEMALE, sixthAndSeventhChildGap = 2,
        dob8 = 1_070_000_000_000L, age8 = 6, gender8 = Gender.MALE, seventhAndEighthChildGap = 2,
        dob9 = 1_080_000_000_000L, age9 = 4, gender9 = Gender.FEMALE, eighthAndNinthChildGap = 2,
        processed = "Y",
        createdBy = "creator",
        createdDate = 1_600_000_000_000L,
        updatedBy = "modifier",
        updatedDate = 1_600_000_000_000L,
        lmp_date = 1_650_000_000_000L,
        isKitHandedOver = true,
        kitHandedOverDate = 1_650_100_000_000L,
        kitPhoto1 = "photo1",
        kitPhoto2 = "photo2",
        syncState = SyncState.SYNCED
    )

    @Test fun `asPostModel formats lmpDate when greater than zero`() {
        val post = cache().copy(lmpDate = 1_650_000_000_000L).asPostModel(context)
        assertNotEquals("", post.lmpDate)
        assertNotNull(post.lmpDate)
    }

    @Test fun `asPostModel default lmpDate maps to empty string`() {
        val post = cache().copy(lmpDate = 0L).asPostModel(context)
        assertEquals("", post.lmpDate)
    }

    @Test fun `asPostModel falls back to empty string when kit photo encoding is null`() {
        every { ImageUtils.getEncodedStringForBenImage(any(), any()) } returns null
        val post = cache().asPostModel(context)
        assertEquals("", post.kitPhoto1)
        assertEquals("", post.kitPhoto2)
    }

    @Test fun `asPostModel maps kitHandedOverDate when populated`() {
        val post = cache().copy(kitHandedOverDate = 1_650_100_000_000L).asPostModel(context)
        assertNotEquals("null", post.kitHandedOverDate)
    }

    @Test fun `asPostModel maps isKitHandedOver true`() {
        val post = cache().copy(isKitHandedOver = true).asPostModel(context)
        assertTrue(post.isKitHandedOver)
    }

    @Test fun `asPostModel throws when isKitHandedOver is null`() {
        val withNullFlag = cache().copy(isKitHandedOver = null)
        assertThrows(NullPointerException::class.java) {
            withNullFlag.asPostModel(context)
        }
    }

    @Test fun `asPostModel maps dob age gender and gap for children two through nine`() {
        val post = fullyPopulated().asPostModel(context)

        assertEquals(18, post.age2)
        assertEquals(Gender.MALE, post.gender2)
        assertEquals(2, post.firstAndSecondChildGap)

        assertEquals(16, post.age3)
        assertEquals(Gender.FEMALE, post.gender3)
        assertEquals(2, post.secondAndThirdChildGap)

        assertEquals(14, post.age4)
        assertEquals(Gender.MALE, post.gender4)
        assertEquals(2, post.thirdAndFourthChildGap)

        assertEquals(12, post.age5)
        assertEquals(Gender.FEMALE, post.gender5)
        assertEquals(2, post.fourthAndFifthChildGap)

        assertEquals(10, post.age6)
        assertEquals(Gender.MALE, post.gender6)
        assertEquals(2, post.fifthANdSixthChildGap)

        assertEquals(8, post.age7)
        assertEquals(Gender.FEMALE, post.gender7)
        assertEquals(2, post.sixthAndSeventhChildGap)

        assertEquals(6, post.age8)
        assertEquals(Gender.MALE, post.gender8)
        assertEquals(2, post.seventhAndEighthChildGap)

        assertEquals(4, post.age9)
        assertEquals(Gender.FEMALE, post.gender9)
        assertEquals(2, post.eighthAndNinthChildGap)

        assertNotNull(post.dob2)
        assertNotNull(post.dob9)
    }

    @Test fun `equals is true when all fields match`() {
        assertEquals(fullyPopulated(), fullyPopulated())
    }

    @Test fun `equals is false when a field differs`() {
        assertNotEquals(fullyPopulated(), fullyPopulated().copy(bankName = "Other Bank"))
    }

    @Test fun `equals is false against null and a different type`() {
        val cache = fullyPopulated()
        assertFalse(cache.equals(null))
        assertFalse(cache.equals("not a cache"))
        assertTrue(cache.equals(cache))
    }

    @Test fun `hashCode is stable and equal for equal objects`() {
        val first = fullyPopulated()
        val second = fullyPopulated()
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test fun `hashCode does not throw when all optional fields are null`() {
        val minimal = cache()
        assertNotNull(minimal.hashCode())
    }

    @Test fun `toString contains key field values`() {
        val text = fullyPopulated().toString()
        assertTrue(text.contains("EligibleCoupleRegCache"))
        assertTrue(text.contains("SBI"))
        assertTrue(text.contains("SBIN0001"))
    }

    @Test fun `copy without arguments produces an equal but distinct instance`() {
        val original = fullyPopulated()
        val copied = original.copy()
        assertEquals(original, copied)
        assertEquals(original.hashCode(), copied.hashCode())
    }

    @Test fun `asPostModel maps gender1 as MALE`() {
        val post = cache().copy(gender1 = Gender.MALE, age1 = 30).asPostModel(context)
        assertEquals(Gender.MALE, post.gender1)
        assertEquals(30, post.age1)
    }

    @Test fun `asPostModel maps gender1 as TRANSGENDER`() {
        val post = cache().copy(gender1 = Gender.TRANSGENDER).asPostModel(context)
        assertEquals(Gender.TRANSGENDER, post.gender1)
    }

    @Test fun `asPostModel maps gender1 and age1 as null when unset`() {
        val post = cache().asPostModel(context)
        assertEquals(null, post.gender1)
        assertEquals(null, post.age1)
    }

    @Test fun `asPostModel maps gender5 as TRANSGENDER`() {
        val post = cache().copy(gender5 = Gender.TRANSGENDER, age5 = 9).asPostModel(context)
        assertEquals(Gender.TRANSGENDER, post.gender5)
        assertEquals(9, post.age5)
    }

    @Test fun `asPostModel maps dob1 when populated`() {
        val post = cache().copy(dob1 = 1_000_000_000_000L).asPostModel(context)
        assertNotNull(post.dob1)
    }

    @Test fun `asPostModel maps dob1 as null when unset`() {
        val post = cache().asPostModel(context)
        assertEquals(null, post.dob1)
    }

    @Test fun `asPostModel maps gap fields as null when unset`() {
        val post = cache().asPostModel(context)
        assertEquals(null, post.marriageFirstChildGap)
        assertEquals(null, post.firstAndSecondChildGap)
        assertEquals(null, post.eighthAndNinthChildGap)
    }

    @Test fun `asPostModel maps null bank details as null`() {
        val post = cache().asPostModel(context)
        assertEquals(null, post.bankAccount)
        assertEquals(null, post.bankName)
        assertEquals(null, post.branchName)
        assertEquals(null, post.ifsc)
    }

    @Test fun `asPostModel maps kitHandedOverDate as literal null string when unset`() {
        val post = cache().asPostModel(context)
        assertEquals("null", post.kitHandedOverDate)
    }

    @Test fun `asPostModel maps isKitHandedOver false explicitly`() {
        val post = cache().copy(isKitHandedOver = false).asPostModel(context)
        assertFalse(post.isKitHandedOver)
    }

    @Test fun `asPostModel maps dob9 and gender9 when populated as MALE`() {
        val post = cache().copy(dob9 = 1_080_000_000_000L, age9 = 3, gender9 = Gender.MALE)
            .asPostModel(context)
        assertNotNull(post.dob9)
        assertEquals(3, post.age9)
        assertEquals(Gender.MALE, post.gender9)
    }
}
