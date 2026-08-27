package org.piramalswasthya.sakhi.ui.asha_supervisor

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

class SupervisorHomeFragmentDirectionsTest {

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
    fun actionSupervisorHomeFragmentToIncentiveVerificationFragment_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionSupervisorHomeFragmentToIncentiveVerificationFragment(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionSupervisorHomeFragmentToIncentiveVerificationFragment(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToAllHouseholdFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToAllHouseholdFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToAllHouseholdFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToAllBenFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToAllBenFragments(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToAllBenFragments(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToEligibleCoupleTrackingListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToEligibleCoupleTrackingListFragments(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToEligibleCoupleTrackingListFragments(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToPwRegistrationFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToPwRegistrationFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToPwRegistrationFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToPwAncVisitsFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToPwAncVisitsFragments(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToPwAncVisitsFragments(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToPregnantListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToPregnantListFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToPregnantListFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToDeliveryOutcomeListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToDeliveryOutcomeListFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToDeliveryOutcomeListFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToChildImmunizationListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToChildImmunizationListFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToChildImmunizationListFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToNcdListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToNcdListFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToNcdListFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToNcdPriorityListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToNcdPriorityListFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToNcdPriorityListFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToTBSuspectedListFragments_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToTBSuspectedListFragments()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToTBSuspectedListFragments()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavSupervisorToIncentiveVerificationFragment_buildsDirections() {
        val d = SupervisorHomeFragmentDirections.actionNavSupervisorToIncentiveVerificationFragment(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = SupervisorHomeFragmentDirections.actionNavSupervisorToIncentiveVerificationFragment(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
