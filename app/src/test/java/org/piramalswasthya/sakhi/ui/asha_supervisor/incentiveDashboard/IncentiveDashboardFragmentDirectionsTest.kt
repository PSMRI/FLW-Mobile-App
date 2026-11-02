package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.os.Bundle
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IncentiveDashboardFragmentDirectionsTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun actionIncentiveDashboardFragmentToIncentiveVerificationFragment_buildsDirections() {
        val d = IncentiveDashboardFragmentDirections.actionIncentiveDashboardFragmentToIncentiveVerificationFragment(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = IncentiveDashboardFragmentDirections.actionIncentiveDashboardFragmentToIncentiveVerificationFragment(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
