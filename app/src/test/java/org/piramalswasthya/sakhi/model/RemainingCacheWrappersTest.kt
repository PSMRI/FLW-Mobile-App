package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class RemainingCacheWrappersTest {

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

    // ---------------- BenWithChildRegCache ----------------

    @Test fun `BenWithChildRegCache exposes ben and childRegistration`() {
        val childReg = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)
        val wrapper = BenWithChildRegCache(ben = ben(), childRegistration = childReg)
        assertEquals(1L, wrapper.ben.benId)
        assertEquals(childReg, wrapper.childRegistration)
    }

    @Test fun `BenWithChildRegCache tolerates null childRegistration`() {
        val wrapper = BenWithChildRegCache(ben = ben(), childRegistration = null)
        assertNull(wrapper.childRegistration)
    }

    @Test fun `BenWithChildRegCache equals and copy`() {
        val benInstance = ben()
        val a = BenWithChildRegCache(ben = benInstance, childRegistration = null)
        val b = BenWithChildRegCache(ben = benInstance, childRegistration = null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        val childReg = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)
        assertNotEquals(a, a.copy(childRegistration = childReg))
    }

    // ---------------- BenWithTbConfirmedCache / BenWithTbConfirmedDomain ----------------

    @Test fun `BenWithTbConfirmedCache asTbSuspectedDomainModel maps ben and passes through tb`() {
        val tb = TBConfirmedTreatmentCache(benId = 1L)
        val wrapper = BenWithTbConfirmedCache(ben = ben(), tb = tb)

        val domain = wrapper.asTbSuspectedDomainModel()

        assertEquals(1L, domain.ben.benId)
        assertEquals(tb, domain.tb)
    }

    @Test fun `BenWithTbConfirmedCache asTbSuspectedDomainModel tolerates null tb`() {
        val domain = BenWithTbConfirmedCache(ben = ben(), tb = null).asTbSuspectedDomainModel()
        assertNull(domain.tb)
    }

    @Test fun `BenWithTbConfirmedDomain equals and copy`() {
        val tb = TBConfirmedTreatmentCache(benId = 1L)
        val benInstance = ben()
        val a = BenWithTbConfirmedDomain(ben = benInstance.asBasicDomainModel(), tb = tb)
        val b = BenWithTbConfirmedDomain(ben = benInstance.asBasicDomainModel(), tb = tb)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(tb = null))
    }

    // ---------------- HRPPregnantTrackBen ----------------

    @Test fun `HRPPregnantTrackBen exposes ben and trackList`() {
        val track = HRPPregnantTrackCache(benId = 1L)
        val wrapper = HRPPregnantTrackBen(
            ben = ben().asBasicDomainModel(),
            trackList = listOf(track)
        )

        assertEquals(1, wrapper.trackList.size)
        assertEquals(1L, wrapper.ben.benId)
    }

    @Test fun `HRPPregnantTrackBen copy overrides trackList independently`() {
        val wrapper = HRPPregnantTrackBen(ben = ben().asBasicDomainModel(), trackList = emptyList())
        val track = HRPPregnantTrackCache(benId = 1L)
        val copy = wrapper.copy(trackList = listOf(track))
        assertTrue(wrapper.trackList.isEmpty())
        assertEquals(1, copy.trackList.size)
    }

    // ---------------- BenWithHRNPACache ----------------

    @Test fun `BenWithHRNPACache asDomainModel maps ben and passes through assess`() {
        val assess = HRPNonPregnantAssessCache(benId = 1L)
        val domain = BenWithHRNPACache(ben = ben(), assess = assess).asDomainModel()

        assertEquals(1L, domain.ben.benId)
        assertEquals(assess, domain.assess)
    }

    @Test fun `BenWithHRNPACache asDomainModel tolerates null assess`() {
        val domain = BenWithHRNPACache(ben = ben(), assess = null).asDomainModel()
        assertNull(domain.assess)
    }

    // ---------------- CbacCachePush ----------------

    @Test fun `CbacCachePush exposes cbac hhId and benGender`() {
        val cbac = CbacCache(benId = 1L, ashaId = 2, syncState = SyncState.SYNCED)
        val push = CbacCachePush(cbac = cbac, hhId = 100L, benGender = Gender.FEMALE)

        assertEquals(cbac, push.cbac)
        assertEquals(100L, push.hhId)
        assertEquals(Gender.FEMALE, push.benGender)
    }

    @Test fun `CbacCachePush equals and copy`() {
        val cbac = CbacCache(benId = 1L, ashaId = 2, syncState = SyncState.SYNCED)
        val a = CbacCachePush(cbac = cbac, hhId = 100L, benGender = Gender.FEMALE)
        val b = CbacCachePush(cbac = cbac, hhId = 100L, benGender = Gender.FEMALE)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(benGender = Gender.MALE))
    }

    // ---------------- BenWithECRCache ----------------

    @Test fun `BenWithECRCache asDomainModel maps ben ecr and childCount`() {
        val ecr = EligibleCoupleRegCache(benId = 1L, createdBy = "user", updatedBy = "user", syncState = SyncState.SYNCED)
        val domain = BenWithECRCache(ben = ben(), ecr = ecr).asDomainModel(childCount = 3)

        assertEquals(1L, domain.ben.benId)
        assertEquals(ecr, domain.ecr)
        assertEquals(3, domain.childCount)
    }

    @Test fun `BenWithECRCache asDomainModel defaults childCount to zero when omitted`() {
        val domain = BenWithECRCache(ben = ben(), ecr = null).asDomainModel()
        assertNull(domain.ecr)
        assertEquals(0, domain.childCount)
    }

    // ---------------- BenWithCbacAndReferalCache ----------------

    @Test fun `BenWithCbacAndReferalCache asDomainModel maps ben cbacList and referral`() {
        val referral = ReferalCache(benId = 1L, syncState = SyncState.SYNCED)
        val cbacList = listOf(CbacCache(benId = 1L, ashaId = 2, syncState = SyncState.SYNCED))
        val wrapper = BenWithCbacAndReferalCache(referral = referral, cbacList = cbacList, ben = ben())

        val domain = wrapper.asDomainModel()

        assertEquals(1L, domain.ben.benId)
        assertEquals(cbacList, domain.savedCbacRecords)
        assertEquals(referral, domain.referalCac)
    }

    @Test fun `BenWithCbacAndReferalCache asDomainModel tolerates an empty cbacList`() {
        val referral = ReferalCache(benId = 1L, syncState = SyncState.SYNCED)
        val domain = BenWithCbacAndReferalCache(referral = referral, cbacList = emptyList(), ben = ben()).asDomainModel()
        assertTrue(domain.savedCbacRecords.isEmpty())
    }

    // ---------------- InfantRegWithBen ----------------

    @Test fun `InfantRegWithBen asBasicDomainModel maps motherBen infant and childBen`() {
        val infant = InfantRegCache(motherBenId = 1L, isActive = true, babyIndex = 1, createdBy = "user", updatedBy = "user", syncState = SyncState.SYNCED)
        val wrapper = InfantRegWithBen(infant = infant, motherBen = ben(1L), childBen = ben(2L))

        val domain = wrapper.asBasicDomainModel()

        assertEquals(1L, domain.motherBen.benId)
        assertEquals(infant, domain.infant)
        assertEquals(2L, domain.childBen?.benId)
    }

    @Test fun `InfantRegWithBen asBasicDomainModel tolerates null childBen`() {
        val infant = InfantRegCache(motherBenId = 1L, isActive = true, babyIndex = 1, createdBy = "user", updatedBy = "user", syncState = SyncState.SYNCED)
        val domain = InfantRegWithBen(infant = infant, motherBen = ben(1L), childBen = null).asBasicDomainModel()
        assertNull(domain.childBen)
    }

    // ---------------- ProfileActivityListResponse ----------------

    @Test fun `ProfileActivityListResponse exposes constructor values and generated members`() {
        val data = ProfileActivityNetwork(id = 1L, name = "Asha")
        val response = ProfileActivityListResponse(data = data, statusCode = 200, status = "OK")

        assertEquals(1L, response.data.id)
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.status)
        val same = response.copy()
        assertEquals(response, same)
        assertEquals(response.hashCode(), same.hashCode())
        assertNotEquals(response, response.copy(status = "FAIL"))
    }

    // ---------------- LeprosyFollowUpRequestDTO ----------------

    @Test fun `LeprosyFollowUpRequestDTO exposes userName and leprosyFollowUpLists`() {
        val dto = LeprosyFollowUpRequestDTO(userName = "asha_user", leprosyFollowUpLists = emptyList())
        assertEquals("asha_user", dto.userName)
        assertTrue(dto.leprosyFollowUpLists.isEmpty())
    }

    @Test fun `LeprosyFollowUpRequestDTO equals and copy`() {
        val a = LeprosyFollowUpRequestDTO(userName = "asha_user", leprosyFollowUpLists = emptyList())
        val b = LeprosyFollowUpRequestDTO(userName = "asha_user", leprosyFollowUpLists = emptyList())
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(userName = "other"))
    }
}
