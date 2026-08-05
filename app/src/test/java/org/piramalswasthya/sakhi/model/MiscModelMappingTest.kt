package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.network.ABHAGeneratedDTO
import org.piramalswasthya.sakhi.network.NCDReferalDTO

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
        val domain = IncentiveActivityWithRecords(
            activity = activityCache(),
            records = listOf(recordCache())
        ).asDomainModel()
        assertEquals(80L, domain.activity.id)
        assertEquals(1, domain.records.size)
    }
}
