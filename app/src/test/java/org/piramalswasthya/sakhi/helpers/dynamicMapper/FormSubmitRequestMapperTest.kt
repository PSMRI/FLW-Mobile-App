package org.piramalswasthya.sakhi.helpers.dynamicMapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity

class FormSubmitRequestMapperTest {

    private fun jsonWithHindiDigits() = """
        {
            "formId": "F1",
            "beneficiaryId": 101,
            "houseHoldId": 202,
            "visitDate": "०१-०२-२०२४",
            "fields": {
                "name": "Asha",
                "age": "२५",
                "count": 5
            }
        }
    """.trimIndent()

    @Test
    fun `fromEntity FormResponseJsonEntity maps common fields and converts digits`() {
        val entity = FormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDay = "1",
            visitDate = "2024-02-01",
            formId = "F1",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha1")

        requireNotNull(result)
        assertEquals("asha1", result.userName)
        assertEquals("F1", result.formId)
        assertEquals(101L, result.beneficiaryId)
        assertEquals(202L, result.houseHoldId)
        assertEquals("01-02-2024", result.visitDate)
        assertEquals("25", result.fields["age"])
        assertEquals("Asha", result.fields["name"])
    }

    @Test
    fun `fromEntity leaves non-string field values untouched`() {
        val entity = FormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDay = "1",
            visitDate = "2024-02-01",
            formId = "F1",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha1")

        requireNotNull(result)
        assertEquals(5.0, result.fields["count"])
    }

    @Test
    fun `fromEntity returns null when formDataJson is malformed`() {
        val entity = FormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDay = "1",
            visitDate = "2024-02-01",
            formId = "F1",
            version = 1,
            formDataJson = "{ not valid json"
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha1")

        assertNull(result)
    }

    @Test
    fun `fromEntity returns null when fields object is missing`() {
        val entity = FormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDay = "1",
            visitDate = "2024-02-01",
            formId = "F1",
            version = 1,
            formDataJson = """{"formId":"F1","beneficiaryId":1,"houseHoldId":2,"visitDate":"2024-02-01"}"""
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha1")

        assertNull(result)
    }

    @Test
    fun `fromEntity MosquitoNetFormResponseJsonEntity delegates to common mapping`() {
        val entity = MosquitoNetFormResponseJsonEntity(
            hhId = 2L,
            formId = "F2",
            version = 1,
            visitDate = "2024-02-01",
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha2")

        requireNotNull(result)
        assertEquals("asha2", result.userName)
        assertEquals("01-02-2024", result.visitDate)
    }

    @Test
    fun `fromEntity BenIfaFormResponseJsonEntity delegates to common mapping`() {
        val entity = BenIfaFormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDate = "2024-02-01",
            formId = "F3",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha3")

        requireNotNull(result)
        assertEquals(101L, result.beneficiaryId)
    }

    @Test
    fun `fromEntity FormResponseJsonEntityHBYC delegates to common mapping`() {
        val entity = FormResponseJsonEntityHBYC(
            benId = 1L,
            hhId = 2L,
            visitDay = "1",
            visitDate = "2024-02-01",
            formId = "F4",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha4")

        requireNotNull(result)
        assertEquals(202L, result.houseHoldId)
    }

    @Test
    fun `fromEntity EyeSurgeryFormResponseJsonEntity delegates to common mapping`() {
        val entity = EyeSurgeryFormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDate = "2024-02-01",
            visitMonth = "2",
            formId = "F5",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha5")

        requireNotNull(result)
        assertEquals("F1", result.formId)
    }

    @Test
    fun `fromEntity CUFYFormResponseJsonEntity delegates to common mapping`() {
        val entity = CUFYFormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDate = "2024-02-01",
            formId = "F6",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha6")

        requireNotNull(result)
        assertTrue(result.fields.containsKey("name"))
    }

    @Test
    fun `fromEntity NCDReferalFormResponseJsonEntity delegates to common mapping`() {
        val entity = NCDReferalFormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitNo = 1,
            followUpNo = 1,
            treatmentStartDate = "2024-02-01",
            diagnosisCodes = null,
            formId = "F7",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha7")

        requireNotNull(result)
        assertEquals("asha7", result.userName)
    }

    @Test
    fun `fromEntity FilariaMDAFormResponseJsonEntity delegates to common mapping`() {
        val entity = FilariaMDAFormResponseJsonEntity(
            hhId = 2L,
            visitDate = "2024-02-01",
            visitMonth = "2",
            formId = "F8",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.fromEntity(entity, "asha8")

        requireNotNull(result)
        assertEquals("asha8", result.userName)
    }

    @Test
    fun `formEntity ANCFormResponseJsonEntity delegates to common mapping`() {
        val entity = ANCFormResponseJsonEntity(
            benId = 1L,
            visitDay = "1",
            visitDate = "2024-02-01",
            formId = "F9",
            version = 1,
            formDataJson = jsonWithHindiDigits()
        )

        val result = FormSubmitRequestMapper.formEntity(entity, "asha9")

        requireNotNull(result)
        assertEquals("asha9", result.userName)
        assertEquals(101L, result.beneficiaryId)
    }
}
