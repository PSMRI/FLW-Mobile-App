package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in PMSMA.kt:
 *  - PMSMACache.asPostModel()
 *  - PmsmaPost.toPmsmaCache()
 */
class PmsmaMappingTest {

    // ---------------------------------------------------------------
    // PMSMACache.asPostModel()
    // ---------------------------------------------------------------

    @Test
    fun `PMSMACache asPostModel maps core fields`() {
        val cache = PMSMACache(
            id = 3L,
            benId = 88L,
            visitNumber = 2,
            isActive = true,
            haveMCPCard = true,
            husbandName = "Ravi",
            numANC = 4,
            weight = 60,
            systolicBloodPressure = "120",
            bloodPressure = "80",
            twinPregnancy = false,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertEquals(3L, post.id)
        assertEquals(88L, post.benId)
        assertEquals(2, post.visitNumber)
        assertTrue(post.isActive)
        assertTrue(post.haveMCPCard)
        assertEquals("Ravi", post.husbandName)
        assertEquals(4, post.numANC)
        assertEquals(60, post.weight)
        assertEquals(120, post.systolicBloodPressure)
        assertEquals(80, post.diastolicBloodPressure)
        assertFalse(post.twinPregnancy)
    }

    @Test
    fun `PMSMACache asPostModel maps null blood pressures`() {
        val cache = PMSMACache(
            benId = 1L,
            visitNumber = 1,
            isActive = true,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertNull(post.systolicBloodPressure)
        assertNull(post.diastolicBloodPressure)
    }

    // ---------------------------------------------------------------
    // PmsmaPost.toPmsmaCache()
    // ---------------------------------------------------------------

    @Test
    fun `PmsmaPost toPmsmaCache maps core fields and sets synced`() {
        val post = PmsmaPost(
            id = 4L,
            benId = 99L,
            visitNumber = 1,
            isActive = true,
            rchNumber = "RCH-9",
            haveMCPCard = true,
            husbandName = "Mohan",
            numANC = 2,
            weight = 55,
            systolicBloodPressure = 118,
            diastolicBloodPressure = 76,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toPmsmaCache()

        assertEquals(4L, cache.id)
        assertEquals(99L, cache.benId)
        assertEquals(1, cache.visitNumber)
        assertTrue(cache.isActive)
        assertEquals("RCH-9", cache.mctsNumberOrRchNumber)
        assertTrue(cache.haveMCPCard)
        assertEquals("Mohan", cache.husbandName)
        assertEquals(2, cache.numANC)
        assertEquals(55, cache.weight)
        assertEquals("118", cache.systolicBloodPressure)
        assertEquals("76", cache.bloodPressure)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------------------------------------------------------
    // Branch-variant coverage: visitDate presence, optional clinical
    // fields, and generated data-class methods.
    // ---------------------------------------------------------------

    @Test
    fun `PMSMACache asPostModel maps visitDate and optional clinical fields`() {
        val cache = PMSMACache(
            id = 10L,
            benId = 5L,
            visitDate = 1_650_000_000_000L,
            visitNumber = 3,
            anyOtherHighRiskCondition = "Anemia",
            isActive = true,
            givenMCPCard = true,
            address = "Village Road",
            mobileNumber = 9876543210L,
            fetalHRPM = 140,
            twinPregnancy = true,
            urineAlbumin = "Nil",
            haemoglobinAndBloodGroup = "O+",
            hiv = "Negative",
            vdrl = "Negative",
            hbsc = "Negative",
            malaria = "Negative",
            hivTestDuringANC = true,
            swollenCondtion = true,
            bloodSugarTest = true,
            ultraSound = true,
            ironFolicAcid = true,
            calciumSupplementation = true,
            tetanusToxoid = "TT1",
            lastMenstrualPeriod = 1_600_000_000_000L,
            expectedDateOfDelivery = 1_620_000_000_000L,
            highriskSymbols = true,
            highRiskReason = "Hypertension",
            highRiskPregnant = true,
            highRiskPregnancyReferred = true,
            birthPrepAndNutritionAndFamilyPlanning = true,
            medicalOfficerSign = "Dr. Sharma",
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertNotNull(post.visitDate)
        assertEquals(3, post.visitNumber)
        assertEquals("Anemia", post.anyOtherHighRiskCondition)
        assertTrue(post.givenMCPCard!!)
        assertEquals("Village Road", post.address)
        assertEquals(9876543210L, post.mobileNumber)
        assertEquals(140, post.fetalHRPM)
        assertTrue(post.twinPregnancy)
        assertEquals("Nil", post.urineAlbumin)
        assertEquals("O+", post.haemoglobinAndBloodGroup)
        assertEquals("Negative", post.hiv)
        assertEquals("Negative", post.vdrl)
        assertEquals("Negative", post.hbsc)
        assertEquals("Negative", post.malaria)
        assertTrue(post.hivTestDuringANC!!)
        assertTrue(post.swollenCondition!!)
        assertTrue(post.bloodSugarTest!!)
        assertTrue(post.ultraSound!!)
        assertTrue(post.ironFolicAcid!!)
        assertTrue(post.calciumSupplementation!!)
        assertEquals("TT1", post.tetanusToxoid)
        assertNotNull(post.lastMenstrualPeriod)
        assertNotNull(post.expectedDateOfDelivery)
        assertTrue(post.highriskSymbols!!)
        assertEquals("Hypertension", post.highRiskReason)
        assertTrue(post.highRiskPregnant!!)
        assertTrue(post.highRiskPregnancyReferred!!)
        assertTrue(post.birthPrepNutriAndFamilyPlanning!!)
        assertEquals("Dr. Sharma", post.medicalOfficerSign)
    }

    @Test
    fun `PMSMACache asPostModel with null visitDate keeps post visitDate null`() {
        val cache = PMSMACache(
            benId = 2L,
            visitDate = null,
            visitNumber = 1,
            isActive = false,
            givenMCPCard = null,
            twinPregnancy = false,
            hivTestDuringANC = null,
            swollenCondtion = null,
            bloodSugarTest = null,
            ultraSound = null,
            ironFolicAcid = null,
            calciumSupplementation = null,
            highriskSymbols = null,
            highRiskPregnant = null,
            highRiskPregnancyReferred = null,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.SYNCED
        )

        val post = cache.asPostModel()

        assertNull(post.visitDate)
        assertFalse(post.isActive)
        assertNull(post.givenMCPCard)
        assertNull(post.hivTestDuringANC)
        assertNull(post.swollenCondition)
        assertNull(post.bloodSugarTest)
        assertNull(post.ultraSound)
        assertNull(post.ironFolicAcid)
        assertNull(post.calciumSupplementation)
        assertNull(post.highriskSymbols)
        assertNull(post.highRiskPregnant)
        assertNull(post.highRiskPregnancyReferred)
    }

    @Test
    fun `PmsmaPost toPmsmaCache maps null blood pressures to literal null string`() {
        val post = PmsmaPost(
            benId = 1L,
            visitNumber = 1,
            isActive = true,
            systolicBloodPressure = null,
            diastolicBloodPressure = null,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toPmsmaCache()

        assertEquals("null", cache.systolicBloodPressure)
        assertEquals("null", cache.bloodPressure)
    }

    @Test
    fun `PmsmaPost toPmsmaCache maps null visitDate and date strings to defaults`() {
        val post = PmsmaPost(
            benId = 6L,
            visitNumber = 1,
            isActive = false,
            visitDate = null,
            lastMenstrualPeriod = null,
            expectedDateOfDelivery = null,
            createdDate = null,
            updatedDate = null,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toPmsmaCache()

        assertEquals(0L, cache.visitDate)
        assertEquals(0L, cache.lastMenstrualPeriod)
        assertEquals(0L, cache.expectedDateOfDelivery)
        assertEquals(0L, cache.createdDate)
        assertEquals(0L, cache.updatedDate)
    }

    @Test
    fun `PmsmaPost toPmsmaCache maps all optional clinical fields`() {
        val post = PmsmaPost(
            id = 7L,
            benId = 8L,
            visitNumber = 2,
            anyOtherHighRiskCondition = "Diabetes",
            isActive = true,
            rchNumber = "RCH-1",
            givenMCPCard = true,
            husbandName = "Suresh",
            address = "Sector 5",
            mobileNumber = 9123456780L,
            numANC = 3,
            weight = 62,
            abdominalCheckUp = "Normal",
            fetalHRPM = 145,
            twinPregnancy = true,
            urineAlbumin = "Trace",
            haemoglobinAndBloodGroup = "B+",
            hiv = "Negative",
            vdrl = "Negative",
            hbsc = "Negative",
            malaria = "Negative",
            hivTestDuringANC = true,
            swollenCondition = true,
            bloodSugarTest = true,
            ultraSound = true,
            ironFolicAcid = true,
            calciumSupplementation = true,
            tetanusToxoid = "TT2",
            highriskSymbols = true,
            highRiskReason = "PIH",
            highRiskPregnant = true,
            highRiskPregnancyReferred = false,
            birthPrepNutriAndFamilyPlanning = true,
            medicalOfficerSign = "Dr. Rao",
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toPmsmaCache()

        assertEquals("Diabetes", cache.anyOtherHighRiskCondition)
        assertTrue(cache.givenMCPCard!!)
        assertEquals("Suresh", cache.husbandName)
        assertEquals("Sector 5", cache.address)
        assertEquals(9123456780L, cache.mobileNumber)
        assertEquals(3, cache.numANC)
        assertEquals(62, cache.weight)
        assertEquals("Normal", cache.abdominalCheckUp)
        assertEquals(145, cache.fetalHRPM)
        assertTrue(cache.twinPregnancy)
        assertEquals("Trace", cache.urineAlbumin)
        assertEquals("B+", cache.haemoglobinAndBloodGroup)
        assertTrue(cache.hivTestDuringANC!!)
        assertTrue(cache.swollenCondtion!!)
        assertTrue(cache.bloodSugarTest!!)
        assertTrue(cache.ultraSound!!)
        assertTrue(cache.ironFolicAcid!!)
        assertTrue(cache.calciumSupplementation!!)
        assertEquals("TT2", cache.tetanusToxoid)
        assertTrue(cache.highriskSymbols!!)
        assertEquals("PIH", cache.highRiskReason)
        assertTrue(cache.highRiskPregnant!!)
        assertFalse(cache.highRiskPregnancyReferred!!)
        assertTrue(cache.birthPrepAndNutritionAndFamilyPlanning!!)
        assertEquals("Dr. Rao", cache.medicalOfficerSign)
        assertEquals("P", cache.processed)
    }

    @Test
    fun `PMSMACache equals hashCode copy and toString cover generated methods`() {
        val base = PMSMACache(
            id = 1L,
            benId = 1L,
            visitDate = 1_600_000_000_000L,
            visitNumber = 1,
            anyOtherHighRiskCondition = "x",
            isActive = true,
            mctsNumberOrRchNumber = "RCH",
            haveMCPCard = true,
            givenMCPCard = true,
            husbandName = "H",
            address = "A",
            mobileNumber = 123L,
            numANC = 1,
            weight = 50,
            systolicBloodPressure = "120",
            bloodPressure = "80",
            abdominalCheckUp = "N",
            fetalHRPM = 140,
            twinPregnancy = true,
            urineAlbumin = "Nil",
            haemoglobinAndBloodGroup = "O+",
            hiv = "N",
            vdrl = "N",
            hbsc = "N",
            malaria = "N",
            hivTestDuringANC = true,
            swollenCondtion = true,
            bloodSugarTest = true,
            ultraSound = true,
            ironFolicAcid = true,
            calciumSupplementation = true,
            tetanusToxoid = "TT",
            lastMenstrualPeriod = 1_500_000_000_000L,
            expectedDateOfDelivery = 1_520_000_000_000L,
            highriskSymbols = true,
            highRiskReason = "R",
            highRiskPregnant = true,
            highRiskPregnancyReferred = true,
            birthPrepAndNutritionAndFamilyPlanning = true,
            medicalOfficerSign = "Dr",
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.SYNCED
        )
        val sameValues = base.copy()
        val different = base.copy(visitNumber = 2)

        assertEquals(base, sameValues)
        assertEquals(base.hashCode(), sameValues.hashCode())
        assertTrue(base.toString().contains("PMSMACache"))
        assertFalse(base == different)
        assertFalse(base.equals(null))
        assertFalse(base.equals("not a cache"))
        assertEquals(base, base)

        base.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(base) } }
    }

    @Test
    fun `PMSMACache copy overrides every constructor field`() {
        val base = PMSMACache(
            id = 1L,
            benId = 1L,
            visitDate = 1_600_000_000_000L,
            visitNumber = 1,
            anyOtherHighRiskCondition = "x",
            isActive = true,
            mctsNumberOrRchNumber = "RCH",
            haveMCPCard = true,
            givenMCPCard = true,
            husbandName = "H",
            address = "A",
            mobileNumber = 123L,
            numANC = 1,
            weight = 50,
            systolicBloodPressure = "120",
            bloodPressure = "80",
            abdominalCheckUp = "N",
            fetalHRPM = 140,
            twinPregnancy = true,
            urineAlbumin = "Nil",
            haemoglobinAndBloodGroup = "O+",
            hiv = "N",
            vdrl = "N",
            hbsc = "N",
            malaria = "N",
            hivTestDuringANC = true,
            swollenCondtion = true,
            bloodSugarTest = true,
            ultraSound = true,
            ironFolicAcid = true,
            calciumSupplementation = true,
            tetanusToxoid = "TT",
            lastMenstrualPeriod = 1_500_000_000_000L,
            expectedDateOfDelivery = 1_520_000_000_000L,
            highriskSymbols = true,
            highRiskReason = "R",
            highRiskPregnant = true,
            highRiskPregnancyReferred = true,
            birthPrepAndNutritionAndFamilyPlanning = true,
            medicalOfficerSign = "Dr",
            processed = "N",
            createdBy = "c",
            createdDate = 1_400_000_000_000L,
            updatedBy = "u",
            updatedDate = 1_450_000_000_000L,
            syncState = SyncState.SYNCED
        )

        val overridden = base.copy(
            id = 2L,
            benId = 2L,
            visitDate = 1_601_000_000_000L,
            visitNumber = 2,
            anyOtherHighRiskCondition = "y",
            isActive = false,
            mctsNumberOrRchNumber = "RCH-2",
            haveMCPCard = false,
            givenMCPCard = false,
            husbandName = "H2",
            address = "A2",
            mobileNumber = 456L,
            numANC = 2,
            weight = 55,
            systolicBloodPressure = "130",
            bloodPressure = "90",
            abdominalCheckUp = "Y",
            fetalHRPM = 150,
            twinPregnancy = false,
            urineAlbumin = "Trace",
            haemoglobinAndBloodGroup = "B+",
            hiv = "P",
            vdrl = "P",
            hbsc = "P",
            malaria = "P",
            hivTestDuringANC = false,
            swollenCondtion = false,
            bloodSugarTest = false,
            ultraSound = false,
            ironFolicAcid = false,
            calciumSupplementation = false,
            tetanusToxoid = "TT2",
            lastMenstrualPeriod = 1_501_000_000_000L,
            expectedDateOfDelivery = 1_521_000_000_000L,
            highriskSymbols = false,
            highRiskReason = "R2",
            highRiskPregnant = false,
            highRiskPregnancyReferred = false,
            birthPrepAndNutritionAndFamilyPlanning = false,
            medicalOfficerSign = "Dr2",
            processed = "P",
            createdBy = "c2",
            createdDate = 1_401_000_000_000L,
            updatedBy = "u2",
            updatedDate = 1_451_000_000_000L,
            syncState = SyncState.UNSYNCED
        )

        assertFalse(base == overridden)
        assertEquals(2L, overridden.id)
        assertEquals("RCH-2", overridden.mctsNumberOrRchNumber)
        assertFalse(overridden.isActive)
        assertEquals(SyncState.UNSYNCED, overridden.syncState)
    }
}
