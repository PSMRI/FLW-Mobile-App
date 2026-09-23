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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import java.util.concurrent.TimeUnit

/**
 * Tests for the pure mapper functions in MaternalHealth.kt:
 *  - PregnantWomenVisitCache.asDomainModel()
 *  - PregnantWomanRegistrationCache.asPwrPost()
 *  - PwrPost.toPwrCache()
 *  - PregnantWomanAncCache.asPostModel()
 *  - ANCPost.toAncCache(context)
 *  - BenWithPwrCache.asPwrDomainModel() / asBenBasicDomainModelForHRPPregAssessmentForm()
 *
 * Skipped: BenWithAncVisitCache mappers and file/multipart handling.
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

    @Test
    fun `PwrPost copy toString and equality`() {
        val post = PwrPost(
            id = 1L, benId = 33L, registrationDate = "2024-01-01", rchId = 555L, mcpCardId = 999L,
            lmpDate = "2024-01-02", bloodGroup = "A+", weight = 60, height = 170,
            rprTestResult = "Negative", dateOfRprTest = "2024-01-03", hivTestResult = "Negative",
            hbsAgTestResult = "Negative", dateOfHivTest = "2024-01-04", dateOfHbsAgTest = "2024-01-05",
            pastIllness = "None", otherPastIllness = "None2", isFirstPregnancyTest = true,
            numPrevPregnancy = 1, pregComplication = "None3", otherComplication = "None4",
            isRegistered = true, rhNegative = "No", homeDelivery = "No", badObstetric = "No",
            isHrpCase = true, assignedAsHrpBy = "asha", tdDose1Date = "2024-01-06",
            tdDose2Date = "2024-01-07", tdDoseBoosterDate = "2024-01-08", isActive = true,
            createdDate = "2024-01-09", createdBy = "c", updatedDate = "2024-01-10", updatedBy = "u"
        )
        val same = post.copy()
        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, post.copy(rchId = 1L))
        assertTrue(post.toString().contains("PwrPost"))

        assertNotEquals(post, post.copy(id = 999L))
        assertNotEquals(post, post.copy(benId = 999L))
        assertNotEquals(post, post.copy(registrationDate = "Other"))
        assertNotEquals(post, post.copy(mcpCardId = 111L))
        assertNotEquals(post, post.copy(lmpDate = "Other"))
        assertNotEquals(post, post.copy(bloodGroup = "Other"))
        assertNotEquals(post, post.copy(weight = 999))
        assertNotEquals(post, post.copy(height = 999))
        assertNotEquals(post, post.copy(rprTestResult = "Other"))
        assertNotEquals(post, post.copy(dateOfRprTest = "Other"))
        assertNotEquals(post, post.copy(hivTestResult = "Other"))
        assertNotEquals(post, post.copy(hbsAgTestResult = "Other"))
        assertNotEquals(post, post.copy(dateOfHivTest = "Other"))
        assertNotEquals(post, post.copy(dateOfHbsAgTest = "Other"))
        assertNotEquals(post, post.copy(pastIllness = "Other"))
        assertNotEquals(post, post.copy(otherPastIllness = "Other"))
        assertNotEquals(post, post.copy(isFirstPregnancyTest = false))
        assertNotEquals(post, post.copy(numPrevPregnancy = 999))
        assertNotEquals(post, post.copy(pregComplication = "Other"))
        assertNotEquals(post, post.copy(otherComplication = "Other"))
        assertNotEquals(post, post.copy(isRegistered = false))
        assertNotEquals(post, post.copy(rhNegative = "Other"))
        assertNotEquals(post, post.copy(homeDelivery = "Other"))
        assertNotEquals(post, post.copy(badObstetric = "Other"))
        assertNotEquals(post, post.copy(isHrpCase = false))
        assertNotEquals(post, post.copy(assignedAsHrpBy = "Other"))
        assertNotEquals(post, post.copy(tdDose1Date = "Other"))
        assertNotEquals(post, post.copy(tdDose2Date = "Other"))
        assertNotEquals(post, post.copy(tdDoseBoosterDate = "Other"))
        assertNotEquals(post, post.copy(isActive = false))
        assertNotEquals(post, post.copy(createdDate = "Other"))
        assertNotEquals(post, post.copy(createdBy = "Other"))
        assertNotEquals(post, post.copy(updatedDate = "Other"))
        assertNotEquals(post, post.copy(updatedBy = "Other"))
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

    // ===================================================================
    // Generated members / property accessors
    // ===================================================================

    private fun minimalAnc() = PregnantWomanAncCache(
        benId = 1L,
        visitNumber = 1,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.UNSYNCED,
        frontFilePath = null,
        backFilePath = null
    )

    private fun minimalPwr() = PregnantWomanRegistrationCache(
        benId = 1L,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.UNSYNCED
    )

    private fun sweep(obj: Any) {
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
        obj.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(obj) } }
    }

    @Test
    fun `PregnantWomanAncCache default constructor and accessors`() {
        val cache = minimalAnc()

        assertNotNull(cache)
        assertEquals(1L, cache.benId)
        assertEquals(1, cache.visitNumber)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
        assertNull(cache.frontFilePath)
        assertNull(cache.backFilePath)

        sweep(cache)
        assertEquals(cache, cache.copy())
        assertEquals(cache.hashCode(), cache.copy().hashCode())
        assertTrue(cache.toString().contains("PregnantWomanAncCache"))
    }

    @Test
    fun `PregnantWomanRegistrationCache default constructor and accessors`() {
        val cache = minimalPwr()

        assertNotNull(cache)
        assertEquals(1L, cache.benId)
        assertEquals("creator", cache.createdBy)
        assertEquals("updater", cache.updatedBy)
        assertEquals(SyncState.UNSYNCED, cache.syncState)

        sweep(cache)
        assertEquals(cache, cache.copy())
        assertEquals(cache.hashCode(), cache.copy().hashCode())
        assertTrue(cache.toString().contains("PregnantWomanRegistrationCache"))
    }

    // ---------------------------------------------------------------
    // BenWithPwrCache.asPwrDomainModel() / asBenBasicDomainModelForHRPPregAssessmentForm()
    // ---------------------------------------------------------------

    private fun benBasicCache(
        benId: Long = 1L,
        hhId: Long = 2L,
        regDate: Long = 1_600_000_000_000L,
        benName: String? = "Asha",
        benSurname: String? = "Devi",
        gender: Gender = Gender.FEMALE,
        dob: Long = 900_000_000_000L,
        relToHeadId: Int = 1,
        mobileNo: Long = 9876543210L,
        fatherName: String? = "Father",
        motherName: String? = "Mother",
        familyHeadName: String? = "Head",
        spouseName: String? = "Spouse",
        rchId: String? = "RCH-99",
        hrpStatus: Boolean = false,
        syncState: SyncState? = SyncState.SYNCED,
        reproductiveStatusId: Int = 1,
        lastMenstrualPeriod: Long? = 1_650_000_000_000L,
        hrppaFilled: Boolean = false,
        hrpmbpFilled: Boolean = false,
        hrppaSyncState: SyncState? = SyncState.SYNCED
    ) = BenBasicCache(
        benId = benId,
        hhId = hhId,
        regDate = regDate,
        benName = benName,
        benSurname = benSurname,
        gender = gender,
        dob = dob,
        relToHeadId = relToHeadId,
        mobileNo = mobileNo,
        fatherName = fatherName,
        motherName = motherName,
        familyHeadName = familyHeadName,
        spouseName = spouseName,
        rchId = rchId,
        hrpStatus = hrpStatus,
        syncState = syncState,
        reproductiveStatusId = reproductiveStatusId,
        lastMenstrualPeriod = lastMenstrualPeriod,
        isKid = false,
        immunizationStatus = false,
        villageId = 5,
        abhaId = null,
        isNewAbha = false,
        cbacFilled = false,
        cbacSyncState = SyncState.SYNCED,
        cdrFilled = false,
        cdrSyncState = SyncState.SYNCED,
        mdsrFilled = false,
        mdsrSyncState = SyncState.SYNCED,
        pmsmaSyncState = SyncState.SYNCED,
        pmsmaFilled = false,
        hbncFilled = false,
        hbycFilled = false,
        pwrFilled = false,
        pwrSyncState = SyncState.SYNCED,
        doSyncState = SyncState.SYNCED,
        irSyncState = SyncState.SYNCED,
        crSyncState = SyncState.SYNCED,
        ecrFilled = false,
        ectFilled = false,
        tbsnFilled = false,
        tbsnSyncState = SyncState.SYNCED,
        tbspFilled = false,
        tbspSyncState = SyncState.SYNCED,
        hrppaFilled = hrppaFilled,
        hrpnpaFilled = false,
        hrpmbpFilled = hrpmbpFilled,
        hrptFilled = false,
        hrptrackingDone = false,
        hrnptrackingDone = false,
        hrnptFilled = false,
        hrppaSyncState = hrppaSyncState,
        hrpnpaSyncState = SyncState.SYNCED,
        hrpmbpSyncState = SyncState.SYNCED,
        hrptSyncState = SyncState.SYNCED,
        hrnptSyncState = SyncState.SYNCED,
        isDelivered = false,
        pwHrp = false,
        irFilled = false,
        isMdsr = false,
        crFilled = false,
        doFilled = false,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = true
    )

    private fun pwrCache(benId: Long = 1L, active: Boolean = true) = PregnantWomanRegistrationCache(
        benId = benId,
        active = active,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.UNSYNCED
    )

    @Test
    fun `BenWithPwrCache asPwrDomainModel picks the active pwr and maps ben`() {
        val inactive = pwrCache(benId = 1L, active = false)
        val active = pwrCache(benId = 1L, active = true)
        val benWithPwr = BenWithPwrCache(ben = benBasicCache(benId = 1L, benName = "Asha"), pwr = listOf(inactive, active))

        val domain = benWithPwr.asPwrDomainModel()

        assertEquals(active, domain.pwr)
        assertEquals(1L, domain.ben.benId)
        assertEquals("Asha", domain.ben.benName)
    }

    @Test
    fun `BenWithPwrCache asPwrDomainModel returns null pwr when none active`() {
        val benWithPwr = BenWithPwrCache(
            ben = benBasicCache(),
            pwr = listOf(pwrCache(active = false), pwrCache(active = false))
        )

        val domain = benWithPwr.asPwrDomainModel()

        assertNull(domain.pwr)
    }

    @Test
    fun `BenWithPwrCache asPwrDomainModel returns null pwr when list is empty`() {
        val benWithPwr = BenWithPwrCache(ben = benBasicCache(), pwr = emptyList())

        val domain = benWithPwr.asPwrDomainModel()

        assertNull(domain.pwr)
    }

    @Test
    fun `BenWithPwrCache asBenBasicDomainModelForHRPPregAssessmentForm maps populated fields`() {
        val benWithPwr = BenWithPwrCache(
            ben = benBasicCache(
                familyHeadName = "Head",
                spouseName = "Spouse",
                rchId = "RCH-99",
                lastMenstrualPeriod = 1_650_000_000_000L,
                hrppaFilled = true,
                hrpmbpFilled = true,
                hrppaSyncState = SyncState.SYNCED
            ),
            pwr = emptyList()
        )

        val form = benWithPwr.asBenBasicDomainModelForHRPPregAssessmentForm()

        assertEquals(1L, form.benId)
        assertEquals("Head", form.familyHeadName)
        assertEquals("Spouse", form.spouseName)
        assertEquals("RCH-99", form.rchId)
        assertNotNull(form.lastMenstrualPeriod)
        assertNotNull(form.edd)
        assertTrue(form.form1Filled)
        assertTrue(form.form2Filled)
        assertTrue(form.form2Enabled)
        assertEquals(SyncState.SYNCED, form.syncState)
        assertFalse(form.isConsent)
    }

    @Test
    fun `BenWithPwrCache asBenBasicDomainModelForHRPPregAssessmentForm defaults for null optional fields`() {
        val benWithPwr = BenWithPwrCache(
            ben = benBasicCache(
                benSurname = null,
                familyHeadName = null,
                spouseName = null,
                rchId = null,
                fatherName = null,
                lastMenstrualPeriod = null,
                hrppaFilled = false,
                hrpmbpFilled = false
            ),
            pwr = emptyList()
        )

        val form = benWithPwr.asBenBasicDomainModelForHRPPregAssessmentForm()

        assertEquals("", form.benSurname)
        assertEquals("", form.familyHeadName)
        assertEquals("", form.spouseName)
        assertEquals("Not Available", form.rchId)
        assertNull(form.fatherName)
        assertNull(form.lastMenstrualPeriod)
        assertNull(form.edd)
        assertFalse(form.form1Filled)
        assertFalse(form.form2Filled)
    }

    // ---------------------------------------------------------------
    // PregnantWomanAncCache asPostModel(): optional-date ?.let branches
    // ---------------------------------------------------------------

    @Test
    fun `asPostModel maps all optional date fields when present`() {
        val now = System.currentTimeMillis()
        val cache = PregnantWomanAncCache(
            benId = 5L,
            visitNumber = 1,
            lmpDate = now,
            visitDate = now,
            dateSterilisation = now,
            abortionDate = now,
            deathDate = now,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED,
            frontFilePath = null,
            backFilePath = null
        )

        val post = cache.asPostModel()

        assertNotNull(post.lmpDate)
        assertNotNull(post.visitDate)
        assertNotNull(post.dateSterilisation)
        assertNotNull(post.abortionDate)
        assertNotNull(post.deathDate)
    }

    @Test
    fun `asPostModel leaves optional date fields and pulseRate null when absent`() {
        val post = minimalAnc().asPostModel()

        assertNull(post.lmpDate)
        assertNull(post.visitDate)
        assertNull(post.dateSterilisation)
        assertNull(post.abortionDate)
        assertNull(post.deathDate)
        assertNull(post.pulseRate)
    }

    // ---------------------------------------------------------------
    // BenWithAncListDomain: derived/computed property branches
    // ---------------------------------------------------------------

    private fun ancListDomain(
        pwr: PregnantWomanRegistrationCache? = null,
        lmpDate: Long? = null,
        eddDate: Long? = null,
        weekOfPregnancy: Int? = null,
        savedAncRecords: List<PregnantWomanAncCache> = emptyList(),
        showAddAnc: Boolean = false,
        pmsmaFillable: Boolean = false,
        hasPmsma: Boolean = false,
        syncState: SyncState? = null
    ) = BenWithAncListDomain(
        ben = mockk<BenBasicDomain>(relaxed = true),
        pwr = pwr,
        anc = emptyList(),
        savedAncRecords = savedAncRecords,
        pmsma = emptyList(),
        lmpDate = lmpDate,
        eddDate = eddDate,
        weekOfPregnancy = weekOfPregnancy,
        showAddAnc = showAddAnc,
        pmsmaFillable = pmsmaFillable,
        hasPmsma = hasPmsma,
        syncState = syncState
    )

    @Test
    fun `BenWithAncListDomain uses pwr lmp and edd when pwr present`() {
        val pwr = PregnantWomanRegistrationCache(
            benId = 1L,
            lmpDate = 1_650_000_000_000L,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.SYNCED
        )

        val domain = ancListDomain(pwr = pwr)

        assertEquals(pwr.lmpDate, domain.finalLmpDate)
        assertEquals(pwr.lmpDate + TimeUnit.DAYS.toMillis(280), domain.finalEddDate)
        assertNotNull(domain.finalWeeksOfPregnancy)
        assertNotNull(domain.lmpString)
        assertNotNull(domain.eddString)
    }

    @Test
    fun `BenWithAncListDomain falls back to lmpDate and eddDate fields when pwr absent`() {
        val domain = ancListDomain(
            pwr = null,
            lmpDate = 1_600_000_000_000L,
            eddDate = 1_700_000_000_000L,
            weekOfPregnancy = 15
        )

        val expectedWeeks = (TimeUnit.MILLISECONDS.toDays(
            org.piramalswasthya.sakhi.helpers.getTodayMillis() - 1_600_000_000_000L
        ) / 7).toInt()

        assertEquals(1_600_000_000_000L, domain.finalLmpDate)
        assertEquals(1_700_000_000_000L, domain.finalEddDate)
        assertEquals(expectedWeeks, domain.finalWeeksOfPregnancy)
        assertEquals(expectedWeeks.toString(), domain.weeksOfPregnancy)
    }

    @Test
    fun `BenWithAncListDomain weeksOfPregnancy is NA when all pregnancy fields absent`() {
        val domain = ancListDomain()

        assertNull(domain.finalLmpDate)
        assertNull(domain.finalWeeksOfPregnancy)
        assertEquals("N/A", domain.weeksOfPregnancy)
        assertNull(domain.lmpString)
        assertNull(domain.eddString)
    }

    @Test
    fun `BenWithAncListDomain isAbortionFormFilled true when a record has terminationDoneBy`() {
        val ancRec = PregnantWomanAncCache(
            benId = 1L,
            visitNumber = 1,
            terminationDoneBy = "ANM",
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.SYNCED,
            frontFilePath = null,
            backFilePath = null
        )

        val domain = ancListDomain(savedAncRecords = listOf(ancRec))

        assertTrue(domain.isAbortionFormFilled)
    }

    @Test
    fun `BenWithAncListDomain isAbortionFormFilled false when no termination records`() {
        val domain = ancListDomain(savedAncRecords = emptyList())

        assertFalse(domain.isAbortionFormFilled)
    }

    @Test
    fun `BenWithAncListDomain isDeliveryButtonVisible true beyond 22 weeks`() {
        val domain = ancListDomain(weekOfPregnancy = 23)

        assertTrue(domain.isDeliveryButtonVisible)
    }

    @Test
    fun `BenWithAncListDomain isDeliveryButtonVisible false at or below 22 weeks`() {
        val domain = ancListDomain(weekOfPregnancy = 22)

        assertFalse(domain.isDeliveryButtonVisible)
    }

    @Test
    fun `BenWithAncListDomain isDeliveryButtonVisible false when weeks fully absent`() {
        val domain = ancListDomain()

        assertFalse(domain.isDeliveryButtonVisible)
    }

    @Test
    fun `BenWithAncListDomain abortionDateString reflects abortionDate when set`() {
        val domain = ancListDomain().apply { abortionDate = 1_600_000_000_000L }

        assertNotNull(domain.abortionDateString)
    }

    @Test
    fun `BenWithAncListDomain abortionDateString null when abortionDate absent`() {
        val domain = ancListDomain()

        assertNull(domain.abortionDateString)
    }

    @Test
    fun `PwrPost getters read all fields and setters mutate var fields`() {
        val post = PwrPost(
            id = 1L, benId = 33L, registrationDate = "2024-01-01", rchId = 555L, mcpCardId = 999L,
            lmpDate = "2024-01-02", bloodGroup = "A+", weight = 60, height = 170,
            rprTestResult = "Negative", dateOfRprTest = "2024-01-03", hivTestResult = "Negative",
            hbsAgTestResult = "Negative", dateOfHivTest = "2024-01-04", dateOfHbsAgTest = "2024-01-05",
            pastIllness = "None", otherPastIllness = "None2", isFirstPregnancyTest = true,
            numPrevPregnancy = 1, pregComplication = "None3", otherComplication = "None4",
            isRegistered = true, rhNegative = "No", homeDelivery = "No", badObstetric = "No",
            isHrpCase = true, assignedAsHrpBy = "asha", tdDose1Date = "2024-01-06",
            tdDose2Date = "2024-01-07", tdDoseBoosterDate = "2024-01-08", isActive = true,
            createdDate = "2024-01-09", createdBy = "c", updatedDate = "2024-01-10", updatedBy = "u"
        )

        assertEquals(1L, post.id)
        assertEquals(33L, post.benId)
        assertEquals("2024-01-01", post.registrationDate)
        assertEquals(555L, post.rchId)
        assertEquals(999L, post.mcpCardId)
        assertEquals("A+", post.bloodGroup)
        assertEquals(60, post.weight)
        assertEquals(170, post.height)
        assertEquals("Negative", post.rprTestResult)
        assertEquals("2024-01-03", post.dateOfRprTest)
        assertEquals("Negative", post.hivTestResult)
        assertEquals("Negative", post.hbsAgTestResult)
        assertEquals("2024-01-04", post.dateOfHivTest)
        assertEquals("2024-01-05", post.dateOfHbsAgTest)
        assertEquals("None", post.pastIllness)
        assertEquals("None2", post.otherPastIllness)
        assertEquals(1, post.numPrevPregnancy)
        assertEquals("None3", post.pregComplication)
        assertEquals("None4", post.otherComplication)
        assertEquals("2024-01-06", post.tdDose1Date)
        assertEquals("2024-01-07", post.tdDose2Date)
        assertEquals("2024-01-08", post.tdDoseBoosterDate)
        assertTrue(post.isActive)
        assertEquals("2024-01-09", post.createdDate)
        assertEquals("c", post.createdBy)

        post.lmpDate = "2025-01-01"
        assertEquals("2025-01-01", post.lmpDate)
        post.isFirstPregnancyTest = false
        assertFalse(post.isFirstPregnancyTest)
        post.isRegistered = false
        assertFalse(post.isRegistered)
        post.rhNegative = "Yes"
        assertEquals("Yes", post.rhNegative)
        post.homeDelivery = "Yes"
        assertEquals("Yes", post.homeDelivery)
        post.badObstetric = "Yes"
        assertEquals("Yes", post.badObstetric)
        post.isHrpCase = false
        assertFalse(post.isHrpCase)
        post.assignedAsHrpBy = "anm"
        assertEquals("anm", post.assignedAsHrpBy)
        post.updatedDate = "2025-02-02"
        assertEquals("2025-02-02", post.updatedDate)
        post.updatedBy = "u2"
        assertEquals("u2", post.updatedBy)
    }

    @Test
    fun `BenWithPwrCache generated members - copy, equals, hashCode, toString, components`() {
        val benWithPwr = BenWithPwrCache(ben = benBasicCache(), pwr = listOf(pwrCache()))
        val copy = benWithPwr.copy()

        assertEquals(benWithPwr, copy)
        assertEquals(benWithPwr.hashCode(), copy.hashCode())
        assertTrue(benWithPwr.toString().contains("BenWithPwrCache"))
        assertEquals(benWithPwr.ben, benWithPwr.component1())
        assertEquals(benWithPwr.pwr, benWithPwr.component2())

        val differentPwr = benWithPwr.copy(pwr = emptyList())
        assertNotEquals(benWithPwr, differentPwr)
    }
}
