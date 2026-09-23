package org.piramalswasthya.sakhi.model.dynamicModel

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicModelsTest {

    private fun apiResponse() = ApiResponse(
        success = true,
        message = "ok",
        statusCode = 200,
        data = "payload"
    )

    @Test fun `ApiResponse exposes constructor values`() {
        val res = apiResponse()
        assertTrue(res.success)
        assertEquals("ok", res.message)
        assertEquals(200, res.statusCode)
        assertEquals("payload", res.data)
    }

    @Test fun `ApiResponse defaults message statusCode and data to null`() {
        val res = ApiResponse<String>(success = false)
        assertFalse(res.success)
        assertNull(res.message)
        assertNull(res.statusCode)
        assertNull(res.data)
    }

    @Test fun `ApiResponse equals and copy`() {
        val a = apiResponse()
        val b = apiResponse()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("ApiResponse"))
        assertNotEquals(a, a.copy(success = false))
    }

    private fun hbncVisitResponse(id: Int = 1) = HBNCVisitResponse(
        id = id,
        houseHoldId = 100L,
        beneficiaryId = 200L,
        visitDate = "2024-01-01",
        eyeSide = "LEFT",
        fields = JsonObject().apply { addProperty("weight", 3.5) }
    )

    @Test fun `HBNCVisitResponse exposes constructor values`() {
        val res = hbncVisitResponse()
        assertEquals(1, res.id)
        assertEquals(100L, res.houseHoldId)
        assertEquals(200L, res.beneficiaryId)
        assertEquals("2024-01-01", res.visitDate)
        assertEquals("LEFT", res.eyeSide)
        assertEquals(3.5, res.fields.get("weight").asDouble, 0.0001)
    }

    @Test fun `HBNCVisitResponse equals and copy`() {
        val a = hbncVisitResponse()
        val b = hbncVisitResponse()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("HBNCVisitResponse"))
        assertNotEquals(a, a.copy(id = 2))
    }

    private fun hbncVisitListResponse() = HBNCVisitListResponse(
        data = listOf(hbncVisitResponse()),
        statusCode = 200,
        errorMessage = null,
        status = "SUCCESS"
    )

    @Test fun `HBNCVisitListResponse exposes constructor values`() {
        val res = hbncVisitListResponse()
        assertEquals(1, res.data.size)
        assertEquals(200, res.statusCode)
        assertNull(res.errorMessage)
        assertEquals("SUCCESS", res.status)
    }

    @Test fun `HBNCVisitListResponse defaults to empty data and zero statusCode`() {
        val res = HBNCVisitListResponse()
        assertTrue(res.data.isEmpty())
        assertEquals(0, res.statusCode)
        assertNull(res.errorMessage)
        assertNull(res.status)
    }

    @Test fun `HBNCVisitListResponse equals and copy`() {
        val a = hbncVisitListResponse()
        val b = hbncVisitListResponse()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("HBNCVisitListResponse"))
        assertNotEquals(a, a.copy(statusCode = 500))
    }

    private fun hbncVisitRequest() = HBNCVisitRequest(
        fromDate = "2024-01-01",
        toDate = "2024-01-31",
        pageNo = 1,
        ashaId = 55,
        userName = "asha1"
    )

    @Test fun `HBNCVisitRequest exposes constructor values`() {
        val req = hbncVisitRequest()
        assertEquals("2024-01-01", req.fromDate)
        assertEquals("2024-01-31", req.toDate)
        assertEquals(1, req.pageNo)
        assertEquals(55, req.ashaId)
        assertEquals("asha1", req.userName)
    }

    @Test fun `HBNCVisitRequest equals and copy`() {
        val a = hbncVisitRequest()
        val b = hbncVisitRequest()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("HBNCVisitRequest"))
        assertNotEquals(a, a.copy(pageNo = 2))
    }

    private fun mdaCampaignItem() = MDACampaignItem(
        srNo = 1,
        startDate = "2024-01-01",
        endDate = "2024-01-31",
        noOffamilies = "10",
        noOfIndividuals = "40"
    )

    @Test fun `MDACampaignItem exposes constructor values`() {
        val item = mdaCampaignItem()
        assertEquals(1, item.srNo)
        assertEquals("2024-01-01", item.startDate)
        assertEquals("2024-01-31", item.endDate)
        assertEquals("10", item.noOffamilies)
        assertEquals("40", item.noOfIndividuals)
    }

    @Test fun `MDACampaignItem equals and copy`() {
        val a = mdaCampaignItem()
        val b = mdaCampaignItem()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("MDACampaignItem"))
        assertNotEquals(a, a.copy(srNo = 2))
    }

    private fun visitCard() = VisitCard(
        visitDay = "Day 1",
        visitDate = "2024-01-01",
        isCompleted = true,
        isEditable = false,
        isBabyDeath = false
    )

    @Test fun `VisitCard exposes constructor values`() {
        val card = visitCard()
        assertEquals("Day 1", card.visitDay)
        assertEquals("2024-01-01", card.visitDate)
        assertTrue(card.isCompleted)
        assertFalse(card.isEditable)
        assertFalse(card.isBabyDeath)
    }

    @Test fun `VisitCard equals and copy`() {
        val a = visitCard()
        val b = visitCard()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("VisitCard"))
        assertNotEquals(a, a.copy(isBabyDeath = true))
    }
}
