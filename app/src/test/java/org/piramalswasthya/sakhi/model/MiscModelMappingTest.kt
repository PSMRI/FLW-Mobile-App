package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.ABHAGeneratedDTO
import org.piramalswasthya.sakhi.network.NCDReferalDTO
import org.piramalswasthya.sakhi.network.getLongFromDate

/**
 * Pure mapper coverage for assorted model classes:
 * CDR, MDSR, ReferalCache/Converters, ABHAModel, GeneralOPDBenificiary, Incentives.
 * PMJAYCache.asPostModel() intentionally skipped (needs User/HouseholdCache/BenRegCache graph).
 */
class MiscModelMappingTest {

    // =====================================================
    // CDRCache.asPostModel()  /  CDRPost.asCacheModel()
    // =====================================================

    private fun cdrCache() = CDRCache(
        benId = 10L,
        processed = "N",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `CDRCache asPostModel maps benId and id`() {
        val post = cdrCache().copy(id = 3).asPostModel()
        assertEquals(3, post.id)
        assertEquals(10L, post.benId)
    }

    @Test fun `CDRCache asPostModel maps mohalla to colony`() {
        val post = cdrCache().copy(mohalla = "colonyX", motherName = "mom").asPostModel()
        assertEquals("colonyX", post.colony)
        assertEquals("mom", post.motherName)
    }

    @Test fun `CDRCache asPostModel passes through mobileNumber`() {
        val post = cdrCache().copy(mobileNumber = 99L).asPostModel()
        assertEquals(99L, post.mobileNumber)
    }

    @Test fun `CDRPost asCacheModel maps benId and forces processed and synced`() {
        val cache = CDRPost(id = 5, benId = 20L, colony = "colY").asCacheModel()
        assertEquals(20L, cache.benId)
        assertEquals("N", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertEquals("colY", cache.mohalla)
    }

    @Test fun `CDRCache asPostModel maps visitDate to null when unset`() {
        val post = cdrCache().copy(visitDate = null).asPostModel()
        assertNull(post.visitDate)
    }

    @Test fun `CDRCache asPostModel formats visitDate when populated`() {
        val post = cdrCache().copy(visitDate = 1_600_000_000_000L).asPostModel()
        assertNotNull(post.visitDate)
    }

    @Test fun `CDRCache asPostModel maps timeOfDeath to null when unset`() {
        val post = cdrCache().asPostModel()
        assertNull(post.timeOfDeath)
    }

    @Test fun `CDRCache asPostModel formats timeOfDeath when populated`() {
        val post = cdrCache().copy(timeOfDeath = 1_600_000_000_000L).asPostModel()
        assertNotNull(post.timeOfDeath)
    }

    @Test fun `CDRCache asPostModel maps dateOfNotification to null when unset`() {
        val post = cdrCache().asPostModel()
        assertNull(post.dateOfNotification)
    }

    @Test fun `CDRCache asPostModel formats dateOfNotification when populated`() {
        val post = cdrCache().copy(dateOfNotification = 1_600_000_000_000L).asPostModel()
        assertNotNull(post.dateOfNotification)
    }

    @Test fun `CDRCache asPostModel maps createdDate to null when unset`() {
        val post = cdrCache().copy(createdDate = null).asPostModel()
        assertNull(post.createdDate)
    }

    @Test fun `CDRCache asPostModel maps updatedDate to null when unset`() {
        val post = cdrCache().copy(updatedDate = null).asPostModel()
        assertNull(post.updatedDate)
    }

    @Test fun `CDRPost asCacheModel formats visitDate when populated`() {
        val cache = CDRPost(id = 1, benId = 1L, visitDate = "2024-01-01").asCacheModel()
        assertEquals(getLongFromDate("2024-01-01"), cache.visitDate)
    }

    @Test fun `CDRPost asCacheModel maps visitDate to null when unset`() {
        val cache = CDRPost(id = 1, benId = 1L).asCacheModel()
        assertNull(cache.visitDate)
    }

    @Test fun `CDRPost asCacheModel formats timeOfDeath when populated`() {
        val cache = CDRPost(id = 1, benId = 1L, timeOfDeath = "2024-01-01").asCacheModel()
        assertEquals(getLongFromDate("2024-01-01"), cache.timeOfDeath)
    }

    @Test fun `CDRPost asCacheModel maps timeOfDeath to null when unset`() {
        val cache = CDRPost(id = 1, benId = 1L).asCacheModel()
        assertNull(cache.timeOfDeath)
    }

    @Test fun `CDRPost asCacheModel formats dateOfNotification when populated`() {
        val cache = CDRPost(id = 1, benId = 1L, dateOfNotification = "2024-01-01").asCacheModel()
        assertEquals(getLongFromDate("2024-01-01"), cache.dateOfNotification)
    }

    @Test fun `CDRPost asCacheModel maps dateOfNotification to null when unset`() {
        val cache = CDRPost(id = 1, benId = 1L).asCacheModel()
        assertNull(cache.dateOfNotification)
    }

    @Test fun `CDRPost asCacheModel formats createdDate when populated`() {
        val cache = CDRPost(id = 1, benId = 1L, createdDate = "2024-01-01").asCacheModel()
        assertEquals(getLongFromDate("2024-01-01"), cache.createdDate)
    }

    @Test fun `CDRPost asCacheModel maps createdDate to null when unset`() {
        val cache = CDRPost(id = 1, benId = 1L).asCacheModel()
        assertNull(cache.createdDate)
    }

    @Test fun `CDRPost asCacheModel formats updatedDate when populated`() {
        val cache = CDRPost(id = 1, benId = 1L, updatedDate = "2024-01-01").asCacheModel()
        assertEquals(getLongFromDate("2024-01-01"), cache.updatedDate)
    }

    @Test fun `CDRPost asCacheModel maps updatedDate to null when unset`() {
        val cache = CDRPost(id = 1, benId = 1L).asCacheModel()
        assertNull(cache.updatedDate)
    }

    // =====================================================
    // MDSRCache.asPostModel()  /  MdsrPost.asCacheModel()
    // =====================================================

    private fun mdsrCache() = MDSRCache(
        benId = 11L,
        processed = "N",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `MDSRCache asPostModel maps benId`() {
        val post = mdsrCache().copy(id = 2).asPostModel()
        assertEquals(2, post.id)
        assertEquals(11L, post.benId)
    }

    @Test fun `MDSRCache asPostModel maps reason and signature`() {
        val post = mdsrCache().copy(reasonOfDeath = "r", blockMOSign = "sig", actionTaken = true).asPostModel()
        assertEquals("r", post.reasonDeath)
        assertEquals("sig", post.signature)
        assertEquals(true, post.actionTaken)
    }

    @Test fun `MdsrPost asCacheModel maps reason and signature back and forces synced`() {
        val cache = MdsrPost(benId = 21L, actionTaken = false, reasonDeath = "rr", signature = "s2").asCacheModel()
        assertEquals(21L, cache.benId)
        assertEquals("rr", cache.reasonOfDeath)
        assertEquals("s2", cache.blockMOSign)
        assertEquals("N", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // =====================================================
    // getMonth() / getYear() / getDays()
    // =====================================================

    @Test fun `getMonth returns a two digit value`() {
        assertTrue(getMonth().matches(Regex("\\d{2}")))
    }

    @Test fun `getYear returns a four digit value`() {
        assertTrue(getYear().matches(Regex("\\d{4}")))
    }

    @Test fun `getDays returns a two digit value`() {
        assertTrue(getDays().matches(Regex("\\d{2}")))
    }

    // =====================================================
    // List<BenWithAncDoPncCache>.filterMdsr()
    // =====================================================

    private fun mdsrBen(benId: Long = 1L) = BenBasicCache(
        benId = benId,
        hhId = 2L,
        regDate = 1_600_000_000_000L,
        benName = "Jane",
        benSurname = "Doe",
        gender = Gender.FEMALE,
        dob = 500_000_000_000L,
        relToHeadId = 1,
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

    private fun mdsrDeliveryOutcome(complication: String? = null) = DeliveryOutcomeCache(
        benId = 1L,
        isActive = true,
        complication = complication,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.UNSYNCED
    )

    private fun mdsrPnc(motherDeath: Boolean = false) = PNCVisitCache(
        benId = 1L,
        pncPeriod = 1,
        isActive = true,
        motherDeath = motherDeath,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.UNSYNCED
    )

    private fun mdsrAnc(maternalDeath: Boolean? = null) = PregnantWomanAncCache(
        benId = 1L,
        visitNumber = 1,
        maternalDeath = maternalDeath,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.UNSYNCED,
        frontFilePath = null,
        backFilePath = null
    )

    @Test fun `filterMdsr keeps a record whose anc reports a maternal death`() {
        val record = BenWithAncDoPncCache(
            ben = mdsrBen(),
            anc = listOf(mdsrAnc(maternalDeath = true)),
            deliveryOutcome = listOf(mdsrDeliveryOutcome()),
            pnc = listOf(mdsrPnc())
        )

        val result = listOf(record).filterMdsr()

        assertEquals(1, result.size)
    }

    @Test fun `filterMdsr keeps a record whose delivery outcome complication is DEATH`() {
        val record = BenWithAncDoPncCache(
            ben = mdsrBen(),
            anc = listOf(mdsrAnc()),
            deliveryOutcome = listOf(mdsrDeliveryOutcome(complication = "DEATH")),
            pnc = listOf(mdsrPnc())
        )

        val result = listOf(record).filterMdsr()

        assertEquals(1, result.size)
    }

    @Test fun `filterMdsr keeps a record whose pnc reports a mother death`() {
        val record = BenWithAncDoPncCache(
            ben = mdsrBen(),
            anc = listOf(mdsrAnc()),
            deliveryOutcome = listOf(mdsrDeliveryOutcome()),
            pnc = listOf(mdsrPnc(motherDeath = true))
        )

        val result = listOf(record).filterMdsr()

        assertEquals(1, result.size)
    }

    @Test fun `filterMdsr drops a record with no death indicators`() {
        val record = BenWithAncDoPncCache(
            ben = mdsrBen(),
            anc = listOf(mdsrAnc()),
            deliveryOutcome = listOf(mdsrDeliveryOutcome()),
            pnc = listOf(mdsrPnc())
        )

        val result = listOf(record).filterMdsr()

        assertTrue(result.isEmpty())
    }

    // =====================================================
    // ReferalCache.toDTO()  /  NCDReferalDTO.toCache()  /  Converters
    // =====================================================

    private fun referal() = ReferalCache(
        benId = 30L,
        syncState = SyncState.UNSYNCED
    )

    @Test fun `ReferalCache toDTO maps benId and reason`() {
        val dto = referal().copy(referralReason = "fever", referredToInstituteID = 7).toDTO()
        assertEquals(30L, dto.benId)
        assertEquals("fever", dto.referralReason)
        assertEquals(7, dto.referredToInstituteID)
    }

    @Test fun `ReferalCache toDTO copies benId into beneficiaryRegID`() {
        val dto = referal().toDTO()
        assertEquals(30L, dto.beneficiaryRegID)
        assertEquals(SyncState.SYNCED, dto.syncState)
    }

    @Test fun `ReferalCache toDTO passes through isSpecialist`() {
        val dto = referal().copy(isSpecialist = true).toDTO()
        assertEquals(true, dto.isSpecialist)
    }

    private fun referalDto() = NCDReferalDTO(
        benId = 40L,
        referredToInstituteID = 1,
        refrredToAdditionalServiceList = null,
        referredToInstituteName = "inst",
        referralReason = "cough",
        revisitDate = "2024-01-01",
        vanID = 2,
        parkingPlaceID = 3,
        beneficiaryRegID = 40L,
        benVisitID = 4L,
        visitCode = 5L,
        providerServiceMapID = 6,
        createdBy = "creator",
        type = "typeA"
    )

    @Test fun `NCDReferalDTO toCache maps benId and reason and forces synced`() {
        val cache = referalDto().toCache()
        assertEquals(40L, cache.benId)
        assertEquals("cough", cache.referralReason)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `NCDReferalDTO toCache forces isSpecialist false`() {
        val cache = referalDto().copy(isSpecialist = true).toCache()
        assertFalse(cache.isSpecialist!!)
    }

    @Test fun `Converters toStringList splits and trims`() {
        val list = Converters().toStringList("a, b ,c")
        assertEquals(listOf("a", "b", "c"), list)
    }

    @Test fun `Converters toStringList null returns null`() {
        assertNull(Converters().toStringList(null))
    }

    @Test fun `Converters fromStringList joins with comma`() {
        assertEquals("a,b,c", Converters().fromStringList(listOf("a", "b", "c")))
    }

    @Test fun `Converters fromStringList null returns null`() {
        assertNull(Converters().fromStringList(null))
    }

    // =====================================================
    // ABHAModel.toDTO()  /  ABHAGeneratedDTO.toCache()
    // =====================================================

    private fun abha() = ABHAModel(
        beneficiaryID = 50L,
        beneficiaryRegID = 51L,
        benName = "Asha",
        createdBy = "creator",
        message = "ok",
        txnId = "txn-1",
        providerServiceMapId = 9
    )

    @Test fun `ABHAModel toDTO maps identity fields`() {
        val dto = abha().toDTO()
        assertEquals(50L, dto.beneficiaryID)
        assertEquals(51L, dto.beneficiaryRegID)
        assertEquals("Asha", dto.benName)
        assertEquals("txn-1", dto.txnId)
    }

    @Test fun `ABHAModel toDTO maps health and isNewAbha`() {
        val dto = abha().copy(healthId = "hid", isNewAbha = true).toDTO()
        assertEquals("hid", dto.healthId)
        assertEquals(true, dto.isNewAbha)
    }

    private fun abhaDto() = ABHAGeneratedDTO(
        beneficiaryID = 60L,
        beneficiaryRegID = 61L,
        benName = "Bala",
        createdBy = "creator",
        message = "ok",
        txnId = "txn-2",
        providerServiceMapId = 8
    )

    @Test fun `ABHAGeneratedDTO toCache maps identity fields`() {
        val cache = abhaDto().toCache()
        assertEquals(60L, cache.beneficiaryID)
        assertEquals("Bala", cache.benName)
        assertEquals(8, cache.providerServiceMapId)
    }

    @Test fun `ABHAGeneratedDTO toCache default syncState is UNSYNCED`() {
        assertEquals(SyncState.UNSYNCED, abhaDto().toCache().syncState)
    }

    // =====================================================
    // GeneralOPDNetwork.asGeneralCacheModel()
    // =====================================================

    private fun generalOpd() = GeneralOPDNetwork(
        benFlowID = null, beneficiaryRegID = null, benVisitID = null, visitCode = null,
        benVisitNo = null, nurseFlag = null, doctorFlag = null, pharmacist_flag = null,
        lab_technician_flag = null, radiologist_flag = null, oncologist_flag = null,
        specialist_flag = null, agentId = null, visitDate = null, modified_by = null,
        modified_date = null, benName = "Ravi", deleted = null, firstName = "Ravi",
        lastName = "K", age = "35", ben_age_val = null, genderID = null, genderName = null,
        preferredPhoneNum = null, fatherName = null, spouseName = null, districtName = null,
        servicePointName = null, registrationDate = null, benVisitDate = null,
        consultationDate = null, consultantID = null, consultantName = null,
        visitSession = null, servicePointID = null, districtID = null, villageID = null,
        vanID = null, beneficiaryId = 70L, dob = null, tc_SpecialistLabFlag = null,
        visitReason = null, village = null, visitCategory = null
    )

    @Test fun `GeneralOPDNetwork asGeneralCacheModel maps beneficiaryId and name`() {
        val cache = generalOpd().asGeneralCacheModel()
        assertEquals(70L, cache.beneficiaryId)
        assertEquals("Ravi", cache.benName)
        assertEquals("35", cache.age)
    }

    @Test fun `GeneralOPDNetwork asGeneralCacheModel passes through firstName`() {
        val cache = generalOpd().asGeneralCacheModel()
        assertEquals("Ravi", cache.firstName)
        assertEquals("K", cache.lastName)
    }

    // =====================================================
    // Incentives mappers
    // =====================================================

    private fun activityNetwork() = IncentiveActivityNetwork(
        id = 80L, name = "act", description = "desc", paymentParam = "pp",
        rate = 100, state = 1, district = 2, group = "g", groupName = "gn",
        fmrCode = "fmr", fmrCodeOld = "fmrOld", createdDate = "2024-01-01",
        createdBy = "creator", updatedDate = "2024-01-02", updatedBy = "updater"
    )

    @Test fun `IncentiveActivityNetwork asCacheModel maps core fields`() {
        val cache = activityNetwork().asCacheModel()
        assertEquals(80L, cache.id)
        assertEquals("act", cache.name)
        assertEquals(100, cache.rate)
        assertEquals("g", cache.group)
        assertEquals("fmr", cache.fmrCode)
    }

    private fun recordNetwork() = IncentiveRecordNetwork(
        id = 90L, activityId = 80L, ashaId = 5, benId = 91L, amount = 250L,
        startDate = "2024-01-01", endDate = "2024-02-01", createdDate = "2024-01-01",
        createdBy = "creator", updatedDate = "2024-01-02", updatedBy = "updater",
        isEligible = true, verifiedByUserName = null, reason = null, otherReason = null,
        approvalStatus = 1, verifiedByUserId = 3, isClaimed = false,
        approvalDate = null, calimedDate = null, supervisorRole = null
    )

    @Test fun `IncentiveRecordNetwork asCacheModel maps core fields`() {
        val cache = recordNetwork().asCacheModel()
        assertEquals(90L, cache.id)
        assertEquals(91L, cache.benId)
        assertEquals(250L, cache.amount)
    }

    @Test fun `IncentiveRecordNetwork asCacheModel defaults null strings to empty`() {
        val cache = recordNetwork().asCacheModel()
        assertEquals("", cache.verifiedByUserName)
        assertEquals("", cache.reason)
        assertEquals("", cache.supervisorRole)
    }

    private fun activityCache() = IncentiveActivityCache(
        id = 80L, name = "act", description = "desc", paymentParam = "pp",
        rate = 100, state = 1, district = 2, group = "g", groupName = "gn",
        fmrCode = "fmr", fmrCodeOld = "fmrOld"
    )

    private fun recordCache() = IncentiveRecordCache(
        id = 90L, activityId = 80L, ashaId = 5, benId = 91L, amount = 250L, name = "n",
        startDate = 1L, endDate = 2L, createdDate = 1L, createdBy = "c", updatedDate = 2L,
        updatedBy = "u", isEligible = true, verifiedByUserName = "v", reason = "r",
        otherReason = "o", approvalStatus = 1, verifiedByUserId = 3, isClaimed = false,
        approvalDate = "d", calimedDate = "cd", supervisorRole = "role"
    )

    @Test fun `IncentiveCache asDomainModel keeps record and activity and null ben`() {
        val domain = IncentiveCache(record = recordCache(), activity = activityCache(), ben = null).asDomainModel()
        assertEquals(90L, domain.record.id)
        assertEquals(80L, domain.activity.id)
        assertNull(domain.ben)
    }

    @Test fun `IncentiveActivityWithRecords asDomainModel maps activity and records`() {
        val withRecords = IncentiveActivityWithRecords(
            activity = activityCache(),
            records = listOf(recordCache())
        )
        assertEquals(80L, withRecords.activity.id)
        assertEquals(1, withRecords.records.size)
        val domain = withRecords.asDomainModel()
        assertEquals(80L, domain.activity.id)
        assertEquals(1, domain.records.size)
    }

    @Test fun `UserDomain exposes districts and blocks`() {
        val loc = LocationEntity(1, "loc")
        val user = UserDomain(
            userId = 1,
            userName = "u",
            password = "p",
            country = LocationEntity(0, "India"),
            states = listOf(loc),
            districts = listOf(loc),
            blocks = listOf(loc),
            villages = listOf(loc),
            contactNo = "999",
            userType = "asha",
            loggedIn = true
        )
        assertEquals(1, user.districts.size)
        assertEquals(1, user.blocks.size)
    }
}
