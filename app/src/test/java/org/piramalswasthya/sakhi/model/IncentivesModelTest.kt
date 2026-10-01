package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IncentivesModelTest {

    private fun activityCache() = IncentiveActivityCache(
        id = 1L,
        name = "Activity",
        description = "Desc",
        paymentParam = "param",
        rate = 100,
        state = 1,
        district = 1,
        group = "group",
        groupName = "groupName",
        fmrCode = null,
        fmrCodeOld = null
    )

    @Test
    fun `IncentiveDomainDTO constructor uses defaults when id and documentsSubmitted omitted`() {
        val dto = IncentiveDomainDTO(
            group = "group",
            groupName = "groupName",
            name = "Activity",
            description = "Desc",
            paymentParam = "param",
            rate = 100L,
            noOfClaims = 2,
            amountClaimed = 200L,
            fmrCode = null
        )

        assertNotNull(dto)
        assertEquals(0L, dto.id)
        assertEquals(null, dto.documentsSubmitted)
    }

    @Test
    fun `IncentiveGrouped constructor uses defaults when optional flags omitted`() {
        val grouped = IncentiveGrouped(
            activityName = "Activity",
            totalAmount = 500L,
            count = 3,
            groupName = "groupName",
            description = "Desc",
            activity = activityCache()
        )

        assertNotNull(grouped)
        assertEquals(false, grouped.hasZeroBen)
        assertEquals(false, grouped.defaultIncentive)
        assertEquals(false, grouped.isEligible)
    }

    @Test
    fun `IncentiveGrouped exposes groupName description and activity`() {
        val grouped = IncentiveGrouped(
            activityName = "Activity",
            totalAmount = 500L,
            count = 3,
            groupName = "groupName",
            description = "Desc",
            activity = activityCache()
        )

        assertEquals("groupName", grouped.groupName)
        assertEquals("Desc", grouped.description)
        assertEquals(activityCache(), grouped.activity)
    }

    @Test
    fun `IncentiveActivityListRequest exposes stateId districtId and language`() {
        val request = IncentiveActivityListRequest(
            stateId = 1,
            districtId = 2,
            language = "en"
        )

        assertEquals(1, request.stateId)
        assertEquals(2, request.districtId)
        assertEquals("en", request.language)
    }

    private fun activityNetwork() = IncentiveActivityNetwork(
        id = 1L,
        name = "Activity",
        description = "Desc",
        paymentParam = "param",
        rate = 100,
        state = 1,
        district = 1,
        group = "group",
        groupName = "groupName",
        fmrCode = null,
        fmrCodeOld = null,
        createdDate = "2023-01-01",
        createdBy = "creator",
        updatedDate = "2023-01-02",
        updatedBy = "updater"
    )

    @Test
    fun `IncentiveActivityListResponse exposes constructor values and generated members`() {
        val response = IncentiveActivityListResponse(
            data = listOf(activityNetwork()),
            statusCode = 200,
            errorMessage = "",
            status = "OK"
        )

        assertEquals(1, response.data.size)
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.status)
        val same = response.copy()
        assertEquals(response, same)
        assertEquals(response.hashCode(), same.hashCode())
        assertNotEquals(response, response.copy(status = "FAIL"))
    }

    @Test
    fun `IncentiveActivityListResponse tolerates empty data list`() {
        val response = IncentiveActivityListResponse(
            data = emptyList(),
            statusCode = 500,
            errorMessage = "error",
            status = "FAIL"
        )
        assertTrue(response.data.isEmpty())
        assertEquals("error", response.errorMessage)
    }

    private fun recordNetwork() = IncentiveRecordNetwork(
        id = 1L,
        activityId = 2L,
        ashaId = 3,
        benId = 4L,
        amount = 500L,
        name = "Record",
        startDate = "2023-01-01",
        endDate = "2023-01-02",
        createdDate = "2023-01-01",
        createdBy = "creator",
        updatedDate = "2023-01-02",
        updatedBy = "updater",
        isEligible = true,
        verifiedByUserName = "verifier",
        reason = "reason",
        otherReason = "other",
        approvalStatus = 1,
        verifiedByUserId = 5,
        isClaimed = false,
        approvalDate = "2023-01-03",
        calimedDate = "2023-01-04",
        supervisorRole = "supervisor"
    )

    @Test
    fun `IncentiveRecordListResponse exposes constructor values and generated members`() {
        val response = IncentiveRecordListResponse(
            data = listOf(recordNetwork()),
            statusCode = 200,
            errorMessage = "",
            status = "OK"
        )

        assertEquals(1, response.data.size)
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.status)
        val same = response.copy()
        assertEquals(response, same)
        assertEquals(response.hashCode(), same.hashCode())
        assertNotEquals(response, response.copy(status = "FAIL"))
    }

    @Test
    fun `IncentiveRecordListResponse tolerates empty data list`() {
        val response = IncentiveRecordListResponse(
            data = emptyList(),
            statusCode = 500,
            errorMessage = "error",
            status = "FAIL"
        )
        assertTrue(response.data.isEmpty())
        assertEquals("error", response.errorMessage)
    }
}
