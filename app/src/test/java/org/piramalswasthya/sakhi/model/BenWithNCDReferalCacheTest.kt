package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class BenWithNCDReferalCacheTest {

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

    @Test fun `ncdreferalDomainModel maps ben to basic domain model`() {
        val cache = ReferalCache(benId = 1L, syncState = SyncState.SYNCED)
        val benWithReferal = BenWithNCDReferalCache(ben(), cache)

        val domain = benWithReferal.ncdreferalDomainModel()

        assertEquals(1L, domain.ben.benId)
        assertEquals(cache, domain.refcache)
    }

    @Test fun `ncdreferalDomainModel passes through null refcache`() {
        val domain = BenWithNCDReferalCache(ben(), null).ncdreferalDomainModel()

        assertEquals(1L, domain.ben.benId)
        assertNull(domain.refcache)
    }
}
