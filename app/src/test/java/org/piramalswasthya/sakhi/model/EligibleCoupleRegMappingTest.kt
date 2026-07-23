package org.piramalswasthya.sakhi.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
