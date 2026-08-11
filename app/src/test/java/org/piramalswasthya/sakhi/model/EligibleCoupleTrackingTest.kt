package org.piramalswasthya.sakhi.model

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import java.util.Calendar
import java.util.concurrent.TimeUnit

class EligibleCoupleTrackingTest {

    // =====================================================
    // EligibleCoupleTrackingCache Tests
    // =====================================================

    @Test fun `EligibleCoupleTrackingCache can be created`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `EligibleCoupleTrackingCache default id is 0`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0, cache.id)
    }

    @Test fun `EligibleCoupleTrackingCache default visitDate is 0`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0L, cache.visitDate)
    }

    @Test fun `EligibleCoupleTrackingCache copy works`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(benId = 2L)
        assertEquals(2L, copy.benId)
    }

    @Test fun `EligibleCoupleTrackingCache same key fields match`() {
        val a = EligibleCoupleTrackingCache(id = 1, benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals("test", a.createdBy)
    }

    @Test fun `EligibleCoupleTrackingCache inequality`() {
        val a = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleTrackingCache(benId = 2L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }

    @Test fun `EligibleCoupleTrackingCache with SYNCED state`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `EligibleCoupleTrackingCache copy with updated syncState`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val synced = cache.copy(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, synced.syncState)
    }

    @Test fun `EligibleCoupleTrackingCache with all defaults checked`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "c", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals("c", cache.createdBy)
        assertEquals("u", cache.updatedBy)
    }

    @Test fun `EligibleCoupleTrackingCache different createdBy not equal`() {
        val a = EligibleCoupleTrackingCache(benId = 1L, createdBy = "a", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleTrackingCache(benId = 1L, createdBy = "b", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }

    // =====================================================
    // asNetworkModel Mapping Tests (merged from EligibleCoupleTrackingMappingTest)
    // =====================================================

    private fun cache() = EligibleCoupleTrackingCache(
        benId = 42L,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `asNetworkModel maps benId`() {
        assertEquals(42L, cache().asNetworkModel().benId)
    }

    @Test fun `asNetworkModel maps createdBy and updatedBy`() {
        val net = cache().asNetworkModel()
        assertEquals("creator", net.createdBy)
        assertEquals("modifier", net.updatedBy)
    }

    @Test fun `asNetworkModel default isActive is true`() {
        assertEquals(true, cache().asNetworkModel().isActive)
    }

    @Test fun `asNetworkModel lmp_date equals benId by design`() {
        // asNetworkModel sets lmp_date = benId
        assertEquals(42L, cache().asNetworkModel().lmp_date)
    }

    @Test fun `asNetworkModel formats zero lmpDate to date string`() {
        val net = cache().asNetworkModel()
        assertNotNull(net.lmpDate)
    }

    @Test fun `asNetworkModel formats visitDate as datetime`() {
        val net = cache().asNetworkModel()
        assertTrue(net.visitDate.contains("T"))
    }

    @Test fun `asNetworkModel null dateOfAntraInjection stays null`() {
        val net = cache().copy(dateOfAntraInjection = null).asNetworkModel()
        assertNull(net.dateOfAntraInjection)
    }

    @Test fun `asNetworkModel parses dateOfAntraInjection when present`() {
        val net = cache().copy(dateOfAntraInjection = "01-01-2023").asNetworkModel()
        assertNotNull(net.dateOfAntraInjection)
        assertTrue(net.dateOfAntraInjection!!.contains("T"))
    }

    @Test fun `asNetworkModel passes through pregnancy fields`() {
        val net = cache().copy(
            isPregnant = "Yes",
            pregnancyTestResult = "Positive",
            methodOfContraception = "Antra"
        ).asNetworkModel()
        assertEquals("Yes", net.isPregnant)
        assertEquals("Positive", net.pregnancyTestResult)
        assertEquals("Antra", net.methodOfContraception)
    }

    private fun cache2() = EligibleCoupleTrackingCache(
        benId = 55L,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `not-pregnant negative result`() {
        val net = cache2().copy(
            isPregnant = "No",
            pregnancyTestResult = "Negative",
            methodOfContraception = null
        ).asNetworkModel()
        assertEquals("No", net.isPregnant)
        assertEquals("Negative", net.pregnancyTestResult)
        assertNull(net.methodOfContraception)
    }

    @Test fun `inactive flag maps through`() {
        val net = cache2().copy(isActive = false).asNetworkModel()
        assertEquals(false, net.isActive)
    }

    @Test fun `alternate antra date format parses`() {
        val net = cache2().copy(dateOfAntraInjection = "15-08-2022").asNetworkModel()
        assertEquals(true, net.dateOfAntraInjection?.contains("T"))
    }

    @Test fun `condom contraception passes through`() {
        val net = cache2().copy(
            isPregnant = "No",
            methodOfContraception = "Condom"
        ).asNetworkModel()
        assertEquals("Condom", net.methodOfContraception)
    }

    // =====================================================
    // BenWithEcTrackingCache.asDomainModel Mapping Tests
    // =====================================================

    private fun benBasic(
        benId: Long = 1L,
        noOfAliveChildren: Int = 2
    ) = BenBasicCache(
        benId = benId,
        hhId = 10L,
        regDate = System.currentTimeMillis(),
        benName = "John",
        benSurname = "Doe",
        gender = Gender.FEMALE,
        dob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 25L),
        relToHeadId = 3,
        mobileNo = 9998887776L,
        fatherName = "Bob",
        motherName = "Mary",
        familyHeadName = "Head",
        spouseName = "Spouse",
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
        isMarried = true,
        noOfAliveChildren = noOfAliveChildren
    )

    private fun ecReg(benId: Long = 1L) = EligibleCoupleRegCache(
        benId = benId,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    private fun mockResources(): Resources {
        val resources = mockk<Resources>()
        every { resources.getString(any()) } returns "Visited on"
        return resources
    }

    @Test fun `asDomainModel with empty savedECTRecords allows fill and has null allSynced`() {
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = emptyList()
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertTrue(domain.allowFill)
        assertEquals(0L, domain.ectDate)
        assertEquals(0L, domain.lmpDate)
        assertNull(domain.allSynced)
        assertTrue(domain.savedECTRecords.isEmpty())
    }

    @Test fun `asDomainModel uses childCount override when provided`() {
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(noOfAliveChildren = 2),
            ecr = ecReg(),
            savedECTRecords = emptyList()
        )
        val domain = benWithEct.asDomainModel(childCount = 5, resources = mockResources())
        assertEquals("5", domain.numChildren)
    }

    @Test fun `asDomainModel falls back to ben noOfAliveChildren when childCount is null`() {
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(noOfAliveChildren = 4),
            ecr = ecReg(),
            savedECTRecords = emptyList()
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertEquals("4", domain.numChildren)
    }

    @Test fun `asDomainModel forwards ben basic domain fields`() {
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(benId = 77L),
            ecr = ecReg(benId = 77L),
            savedECTRecords = emptyList()
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertEquals(77L, domain.ben.benId)
        assertEquals("John", domain.ben.benName)
    }

    @Test fun `asDomainModel picks most recent visit as ectDate and lmpDate`() {
        val olderRecord = EligibleCoupleTrackingCache(
            benId = 1L,
            visitDate = 1000L,
            lmpDate = 111L,
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.SYNCED
        )
        val newerRecord = EligibleCoupleTrackingCache(
            benId = 1L,
            visitDate = 2000L,
            lmpDate = 222L,
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.SYNCED
        )
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = listOf(olderRecord, newerRecord)
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertEquals(2000L, domain.ectDate)
        assertEquals(222L, domain.lmpDate)
        assertEquals(2, domain.savedECTRecords.size)
    }

    @Test fun `asDomainModel allSynced is SYNCED when all records synced`() {
        val record = EligibleCoupleTrackingCache(
            benId = 1L,
            visitDate = System.currentTimeMillis(),
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.SYNCED
        )
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = listOf(record)
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertEquals(SyncState.SYNCED, domain.allSynced)
    }

    @Test fun `asDomainModel allSynced is UNSYNCED when any record unsynced`() {
        val synced = EligibleCoupleTrackingCache(
            benId = 1L,
            visitDate = System.currentTimeMillis(),
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.SYNCED
        )
        val unsynced = synced.copy(syncState = SyncState.UNSYNCED)
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = listOf(synced, unsynced)
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertEquals(SyncState.UNSYNCED, domain.allSynced)
    }

    @Test fun `asDomainModel allowFill is false when last visit is in current month`() {
        val record = EligibleCoupleTrackingCache(
            benId = 1L,
            visitDate = System.currentTimeMillis(),
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.SYNCED
        )
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = listOf(record)
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertFalse(domain.allowFill)
    }

    @Test fun `asDomainModel allowFill is true when last visit was in a previous month`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -2)
        val record = EligibleCoupleTrackingCache(
            benId = 1L,
            visitDate = cal.timeInMillis,
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.SYNCED
        )
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = listOf(record)
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        assertTrue(domain.allowFill)
    }

    @Test fun `asDomainModel maps ECTDomain fields including filled on string`() {
        val record = EligibleCoupleTrackingCache(
            benId = 9L,
            visitDate = 12345L,
            createdDate = 6789L,
            createdBy = "creator",
            updatedBy = "modifier",
            syncState = SyncState.UNSYNCED
        )
        val benWithEct = BenWithEcTrackingCache(
            ben = benBasic(),
            ecr = ecReg(),
            savedECTRecords = listOf(record)
        )
        val domain = benWithEct.asDomainModel(childCount = null, resources = mockResources())
        val ectDomain = domain.savedECTRecords.first()
        assertEquals(9L, ectDomain.benId)
        assertEquals(6789L, ectDomain.created)
        assertEquals(12345L, ectDomain.visited)
        assertEquals(SyncState.UNSYNCED, ectDomain.syncState)
        assertTrue(ectDomain.filledOnString.contains("Visited on"))
    }
}
