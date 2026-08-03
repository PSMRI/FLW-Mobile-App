package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the BenWith*.as*DomainModel wrapper mappers across the disease-screening /
 * adolescent / ABHA / maternal model files. Each mapper delegates to
 * [BenBasicCache.asBasicDomainModel] (pure JVM logic) and passes the screening cache
 * through. Both the null and non-null screening-cache branches are exercised.
 */
class BenWithScreeningDomainMappingTest {

    private val adultDob = System.currentTimeMillis() - 365L * 40 * 24 * 60 * 60 * 1000

    private fun ben(benId: Long = 1L) = BenBasicCache(
        benId = benId,
        hhId = 2L,
        regDate = 1_600_000_000_000L,
        benName = "John",
        benSurname = "Doe",
        gender = Gender.MALE,
        dob = adultDob,
        relToHeadId = 3,
        mobileNo = 9998887776L,
        fatherName = "Bob",
        motherName = "Mary",
        familyHeadName = "Head",
        spouseName = null,
        rchId = "RCH1",
        hrpStatus = false,
        syncState = SyncState.SYNCED,
        reproductiveStatusId = 2,
        lastMenstrualPeriod = null,
        isKid = false,
        immunizationStatus = false,
        villageId = 5,
        abhaId = "abha1",
        isNewAbha = true,
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
        hrppaFilled = false,
        hrpnpaFilled = false,
        hrpmbpFilled = false,
        hrptFilled = false,
        hrptrackingDone = false,
        hrnptrackingDone = false,
        hrnptFilled = false,
        hrppaSyncState = SyncState.SYNCED,
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
        isMarried = false
    )

    // ===================== Filaria =====================

