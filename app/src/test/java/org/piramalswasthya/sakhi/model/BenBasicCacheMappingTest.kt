package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Tests for the pure mapper/helper functions on [BenBasicCache] and its companion,
 * plus the top-level date/age helpers declared in Ben.kt.
 *
 * All of these are pure JVM logic (SimpleDateFormat / Calendar only) so no Android
 * mocking is required.
 */
class BenBasicCacheMappingTest {

    private val regDateVal = 1_600_000_000_000L
    private val adultDob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365L * 40)

    private val expectedRegDate =
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date(regDateVal))

    private fun ben(
        benId: Long = 1L,
        hhId: Long = 2L,
        regDate: Long = regDateVal,
        benName: String? = "John",
        benSurname: String? = "Doe",
        gender: Gender = Gender.MALE,
        dob: Long = adultDob,
        relToHeadId: Int = 3,
        mobileNo: Long = 9998887776L,
        fatherName: String? = "Bob",
        motherName: String? = "Mary",
        familyHeadName: String? = "Head",
        spouseName: String? = null,
        rchId: String? = "RCH1",
        hrpStatus: Boolean = false,
        syncState: SyncState? = SyncState.SYNCED,
        reproductiveStatusId: Int = 2,
        lastMenstrualPeriod: Long? = null,
        isKid: Boolean = false,
        immunizationStatus: Boolean = false,
        villageId: Int = 5,
        abhaId: String? = "abha1",
        isNewAbha: Boolean = true,
        cbacFilled: Boolean = false,
        cdrFilled: Boolean = false,
        mdsrFilled: Boolean = false,
        pmsmaFilled: Boolean = false,
        hbncFilled: Boolean = false,
        hbycFilled: Boolean = false,
        pwrFilled: Boolean = false,
        ecrFilled: Boolean = false,
        ectFilled: Boolean = false,
        tbsnFilled: Boolean = false,
        tbspFilled: Boolean = false,
        hrppaFilled: Boolean = false,
        hrpnpaFilled: Boolean = false,
        hrpmbpFilled: Boolean = false,
        hrptFilled: Boolean = false,
        hrptrackingDone: Boolean = false,
        hrnptrackingDone: Boolean = false,
        hrnptFilled: Boolean = false,
        isDelivered: Boolean = false,
        pwHrp: Boolean = false,
        irFilled: Boolean = false,
        isMdsr: Boolean = false,
        crFilled: Boolean = false,
        doFilled: Boolean = false,
        isConsent: Boolean = true,
        isSpouseAdded: Boolean = false,
        isChildrenAdded: Boolean = false,
        isMarried: Boolean = false
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
        isKid = isKid,
        immunizationStatus = immunizationStatus,
        villageId = villageId,
        abhaId = abhaId,
        isNewAbha = isNewAbha,
        cbacFilled = cbacFilled,
        cbacSyncState = SyncState.SYNCED,
        cdrFilled = cdrFilled,
        cdrSyncState = SyncState.SYNCED,
        mdsrFilled = mdsrFilled,
        mdsrSyncState = SyncState.SYNCED,
        pmsmaSyncState = SyncState.SYNCED,
        pmsmaFilled = pmsmaFilled,
        hbncFilled = hbncFilled,
        hbycFilled = hbycFilled,
        pwrFilled = pwrFilled,
        pwrSyncState = SyncState.SYNCED,
        doSyncState = SyncState.SYNCED,
        irSyncState = SyncState.SYNCED,
        crSyncState = SyncState.SYNCED,
        ecrFilled = ecrFilled,
        ectFilled = ectFilled,
        tbsnFilled = tbsnFilled,
        tbsnSyncState = SyncState.SYNCED,
        tbspFilled = tbspFilled,
        tbspSyncState = SyncState.SYNCED,
        hrppaFilled = hrppaFilled,
        hrpnpaFilled = hrpnpaFilled,
        hrpmbpFilled = hrpmbpFilled,
        hrptFilled = hrptFilled,
        hrptrackingDone = hrptrackingDone,
        hrnptrackingDone = hrnptrackingDone,
        hrnptFilled = hrnptFilled,
        hrppaSyncState = SyncState.SYNCED,
        hrpnpaSyncState = SyncState.SYNCED,
        hrpmbpSyncState = SyncState.SYNCED,
        hrptSyncState = SyncState.SYNCED,
        hrnptSyncState = SyncState.SYNCED,
        isDelivered = isDelivered,
        pwHrp = pwHrp,
        irFilled = irFilled,
        isMdsr = isMdsr,
        crFilled = crFilled,
        doFilled = doFilled,
        isConsent = isConsent,
        isSpouseAdded = isSpouseAdded,
        isChildrenAdded = isChildrenAdded,
        isMarried = isMarried
    )

    // ===================================================================
    // Companion helpers
    // ===================================================================

    @Test fun `getAgeFromDob newborn returns 0`() {
        assertEquals(0, BenBasicCache.getAgeFromDob(System.currentTimeMillis()))
    }

    @Test fun `getAgeFromDob adult returns years`() {
        assertEquals(40, BenBasicCache.getAgeFromDob(adultDob))
    }

    @Test fun `getAgeFromDob months branch is non-negative`() {
        val dob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60)
        assertTrue(BenBasicCache.getAgeFromDob(dob) >= 0)
    }

    @Test fun `getYearsFromDate parses and computes years`() {
        // A date well in the past -> a large positive number of years.
        assertTrue(BenBasicCache.getYearsFromDate("01-01-2000") >= 20)
    }

    @Test fun `getAgeUnitFromDob newborn is DAYS`() {
        assertEquals(AgeUnit.DAYS, BenBasicCache.getAgeUnitFromDob(System.currentTimeMillis()))
    }

    @Test fun `getAgeUnitFromDob adult is YEARS`() {
        assertEquals(AgeUnit.YEARS, BenBasicCache.getAgeUnitFromDob(adultDob))
    }

    // ===================================================================
    // Top-level helpers
    // ===================================================================

    @Test fun `getAgeDisplayString newborn shows days`() {
        assertEquals("0 Days", getAgeDisplayString(System.currentTimeMillis()))
    }

    @Test fun `getAgeDisplayString adult contains Year`() {
        assertTrue(getAgeDisplayString(adultDob).contains("Year"))
    }

    @Test fun `getEddFromLmp null returns null`() {
        assertNull(getEddFromLmp(null))
    }

    @Test fun `getEddFromLmp formats as date`() {
        val edd = getEddFromLmp(0L)
        assertTrue(edd!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test fun `getDateTimeStringFromLong null returns null`() {
        assertNull(getDateTimeStringFromLong(null))
    }

    @Test fun `getDateTimeStringFromLong has T separator and Z suffix`() {
        val s = getDateTimeStringFromLong(regDateVal)!!
        assertTrue(s.contains("T"))
        assertTrue(s.endsWith(".000Z"))
    }

    // ===================================================================
    // asBasicDomainModel
    // ===================================================================

    @Test fun `asBasicDomainModel maps core fields`() {
        val d = ben().asBasicDomainModel()
        assertEquals(1L, d.benId)
        assertEquals(2L, d.hhId)
        assertEquals("John", d.benName)
        assertEquals("Doe", d.benSurname)
        assertEquals("MALE", d.gender)
        assertEquals(expectedRegDate, d.regDate)
        assertEquals("9998887776", d.mobileNo)
        assertEquals(3, d.relToHeadId)
        assertEquals(2, d.reproductiveStatusId)
        assertEquals("abha1", d.abhaId)
        assertTrue(d.isNewAbha)
        assertEquals(SyncState.SYNCED, d.syncState)
    }

    @Test fun `asBasicDomainModel resolves name fallbacks`() {
        val d = ben(motherName = "Mary", fatherName = "Bob", familyHeadName = "Head", spouseName = null).asBasicDomainModel()
        assertEquals("Mary", d.motherName)
        assertEquals("Bob", d.fatherName)
        assertEquals("Head", d.familyHeadName)
        assertEquals("Not Available", d.spouseName)
        assertEquals("RCH1", d.rchId)
    }

    @Test fun `asBasicDomainModel null names give Not Available`() {
        val d = ben(motherName = null, fatherName = null, familyHeadName = null).asBasicDomainModel()
        assertEquals("Not Available", d.motherName)
        assertEquals("Not Available", d.fatherName)
        assertEquals("Not Available", d.familyHeadName)
    }

    // ===================================================================
    // asBasicDomainModelCHO
    // ===================================================================

    @Test fun `asBasicDomainModelCHO forces relToHeadId to 0`() {
        val d = ben().asBasicDomainModelCHO()
        assertEquals(0, d.relToHeadId)
        assertEquals(1L, d.benId)
        assertEquals("MALE", d.gender)
        // CHO variant uses a plain elvis for spouseName (no isNotEmpty check)
        assertEquals("Not Available", d.spouseName)
        assertEquals("Bob", d.fatherName)
        assertEquals("RCH1", d.rchId)
    }

    @Test fun `asBasicDomainModelCHO null familyHead gives empty string`() {
        val d = ben(familyHeadName = null).asBasicDomainModelCHO()
        assertEquals("", d.familyHeadName)
    }

    // ===================================================================
    // Form domain mappers (BenBasicDomainForForm)
    // ===================================================================

    @Test fun `asBasicDomainModelForFpotForm sets form1Filled false`() {
        val d = ben().asBasicDomainModelForFpotForm()
        assertEquals(1L, d.benId)
        assertEquals("Doe", d.benSurname)
        assertFalse(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
        assertTrue(d.isConsent)
    }

    @Test fun `asBenBasicDomainModelForTbsnForm uses tbsn state`() {
        val d = ben(tbsnFilled = true).asBenBasicDomainModelForTbsnForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
    }

    @Test fun `asBenBasicDomainModelForTbspForm uses tbsp state`() {
        val d = ben(tbspFilled = true).asBenBasicDomainModelForTbspForm()
        assertTrue(d.form1Filled)
    }

    @Test fun `asBenBasicDomainModelForCdrForm uses cdr state`() {
        val d = ben(cdrFilled = true).asBenBasicDomainModelForCdrForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
    }

    @Test fun `asBenBasicDomainModelForMdsrForm uses mdsr state`() {
        val d = ben(mdsrFilled = true).asBenBasicDomainModelForMdsrForm()
        assertTrue(d.form1Filled)
    }

    @Test fun `asBenBasicDomainModelForPmsmaForm uses pmsma filled`() {
        val d = ben(pmsmaFilled = true).asBenBasicDomainModelForPmsmaForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
    }

    @Test fun `asBenBasicDomainModelECTForm uses ect filled`() {
        val d = ben(ectFilled = true).asBenBasicDomainModelECTForm()
        assertTrue(d.form1Filled)
    }

    @Test fun `asBasicDomainModelForPmjayForm always false`() {
        val d = ben().asBasicDomainModelForPmjayForm()
        assertFalse(d.form1Filled)
    }

    @Test fun `asBenBasicDomainModelForHbncForm enabled when filled`() {
        val d = ben(hbncFilled = true).asBenBasicDomainModelForHbncForm()
        assertFalse(d.form1Filled)
        assertTrue(d.form1Enabled)
        assertFalse(d.isConsent)
    }

    @Test fun `asBenBasicDomainModelForHbncForm disabled for old dob`() {
        val d = ben(hbncFilled = false, dob = adultDob).asBenBasicDomainModelForHbncForm()
        assertFalse(d.form1Enabled)
    }

    @Test fun `asBenBasicDomainModelForHbycForm enabled when filled`() {
        val d = ben(hbycFilled = true).asBenBasicDomainModelForHbycForm()
        assertTrue(d.form1Enabled)
    }

    @Test fun `asBenBasicDomainModelForPregnantWomanRegistrationForm uses pwr`() {
        val d = ben(pwrFilled = true).asBenBasicDomainModelForPregnantWomanRegistrationForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
        assertFalse(d.isConsent)
    }

    @Test fun `asBenBasicDomainModelForHRPPregAssessmentForm maps forms`() {
        val d = ben(hrppaFilled = true, hrpmbpFilled = true).asBenBasicDomainModelForHRPPregAssessmentForm()
        assertTrue(d.form1Filled)
        assertTrue(d.form2Enabled)
        assertTrue(d.form2Filled)
    }

    @Test fun `asBenBasicDomainModelForHRPPregAssessmentForm null lmp gives null edd`() {
        val d = ben(lastMenstrualPeriod = null).asBenBasicDomainModelForHRPPregAssessmentForm()
        assertNull(d.lastMenstrualPeriod)
        assertNull(d.edd)
    }

    @Test fun `asBenBasicDomainModelForHRPNonPregAssessmentForm uses hrpnpa`() {
        val d = ben(hrpnpaFilled = true).asBenBasicDomainModelForHRPNonPregAssessmentForm()
        assertTrue(d.form1Filled)
    }

    @Test fun `asBenBasicDomainModelForHRPNonPregTrackForm toggles enable`() {
        val d = ben(hrnptrackingDone = false, hrnptFilled = true).asBenBasicDomainModelForHRPNonPregTrackForm()
        assertFalse(d.form1Filled)
        assertTrue(d.form1Enabled)
        assertTrue(d.form2Filled)
        assertTrue(d.form2Enabled)
    }

    @Test fun `asBenBasicDomainModelForHRPPregTrackForm toggles enable`() {
        val d = ben(hrptrackingDone = true, hrptFilled = true).asBenBasicDomainModelForHRPPregTrackForm()
        assertTrue(d.form1Filled)
        assertFalse(d.form1Enabled)
        assertTrue(d.form2Filled)
        assertTrue(d.form2Enabled)
    }

    @Test fun `asBenBasicDomainModelForInfantRegistrationForm uses irFilled`() {
        val d = ben(irFilled = true).asBenBasicDomainModelForInfantRegistrationForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
    }

    @Test fun `asBenBasicDomainModelForChildRegistrationForm uses irFilled and cr state`() {
        // Note: source maps form1Filled from irFilled (not crFilled) but syncState from crSyncState.
        val d = ben(irFilled = true).asBenBasicDomainModelForChildRegistrationForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
    }

    @Test fun `asBenBasicDomainModelForDeliveryOutcomeForm uses doFilled`() {
        val d = ben(doFilled = true).asBenBasicDomainModelForDeliveryOutcomeForm()
        assertTrue(d.form1Filled)
        assertTrue(d.isConsent)
    }

    @Test fun `asBenBasicDomainModelForEligibleCoupleRegistrationForm uses ecr`() {
        val d = ben(ecrFilled = true).asBenBasicDomainModelForEligibleCoupleRegistrationForm()
        assertTrue(d.form1Filled)
        assertEquals(SyncState.SYNCED, d.syncState)
    }
}
