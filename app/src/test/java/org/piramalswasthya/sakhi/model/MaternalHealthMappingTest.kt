package org.piramalswasthya.sakhi.model

import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in MaternalHealth.kt:
 *  - PregnantWomenVisitCache.asDomainModel()
 *  - PregnantWomanRegistrationCache.asPwrPost()
 *  - PwrPost.toPwrCache()
 *  - PregnantWomanAncCache.asPostModel()
 *  - ANCPost.toAncCache(context)
 *
 * Skipped: BenWithPwrCache / BenWithAncVisitCache mappers (require heavy
 * BenBasicCache + related-entity graphs) and file/multipart handling.
 */
class MaternalHealthMappingTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ---------------------------------------------------------------
    // PregnantWomenVisitCache.asDomainModel()
    // ---------------------------------------------------------------

    @Test
    fun `PregnantWomenVisitCache asDomainModel maps core fields`() {
        val cache = PregnantWomenVisitCache(
            benId = 11L,
            name = "Asha",
            dob = 0L,
            mobileNo = 9876543210L,
            spouseName = "Ravi",
            lmp = System.currentTimeMillis()
        )

        val domain = cache.asDomainModel()

        assertEquals(11L, domain.benId)
        assertEquals("Asha", domain.name)
        assertEquals("Ravi", domain.spouseName)
        assertEquals("9876543210", domain.mobileNo)
        // rchId / familyHeadName default to "Not Available"
        assertEquals("Not Available", domain.rchId)
        assertEquals("Not Available", domain.familyHeadName)
    }

    @Test
    fun `PregnantWomenVisitCache asDomainModel keeps provided rchId and familyHeadName`() {
        val cache = PregnantWomenVisitCache(
            benId = 1L,
            name = "N",
            dob = 0L,
            mobileNo = 1L,
            rchId = "RCH-1",
            familyHeadName = "Head",
            spouseName = "S",
            lmp = System.currentTimeMillis()
        )

        val domain = cache.asDomainModel()

        assertEquals("RCH-1", domain.rchId)
        assertEquals("Head", domain.familyHeadName)
    }

    // ---------------------------------------------------------------
    // PregnantWomanRegistrationCache.asPwrPost()
    // ---------------------------------------------------------------

    @Test
    fun `PregnantWomanRegistrationCache asPwrPost maps core fields`() {
        val cache = PregnantWomanRegistrationCache(
            id = 7L,
            benId = 22L,
            rchId = 12345L,
            mcpCardNumber = 6789L,
            lmpDate = 1_600_000_000_000L,
            bloodGroup = "O+",
            weight = 55,
            height = 160,
            is1st = true,
            active = true,
            isHrp = false,
            createdBy = "creator",
            updatedBy = "updater",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPwrPost()

        assertEquals(22L, post.benId)
        assertEquals(12345L, post.rchId)
        assertEquals(6789L, post.mcpCardId)
        assertEquals("O+", post.bloodGroup)
        assertEquals(55, post.weight)
        assertEquals(160, post.height)
        assertTrue(post.isActive)
        assertTrue(post.isFirstPregnancyTest)
        assertFalse(post.isHrpCase)
        assertEquals("creator", post.createdBy)
        assertEquals("updater", post.updatedBy)
        assertNotNull(post.lmpDate)
    }

    // ---------------------------------------------------------------
    // PwrPost.toPwrCache()
    // ---------------------------------------------------------------

    @Test
    fun `PwrPost toPwrCache maps core fields and sets synced`() {
        val post = PwrPost(
            benId = 33L,
            rchId = 555L,
            mcpCardId = 999L,
            weight = 60,
            height = 170,
            bloodGroup = "A+",
            isActive = true,
            isHrpCase = true,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toPwrCache()

        assertEquals(33L, cache.benId)
        assertEquals(555L, cache.rchId)
        assertEquals(999L, cache.mcpCardNumber)
        assertEquals(60, cache.weight)
        assertEquals(170, cache.height)
        assertEquals("A+", cache.bloodGroup)
        assertTrue(cache.active)
        assertTrue(cache.isHrp)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertEquals("c", cache.createdBy)
        assertEquals("u", cache.updatedBy)
    }

    // ---------------------------------------------------------------
    // PregnantWomanAncCache.asPostModel()
    // ---------------------------------------------------------------

    @Test
    fun `PregnantWomanAncCache asPostModel maps core fields`() {
        val cache = PregnantWomanAncCache(
            benId = 44L,
            visitNumber = 2,
            weekOfPregnancy = 12,
            weight = 58,
            bpSystolic = 120,
            bpDiastolic = 80,
            pulseRate = "72",
            hb = 11.5,
            urineAlbumin = "Present",
            randomBloodSugarTest = "Done",
            numFolicAcidTabGiven = 30,
            numIfaAcidTabGiven = 45,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED,
            frontFilePath = null,
            backFilePath = null
        )

        val post = cache.asPostModel()

        assertEquals(44L, post.benId)
        assertEquals(2, post.ancVisit)
        assertEquals(12, post.weekOfPregnancy)
        assertEquals(58, post.weightOfPW)
        assertEquals(120, post.bpSystolic)
        assertEquals(80, post.bpDiastolic)
        assertEquals(72, post.pulseRate)
        assertEquals(11.5, post.hb!!, 0.0001)
        assertTrue(post.urineAlbuminPresent!!)
        assertTrue(post.bloodSugarTestDone!!)
        assertEquals(30, post.folicAcidTabs)
        assertEquals(45, post.ifaTabs)
        assertTrue(post.isActive)
    }

    @Test
    fun `PregnantWomanAncCache asPostModel handles null pulseRate string`() {
        val cache = PregnantWomanAncCache(
            benId = 1L,
            visitNumber = 1,
            pulseRate = "null",
            urineAlbumin = "Absent",
            randomBloodSugarTest = "Not Done",
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED,
            frontFilePath = null,
            backFilePath = null
        )

        val post = cache.asPostModel()

        assertNull(post.pulseRate)
        assertFalse(post.urineAlbuminPresent!!)
        assertFalse(post.bloodSugarTestDone!!)
    }

    // ---------------------------------------------------------------
    // ANCPost.toAncCache(context)
    // ---------------------------------------------------------------

    @Test
    fun `ANCPost toAncCache maps core fields and sets synced`() {
        val context = mockk<Context>(relaxed = true)
        val post = ANCPost(
            benId = 55L,
            isActive = true,
            ancVisit = 3,
            weekOfPregnancy = 20,
            weightOfPW = 62,
            bpSystolic = 118,
            bpDiastolic = 78,
            hb = 12.0,
            urineAlbuminPresent = true,
            bloodSugarTestDone = true,
            folicAcidTabs = 10,
            ifaTabs = 20,
            createdBy = "c",
            updatedBy = "u",
            frontFilePath = null,
            backFilePath = null
        )

        val cache = post.toAncCache(context)

        assertEquals(55L, cache.benId)
        assertEquals(3, cache.visitNumber)
        assertEquals(62, cache.weight)
        assertEquals("Present", cache.urineAlbumin)
        assertEquals("Done", cache.randomBloodSugarTest)
        assertEquals(10, cache.numFolicAcidTabGiven)
        assertEquals(20, cache.numIfaAcidTabGiven)
        assertFalse(cache.isAborted)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `ANCPost toAncCache maps absent urine and undone sugar`() {
        val context = mockk<Context>(relaxed = true)
        val post = ANCPost(
            benId = 1L,
            isActive = true,
            ancVisit = 1,
            urineAlbuminPresent = false,
            bloodSugarTestDone = false,
            createdBy = "c",
            updatedBy = "u",
            frontFilePath = null,
            backFilePath = null
        )

        val cache = post.toAncCache(context)

        assertEquals("Absent", cache.urineAlbumin)
        assertEquals("Not Done", cache.randomBloodSugarTest)
    }

    // ---------------------------------------------------------------
    // Branch-variant coverage: flips boolean/string discriminators
    // (isActive / is1st / isHrp / urineAlbumin / bloodSugar / pulseRate).
    // ---------------------------------------------------------------

    @Test
    fun `asPwrPost with first-false hrp-true inactive`() {
        val cache = PregnantWomanRegistrationCache(
            id = 8L,
            benId = 23L,
            rchId = 1L,
            mcpCardNumber = 2L,
            lmpDate = 1_600_000_000_000L,
            bloodGroup = "B+",
            weight = 50,
            height = 150,
            is1st = false,
            active = false,
            isHrp = true,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )
        val post = cache.asPwrPost()
        assertEquals(23L, post.benId)
        assertFalse(post.isActive)
        assertFalse(post.isFirstPregnancyTest)
        assertTrue(post.isHrpCase)
    }

    @Test
    fun `toPwrCache inactive non-hrp`() {
        val post = PwrPost(
            benId = 34L,
            rchId = 5L,
            mcpCardId = 9L,
            weight = 61,
            height = 171,
            bloodGroup = "AB+",
            isActive = false,
            isHrpCase = false,
            createdBy = "c",
            updatedBy = "u"
        )
        val cache = post.toPwrCache()
        assertEquals(34L, cache.benId)
        assertFalse(cache.active)
        assertFalse(cache.isHrp)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `asPostModel with present urine and undone sugar`() {
        val cache = PregnantWomanAncCache(
            benId = 2L,
            visitNumber = 3,
            weekOfPregnancy = 30,
            weight = 60,
            pulseRate = "88",
            hb = null,
            urineAlbumin = "Present",
            randomBloodSugarTest = "Not Done",
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED,
            frontFilePath = null,
            backFilePath = null
        )
        val post = cache.asPostModel()
        assertEquals(2L, post.benId)
        assertEquals(88, post.pulseRate)
        assertTrue(post.urineAlbuminPresent!!)
        assertFalse(post.bloodSugarTestDone!!)
    }

    @Test
    fun `toAncCache with present urine and done sugar`() {
        val context = mockk<Context>(relaxed = true)
        val post = ANCPost(
            benId = 3L,
            isActive = true,
            ancVisit = 2,
            weekOfPregnancy = 16,
            weightOfPW = 59,
            urineAlbuminPresent = true,
            bloodSugarTestDone = true,
            folicAcidTabs = 5,
            ifaTabs = 8,
            createdBy = "c",
            updatedBy = "u",
            frontFilePath = null,
            backFilePath = null
        )
        val cache = post.toAncCache(context)
        assertEquals("Present", cache.urineAlbumin)
        assertEquals("Done", cache.randomBloodSugarTest)
        assertNotNull(cache)
    }
}
