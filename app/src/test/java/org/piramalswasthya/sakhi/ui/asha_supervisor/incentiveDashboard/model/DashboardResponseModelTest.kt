package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardResponseModelTest {

    private fun incentiveSummary() = IncentiveSummary(
        overDue = 1,
        rejected = 2,
        pending = 3,
        verified = 4,
        unclaimed = 5
    )

    private fun location() = Location(
        district = "District",
        blockOrUlb = "Block",
        locationType = "Rural",
        state = "State"
    )

    private fun facility() = Facility(
        facilityId = 1,
        ashaCount = 10,
        facilityType = "PHC",
        facilityName = "Facility A"
    )

    private fun supervisor() = Supervisor(
        gender = "F",
        mobile = "9999999999",
        fullName = "Supervisor A",
        userId = 42
    )

    private fun dashboardData() = DashboardData(
        incentiveSummary = incentiveSummary(),
        location = location(),
        totalAshaCount = 50,
        facilities = listOf(facility()),
        supervisor = supervisor()
    )

    @Test fun `IncentiveSummary exposes constructor values and generated members`() {
        val summary = incentiveSummary()
        assertEquals(1, summary.overDue)
        assertEquals(2, summary.rejected)
        assertEquals(3, summary.pending)
        assertEquals(4, summary.verified)
        assertEquals(5, summary.unclaimed)
        val same = summary.copy()
        assertEquals(summary, same)
        assertEquals(summary.hashCode(), same.hashCode())
        assertTrue(summary.toString().contains("IncentiveSummary"))
        assertNotEquals(summary, summary.copy(pending = 99))
    }

    @Test fun `Location exposes constructor values and generated members`() {
        val loc = location()
        assertEquals("District", loc.district)
        assertEquals("Block", loc.blockOrUlb)
        assertEquals("Rural", loc.locationType)
        assertEquals("State", loc.state)
        val same = loc.copy()
        assertEquals(loc, same)
        assertEquals(loc.hashCode(), same.hashCode())
        assertNotEquals(loc, loc.copy(state = "Other"))
    }

    @Test fun `Facility exposes constructor values and generated members`() {
        val fac = facility()
        assertEquals(1, fac.facilityId)
        assertEquals(10, fac.ashaCount)
        assertEquals("PHC", fac.facilityType)
        assertEquals("Facility A", fac.facilityName)
        val same = fac.copy()
        assertEquals(fac, same)
        assertEquals(fac.hashCode(), same.hashCode())
        assertNotEquals(fac, fac.copy(ashaCount = 0))
    }

    @Test fun `Supervisor exposes constructor values and generated members`() {
        val sup = supervisor()
        assertEquals("F", sup.gender)
        assertEquals("9999999999", sup.mobile)
        assertEquals("Supervisor A", sup.fullName)
        assertEquals(42, sup.userId)
        val same = sup.copy()
        assertEquals(sup, same)
        assertEquals(sup.hashCode(), same.hashCode())
        assertNotEquals(sup, sup.copy(userId = 0))
    }

    @Test fun `DashboardData exposes constructor values and generated members`() {
        val data = dashboardData()
        assertEquals(50, data.totalAshaCount)
        assertEquals(1, data.facilities.size)
        assertEquals(incentiveSummary(), data.incentiveSummary)
        assertEquals(location(), data.location)
        assertEquals(supervisor(), data.supervisor)
        val same = data.copy()
        assertEquals(data, same)
        assertEquals(data.hashCode(), same.hashCode())
        assertNotEquals(data, data.copy(totalAshaCount = 0))
    }

    @Test fun `DashboardData tolerates an empty facilities list`() {
        val data = dashboardData().copy(facilities = emptyList())
        assertTrue(data.facilities.isEmpty())
    }

    @Test fun `DashboardResponse exposes constructor values and generated members`() {
        val response = DashboardResponse(
            data = dashboardData(),
            statusCode = 200,
            errorMessage = null,
            status = "OK"
        )
        assertEquals(50, response.data?.totalAshaCount)
        assertEquals(200, response.statusCode)
        assertNull(response.errorMessage)
        assertEquals("OK", response.status)
        val same = response.copy()
        assertEquals(response, same)
        assertEquals(response.hashCode(), same.hashCode())
        assertNotEquals(response, response.copy(status = "FAIL"))
    }

    @Test fun `DashboardResponse tolerates null data and errorMessage`() {
        val response = DashboardResponse(
            data = null,
            statusCode = 500,
            errorMessage = "error",
            status = "FAIL"
        )
        assertNull(response.data)
        assertEquals("error", response.errorMessage)
    }
}
