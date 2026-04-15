package org.piramalswasthya.sakhi.model.dynamicEntity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC

class AllDynamicEntitiesTest {

    // =====================================================
    // ANCFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `ANCFormResponseJsonEntity creation`() {
        val e = ANCFormResponseJsonEntity(benId = 1L, visitDay = "Day 1", visitDate = "14-04-2026", formId = "anc_form_001", version = 1, formDataJson = "{}")
        assertEquals(1L, e.benId)
        assertEquals("Day 1", e.visitDay)
        assertEquals("14-04-2026", e.visitDate)
        assertEquals("anc_form_001", e.formId)
    }

    @Test fun `ANCFormResponseJsonEntity default id 0`() {
        val e = ANCFormResponseJsonEntity(benId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, e.id)
    }

    @Test fun `ANCFormResponseJsonEntity default isSynced false`() {
        val e = ANCFormResponseJsonEntity(benId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }

    @Test fun `ANCFormResponseJsonEntity default syncedAt null`() {
        val e = ANCFormResponseJsonEntity(benId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertNull(e.syncedAt)
    }

    @Test fun `ANCFormResponseJsonEntity copy`() {
        val e = ANCFormResponseJsonEntity(benId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        val copy = e.copy(benId = 99L)
        assertEquals(99L, copy.benId)
    }

    @Test fun `ANCFormResponseJsonEntity same key fields`() {
        val ts = 1000L
        val a = ANCFormResponseJsonEntity(id = 1, benId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}", createdAt = ts)
        val b = ANCFormResponseJsonEntity(id = 1, benId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}", createdAt = ts)
        assertEquals(a, b)
    }

    // =====================================================
    // CUFYFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `CUFYFormResponseJsonEntity creation`() {
        val e = CUFYFormResponseJsonEntity(benId = 1L, hhId = 2L, visitDate = "14-04-2026", formId = "ors_form_001", version = 1, formDataJson = "{}")
        assertEquals(1L, e.benId)
        assertEquals(2L, e.hhId)
    }

    @Test fun `CUFYFormResponseJsonEntity default id 0`() {
        val e = CUFYFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, e.id)
    }

    @Test fun `CUFYFormResponseJsonEntity default isSynced false`() {
        val e = CUFYFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }

    @Test fun `CUFYFormResponseJsonEntity has updatedAt`() {
        val e = CUFYFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertTrue(e.updatedAt > 0)
    }

    @Test fun `CUFYFormResponseJsonEntity copy`() {
        val e = CUFYFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        val copy = e.copy(formId = "new_form")
        assertEquals("new_form", copy.formId)
    }

    // =====================================================
    // FormResponseJsonEntityHBYC Tests
    // =====================================================

    @Test fun `FormResponseJsonEntityHBYC creation`() {
        val e = FormResponseJsonEntityHBYC(benId = 1L, hhId = 2L, visitDay = "3 Months", visitDate = "d", formId = "hbyc_form_001", version = 1, formDataJson = "{}")
        assertEquals(1L, e.benId)
        assertEquals(2L, e.hhId)
        assertEquals("3 Months", e.visitDay)
    }

    @Test fun `FormResponseJsonEntityHBYC default id 0`() {
        val e = FormResponseJsonEntityHBYC(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, e.id)
    }

    @Test fun `FormResponseJsonEntityHBYC default isSynced false`() {
        val e = FormResponseJsonEntityHBYC(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }

    @Test fun `FormResponseJsonEntityHBYC copy`() {
        val e = FormResponseJsonEntityHBYC(benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        val copy = e.copy(visitDay = "6 Months")
        assertEquals("6 Months", copy.visitDay)
    }

    @Test fun `FormResponseJsonEntityHBYC same key fields`() {
        val a = FormResponseJsonEntityHBYC(id = 1, benId = 1L, hhId = 1L, visitDay = "1", visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals(1L, a.hhId)
    }

    // =====================================================
    // NCDReferalFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `NCDReferalFormResponseJsonEntity creation`() {
        val e = NCDReferalFormResponseJsonEntity(benId = 1L, hhId = 2L, visitNo = 1, followUpNo = 1, treatmentStartDate = "14-04-2026", diagnosisCodes = "D001", formId = "ncd_form", version = 1, formDataJson = "{}")
        assertEquals(1L, e.benId)
        assertEquals(2L, e.hhId)
        assertEquals(1, e.visitNo)
        assertEquals(1, e.followUpNo)
        assertEquals("D001", e.diagnosisCodes)
    }

    @Test fun `NCDReferalFormResponseJsonEntity default followUpDate null`() {
        val e = NCDReferalFormResponseJsonEntity(benId = 1L, hhId = 1L, visitNo = 1, followUpNo = 1, treatmentStartDate = "d", diagnosisCodes = null, formId = "f", version = 1, formDataJson = "{}")
        assertNull(e.followUpDate)
    }

    @Test fun `NCDReferalFormResponseJsonEntity default isSynced false`() {
        val e = NCDReferalFormResponseJsonEntity(benId = 1L, hhId = 1L, visitNo = 1, followUpNo = 1, treatmentStartDate = "d", diagnosisCodes = null, formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }

    @Test fun `NCDReferalFormResponseJsonEntity copy`() {
        val e = NCDReferalFormResponseJsonEntity(benId = 1L, hhId = 1L, visitNo = 1, followUpNo = 1, treatmentStartDate = "d", diagnosisCodes = null, formId = "f", version = 1, formDataJson = "{}")
        val copy = e.copy(visitNo = 5)
        assertEquals(5, copy.visitNo)
    }

    // =====================================================
    // InfantEntity Tests
    // =====================================================

    @Test fun `InfantEntity creation`() {
        val e = InfantEntity(rchId = "RCH001", name = "Baby", motherName = "Mother", fatherName = "Father", dob = "14-04-2026", gender = "Male", phoneNumber = "9876543210")
        assertEquals("RCH001", e.rchId)
        assertEquals("Baby", e.name)
        assertEquals("Mother", e.motherName)
        assertEquals("Father", e.fatherName)
        assertEquals("Male", e.gender)
    }

    @Test fun `InfantEntity default id 0`() {
        val e = InfantEntity(rchId = "R", name = "N", motherName = "M", fatherName = null, dob = "d", gender = "F", phoneNumber = "p")
        assertEquals(0, e.id)
    }

    @Test fun `InfantEntity default sncuDischarged false`() {
        val e = InfantEntity(rchId = "R", name = "N", motherName = "M", fatherName = null, dob = "d", gender = "F", phoneNumber = "p")
        assertFalse(e.sncuDischarged)
    }

    @Test fun `InfantEntity fatherName can be null`() {
        val e = InfantEntity(rchId = "R", name = "N", motherName = "M", fatherName = null, dob = "d", gender = "F", phoneNumber = "p")
        assertNull(e.fatherName)
    }

    @Test fun `InfantEntity copy`() {
        val e = InfantEntity(rchId = "R", name = "N", motherName = "M", fatherName = "F", dob = "d", gender = "M", phoneNumber = "p")
        val copy = e.copy(name = "NewBaby")
        assertEquals("NewBaby", copy.name)
    }

    @Test fun `InfantEntity equals`() {
        val a = InfantEntity(id = 1, rchId = "R", name = "N", motherName = "M", fatherName = "F", dob = "d", gender = "M", phoneNumber = "p")
        val b = InfantEntity(id = 1, rchId = "R", name = "N", motherName = "M", fatherName = "F", dob = "d", gender = "M", phoneNumber = "p")
        assertEquals(a, b)
    }

    // =====================================================
    // NCDFollowUpResponse Tests
    // =====================================================

    @Test fun `NCDFollowUpResponse creation`() {
        val resp = NCDFollowUpResponse(200, emptyList())
        assertEquals(200, resp.statusCode)
        assertTrue(resp.data.isEmpty())
    }

    @Test fun `NCDFollowUpResponse with data`() {
        val req = FormNCDFollowUpSubmitRequest(1, 1L, 1L, 1, 1, "d", null, null, "f", 1, "{}")
        val resp = NCDFollowUpResponse(200, listOf(req))
        assertEquals(1, resp.data.size)
    }

    // =====================================================
    // FormNCDFollowUpSubmitRequest Tests
    // =====================================================

    @Test fun `FormNCDFollowUpSubmitRequest creation`() {
        val req = FormNCDFollowUpSubmitRequest(1, 1L, 2L, 1, 1, "14-04-2026", null, "D001", "ncd_form", 1, "{}")
        assertEquals(1, req.id)
        assertEquals(1L, req.benId)
        assertEquals(2L, req.hhId)
        assertEquals(1, req.visitNo)
        assertEquals(1, req.followUpNo)
        assertEquals("D001", req.diagnosisCodes)
    }

    @Test fun `FormNCDFollowUpSubmitRequest followUpDate can be null`() {
        val req = FormNCDFollowUpSubmitRequest(1, 1L, 1L, 1, 1, "d", null, null, "f", 1, "{}")
        assertNull(req.followUpDate)
    }

    @Test fun `FormNCDFollowUpSubmitRequest copy`() {
        val req = FormNCDFollowUpSubmitRequest(1, 1L, 1L, 1, 1, "d", null, null, "f", 1, "{}")
        val copy = req.copy(visitNo = 3)
        assertEquals(3, copy.visitNo)
    }

    @Test fun `FormNCDFollowUpSubmitRequest equals`() {
        val a = FormNCDFollowUpSubmitRequest(1, 1L, 1L, 1, 1, "d", null, null, "f", 1, "{}")
        val b = FormNCDFollowUpSubmitRequest(1, 1L, 1L, 1, 1, "d", null, null, "f", 1, "{}")
        assertEquals(a, b)
    }
}
