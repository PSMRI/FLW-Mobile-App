package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FpotCacheAccessorTest {

    private fun cache() = FPOTCache(benId = 71L, hhId = 72L)

    @Test
    fun `identity fields and defaults are readable`() {
        val fpot = cache()
        assertEquals(0, fpot.id)
        assertEquals(71L, fpot.benId)
        assertEquals(72L, fpot.hhId)
        assertNull(fpot.monthlySerialNumber)
        assertNull(fpot.annualSerialNumber)
        assertNull(fpot.spouseName)
        assertNull(fpot.category)
        assertNull(fpot.benAddress)
        assertNull(fpot.contactNumber)
        assertNull(fpot.educationalQualification)
        assertNull(fpot.numChildren)
        assertNull(fpot.youngestChildAge)
        assertNull(fpot.sterilization)
        assertNull(fpot.mrCheckListFilled)
        assertEquals(0L, fpot.dateOfOperation)
        assertNull(fpot.femaleSterilization)
        assertEquals(0L, fpot.secondFollowUpExpectedDate)
        assertEquals(0L, fpot.followUpActualDate)
        assertNull(fpot.followUpDetails)
        assertNull(fpot.secondPostFollowUpCounselling)
        assertEquals(0L, fpot.thirdFollowUpExpectedDate)
        assertNull(fpot.menstruationStarted)
        assertNull(fpot.spermatozoaFoundInSemen)
        assertNull(fpot.thirdPostFollowUpCounselling)
        assertEquals(0L, fpot.sterilizationOrVasectomyIssueDate)
        assertNull(fpot.notIssuedReason)
        assertNull(fpot.sterilizationOrVasectomyDocSubmitted)
        assertNull(fpot.remarks)
        assertNull(fpot.createdBy)
        assertNotNull(fpot.createdDate)
        assertNull(fpot.processed)
    }

    @Test
    fun `beneficiary detail setters round trip`() {
        val fpot = cache()
        fpot.monthlySerialNumber = "M1"
        fpot.annualSerialNumber = "A1"
        fpot.spouseName = "Spouse"
        fpot.category = "OBC"
        fpot.benAddress = "Address"
        fpot.contactNumber = "9999999999"
        fpot.educationalQualification = "Graduate"
        fpot.numChildren = "2"
        fpot.youngestChildAge = "3"

        assertEquals("M1", fpot.monthlySerialNumber)
        assertEquals("A1", fpot.annualSerialNumber)
        assertEquals("Spouse", fpot.spouseName)
        assertEquals("OBC", fpot.category)
        assertEquals("Address", fpot.benAddress)
        assertEquals("9999999999", fpot.contactNumber)
        assertEquals("Graduate", fpot.educationalQualification)
        assertEquals("2", fpot.numChildren)
        assertEquals("3", fpot.youngestChildAge)
    }

    @Test
    fun `operation and follow up setters round trip`() {
        val fpot = cache()
        fpot.sterilization = true
        fpot.mrCheckListFilled = false
        fpot.dateOfOperation = 1_700_000_000_000L
        fpot.femaleSterilization = "Yes"
        fpot.secondFollowUpExpectedDate = 1_700_100_000_000L
        fpot.followUpActualDate = 1_700_200_000_000L
        fpot.followUpDetails = "details"
        fpot.secondPostFollowUpCounselling = "counselling"
        fpot.thirdFollowUpExpectedDate = 1_700_300_000_000L

        assertEquals(true, fpot.sterilization)
        assertEquals(false, fpot.mrCheckListFilled)
        assertEquals(1_700_000_000_000L, fpot.dateOfOperation)
        assertEquals("Yes", fpot.femaleSterilization)
        assertEquals(1_700_100_000_000L, fpot.secondFollowUpExpectedDate)
        assertEquals(1_700_200_000_000L, fpot.followUpActualDate)
        assertEquals("details", fpot.followUpDetails)
        assertEquals("counselling", fpot.secondPostFollowUpCounselling)
        assertEquals(1_700_300_000_000L, fpot.thirdFollowUpExpectedDate)
    }

    @Test
    fun `certificate and audit setters round trip`() {
        val fpot = cache()
        fpot.menstruationStarted = true
        fpot.spermatozoaFoundInSemen = "No"
        fpot.thirdPostFollowUpCounselling = "third"
        fpot.sterilizationOrVasectomyIssueDate = 1_700_400_000_000L
        fpot.notIssuedReason = "reason"
        fpot.sterilizationOrVasectomyDocSubmitted = "Yes"
        fpot.remarks = "remark"
        fpot.createdBy = "creator"
        fpot.createdDate = 1_700_500_000_000L
        fpot.processed = "U"

        assertEquals(true, fpot.menstruationStarted)
        assertEquals("No", fpot.spermatozoaFoundInSemen)
        assertEquals("third", fpot.thirdPostFollowUpCounselling)
        assertEquals(1_700_400_000_000L, fpot.sterilizationOrVasectomyIssueDate)
        assertEquals("reason", fpot.notIssuedReason)
        assertEquals("Yes", fpot.sterilizationOrVasectomyDocSubmitted)
        assertEquals("remark", fpot.remarks)
        assertEquals("creator", fpot.createdBy)
        assertEquals(1_700_500_000_000L, fpot.createdDate)
        assertEquals("U", fpot.processed)
    }

    @Test
    fun `equality and copy behave as data class`() {
        val fpot = cache().copy(remarks = "r")
        assertEquals(fpot, fpot.copy())
        assertEquals(fpot.hashCode(), fpot.copy().hashCode())
    }
}
