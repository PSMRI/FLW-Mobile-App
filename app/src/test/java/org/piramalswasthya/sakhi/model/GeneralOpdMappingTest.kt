package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneralOpdMappingTest {

    private fun network(
        deleted: Boolean? = false,
        benName: String? = "Ben Name"
    ) = GeneralOPDNetwork(
        benFlowID = 1L,
        beneficiaryRegID = 2L,
        benVisitID = 3L,
        visitCode = 4L,
        benVisitNo = 5,
        nurseFlag = 6,
        doctorFlag = 7,
        pharmacist_flag = 8,
        lab_technician_flag = 9,
        radiologist_flag = 10,
        oncologist_flag = 11,
        specialist_flag = 12,
        agentId = "agent",
        visitDate = "01-01-2024",
        modified_by = "modifier",
        modified_date = "02-01-2024",
        benName = benName,
        deleted = deleted,
        firstName = "First",
        lastName = "Last",
        age = "30",
        ben_age_val = 30,
        genderID = 2,
        genderName = "Female",
        preferredPhoneNum = "9999999999",
        fatherName = "Father",
        spouseName = "Spouse",
        districtName = "District",
        servicePointName = "ServicePoint",
        registrationDate = "03-01-2024",
        benVisitDate = "04-01-2024",
        consultationDate = "05-01-2024",
        consultantID = 13L,
        consultantName = "Consultant",
        visitSession = "Morning",
        servicePointID = 14L,
        districtID = 15L,
        villageID = 16L,
        vanID = 17L,
        beneficiaryId = 18L,
        dob = "01-01-1994",
        tc_SpecialistLabFlag = 19,
        visitReason = "Reason",
        village = "Village",
        visitCategory = "Category"
    )

    @Test
    fun `asGeneralCacheModel maps identifier fields`() {
        val cache = network().asGeneralCacheModel()
        assertEquals(1L, cache.benFlowID)
        assertEquals(2L, cache.beneficiaryRegID)
        assertEquals(3L, cache.benVisitID)
        assertEquals(4L, cache.visitCode)
        assertEquals(5, cache.benVisitNo)
        assertEquals(18L, cache.beneficiaryId)
        assertEquals(13L, cache.consultantID)
        assertEquals(14L, cache.servicePointID)
        assertEquals(15L, cache.districtID)
        assertEquals(16L, cache.villageID)
        assertEquals(17L, cache.vanID)
    }

    @Test
    fun `asGeneralCacheModel maps worker flags`() {
        val cache = network().asGeneralCacheModel()
        assertEquals(6, cache.nurseFlag)
        assertEquals(7, cache.doctorFlag)
        assertEquals(8, cache.pharmacist_flag)
        assertEquals(9, cache.lab_technician_flag)
        assertEquals(10, cache.radiologist_flag)
        assertEquals(11, cache.oncologist_flag)
        assertEquals(12, cache.specialist_flag)
        assertEquals(19, cache.tc_SpecialistLabFlag)
    }

    @Test
    fun `asGeneralCacheModel maps beneficiary details`() {
        val cache = network().asGeneralCacheModel()
        assertEquals("agent", cache.agentId)
        assertEquals("01-01-2024", cache.visitDate)
        assertEquals("modifier", cache.modified_by)
        assertEquals("02-01-2024", cache.modified_date)
        assertEquals("Ben Name", cache.benName)
        assertEquals(false, cache.deleted)
        assertEquals("First", cache.firstName)
        assertEquals("Last", cache.lastName)
        assertEquals("30", cache.age)
        assertEquals(30, cache.ben_age_val)
        assertEquals(2, cache.genderID)
        assertEquals("Female", cache.genderName)
        assertEquals("9999999999", cache.preferredPhoneNum)
        assertEquals("Father", cache.fatherName)
        assertEquals("Spouse", cache.spouseName)
        assertEquals("01-01-1994", cache.dob)
    }

    @Test
    fun `asGeneralCacheModel maps visit and location details`() {
        val cache = network().asGeneralCacheModel()
        assertEquals("District", cache.districtName)
        assertEquals("ServicePoint", cache.servicePointName)
        assertEquals("03-01-2024", cache.registrationDate)
        assertEquals("04-01-2024", cache.benVisitDate)
        assertEquals("05-01-2024", cache.consultationDate)
        assertEquals("Consultant", cache.consultantName)
        assertEquals("Morning", cache.visitSession)
        assertEquals("Reason", cache.visitReason)
        assertEquals("Village", cache.village)
        assertEquals("Category", cache.visitCategory)
    }

    @Test
    fun `network fields are readable directly`() {
        val net = network(deleted = true, benName = null)
        assertEquals(1L, net.benFlowID)
        assertEquals(2L, net.beneficiaryRegID)
        assertEquals(3L, net.benVisitID)
        assertEquals(4L, net.visitCode)
        assertEquals(5, net.benVisitNo)
        assertEquals(6, net.nurseFlag)
        assertEquals(7, net.doctorFlag)
        assertEquals(8, net.pharmacist_flag)
        assertEquals(9, net.lab_technician_flag)
        assertEquals(10, net.radiologist_flag)
        assertEquals(11, net.oncologist_flag)
        assertEquals(12, net.specialist_flag)
        assertEquals("agent", net.agentId)
        assertEquals("01-01-2024", net.visitDate)
        assertEquals("modifier", net.modified_by)
        assertEquals("02-01-2024", net.modified_date)
        assertNull(net.benName)
        assertEquals(true, net.deleted)
        assertEquals("First", net.firstName)
        assertEquals("Last", net.lastName)
        assertEquals("30", net.age)
        assertEquals(30, net.ben_age_val)
        assertEquals(2, net.genderID)
        assertEquals("Female", net.genderName)
        assertEquals("9999999999", net.preferredPhoneNum)
        assertEquals("Father", net.fatherName)
        assertEquals("Spouse", net.spouseName)
        assertEquals("District", net.districtName)
        assertEquals("ServicePoint", net.servicePointName)
        assertEquals("03-01-2024", net.registrationDate)
        assertEquals("04-01-2024", net.benVisitDate)
        assertEquals("05-01-2024", net.consultationDate)
        assertEquals(13L, net.consultantID)
        assertEquals("Consultant", net.consultantName)
        assertEquals("Morning", net.visitSession)
        assertEquals(14L, net.servicePointID)
        assertEquals(15L, net.districtID)
        assertEquals(16L, net.villageID)
        assertEquals(17L, net.vanID)
        assertEquals(18L, net.beneficiaryId)
        assertEquals("01-01-1994", net.dob)
        assertEquals(19, net.tc_SpecialistLabFlag)
        assertEquals("Reason", net.visitReason)
        assertEquals("Village", net.village)
        assertEquals("Category", net.visitCategory)
    }

    @Test
    fun `null network fields map through to a null cache`() {
        val net = GeneralOPDNetwork(
            benFlowID = null,
            beneficiaryRegID = null,
            benVisitID = null,
            visitCode = null,
            benVisitNo = null,
            nurseFlag = null,
            doctorFlag = null,
            pharmacist_flag = null,
            lab_technician_flag = null,
            radiologist_flag = null,
            oncologist_flag = null,
            specialist_flag = null,
            agentId = null,
            visitDate = null,
            modified_by = null,
            modified_date = null,
            benName = null,
            deleted = null,
            firstName = null,
            lastName = null,
            age = null,
            ben_age_val = null,
            genderID = null,
            genderName = null,
            preferredPhoneNum = null,
            fatherName = null,
            spouseName = null,
            districtName = null,
            servicePointName = null,
            registrationDate = null,
            benVisitDate = null,
            consultationDate = null,
            consultantID = null,
            consultantName = null,
            visitSession = null,
            servicePointID = null,
            districtID = null,
            villageID = null,
            vanID = null,
            beneficiaryId = 99L,
            dob = null,
            tc_SpecialistLabFlag = null,
            visitReason = null,
            village = null,
            visitCategory = null
        )

        val cache = net.asGeneralCacheModel()

        assertEquals(99L, cache.beneficiaryId)
        assertNull(cache.benFlowID)
        assertNull(cache.beneficiaryRegID)
        assertNull(cache.benVisitID)
        assertNull(cache.visitCode)
        assertNull(cache.benVisitNo)
        assertNull(cache.nurseFlag)
        assertNull(cache.doctorFlag)
        assertNull(cache.pharmacist_flag)
        assertNull(cache.lab_technician_flag)
        assertNull(cache.radiologist_flag)
        assertNull(cache.oncologist_flag)
        assertNull(cache.specialist_flag)
        assertNull(cache.agentId)
        assertNull(cache.visitDate)
        assertNull(cache.modified_by)
        assertNull(cache.modified_date)
        assertNull(cache.benName)
        assertNull(cache.deleted)
        assertNull(cache.firstName)
        assertNull(cache.lastName)
        assertNull(cache.age)
        assertNull(cache.ben_age_val)
        assertNull(cache.genderID)
        assertNull(cache.genderName)
        assertNull(cache.preferredPhoneNum)
        assertNull(cache.fatherName)
        assertNull(cache.spouseName)
        assertNull(cache.districtName)
        assertNull(cache.servicePointName)
        assertNull(cache.registrationDate)
        assertNull(cache.benVisitDate)
        assertNull(cache.consultationDate)
        assertNull(cache.consultantID)
        assertNull(cache.consultantName)
        assertNull(cache.visitSession)
        assertNull(cache.servicePointID)
        assertNull(cache.districtID)
        assertNull(cache.villageID)
        assertNull(cache.vanID)
        assertNull(cache.dob)
        assertNull(cache.tc_SpecialistLabFlag)
        assertNull(cache.visitReason)
        assertNull(cache.village)
        assertNull(cache.visitCategory)
    }

    @Test
    fun `cache equality and copy behave as data class`() {
        val cache = network().asGeneralCacheModel()
        assertEquals(cache, cache.copy())
        assertEquals(cache.hashCode(), cache.copy().hashCode())
        assertTrue(cache.toString().contains("GeneralOPEDBeneficiary"))
    }
}