    @Test fun `filaria domain with cache passes ben and screening`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        val d = BenWithFilariaScreeningCache(ben(), cache).asFilariaScreeningDomainModel()
        assertEquals(1L, d.ben.benId)
        assertEquals("MALE", d.ben.gender)
        assertEquals(cache, d.filaria)
    }

    @Test fun `filaria domain with null screening`() {
        val d = BenWithFilariaScreeningCache(ben(), null).asFilariaScreeningDomainModel()
        assertNull(d.filaria)
        assertEquals(1L, d.ben.benId)
    }

    // ===================== Malaria screening =====================

    @Test fun `malaria domain with cache`() {
        val cache = MalariaScreeningCache(benId = 1L, visitId = 3L, houseHoldDetailsId = 101L)
        val d = BenWithMalariaScreeningCache(ben(), cache).asMalariaScreeningDomainModel()
        assertEquals(cache, d.tb)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `malaria domain with null screening`() {
        val d = BenWithMalariaScreeningCache(ben(), null).asMalariaScreeningDomainModel()
        assertNull(d.tb)
    }

    // ===================== Malaria confirmed =====================

    @Test fun `malariaConfirmed domain maps slideTestName and cache`() {
        val cache = MalariaConfirmedCasesCache(benId = 1L, houseHoldDetailsId = 102L)
        val d = BenWithMalariaConfirmedCache(ben(), cache, "Slide-A").asMalariaConfirmedDomainModel()
        assertEquals(cache, d.malariaConfirmed)
        assertEquals("Slide-A", d.slideTestName)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `malariaConfirmed domain with nulls`() {
        val d = BenWithMalariaConfirmedCache(ben(), null, null).asMalariaConfirmedDomainModel()
        assertNull(d.malariaConfirmed)
        assertNull(d.slideTestName)
    }

    // ===================== Kala Azar =====================

    @Test fun `kalaAzar domain with cache`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 103L)
        val d = BenWithKALAZARScreeningCache(ben(), cache).asKALAZARScreeningDomainModel()
        assertEquals(cache, d.kala)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `kalaAzar domain with null`() {
        assertNull(BenWithKALAZARScreeningCache(ben(), null).asKALAZARScreeningDomainModel().kala)
    }

    // ===================== AES =====================

    @Test fun `aes domain with cache`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 104L)
        val d = BenWithAESScreeningCache(ben(), cache).asAESScreeningDomainModel()
        assertEquals(cache, d.aes)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `aes domain with null`() {
        assertNull(BenWithAESScreeningCache(ben(), null).asAESScreeningDomainModel().aes)
    }

    // ===================== TB screening =====================

    @Test fun `tbScreening domain with cache`() {
        val cache = TBScreeningCache(benId = 1L)
        val d = BenWithTbScreeningCache(ben(), cache).asTbScreeningDomainModel()
        assertEquals(cache, d.tb)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `tbScreening domain with null`() {
        assertNull(BenWithTbScreeningCache(ben(), null).asTbScreeningDomainModel().tb)
    }

    // ===================== TB suspected =====================

    @Test fun `tbSuspected domain maps suspected and confirmed list`() {
        val suspected = TBSuspectedCache(benId = 1L)
        val confirmed = listOf(TBConfirmedTreatmentCache(benId = 1L))
        val d = BenWithTbSuspectedCache(ben(), suspected, confirmed).asTbSuspectedDomainModel()
        assertEquals(suspected, d.tbSuspected)
        assertEquals(1, d.tbConfirmedList.size)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `tbSuspected domain with null suspected and empty list`() {
        val d = BenWithTbSuspectedCache(ben(), null, emptyList()).asTbSuspectedDomainModel()
        assertNull(d.tbSuspected)
        assertTrue(d.tbConfirmedList.isEmpty())
        // latestTbSyncState getter on empty list is null
        assertNull(d.latestTbSyncState)
    }

    // ===================== Adolescent =====================

    @Test fun `adolescent domain with cache`() {
        val cache = AdolescentHealthCache(benId = 1L)
        val d = BenWithAdolescentCache(ben(), cache).asAdolescentDomainModel()
        assertEquals(cache, d.adolescent)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `adolescent domain with null`() {
        assertNull(BenWithAdolescentCache(ben(), null).asAdolescentDomainModel().adolescent)
    }

    // ===================== ABHA generated =====================

    @Test fun `abha generated domain with null abha`() {
        val d = BenWithABHAGeneratedCache(ben(), null).asBenWithABHAGeneratedDomainModel()
        assertNull(d.abha)
        assertEquals(1L, d.ben.benId)
    }

    // ===================== IRS screening rounds =====================

    @Test fun `screeningRound domain maps round list`() {
        val rounds = listOf(
            IRSRoundScreening(rounds = 1, householdId = 200L),
            IRSRoundScreening(rounds = 2, householdId = 200L)
        )
        val d = BenWithScreeningRound(ben(), rounds).asScreeningRoundDomainModel()
        assertEquals(2, d.round.size)
    }

    @Test fun `screeningRound domain with empty list`() {
        val d = BenWithScreeningRound(ben(), emptyList()).asScreeningRoundDomainModel()
        assertTrue(d.round.isEmpty())
    }

    // ===================== Leprosy =====================

    private fun followUp(visitNumber: Int, followUpDate: Long) = LeprosyFollowUpCache(
        benId = 1L,
        visitNumber = visitNumber,
        followUpDate = followUpDate,
        createdBy = "creator",
        modifiedBy = "modifier"
    )

    @Test fun `leprosy domain resolves current follow-up by visit number`() {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 300L,
            currentVisitNumber = 2,
            createdBy = "creator",
            modifiedBy = "modifier"
        )
        val followUps = listOf(
            followUp(1, 1000L),
            followUp(2, 2000L),
            followUp(2, 5000L)
        )
        val d = BenWithLeprosyScreeningCache(ben(), screening, followUps).asLeprosyScreeningDomainModel()
        assertEquals(screening, d.leprosy)
        assertEquals(3, d.followUps.size)
        // currentFollowUp = first matching visitNumber == currentVisitNumber (2)
        assertNotNull(d.currentFollowUp)
        assertEquals(2, d.currentFollowUp!!.visitNumber)
        // currentVisitFollowUps = all matching visit 2
        assertEquals(2, d.currentVisitFollowUps.size)
        // lastFollowUp = max followUpDate among visit-2 entries
        assertEquals(5000L, d.lastFollowUp!!.followUpDate)
    }

    @Test fun `leprosy domain with no matching follow-ups`() {
        val screening = LeprosyScreeningCache(
            benId = 1L,
            houseHoldDetailsId = 300L,
            currentVisitNumber = 9,
            createdBy = "creator",
            modifiedBy = "modifier"
        )
        val d = BenWithLeprosyScreeningCache(ben(), screening, listOf(followUp(1, 1000L)))
            .asLeprosyScreeningDomainModel()
        assertNull(d.currentFollowUp)
        assertTrue(d.currentVisitFollowUps.isEmpty())
        assertNull(d.lastFollowUp)
    }

    @Test fun `leprosy domain with null screening and empty followups`() {
        val d = BenWithLeprosyScreeningCache(ben(), null, emptyList()).asLeprosyScreeningDomainModel()
        assertNull(d.leprosy)
        assertTrue(d.followUps.isEmpty())
        assertNull(d.currentFollowUp)
    }

    // ===================== Maternal PWR domain =====================

    private fun pwr(active: Boolean, benId: Long = 1L) = PregnantWomanRegistrationCache(
        benId = benId,
        active = active,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `pwr domain picks first active registration`() {
        val list = listOf(pwr(active = false), pwr(active = true))
        val d = BenWithPwrCache(ben(), list).asPwrDomainModel()
        assertNotNull(d.pwr)
        assertTrue(d.pwr!!.active)
        assertEquals(1L, d.ben.benId)
    }

    @Test fun `pwr domain with no active registration is null`() {
        val d = BenWithPwrCache(ben(), listOf(pwr(active = false))).asPwrDomainModel()
        assertNull(d.pwr)
    }

    @Test fun `pwr domain with empty list is null`() {
        val d = BenWithPwrCache(ben(), emptyList()).asPwrDomainModel()
        assertNull(d.pwr)
    }

    // ===================== AHD / Deworming copy mappers =====================

    @Test fun `AHDCache toAHDCache copies fields`() {
        val src = AHDCache(
            id = 5,
            mobilizedForAHD = "Yes",
            ahdPlace = "PHC",
            ahdDate = "2024-01-01",
            image1 = "img1",
            image2 = "img2"
        )
        val out = src.toAHDCache()
        assertEquals(5, out.id)
        assertEquals("Yes", out.mobilizedForAHD)
        assertEquals("PHC", out.ahdPlace)
        assertEquals("2024-01-01", out.ahdDate)
        assertEquals("img1", out.image1)
        assertEquals("img2", out.image2)
    }

    @Test fun `AHDCache toAHDCache with null optional fields`() {
        val out = AHDCache().toAHDCache()
        assertNull(out.mobilizedForAHD)
        assertNull(out.ahdPlace)
        assertNull(out.ahdDate)
    }

    @Test fun `DewormingCache toDewormingCache copies fields and stamps regDate`() {
        val src = DewormingCache(
            id = 7,
            dewormingDone = "Yes",
            dewormingDate = "2024-02-02",
            dewormingLocation = "School",
            ageGroup = 2,
            image1 = "a",
            image2 = "b"
        )
        val out = src.toDewormingCache()
        assertEquals(7, out.id)
        assertEquals("Yes", out.dewormingDone)
        assertEquals("2024-02-02", out.dewormingDate)
        assertEquals("School", out.dewormingLocation)
        assertEquals(2, out.ageGroup)
        assertEquals("a", out.image1)
        assertEquals("b", out.image2)
        // regDate is stamped with current date (dd-MM-yyyy)
        assertNotNull(out.regDate)
        assertTrue(out.regDate!!.matches(Regex("\\d{2}-\\d{2}-\\d{4}")))
    }

    @Test fun `DewormingCache toDewormingCache with null optional fields`() {
        val out = DewormingCache().toDewormingCache()
        assertNull(out.dewormingDone)
        assertNull(out.ageGroup)
        assertNotNull(out.regDate)
    }
}
