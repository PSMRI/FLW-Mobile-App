package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
